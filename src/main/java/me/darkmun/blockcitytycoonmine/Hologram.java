package me.darkmun.blockcitytycoonmine;

import com.comphenix.packetwrapper.WrapperPlayServerEntityDestroy;
import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntityLiving;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Hologram {
    public static void showToPlayer(Location location, String text, Player player, int entityId) {  // делать это с помощью пакетов

        WrapperPlayServerSpawnEntityLiving wrapper = new WrapperPlayServerSpawnEntityLiving();
        wrapper.setEntityID(entityId);
        wrapper.setX(location.getX());
        wrapper.setY(location.getY());
        wrapper.setZ(location.getZ());
        wrapper.setType(EntityType.ARMOR_STAND);

        byte invisibleBitMask = 1 << 5;
        //byte smallBitMask = 1;
        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), invisibleBitMask);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String.class)), text);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
        //dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(11, WrappedDataWatcher.Registry.get(Byte.class)), smallBitMask);

        wrapper.setMetadata(dataWatcher);
        wrapper.sendPacket(player);
    }

    public static void removeToPlayer(Player player, int entityId) {
        if (player != null) {
            WrapperPlayServerEntityDestroy wrapper = new WrapperPlayServerEntityDestroy();
            wrapper.setEntityIds(new int[] {entityId}); // можно создать поле в классе, а не создавать массив каждый раз
            wrapper.sendPacket(player);
        }
    }
}
