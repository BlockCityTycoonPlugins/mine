package me.darkmun.blockcitytycoonmine.listeners;

import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.Hologram;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import me.darkmun.blockcitytycoonmine.durability.DurabilityBlock;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;

import java.lang.reflect.Field;
import java.util.*;

import static me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker.STONE_MINE_CHUNK_X;
import static me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker.STONE_MINE_CHUNK_Z;

public class BreakingStoneListener implements Listener {


    @EventHandler
    public void onPlayerAnim(PlayerAnimationEvent e) {
        Player pl = e.getPlayer();
        Set<Material> ignore = new HashSet<>();
        ignore.add(Material.LADDER);
        ignore.add(Material.AIR);
        List<Block> blocksInLineOfSight = pl.getLineOfSight(ignore, 5);
        for (org.bukkit.block.Block block : blocksInLineOfSight) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            if (((x >= -95 && x <= -90)
                    && (y >= 36 && y <= 38)
                    && (z >= 0 && z <= 4))) {
                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(pl).stream()
                        .filter(durBlock -> durBlock.getBlock().equals(block)).findAny().orElse(null);

                if (!durabilityBlock.isBroken()) {
                    durabilityBlock.reduceDurabilityWith(pl.getInventory().getItemInMainHand());
                    if (durabilityBlock.isBroken()) {
                        durabilityBlock.sendBreakSoundAndParticle(pl);
                        durabilityBlock.sendBreak(pl);
                        BlockCityTycoonMine.getEconomy().depositPlayer(pl, durabilityBlock.getValue());

                        Location location = new Location(block.getWorld(),block.getX() + 0.5, block.getY() - 1.70, block.getZ() + 0.5);

                        ArmorStand hologram = Hologram.createAndShowToPlayer(location,  ChatColor.RED + "" + ChatColor.BOLD + "+" + durabilityBlock.getValue() + "$", pl);
                        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), hologram::remove, BlockCityTycoonMine.getPlugin().getConfig().getLong("value-display-disappear-time") * 20);
                    }
                    else if (durabilityBlock.isNeededToSendBreakAnim()){
                        durabilityBlock.sendHitSound(pl);
                        durabilityBlock.sendBreakAnim(pl);
                        if (!durabilityBlock.runningTask) {
                            durabilityBlock.taskID = Bukkit.getScheduler().runTaskTimer(BlockCityTycoonMine.getPlugin(), () -> {
                                if (pl.isOnline()) {
                                    durabilityBlock.sendBreakAnim(pl);
                                }
                            }, 400, 400).getTaskId();
                            durabilityBlock.runningTask = true;
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void listenToStoneChange(ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(BlockCityTycoonMine.getPlugin(), PacketType.Play.Server.BLOCK_CHANGE) {

            @Override
            public void onPacketSending(PacketEvent event) {
                Player pl = event.getPlayer();
                WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
                int x = wrapper.getLocation().getX();
                int y = wrapper.getLocation().getY();
                int z = wrapper.getLocation().getZ();
                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(pl).stream().filter(durBlock ->
                        durBlock.getBlock().getX() == x && durBlock.getBlock().getY() == y && durBlock.getBlock().getZ() == z).findAny().orElse(null);
                if (((x < -95 || x > -90)
                        || (y < 36 || y > 38)
                        || (z < 0 || z > 4))) {
                    event.setCancelled(true);
                } else if (durabilityBlock == null) {
                    event.setCancelled(true);
                } else if (!durabilityBlock.isSending()) {
                    event.setCancelled(true);
                }
            }
        });
    }

    public static void listenToChunkSend(ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(BlockCityTycoonMine.getPlugin(), PacketType.Play.Server.MAP_CHUNK) {

            @Override
            public void onPacketSending(PacketEvent event) {
                WrapperPlayServerMapChunk wrapper = new WrapperPlayServerMapChunk(event.getPacket());
                Player pl = event.getPlayer();

                if (wrapper.getChunkX() == STONE_MINE_CHUNK_X && wrapper.getChunkZ() == STONE_MINE_CHUNK_Z) {
                    PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(ChunkAndBlockWorker.getPlayerChunk(pl), 65535);
                    byte[] bytes;
                    Field data;
                    try {
                        data = packet.getClass().getDeclaredField("d");
                        data.setAccessible(true);
                        bytes = (byte[])data.get(packet);
                        data.setAccessible(false);
                        wrapper.setData(bytes);
                    } catch (NoSuchFieldException | SecurityException | IllegalAccessException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
