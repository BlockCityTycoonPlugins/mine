package me.darkmun.blockcitytycoonmine.auxiliary_classes;

import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class RandomMaterial {
    public static Material getForPlayer(Player pl) {
        FileConfiguration upConfig = BlockCityTycoonMine.getUpgradesConfig().getConfig();
        FileConfiguration plUpConfig = BlockCityTycoonMine.getPlayersUpgradesConfig().getConfig();
        if (plUpConfig.contains(pl.getUniqueId().toString() + ".stone-spawn-chance")) {
            String[] strings = upConfig.getConfigurationSection("stone-spawn-chance." + plUpConfig.getString(pl.getUniqueId().toString() + ".stone-spawn-chance")).getKeys(false).toArray(new String[0]);
            int[] chances = new int[strings.length];

            for (int i = 0; i < strings.length; i++) {
                chances[i] = upConfig.getInt("stone-spawn-chance." + plUpConfig.getString(pl.getUniqueId().toString() + ".stone-spawn-chance") + "." + strings[i]);
            }

            int index = (int) (Math.random() * 100); // Выбираем случайный индекс из воображаемого массива

            for (int i = 0; i < chances.length; i++) { // Ищем элемент, которому принадлежит этот индекс
                index -= chances[i];
                if(index <= 0) {
                    return Material.valueOf(strings[i].toUpperCase()); // Получаем из названия в конфиге материал
                }
            }
        }
        else return Material.STONE;
        return Material.AIR;
    }
}