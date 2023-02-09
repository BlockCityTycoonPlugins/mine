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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.*;

import static me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker.*;

public class BreakingStoneListener implements Listener {

    private final DecimalFormat df = new DecimalFormat("#.#");

    @EventHandler
    public void onPlayerAnim(PlayerAnimationEvent e) {
        Player pl = e.getPlayer();
        UUID plUUID = pl.getUniqueId();
        Set<Material> ignore = new HashSet<>();
        ignore.add(Material.LADDER);
        ignore.add(Material.AIR);
        List<Block> blocksInLineOfSight = pl.getLineOfSight(ignore, 5);
        for (org.bukkit.block.Block block : blocksInLineOfSight) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            if (((x >= STONE_MINE_LOW_X && x <= STONE_MINE_HIGH_X)
                    && (y >= STONE_MINE_LOW_Y && y <= STONE_MINE_HIGH_Y)
                    && (z >= STONE_MINE_LOW_Z && z <= STONE_MINE_HIGH_Z))) {
                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(plUUID).stream()
                        .filter(durBlock -> durBlock.getBlock().equals(block)).findAny().orElse(null);
                assert durabilityBlock != null;

                boolean inventoryFilled = true;
                for (ItemStack itemStack : pl.getInventory().getStorageContents()) {
                    if (itemStack == null) {
                        inventoryFilled = false;
                        break;
                    }
                }

                if (inventoryFilled) {
                    pl.sendMessage(ChatColor.GOLD + "Ваш инвентарь переполнен! Продайте блоки из шахты или слитки/гемы");
                    break;
                }

                if (!durabilityBlock.isBroken()) {
                    ItemStack itemInMainHand = pl.getInventory().getItemInMainHand();
                    durabilityBlock.reduceDurabilityWith(itemInMainHand);
                    if (durabilityBlock.isBroken()) {
                        if (itemInMainHand.getEnchantmentLevel(Enchantment.SILK_TOUCH) == 1) { //если есть шелковое касание, то блок выпадает
                            Material durabilityBlockMaterial = durabilityBlock.getMaterial();
                            if (durabilityBlockMaterial != Material.STONE && durabilityBlockMaterial != Material.COAL_ORE && durabilityBlockMaterial != Material.COAL_BLOCK) {
                                if (pl.hasPermission("bctglobal.donate.automelting")) { //если есть донат на автопереплавку
                                    Material receivedMeltedMaterial = durabilityBlock.giveMelted(pl);
                                    if (!pl.hasPermission("deluxemenus." + receivedMeltedMaterial + "_1")) {
                                        BlockCityTycoonMine.permission.playerAdd(null, pl, "deluxemenus." + receivedMeltedMaterial + "_1");
                                        BlockCityTycoonMine.permission.playerAdd(null, pl, "deluxemenus." + receivedMeltedMaterial + "BUY_4");
                                    }
                                } else {
                                    durabilityBlock.giveToPlayer(pl);
                                    if (!pl.hasPermission("deluxemenus." + durabilityBlockMaterial + "_32")) {
                                        BlockCityTycoonMine.permission.playerAdd(null, pl, "deluxemenus." + durabilityBlockMaterial + "_32");
                                    }
                                }
                            }
                        }

                        durabilityBlock.sendBreakSoundAndParticle(pl);
                        durabilityBlock.sendBreak(pl);


                        BlockCityTycoonMine.getPlayerEventsDataConfig().reloadConfig();
                        int multiplier = 1;
                        if (BlockCityTycoonMine.getPlayerEventsDataConfig().getConfig().getBoolean(pl.getUniqueId().toString() + ".gold-rush-event.running")
                                && BlockCityTycoonMine.getBCTEventsPlugin().getConfig().getBoolean("gold-rush-event.enable")) {
                            multiplier = BlockCityTycoonMine.getBCTEventsPlugin().getConfig().getInt("gold-rush-event.multiplier");
                        }
                        double newValue = durabilityBlock.getValue() * multiplier;

                        BlockCityTycoonMine.getEconomy().depositPlayer(pl, newValue);

                        Location location = new Location(block.getWorld(),block.getX() + 0.5, block.getY() - 1.70, block.getZ() + 0.5);
                        Hologram.showToPlayer(location, String.format("§c§l+%s$§r", df.format(newValue)), pl, durabilityBlock.getID());
                        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), () ->
                                Hologram.removeToPlayer(pl, durabilityBlock.getID()), BlockCityTycoonMine.getPlugin().getConfig().getLong("value-display-disappear-time") * 20);
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
                UUID plUUID = pl.getUniqueId();
                WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
                int x = wrapper.getLocation().getX();
                int y = wrapper.getLocation().getY();
                int z = wrapper.getLocation().getZ();

                DurabilityBlock durabilityBlock = ChunkAndBlockWorker.getDurabilityBlocks(plUUID).stream().filter(durBlock ->
                        durBlock.getBlock().getX() == x && durBlock.getBlock().getY() == y && durBlock.getBlock().getZ() == z).findAny().orElse(null);

                if (durabilityBlock != null) {
                    if (!durabilityBlock.isSending()) {
                        event.setCancelled(true);
                    }
                }
            }
        });
    }

    public static void listenToChunkSend(ProtocolManager manager) {
        manager.addPacketListener(new PacketAdapter(BlockCityTycoonMine.getPlugin(), PacketType.Play.Server.MAP_CHUNK) {

            @Override
            public void onPacketSending(PacketEvent event) {  // потом подумать как оптимизировать
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
