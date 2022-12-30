package me.darkmun.blockcitytycoonmine.commands;

import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class StoneSpawnChanceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("bctmine.stonespawnchance")) {
            if (args.length == 2 && BlockCityTycoonMine.getPlugin().getUpgradesConfig().getConfig().contains("stone-spawn-chance.level-" + args[1])) {
                Player pl = Bukkit.getPlayerExact(args[0]);
                if (pl != null) {
                    FileConfiguration config = BlockCityTycoonMine.getPlugin().getPlayersUpgradesConfig().getConfig();
                    config.set(pl.getUniqueId().toString() + ".name", args[0]);
                    config.set(pl.getUniqueId().toString() + ".stone-spawn-chance", "level-" + args[1]);
                    BlockCityTycoonMine.getPlugin().getPlayersUpgradesConfig().saveConfig();
                    return true;
                } else {
                    sender.sendMessage(ChatColor.RED + "Игрока с ником " + args[0] + " сейчас нет на сервере");
                }
            } else {
                sender.sendMessage(new String[]{ChatColor.RED + "Должно быть два аргумента, или такого уровня прокачки stone spawn chance не существует", ChatColor.RED + "/stonespawnchance <player> <level>"});
            }
        } else {
            sender.sendMessage(ChatColor.RED + "У вас недостаточно прав на использование этой команды");
        }
        return false;
    }
}