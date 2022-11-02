package me.darkmun.blockcitytycoonmine.listeners;

import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player pl = e.getPlayer();
        if (!ChunkAndBlockWorker.hasDurabilityBlocks(pl)) {
            ChunkAndBlockWorker.createChunkWithDurabilityBlocks(pl);
        }
        else {
            Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), () -> {
                if (pl.isOnline()) {
                    ChunkAndBlockWorker.sendBreakAnimToDurabilityBlocks(pl);
                }
            }, 5);
        }

        for (LivingEntity livingEntity : pl.getWorld().getLivingEntities()) {
            if (livingEntity instanceof ArmorStand) {
                BlockCityTycoonMine.getEntityHider().hideEntity(pl, livingEntity);
            }
        }
    }
}