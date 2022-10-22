package me.darkmun.blockcitytycoonmine;

import com.comphenix.example.EntityHider;
import com.comphenix.packetwrapper.WrapperPlayServerBlockChange;
import com.comphenix.packetwrapper.WrapperPlayServerCustomSoundEffect;
import com.comphenix.packetwrapper.WrapperPlayServerMapChunk;
import com.comphenix.packetwrapper.WrapperPlayServerNamedSoundEffect;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.darkmun.blockcitytycoonmine.commands.StoneSpawnChanceCommand;
import me.darkmun.blockcitytycoonmine.durability.DurabilityBlock;
import me.darkmun.blockcitytycoonmine.listeners.BreakingStoneListener;
import me.darkmun.blockcitytycoonmine.listeners.JoinListener;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.CraftOfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static java.lang.Math.abs;

public final class BlockCityTycoonMine extends JavaPlugin implements CommandExecutor, Listener {

    Chunk chunk = null;
    int count = 0;
    static BlockCityTycoonMine plugin;

    //boolean blockChangeCancel = false;
    //private Config durabilityConfig;
    private Config upgradesConfig;
    private Config playersUpgradesConfig;

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    private static EntityHiderPlus entityHider;
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        entityHider = new EntityHiderPlus(this, EntityHider.Policy.BLACKLIST);
        //getServer().getWorld("world").getLivingEntities().f

        upgradesConfig = new Config();
        upgradesConfig.setup(getDataFolder(), "upgrades");
        //upgradesConfig.saveConfig();

        playersUpgradesConfig = new Config();
        playersUpgradesConfig.setup(getDataFolder(), "players-upgrades");

        if (getConfig().getBoolean("enable")) {
            plugin = this;

            hookToVault();
            getServer().getPluginManager().registerEvents(new BreakingStoneListener(), this);
            getServer().getPluginManager().registerEvents(new JoinListener(), this);
            getCommand("break").setExecutor(this);
            getCommand("setchunk").setExecutor(this);
            getCommand("stonespawnchance").setExecutor(new StoneSpawnChanceCommand());

            ProtocolManager manager = ProtocolLibrary.getProtocolManager();
            BreakingStoneListener.listenToStoneChange(manager);
            BreakingStoneListener.listenToChunkSend(manager);
            getLogger().info("Plugin enabled.");

            manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    WrapperPlayServerNamedSoundEffect wrapperPlayServerNamedSoundEffect = new WrapperPlayServerNamedSoundEffect(event.getPacket());
                    if (wrapperPlayServerNamedSoundEffect.getSoundEffect() == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE) {
                        event.setCancelled(true);
                    }
                    getLogger().info("Sound effect: " + wrapperPlayServerNamedSoundEffect.getSoundEffect() + " Volume: " + wrapperPlayServerNamedSoundEffect.getVolume() + " Pitch: " + wrapperPlayServerNamedSoundEffect.getPitch());
                }
            });
        }
        else {
            getLogger().info("Plugin not enabled.");
        }
    }

    private void hookToVault() {
        if (!setupEconomy() ) {
            getPlugin().getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
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

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            if (command.getName().equals("break")) {

                List<org.bukkit.block.Block> blocks = pl.getLineOfSight(null, 10);
                for(org.bukkit.block.Block block : blocks) {
                    getLogger().info("Type of block" + block.getType());
                }
                //chunk = ((CraftPlayer)pl).getHandle().world.getChunkAt(-6, 0).;
                World world = ((CraftPlayer)pl).getHandle().world;
                //chunk = BreakingStoneListener.copyChunkTo(world.getChunkAt(-6, 0), world, -6, 0);
                //chunk = new Chunk(((CraftPlayer)pl).getHandle().world, -6, 0);
            }
            if (command.getName().equals("setchunk")) {
                if (chunk != null) {
                    //chunk.getSections()[2].getBlocks().setBlock(-90, 37, 0, new );
                    //net.minecraft.server.v1_12_R1.Material material = net.minecraft.server.v1_12_R1.Material.SAND;
                    Block block = Block.getById(1);
                    getLogger().info(block.getName());
                    getLogger().info(String.valueOf(chunk.getSections().length));
                    for (int i = -95; i <= -90; i++) {
                        for (int j = 36; j <= 38; j++) {
                            for (int k = 4; k >= 0; k--) {
                                int x;
                                int y;
                                int z;
                                if (i < 0) {
                                    x = 16 + i % 16;
                                } else {
                                    x = i % 16;
                                }
                                y = j % 16;
                                if (k < 0) {
                                    z = 16 + k % 16;
                                } else {
                                    z = k % 16;
                                }
                                getLogger().info("x: " + x + " y: " + y + " z: " + z);
                                chunk.getSections()[2].getBlocks().setBlock(x,y,z, block.getBlockData());//setBlock(-90, 37, 0, block.getBlockData());
                            }
                        }
                    }


                    getLogger().info(block.getBlockData().getClass().getName());


                    PacketPlayOutMapChunk packet = new PacketPlayOutMapChunk(chunk, 65535);
                    ((CraftPlayer)pl).getHandle().playerConnection.sendPacket(packet);
                    //WrapperPlayServerMapChunk wrapper = new WrapperPlayServerMapChunk();
                    //wrapper.setChunkX(-6);
                    //wrapper.setChunkZ(0);
                    //wrapper.sendPacket(pl);
                }
            }

            return true;
        }
        return false;
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

    /*public Config getDurabilityConfig() {
        return durabilityConfig;
    }*/

    public Config getUpgradesConfig() {
        return upgradesConfig;
    }
    public Config getPlayersUpgradesConfig() { return playersUpgradesConfig; }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    public static EntityHiderPlus getEntityHider() {
        return entityHider;
    }
}
