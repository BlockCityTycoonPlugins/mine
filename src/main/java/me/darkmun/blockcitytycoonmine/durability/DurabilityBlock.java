package me.darkmun.blockcitytycoonmine.durability;

//import com.comphenix.example.EntityHider;
import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.RandomMaterial;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.SoundBlocks;
import me.darkmun.blockcitytycoonmine.listeners.BreakingStoneListener;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.Scoreboard;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_12_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DurabilityBlock {

    private final double MAX_DISTRACTION_LEVEL = 10;
    private final Block block;
    private Material fakeMaterial;
    private ArmorStand valueVisual;
    private double maxDurability; //мейби поменять дюрабилити на класс с enum
    private double currentDurability;
    private int distractionLevel = 0;
    private final int entityID;
    public int taskID;
    private int value;
    private double durabilityToNextDistractionLevel;
    private boolean broken = false;
    private boolean sending = false;
    private boolean neededToSendBreakAnim = true;
    public boolean runningTask = false;

    public DurabilityBlock(Block block, Material material, int entityID) {
        this.block = block;
        this.fakeMaterial = material;
        this.entityID = entityID;
        setValueFromMaterial(material);
        setMaxDurabilityFromMaterial(material);
        this.currentDurability = maxDurability;
        this.durabilityToNextDistractionLevel = maxDurability/MAX_DISTRACTION_LEVEL;
        //createValueVisual();
    }

    private void reduceDurabilityBy(double dur) {
        currentDurability -= dur;

        int tempDistractionLevel = (int) ((maxDurability-currentDurability+(durabilityToNextDistractionLevel/2))/durabilityToNextDistractionLevel);
        if (tempDistractionLevel == distractionLevel) {
            neededToSendBreakAnim = false;
        }
        else {
            neededToSendBreakAnim = true;
            distractionLevel = tempDistractionLevel; //Вариант со полусдвигом вправо
        }

        if (currentDurability <= 0) {   //Также реализовать вариант без сдвига и со сдвигом вправо полностью (то есть при любом ударе минимум первый дистракшн левел будет)
            broken = true;
        }
    }

    public void reduceDurabilityWith(ItemStack item) {
        int itemID = item.getTypeId();
        int efficiencyLevel = item.getEnchantmentLevel(Enchantment.DIG_SPEED);
        if (BlockCityTycoonMine.getPlugin().getUpgradesConfig().getConfig().contains("pickaxe." + itemID + ".ef-" + efficiencyLevel)) {
            reduceDurabilityBy(BlockCityTycoonMine.getPlugin().getUpgradesConfig().getConfig().getDouble("pickaxe." + itemID + ".ef-" + efficiencyLevel));
        }
    }

    public void refreshDurability() {
        this.broken = false;
        setValueFromMaterial(this.fakeMaterial);
        //this.valueVisual.setCustomName("+" + value);
        setMaxDurabilityFromMaterial(this.fakeMaterial);
        this.currentDurability = maxDurability;
        this.distractionLevel = 0;
        this.durabilityToNextDistractionLevel = maxDurability/MAX_DISTRACTION_LEVEL;
    }

    /*private void createValueVisual() {
        valueVisual = block.getWorld().spawn(new Location(block.getWorld(), block.getX()+1, block.getY(), block.getZ()), ArmorStand.class);
        valueVisual.setGravity(false); //Make sure it doesn't fall
        valueVisual.setCanPickupItems(false); //I'm not sure what happens if you leave this as it is, but you might as well disable it
        valueVisual.setCustomName("+" + getValue()); //Set this to the text you want
        valueVisual.setCustomNameVisible(true); //This makes the text appear no matter if your looking at the entity or not
        valueVisual.setVisible(false); //Makes the ArmorStand invisible
    }

    public void showValueToPlayer(Player player) {
        BlockCityTycoonMine.getEntityHider().showEntity(player, valueVisual);
    }
    public void hideValueForPlayer(Player player) {
        BlockCityTycoonMine.getEntityHider().hideEntity(player, valueVisual);
    }*/

    public void sendBreak(Player player) {
        int x = ChunkAndBlockWorker.getXInChunk(block.getX());
        int y = ChunkAndBlockWorker.getYInChunk(block.getY());
        int z = ChunkAndBlockWorker.getZInChunk(block.getZ());
        Bukkit.getLogger().info("Durability block: broken");
        WrapperPlayServerBlockChange wrapperPlayServerBlockChange = new WrapperPlayServerBlockChange();
        wrapperPlayServerBlockChange.setBlockData(WrappedBlockData.createData(Material.AIR));
        wrapperPlayServerBlockChange.setLocation(new BlockPosition(block.getLocation().toVector()));

        Bukkit.getScheduler().cancelTask(taskID);
        runningTask = false;

        WrapperPlayServerNamedSoundEffect wrapper = new WrapperPlayServerNamedSoundEffect();
        wrapper.setSoundEffect(Sound.BLOCK_METAL_BREAK);

        sending = true;
        wrapperPlayServerBlockChange.sendPacket(player);
        sending = false;

        fakeMaterial = Material.AIR;
        ChunkAndBlockWorker.getPlayerChunk(player).getSections()[2].setType(x, y, z, net.minecraft.server.v1_12_R1.Block.getById(0).getBlockData());

        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getLogger().info("Running");

                Material randMaterial = RandomMaterial.getForPlayer(player);
                if (randMaterial == Material.AIR) {
                    player.sendMessage("как блин такое могло случится?");
                    fakeMaterial = Material.YELLOW_FLOWER;
                }
                else {
                    fakeMaterial = randMaterial;
                }

                WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange();
                wrapper.setLocation(new BlockPosition(block.getLocation().toVector()));
                wrapper.setBlockData(WrappedBlockData.createData(fakeMaterial)); //менять тут на рандом

                if(((CraftPlayer)player).getHandle().playerConnection != null && player.isOnline()) {
                    Bukkit.getLogger().info("Player in game");
                    sending = true;
                    wrapper.sendPacket(BlockCityTycoonMine.getPlugin().getServer().getPlayer(player.getUniqueId()));
                    sending = false;
                }

                ChunkAndBlockWorker.getPlayerChunk(player).getSections()[2].setType(x, y, z, net.minecraft.server.v1_12_R1.Block.getById(fakeMaterial.getId()).getBlockData());

                refreshDurability();
                sendBreakAnim(player);
            }
        }, (BlockCityTycoonMine.getPlugin().getConfig().getLong("stone-spawn-speed") * 20));
    }

    public void sendBreakAnim(Player player) {
        if (player.isOnline()) {
            WrapperPlayServerBlockBreakAnimation wrapper = new WrapperPlayServerBlockBreakAnimation();
            wrapper.setLocation(new BlockPosition(block.getLocation().toVector()));
            wrapper.setDestroyStage(this.distractionLevel - 1);
            wrapper.setEntityID(entityID);
            Bukkit.getLogger().info("Distraction level: " + distractionLevel);
            wrapper.sendPacket(Bukkit.getServer().getPlayer(player.getUniqueId()));
        }
    }

    public void sendBreakSoundAndParticle(Player player) {
        if (player.isOnline()) {
            //player.playSound(block.getLocation(), SoundBlocks.getBreakSoundFromMaterial(fakeMaterial), 1.0f, 1.0f);
            WrapperPlayServerWorldEvent wrapper = new WrapperPlayServerWorldEvent();
            wrapper.setEffectId(2001);
            wrapper.setLocation(new BlockPosition(block.getLocation().toVector()));
            wrapper.setDisableRelativeVolume(false);
            wrapper.setData(fakeMaterial.getId());
            wrapper.sendPacket(player);
            //player.playSound(block.getLocation(), SoundBlocks.getBreakSoundFromMaterial(fakeMaterial), 1.0f, SoundBlocks.getPitchFromMaterial(fakeMaterial));

            /*WrapperPlayServerNamedSoundEffect wrapper = new WrapperPlayServerNamedSoundEffect();
            wrapper.setEffectPositionX(block.getX());
            wrapper.setEffectPositionY(block.getY());
            wrapper.setEffectPositionZ(block.getZ());
            wrapper.setSoundEffect(SoundBlocks.getBreakSoundFromMaterial(fakeMaterial));
            wrapper.setSoundCategory(SoundBlocks.SOUND_CATEGORY);
            wrapper.setVolume(1);
            wrapper.setPitch(1);

            Bukkit.getLogger().info("SOUND");
            Bukkit.getLogger().info(fakeMaterial.toString());
            if (SoundBlocks.getBreakSoundFromMaterial(fakeMaterial) != null) {
                Bukkit.getLogger().info(SoundBlocks.getBreakSoundFromMaterial(fakeMaterial).toString());
            }
            Bukkit.getLogger().info(SoundBlocks.SOUND_CATEGORY.toString());*/
        }
    }

    public void sendHitSound(Player player) {
        if (player.isOnline()) {
            player.playSound(block.getLocation(), SoundBlocks.getHitSoundFromMaterial(fakeMaterial), 1.0f, SoundBlocks.getPitchFromMaterial(fakeMaterial));
        }
    }

    private void setValueFromMaterial(Material material) {
        if (BlockCityTycoonMine.getPlugin().getConfig().contains("value-of-blocks." + material.toString().toLowerCase())) {
            value = BlockCityTycoonMine.getPlugin().getConfig().getInt("value-of-blocks." + material.toString().toLowerCase());
        }
        else value = BlockCityTycoonMine.getPlugin().getConfig().getInt("value-of-blocks.default");
    }

    private void setMaxDurabilityFromMaterial(Material material) {
        if (BlockCityTycoonMine.getPlugin().getConfig().contains("durability-of-blocks." + material.toString().toLowerCase())) {
            maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks." + material.toString().toLowerCase());
        }
        else maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.default");
        /*switch (material) {
            case STONE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.stone");
            case COAL_ORE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.coal-ore");
            case COAL_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.coal-block");
            case IRON_ORE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.iron-ore");
            case IRON_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.iron-block");
            case GOLD_ORE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.gold-ore");
            case GOLD_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.gold-block");
            case DIAMOND_ORE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.diamond-ore");
            case DIAMOND_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.diamond-block");
            case EMERALD_ORE:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.emerald-ore");
            case EMERALD_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.emerald-block");
            case LAPIS_BLOCK:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.lapis-block");
            default:
                maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.default");
        }*/
    }

    public double getDurability() {
        return currentDurability;
    }

    public int getValue() {
        return value;
    }

    public int getDistractionLevel() {
        return distractionLevel;
    }

    public Block getBlock() {
        return block;
    }
    public Material getMaterial() {
        return fakeMaterial;
    }
    public ArmorStand getValueVisual() {
        return valueVisual;
    }
    public boolean isBroken() {
        return broken;
    }

    public boolean isSending() {
        return sending;
    }

    public boolean isNeededToSendBreakAnim() {
        return neededToSendBreakAnim;
    }

}