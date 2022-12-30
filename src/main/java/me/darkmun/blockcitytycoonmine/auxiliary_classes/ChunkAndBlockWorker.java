package me.darkmun.blockcitytycoonmine.auxiliary_classes;

import me.darkmun.blockcitytycoonmine.BlockCityTycoonMine;
import me.darkmun.blockcitytycoonmine.durability.DurabilityBlock;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ChunkSection;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.*;

public class ChunkAndBlockWorker {

    public final static int STONE_MINE_CHUNK_X = -6;
    public final static int STONE_MINE_CHUNK_Z = 0;
    public final static int STONE_MINE_LOW_X = -95;
    public final static int STONE_MINE_HIGH_X = -90;
    public final static int STONE_MINE_LOW_Y = 36;
    public final static int STONE_MINE_HIGH_Y = 38;
    public final static int STONE_MINE_LOW_Z = 0;
    public final static int STONE_MINE_HIGH_Z = 4;
    private static final Map<UUID, List<DurabilityBlock>> playersDurabilityBlocks = new HashMap<>();
    private static final Map<UUID, Chunk> playersChunk = new HashMap<>();

    public static Chunk copyChunkTo(Chunk copyChunk, World world, int x, int z) {
        Chunk chunk = new Chunk(world, x, z);
        ChunkSection[] chunkSections = new ChunkSection[16];
        ChunkSection[] chunkSections1 = copyChunk.getSections();
        chunk.tileEntities.putAll(copyChunk.tileEntities);

        for(int i = 0; i < 16; i++) {
            if(chunkSections1[i] == null) {
                chunkSections[i] = null;
            }
            else {
                chunkSections[i] = new ChunkSection(chunkSections1[i].getYPosition(), true);
                chunkSections[i].a(chunkSections1[i].getEmittedLightArray());
                chunkSections[i].b(chunkSections1[i].getSkyLightArray());
                for(int j = 0; j < 16; j++) {
                    for(int k = 0; k < 16; k++) {
                        for(int l = 0; l < 16; l++) {
                            chunkSections[i].setType(j, k, l, chunkSections1[i].getType(j, k, l));
                        }
                    }
                }
                chunkSections[i].a();
            }
        }

        chunk.a(copyChunk.getBiomeIndex());
        chunk.a(chunkSections);
        return chunk;

    }

    public static void createChunkWithDurabilityBlocks(Player player) throws SQLException {
        createDurabilityBlocks(player);
        Chunk chunk = copyChunkTo(((CraftPlayer)player).getHandle().world.getChunkAt(STONE_MINE_CHUNK_X, STONE_MINE_CHUNK_Z), ((CraftWorld) player.getWorld()).getHandle(), STONE_MINE_CHUNK_X, STONE_MINE_CHUNK_Z);
        playersChunk.put(player.getUniqueId(), chunk);
        addDurabilityBlocksToPlayerChunk(player);
    }

    private static void createDurabilityBlocks(Player pl) throws SQLException {
        Map<Integer, Material> durBlockData = BlockCityTycoonMine.getDatabase().readDurabilityBlocks(pl.getUniqueId());
        List<DurabilityBlock> blocks = new ArrayList<>();
        int entID = Integer.MAX_VALUE;
        for (int i = STONE_MINE_LOW_X; i <= STONE_MINE_HIGH_X; i++) {
            for (int j = STONE_MINE_LOW_Y; j <= STONE_MINE_HIGH_Y; j++) {
                for (int k = STONE_MINE_HIGH_Z; k >= STONE_MINE_LOW_Z; k--) {
                    Block block = pl.getWorld().getBlockAt(i, j, k);
                    DurabilityBlock durBlock = new DurabilityBlock(
                            block,
                            durBlockData.isEmpty() ? Material.STONE : durBlockData.get(entID),
                            entID);
                    blocks.add(durBlock);
                    entID--;
                }
            }
        }
        playersDurabilityBlocks.put(pl.getUniqueId(), blocks);
    }

    @SuppressWarnings("deprecation")
    private static void addDurabilityBlocksToPlayerChunk(Player pl) {
        for (int i = STONE_MINE_LOW_X; i <= STONE_MINE_HIGH_X; i++) {
            for (int j = STONE_MINE_LOW_Y; j <= STONE_MINE_HIGH_Y; j++) {
                for (int k = STONE_MINE_HIGH_Z; k >= STONE_MINE_LOW_Z; k--) {
                    int finalI = i;
                    int finalJ = j;
                    int finalK = k;
                    @Nonnull DurabilityBlock durabilityBlock = Objects.requireNonNull(playersDurabilityBlocks.get(pl.getUniqueId()).stream().filter(durBlock ->
                            durBlock.getBlock().getX() == finalI && durBlock.getBlock().getY() == finalJ && durBlock.getBlock().getZ() == finalK).findAny().orElse(null));
                    int id = durabilityBlock.getMaterial().getId();

                    int x = getXInChunk(i);
                    int y = getYInChunk(j);
                    int z = getZInChunk(k);
                    playersChunk.get(pl.getUniqueId()).getSections()[2].setType(x,y,z, net.minecraft.server.v1_12_R1.Block.getById(id).getBlockData());
                }
            }
        }
    }

    public static void sendBreakAnimToDurabilityBlocks(Player player) {
        for (int i = STONE_MINE_LOW_X; i <= STONE_MINE_HIGH_X; i++) {
            for (int j = STONE_MINE_LOW_Y; j <= STONE_MINE_HIGH_Y; j++) {
                for (int k = STONE_MINE_HIGH_Z; k >= STONE_MINE_LOW_Z; k--) {
                    int finalI = i;
                    int finalJ = j;
                    int finalK = k;
                    @Nonnull DurabilityBlock durabilityBlock = Objects.requireNonNull(playersDurabilityBlocks.get(player.getUniqueId()).stream().filter(durBlock ->
                            durBlock.getBlock().getX() == finalI && durBlock.getBlock().getY() == finalJ && durBlock.getBlock().getZ() == finalK).findAny().orElse(null));
                    if (durabilityBlock.getDistractionLevel() != 0) {
                        durabilityBlock.sendBreakAnim(player);
                    }
                }
            }
        }
    }
    public static int getXInChunk(int x) {
        if (x < 0) {
            return 16 + x % 16;
        } else {
            return x % 16;
        }
    }
    public static int getYInChunk(int y) {
        return y % 16;
    }
    public static int getZInChunk(int z) {
        if (z < 0) {
            return 16 + z % 16;
        } else {
            return z % 16;
        }
    }

    public static List<DurabilityBlock> getDurabilityBlocks(UUID plUUID) {
        return playersDurabilityBlocks.get(plUUID);
    }

    public static Chunk getPlayerChunk(Player pl) {
        return playersChunk.get(pl.getUniqueId());
    }

    public static boolean hasDurabilityBlocks(Player player) {
        return playersDurabilityBlocks.containsKey(player.getUniqueId());
    }
}
