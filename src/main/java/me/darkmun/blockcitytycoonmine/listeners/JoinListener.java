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

import java.sql.SQLException;
import java.util.Set;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws SQLException {
        Player pl = e.getPlayer();

        //Если у игрока нет дюрабилити блоков (он зашел в первый раз), то я их создаю, в противном случае, просто спавню трещины на блоках с прошлого выхода из игры
        if (!ChunkAndBlockWorker.hasDurabilityBlocks(pl)) {
            ChunkAndBlockWorker.createChunkWithDurabilityBlocks(pl);
        }
        else {
            Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), () -> {
                if (pl.isOnline()) {
                    ChunkAndBlockWorker.sendBreakAnimToDurabilityBlocks(pl);
                }
            }, 5);  // Пакет не отправляется, если отправлять сразу при входе, поэтому делается задержка в 5 тиков
        }

        //Скрываю все арморстенды, чтобы игроки при заходе не видели надписи других игроков о полученных с камня денег
        for (LivingEntity livingEntity : pl.getWorld().getLivingEntities()) {
            if (livingEntity instanceof ArmorStand) {
                BlockCityTycoonMine.getEntityHider().hideEntity(pl, livingEntity);
            }
        }

        //Создаю в конфиге ценность блока для игрока (нужно для ивента
        /*if (!BlockCityTycoonMine.getPlugin().getPlayersBlockValuesConfig().getConfig().contains(pl.getUniqueId().toString())) {
            Set<String> blocks = BlockCityTycoonMine.getPlugin().getConfig().getConfigurationSection("value-of-blocks").getKeys(false);
            double blockValue;

            for (String block : blocks) {
                blockValue = BlockCityTycoonMine.getPlugin().getConfig().getDouble("value-of-blocks." + block);
                BlockCityTycoonMine.getPlugin().getPlayersBlockValuesConfig().getConfig().set(pl.getUniqueId().toString() + "." + block, blockValue);
            }
        }*/
    }
}