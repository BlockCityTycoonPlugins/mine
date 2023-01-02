package me.darkmun.blockcitytycoonmine;

import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import me.darkmun.blockcitytycoonmine.commands.StoneSpawnChanceCommand;
import me.darkmun.blockcitytycoonmine.commands.UpdatePickaxeCommand;
import me.darkmun.blockcitytycoonmine.durability.DurabilityBlock;
import me.darkmun.blockcitytycoonmine.listeners.BreakingStoneListener;
import me.darkmun.blockcitytycoonmine.listeners.JoinListener;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public final class BlockCityTycoonMine extends JavaPlugin implements CommandExecutor, Listener {
    private static BlockCityTycoonMine plugin;
    private static final Plugin BCTEvents = Bukkit.getServer().getPluginManager().getPlugin("BlockCityTycoonEvents");
    private static final Config upgradesConfig = new Config();
    private static final Config playersUpgradesConfig = new Config();
    private static final Config playerEventsDataConfig = new Config();

    private static final Database database = new Database();
    public static Economy econ = null;
    public static Permission permission = null;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        upgradesConfig.setup(getDataFolder(), "upgrades");

        playersUpgradesConfig.setup(getDataFolder(), "players-upgrades");
        playerEventsDataConfig.setup(getServer().getPluginManager().getPlugin("BlockCityTycoonEvents").getDataFolder(), "playerEventsData");
        playerEventsDataConfig.getConfig().options().copyDefaults(true);

        //playersBlockValuesConfig.setup(getDataFolder(), "players-block-values");

        if (getConfig().getBoolean("enable")) {
            plugin = this;

            try {
                database.initializeDatabase();
            } catch (SQLException e) {
                throw new RuntimeException("Косяк с БД", e);
            }

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
        if (!setupEconomy() || !setupPermissions()) {
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

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    @Override
    public void onDisable() {
        for (LivingEntity entity : getServer().getWorld("world").getLivingEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand armorStand = (ArmorStand) entity;
                armorStand.remove();
            }
        }

        if (getConfig().getBoolean("enable")) {
            try {
                fillMineDatabaseTable();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            database.closeConnection();
        }

        getLogger().info("Plugin disabled.");
    }

    private void fillMineDatabaseTable() throws SQLException {
        for (OfflinePlayer offlinePlayer : getServer().getOfflinePlayers()) {
            UUID plUUID = offlinePlayer.getUniqueId();
            List<DurabilityBlock> blocks = ChunkAndBlockWorker.getDurabilityBlocks(plUUID);
            if (blocks != null) {
                if (database.readDurabilityBlocks(plUUID).isEmpty())
                    database.createDurabilityBlocks(plUUID, blocks);
                else database.updateDurabilityBlocks(plUUID, blocks);
            }
        }
    }

    public static BlockCityTycoonMine getPlugin() {
        return plugin;
    }
    public static Config getUpgradesConfig() {
        return upgradesConfig;
    }
    public static Config getPlayersUpgradesConfig() {
        return playersUpgradesConfig;
    }
    /*public Config getPlayersBlockValuesConfig() {
        return playersBlockValuesConfig;
    }*/
    public static Config getPlayerEventsDataConfig() {
        return playerEventsDataConfig;
    }
    public static Plugin getBCTEventsPlugin() {
        return BCTEvents;
    }

    public static Database getDatabase() {
        return database;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
