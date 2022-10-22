package me.darkmun.blockcitytycoonmine;

import com.comphenix.example.EntityHider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;

public class EntityHiderPlus extends EntityHider {
    /**
     * Construct a new entity hider.
     *
     * @param plugin - the plugin that controls this entity hider.
     * @param policy - the default visibility policy.
     */
    public EntityHiderPlus(Plugin plugin, Policy policy) {
        super(plugin, policy);
    }

    public void showNPCSFromCitizens(Player player) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("Citizens").getDataFolder(), "saves.yml"));
        for (String npcNum : config.getConfigurationSection("npc").getKeys(false)) {
            Bukkit.getLogger().info("In citizens config");
            Bukkit.getLogger().info("In citizens config: ConfigEntityNum: " + npcNum);

            //FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Bukkit.getPluginManager().getPlugin("Citizens").getDataFolder(), "saves.yml"));
            Bukkit.getLogger().info("In citizens config: ConfigEntityName: " + config.getString("npc." + npcNum + ".name"));
            @Nonnull LivingEntity entity = Objects.requireNonNull(player.getWorld().getLivingEntities().stream().filter(livingEntity -> {
                Bukkit.getLogger().info("In citizens config: WorldEntityName: " + livingEntity.getName());
                return livingEntity.getName().equals(config.getString("npc." + npcNum + ".name"));
            }).findAny().orElse(null));
            BlockCityTycoonMine.getEntityHider().showEntity(player, entity);
            Bukkit.getLogger().info("In citizens config: Entity: " + entity.getName());
        }
    }

    public void showEntityByID(Player player, int entityID) {
        setVisibility(player, entityID, true);
    }
}
