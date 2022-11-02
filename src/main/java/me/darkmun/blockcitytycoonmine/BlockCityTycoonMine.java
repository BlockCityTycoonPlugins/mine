package me.darkmun.blockcitytycoonmine;

import com.comphenix.example.EntityHider;
import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.darkmun.blockcitytycoonmine.commands.StoneSpawnChanceCommand;
import me.darkmun.blockcitytycoonmine.commands.UpdatePickaxeCommand;
import me.darkmun.blockcitytycoonmine.listeners.BreakingStoneListener;
import me.darkmun.blockcitytycoonmine.listeners.JoinListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockCityTycoonMine extends JavaPlugin implements CommandExecutor, Listener {
    private static BlockCityTycoonMine plugin;
    private Config upgradesConfig;
    private Config playersUpgradesConfig;

    private static Economy econ = null;
    private static EntityHider entityHider;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);

        upgradesConfig = new Config();
        upgradesConfig.setup(getDataFolder(), "upgrades");

        playersUpgradesConfig = new Config();
        playersUpgradesConfig.setup(getDataFolder(), "players-upgrades");

        if (getConfig().getBoolean("enable")) {
            plugin = this;

            hookToVault();
            getServer().getPluginManager().registerEvents(new BreakingStoneListener(), this);
            getServer().getPluginManager().registerEvents(new JoinListener(), this);

            getCommand("stonespawnchance").setExecutor(new StoneSpawnChanceCommand());
            getCommand("updatepickaxe").setExecutor(new UpdatePickaxeCommand());

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            BreakingStoneListener.listenToStoneChange(manager);
            BreakingStoneListener.listenToChunkSend(manager);

            manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    WrapperPlayServerNamedSoundEffect wrapperPlayServerNamedSoundEffect = new WrapperPlayServerNamedSoundEffect(event.getPacket());
                    if (wrapperPlayServerNamedSoundEffect.getSoundEffect() == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE) {
                        event.setCancelled(true);
                    }
                }
            });
            getLogger().info("Plugin enabled.");
        }
        else {
            getLogger().info("Plugin not enabled.");
        }
    }

    private void hookToVault() {
        if (!setupEconomy() ) {
            getPlugin().getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        for (LivingEntity entity : getServer().getWorld("world").getLivingEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                armorStand.remove();
            }
        }
        getLogger().info("Plugin disabled.");
    }

    public static BlockCityTycoonMine getPlugin() {
        return plugin;
    }
    public Config getUpgradesConfig() {
        return upgradesConfig;
    }
    public Config getPlayersUpgradesConfig() { return playersUpgradesConfig; }
    public static Economy getEconomy() {
        return econ;
    }
    public static EntityHider getEntityHider() {
        return entityHider;
    }
}
