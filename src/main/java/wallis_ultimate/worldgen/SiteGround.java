package wallis_ultimate.worldgen;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/** Finding a place for a structure, and making the ground fit it. */
final class SiteGround {
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    /** Heights are compared at this many places along each side of the site; asking the generator isn't free. */
    private static final int SAMPLES = 4;
    /** Heights are read this far around the site too, so it stays that far from any water. */
    private static final int WATER_DISTANCE = 20;
    /** Ground this little above the water counts as the shore. */
    private static final int SHORE_HEIGHT = 2;
    /** How much a spot on the shore or in the water counts against it, in blocks of unevenness. */
    private static final int WET_PENALTY = 100;
    /** Less than that: better the wrong biome than the water. */
    private static final int WRONG_BIOME_PENALTY = 50;

    private SiteGround() {
    }

    /**
     * How bad this spot is for a structure of that size: its unevenness, whether it's on the water's edge, and whether
     * it's in the wanted kind of biome. Asks the world generator, so nothing needs to be generated yet.
     */
    static int badness(ServerLevel level, int fromX, int fromZ, int sizeX, int sizeZ, Predicate<Holder<Biome>> wantedBiome) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState random = level.getChunkSource().randomState();
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        int wet = 0;
        Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(fromX + sizeX / 2), QuartPos.fromBlock(generator.getSeaLevel()),
                QuartPos.fromBlock(fromZ + sizeZ / 2), random.sampler());
        int wrongBiome = wantedBiome.test(biome) ? 0 : WRONG_BIOME_PENALTY;
        for (int stepX = 0; stepX < SAMPLES; stepX++) {
            for (int stepZ = 0; stepZ < SAMPLES; stepZ++) {
                int x = fromX - WATER_DISTANCE + (sizeX + 2 * WATER_DISTANCE) * stepX / (SAMPLES - 1);
                int z = fromZ - WATER_DISTANCE + (sizeZ + 2 * WATER_DISTANCE) * stepZ / (SAMPLES - 1);
                int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                lowest = Math.min(lowest, ground);
                highest = Math.max(highest, ground);
                // Beaches are the flattest ground there is, so without this everything would end up on the shore.
                if (ground <= generator.getSeaLevel() + SHORE_HEIGHT) {
                    wet = WET_PENALTY;
                }
            }
        }
        return highest - lowest + wet + wrongBiome;
    }

    /** The height to build on: the middle of the spot, but never below the water. */
    static int buildHeight(ServerLevel level, int fromX, int fromZ, int sizeX, int sizeZ) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        int ground = generator.getBaseHeight(fromX + sizeX / 2, fromZ + sizeZ / 2, Heightmap.Types.OCEAN_FLOOR_WG, level, level.getChunkSource().randomState());
        return Math.max(ground, generator.getSeaLevel());
    }

    /** Generates the chunks of the site, so its ground can be read and changed. */
    static void loadChunks(ServerLevel level, int fromX, int fromZ, int toX, int toZ) {
        for (int chunkX = fromX >> 4; chunkX <= toX >> 4; chunkX++) {
            for (int chunkZ = fromZ >> 4; chunkZ <= toZ >> 4; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    /**
     * Makes the ground flat at the given height across the site, with no trees or anything else above it. Around it,
     * for the width of the margin, the ground slopes back to what it was.
     */
    static void flatten(ServerLevel level, int fromX, int fromZ, int sizeX, int sizeZ, int groundY, int margin) {
        loadChunks(level, fromX - margin, fromZ - margin, fromX + sizeX + margin, fromZ + sizeZ + margin);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = fromX - margin; x < fromX + sizeX + margin; x++) {
            for (int z = fromZ - margin; z < fromZ + sizeZ + margin; z++) {
                int outsideX = Math.max(Math.max(fromX - x, x - (fromX + sizeX - 1)), 0);
                int outsideZ = Math.max(Math.max(fromZ - z, z - (fromZ + sizeZ - 1)), 0);
                float distance = (float) Math.sqrt(outsideX * outsideX + outsideZ * outsideZ);
                if (distance > margin) {
                    continue;
                }
                int natural = naturalGround(level, x, z);
                int target = Math.round(Mth.lerp(distance / margin, groundY, natural));
                BlockState surface = level.getBlockState(pos.set(x, natural, z));
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = top; y > target; y--) {
                    level.setBlock(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), QUIET);
                }
                for (int y = natural + 1; y < target; y++) {
                    level.setBlock(pos.set(x, y, z), Blocks.DIRT.defaultBlockState(), QUIET);
                }
                level.setBlock(pos.set(x, target, z), surface, QUIET);
            }
        }
    }

    /** The ground's height, looking past trees, plants, snow and water. */
    static int naturalGround(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
        while (pos.getY() > level.getMinBuildHeight() && !isGround(level.getBlockState(pos))) {
            pos.move(0, -1, 0);
        }
        return pos.getY();
    }

    private static boolean isGround(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.TERRACOTTA)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.CLAY);
    }
}
