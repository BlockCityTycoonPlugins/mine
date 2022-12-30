package me.darkmun.blockcitytycoonmine.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;

public class UpdatePickaxeCommand implements CommandExecutor {
    Material[] pickaxes = new Material[] {Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE};
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender.hasPermission("bctmine.updatepickaxe")) {
            if (args.length == 2) {
                Player pl = Bukkit.getPlayerExact(args[0]);
                if (pl != null) {
                    ItemStack item = getPickaxeFromInventory(pl.getInventory());
                    if (item != null) {
                        if (args[1].startsWith("ef")) {
                            try {
                                int efficiencyLevel = Integer.parseInt(args[1].substring(2));
                                if (efficiencyLevel <= 5 && efficiencyLevel >= 0) {
                                    item.addEnchantment(Enchantment.DIG_SPEED, efficiencyLevel);
                                } else {
                                    sender.sendMessage(ChatColor.RED + "Уровень энчанта должен быть >=0 и <=5");
                                }
                            }
                            catch (NumberFormatException ex) {
                                sender.sendMessage(secondArgumentError());
                            }
                        } else if (args[1].equals("silktouch")) {
                            item.addEnchantment(Enchantment.SILK_TOUCH, 1);
                        } else {
                            try {
                                Material pickaxeMaterial = Material.valueOf(args[1].toUpperCase());
                                if (Arrays.asList(pickaxes).contains(pickaxeMaterial)) {
                                    item.setType(pickaxeMaterial);
                                } else {
                                    sender.sendMessage(secondArgumentError());
                                }
                            }
                            catch (IllegalArgumentException ex) {
                                sender.sendMessage(secondArgumentError());
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "В инвентаре игрока нет кирки");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Игрока с таким ником сейчас нет на сервере");
                }
            } else {
                sender.sendMessage(new String[] {ChatColor.RED + "У данной команды должно быть два аргумента", ChatColor.RED + "/updatepickaxe <player> [material | ef | silk]"});
            }
        } else {
            sender.sendMessage(ChatColor.RED + "У вас недостаточно прав на использование этой команды");
        }
        return false;
    }

    private ItemStack getPickaxeFromInventory(PlayerInventory inventory) {
        return Arrays.stream(inventory.getContents()).filter(itemStack -> {
            return Arrays.asList(pickaxes).contains(itemStack.getType());
        }).findFirst().orElse(null);
    }

    private String secondArgumentError() {
        return ChatColor.RED + "Второй аргумент комманды введен неверно.\n" +
                "/updatepickaxe <player> [material | ef | silk]\n" +
                "material = [wood_pickaxe | stone_pickaxe | iron_pickaxe | gold_pickaxe | diamond_pickaxe]\n" +
                "ef = ef[eflvl]\n" +
                "silk = silktouch";
    }
}
