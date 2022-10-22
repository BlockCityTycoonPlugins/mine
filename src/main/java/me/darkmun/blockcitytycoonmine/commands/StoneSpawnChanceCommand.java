package me.darkmun.blockcitytycoonmine.commands;

import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class StoneSpawnChanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            FileConfiguration config = BlockCityTycoonMine.getPlugin().getPlayersUpgradesConfig().getConfig();
            if (args.length == 1 && BlockCityTycoonMine.getPlugin().getUpgradesConfig().getConfig().contains("stone-spawn-chance.level-" + args[0])) {
                config.set(pl.getUniqueId().toString() + ".stone-spawn-chance", "level-" + args[0]);
                BlockCityTycoonMine.getPlugin().getPlayersUpgradesConfig().saveConfig();
                return true;
            }
            else {
                pl.sendMessage("Больше одного аргумента, или такого уровня прокачки stone spawn chance не существует");
                return false;
            }
        }
        return false;
    }
}