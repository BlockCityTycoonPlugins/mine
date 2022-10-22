package me.darkmun.blockcitytycoonmine.listeners;

//import com.comphenix.example.EntityHider;
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
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;

import static me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker.STONE_MINE_CHUNK_X;
import static me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker.STONE_MINE_CHUNK_Z;

public class BreakingStoneListener implements Listener {


    @EventHandler
    public void onPlayerAnim(PlayerAnimationEvent e) {
        Player pl = e.getPlayer();
        Bukkit.getLogger().info("Anim");
        //Set<Material> ignore = new HashSet<>();
        //ignore.add(Material.AIR);
        Set<Material> ignore = new HashSet<>();
        ignore.add(Material.LADDER);
        ignore.add(Material.AIR);
        List<Block> blocksInLineOfSight = pl.getLineOfSight(ignore, 5);
        for (org.bukkit.block.Block block : blocksInLineOfSight) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            Bukkit.getLogger().info("x: " + x + " y: " + y + " z: " + z + " type: " +block.getType().toString());
            if (((x >= -95 && x <= -90)
                    && (y >= 36 && y <= 38)
                    && (z >= 0 && z <= 4))) {
                Bukkit.getLogger().info("Block: true");
                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(pl).stream()
                        .filter(durBlock -> durBlock.getBlock().equals(block)).findAny().orElse(null);
                /*if (durabilityBlock == null) {
                    Bukkit.getLogger().info("Durability block: false (making new)");
                    Bukkit.getLogger().info("DurabilityBlock: x: " + x + " y: " + y + " z: " + z + " type: " +block.getType().toString());
                    DurabilityBlock durBlock = new DurabilityBlock(block, 5);
                    playersDurabilityBlocks.get(pl.getUniqueId()).add(durBlock);
                    durBlock.reduceDurability();
                    break;
                }*/
                if (!durabilityBlock.isBroken()) {
                    Bukkit.getLogger().info("Durability block: true");

                    durabilityBlock.reduceDurabilityWith(pl.getInventory().getItemInMainHand());
                    Bukkit.getLogger().info("Durability: " + durabilityBlock.getDurability());
                    Bukkit.getLogger().info("DurabilityBlock: x: " + x + " y: " + y + " z: " + z + " type: " +block.getType().toString());
                    if (durabilityBlock.isBroken()) {
                        Bukkit.getLogger().info("Broken");
                        durabilityBlock.sendBreakSoundAndParticle(pl);
                        durabilityBlock.sendBreak(pl);
                        BlockCityTycoonMine.getEconomy().depositPlayer(pl, durabilityBlock.getValue());

                        Location location = new Location(block.getWorld(),block.getX() + 0.5/*getDeltaX(getCardinalDirection(pl))*/, block.getY() - 1.70, block.getZ() + 0.5/*getDeltaZ(getCardinalDirection(pl))*/);

                        //TextComponent tc = new TextComponent("+" + durabilityBlock.getValue() + "$");
                        ArmorStand hologram = Hologram.createAndShowToPlayer(location,  ChatColor.RED + "" + ChatColor.BOLD + "+" + durabilityBlock.getValue() + "$", pl);
                        //durabilityBlock.showValueToPlayer(pl);
                        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                //durabilityBlock.hideValueForPlayer(pl);
                                hologram.remove();
                            }
                        }, 4 * 20);

                        /*durabilityBlock.showValueToPlayer(pl);
                        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                durabilityBlock.hideValueForPlayer(pl);
                            }
                        }, 4 * 20);*/
                        //double value = Bukkit.getServer().getPluginManager().getPlugin("WFX-Business").getConfig().getDouble("DataBaseIncome." + pl.getName() + ".total-income");
                        //value += durabilityBlock.getValue();
                        //Bukkit.getServer().getPluginManager().getPlugin("WFX-Business").getConfig().set("DataBaseIncome." + pl.getName() + ".total-income")
                    }
                    else if (durabilityBlock.isNeededToSendBreakAnim()){
                        Bukkit.getLogger().info("Send break anim");
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

    private double getDeltaX(String playerDirection) {
        switch (playerDirection) {
            case "+X":
                return -0.3;
            case "+Z":
                return 0.3;
            case "-X":
                return 1.3;
            case "-Z":
                return 0.3;
            default:
                return 0;
        }
    }

    private double getDeltaZ(String playerDirection) {
        switch (playerDirection) {
            case "+X":
                return 0.3;
            case "+Z":
                return -0.3;
            case "-X":
                return 0.3;
            case "-Z":
                return 1.3;
            default:
                return 0;
        }
    }

    public String getCardinalDirection(Player player) {
        double rotation = player.getLocation().getYaw();
        if (rotation > 180) {
            rotation -= 360;
        }
        if (rotation < -180) {
            rotation += 360;
        }
        Bukkit.getLogger().info("Rotation: " + rotation);
        if ((rotation <= -135 && rotation >= -180) || (rotation <= 180 && rotation >= 135)) {
            return "-Z";
        }
        if (rotation <= -45 && rotation >= -135) {
            return "+X";
        }
        if ((rotation <= 0 && rotation >= -45) || (rotation <= 45 && rotation >= 0)) {
            return "+Z";
        }
        if (rotation <= 135 && rotation >= 45) {
            return "-X";
        }

        return "";
    }

    public static void listenToStoneChange(ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(BlockCityTycoonMine.getPlugin(), PacketType.Play.Server.BLOCK_CHANGE) {

            @Override
            public void onPacketSending(PacketEvent event) {
                Player pl = event.getPlayer();
                Bukkit.getLogger().info("Block change");
                WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
                int x = wrapper.getLocation().getX();
                int y = wrapper.getLocation().getY();
                int z = wrapper.getLocation().getZ();
                //Bukkit.getLogger().info("Players size: " + playersDurabilityBlocks.size() + " Player durability blocks size: " + playersDurabilityBlocks.get(event.getPlayer().getUniqueId()).size());
                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(pl).stream().filter(durBlock ->
                        durBlock.getBlock().getX() == x && durBlock.getBlock().getY() == y && durBlock.getBlock().getZ() == z).findAny().orElse(null);
                //if(count >= 1) {
                if (((x < -95 || x > -90)
                        || (y < 36 || y > 38)
                        || (z < 0 || z > 4))) {
                    event.setCancelled(true);
                } else if (durabilityBlock == null) {
                    event.setCancelled(true);
                } else if (!durabilityBlock.isSending()) {
                    event.setCancelled(true);
                }
                //}
                //count++;
                //WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
                //BlockPosition pos = new BlockPosition(-84, 39, 25);
                //if (wrapper.getLocation().equals(pos)) {
                //    wrapper.setBlockData(WrappedBlockData.createData(Material.DIRT));
                //}
                /*WrapperPlayServerMapChunk wrapper = new WrapperPlayServerMapChunk(event.getPacket());
                if (wrapper.getChunkX() == -6 && wrapper.getChunkZ() == 0) {

                    if (chunk != null) {
                        getLogger().info("x2: " + wrapper.getChunkX() + "   \tz2: " + wrapper.getChunkZ());
                        PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, 65535);
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
                        //((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(packet);
                    }

                    //getLogger().info("x2: " + wrapper.getChunkX() + "   \tz2: " + wrapper.getChunkZ());
                    //PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, 65535);
                    //chunk.
                    //((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(packet);
                }*/
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


                    Bukkit.getLogger().info("x2: " + wrapper.getChunkX() + "   \tz2: " + wrapper.getChunkZ());
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

                    //((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(packet);


                    //getLogger().info("x2: " + wrapper.getChunkX() + "   \tz2: " + wrapper.getChunkZ());
                    //PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, 65535);
                    //chunk.
                    //((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(packet);
                }
            }
        });
    }
}
