package me.darkmun.blockcitytycoonmine.auxiliary_classes;

import org.bukkit.Material;
import org.bukkit.Sound;

public class SoundBlocks {
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
