package me.darkmun.blockcitytycoonmine.durability;

import com.comphenix.packetwrapper.*;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.ChunkAndBlockWorker;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.RandomMaterial;
import me.darkmun.blockcitytycoonmine.auxiliary_classes.SoundBlocks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


@SuppressWarnings("deprecation")
public class DurabilityBlock {

    private final double MAX_DISTRACTION_LEVEL = 10;
    private final Block block;
    private Material fakeMaterial;
    private double maxDurability; //мейби поменять дюрабилити на класс с enum
    private double currentDurability;
    private int distractionLevel = 0;
    private final int entityID;
    public int taskID;
    private double value;
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
        setMaxDurabilityFromMaterial(this.fakeMaterial);
        this.currentDurability = maxDurability;
        this.distractionLevel = 0;
        this.durabilityToNextDistractionLevel = maxDurability/MAX_DISTRACTION_LEVEL;
    }

    public void sendBreak(Player player) {
        int x = ChunkAndBlockWorker.getXInChunk(block.getX());
        int y = ChunkAndBlockWorker.getYInChunk(block.getY());
        int z = ChunkAndBlockWorker.getZInChunk(block.getZ());
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

        Bukkit.getScheduler().runTaskLater(BlockCityTycoonMine.getPlugin(), () -> {

            Material randMaterial = RandomMaterial.getForPlayer(player);
            if (randMaterial == Material.AIR) {
                player.sendMessage("как блин такое могло случится?");
                fakeMaterial = Material.YELLOW_FLOWER;
            }
            else {
                fakeMaterial = randMaterial;
            }

            WrapperPlayServerBlockChange wrapper1 = new WrapperPlayServerBlockChange();
            wrapper1.setLocation(new BlockPosition(block.getLocation().toVector()));
            wrapper1.setBlockData(WrappedBlockData.createData(fakeMaterial)); //менять тут на рандом

            if(((CraftPlayer)player).getHandle().playerConnection != null && player.isOnline()) {
                sending = true;
                wrapper1.sendPacket(BlockCityTycoonMine.getPlugin().getServer().getPlayer(player.getUniqueId()));
                sending = false;
            }

            ChunkAndBlockWorker.getPlayerChunk(player).getSections()[2].setType(x, y, z, net.minecraft.server.v1_12_R1.Block.getById(fakeMaterial.getId()).getBlockData());

            refreshDurability();
            sendBreakAnim(player);
        }, (BlockCityTycoonMine.getPlugin().getConfig().getLong("stone-spawn-speed") * 20));
    }

    public void sendBreakAnim(Player player) {
        if (player.isOnline()) {
            WrapperPlayServerBlockBreakAnimation wrapper = new WrapperPlayServerBlockBreakAnimation();
            wrapper.setLocation(new BlockPosition(block.getLocation().toVector()));
            wrapper.setDestroyStage(this.distractionLevel - 1);
            wrapper.setEntityID(entityID);
            wrapper.sendPacket(Bukkit.getServer().getPlayer(player.getUniqueId()));
        }
    }

    public void sendBreakSoundAndParticle(Player player) {
        if (player.isOnline()) {
            WrapperPlayServerWorldEvent wrapper = new WrapperPlayServerWorldEvent();
            wrapper.setEffectId(2001);
            wrapper.setLocation(new BlockPosition(block.getLocation().toVector()));
            wrapper.setDisableRelativeVolume(false);
            wrapper.setData(fakeMaterial.getId());
            wrapper.sendPacket(player);
        }
    }

    public void sendHitSound(Player player) {
        if (player.isOnline()) {
            player.playSound(block.getLocation(), SoundBlocks.getHitSoundFromMaterial(fakeMaterial), 1.0f, SoundBlocks.getPitchFromMaterial(fakeMaterial));
        }
    }

    public void giveToPlayer(Player player) {
        player.getInventory().addItem(new ItemStack(fakeMaterial, 1));
    }

    private void setValueFromMaterial(Material material) {
        if (BlockCityTycoonMine.getPlugin().getConfig().contains("value-of-blocks." + material.toString().toLowerCase())) {
            value = BlockCityTycoonMine.getPlugin().getConfig().getDouble("value-of-blocks." + material.toString().toLowerCase());
        }
        else value = BlockCityTycoonMine.getPlugin().getConfig().getDouble("value-of-blocks.default");
    }

    public void updateValue() {  // надо бы пользоваться этим методом лучше
        value = BlockCityTycoonMine.getPlugin().getConfig().getDouble("value-of-blocks." + fakeMaterial.toString().toLowerCase());
    }

    private void setMaxDurabilityFromMaterial(Material material) {
        if (BlockCityTycoonMine.getPlugin().getConfig().contains("durability-of-blocks." + material.toString().toLowerCase())) {
            maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks." + material.toString().toLowerCase());
        }
        else maxDurability = BlockCityTycoonMine.getPlugin().getConfig().getInt("durability-of-blocks.default");
    }

    public double getValue() {
        return value;
    }

    public int getDistractionLevel() {
        return distractionLevel;
    }

    public int getID() {
        return entityID;
    }

    public Block getBlock() {
        return block;
    }
    public Material getMaterial() {
        return fakeMaterial;
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