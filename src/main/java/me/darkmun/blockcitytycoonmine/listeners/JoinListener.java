package me.darkmun.blockcitytycoonmine.listeners;

import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import me.darkmun.blockcitytycoonmine.Config;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player pl = e.getPlayer();
        FileConfiguration plUpConfig = BlockCityTycoonMine.getPlugin().getPlayersUpgradesConfig().getConfig();
        if (!ChunkAndBlockWorker.hasDurabilityBlocks(pl)) {
            ChunkAndBlockWorker.createChunkWithDurabilityBlocks(pl);
        }
        else {
            Bukkit.getLogger().info("Joined");
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