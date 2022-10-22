package me.darkmun.blockcitytycoonmine.auxiliary_classes;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class SoundBlocks {

    public static final EnumWrappers.SoundCategory SOUND_CATEGORY_WRAPPER = EnumWrappers.SoundCategory.BLOCKS;
    public static final SoundCategory SOUND_CATEGORY = SoundCategory.BLOCKS;

    public static Sound getBreakSoundFromMaterial(Material material) {
        switch (material) {
            case STONE:
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return Sound.BLOCK_STONE_BREAK;
            case COAL_BLOCK:
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case DIAMOND_BLOCK:
            case EMERALD_BLOCK:
            case LAPIS_BLOCK:
                return Sound.BLOCK_METAL_BREAK;
            default:
                return null;
        }
    }

    public static Sound getHitSoundFromMaterial(Material material) {
        switch (material) {
            case STONE:
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return Sound.BLOCK_STONE_HIT;
            case COAL_BLOCK:
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case DIAMOND_BLOCK:
            case EMERALD_BLOCK:
            case LAPIS_BLOCK:
                return Sound.BLOCK_METAL_HIT;
            default:
                return null;
        }
    }

    public static float getPitchFromMaterial(Material material) {
        switch (material) {
            case STONE:
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return 0.8f;
            case COAL_BLOCK:
            case IRON_BLOCK:
            case GOLD_BLOCK:
            case DIAMOND_BLOCK:
            case EMERALD_BLOCK:
            case LAPIS_BLOCK:
                return 1.3f;
            default:
                return 0;
        }
    }
}
