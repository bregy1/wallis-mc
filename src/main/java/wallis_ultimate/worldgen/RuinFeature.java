package wallis_ultimate.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * WORLD-014: a small themed ruin as the terrain generates. Where they go and how often is in its placed_feature; this
 * only checks the spot: dry, and flat enough that it sits on the ground. Which ones exist is in RuinBuilds. A site
 * flattens its ground when it's built, which clears any ruin that was there.
 */
public class RuinFeature extends Feature<NoneFeatureConfiguration> {
    /** Blocks of height difference across a ruin's corners that it can still sit on. */
    private static final int MOST_UNEVEN = 2;
    private static final int QUIET = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    @Nullable
    private static volatile BlockPos lastPlaced;

    public RuinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /** For the smoke test, which goes and looks at one. */
    @Nullable
    public static BlockPos lastPlaced() {
        return lastPlaced;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos middle = context.origin();
        Blueprint ruin = RuinBuilds.random(random);
        int halfX = ruin.sizeX() / 2;
        int halfZ = ruin.sizeZ() / 2;
        int reach = Math.max(halfX, halfZ);
        if (!fits(level, middle, reach)) {
            return false;
        }
        Rotation rotation = Rotation.getRandom(random);
        ruin.placeTurned(level, middle, rotation);
        fillUnderneath(level, middle, ruin, rotation);
        lastPlaced = middle;
        return true;
    }

    /** Checks the corners and the middle, which is enough to keep them out of water and off cliffs. */
    private static boolean fits(WorldGenLevel level, BlockPos middle, int reach) {
        int[][] samples = {{0, 0}, {-reach, -reach}, {reach, -reach}, {-reach, reach}, {reach, reach}};
        for (int[] sample : samples) {
            int x = middle.getX() + sample[0];
            int z = middle.getZ() + sample[1];
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            if (surface != level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) || Math.abs(surface - middle.getY()) > MOST_UNEVEN) {
                return false;
            }
        }
        return true;
    }

    /** Where the ground dips a little, it gets filled up to meet the ruin. */
    private static void fillUnderneath(WorldGenLevel level, BlockPos middle, Blueprint ruin, Rotation rotation) {
        BlockPos center = new BlockPos(ruin.sizeX() / 2, 0, ruin.sizeZ() / 2);
        BlockPos.MutableBlockPos fill = new BlockPos.MutableBlockPos();
        ruin.blocks().keySet().forEach(pos -> {
            if (pos.getY() <= 0) {
                BlockPos at = middle.offset(pos.subtract(center).rotate(rotation));
                int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, at.getX(), at.getZ());
                for (int y = surface; y < middle.getY() - 1 && y < at.getY(); y++) {
                    level.setBlock(fill.set(at.getX(), y, at.getZ()), Blocks.DIRT.defaultBlockState(), QUIET);
                }
            }
        });
    }
}
