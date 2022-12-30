package me.darkmun.blockcitytycoonmine;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class Hologram {
    public static ArmorStand createAndShowToPlayer(Location location, String text, Player player) {  // делать это с помощью пакетов

        ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setGravity(false); //Make sure it doesn't fall
        armorStand.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
        armorStand.setCustomName(text); //Set this to the text you want
        armorStand.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
        armorStand.setVisible(false); //Makes the ArmorStand invisible
        for (Player pl : player.getWorld().getPlayers()) {
            if (pl != player) {
                BlockCityTycoonMine.getEntityHider().hideEntity(pl, armorStand);
            }
        }
        return armorStand;
    }
}
