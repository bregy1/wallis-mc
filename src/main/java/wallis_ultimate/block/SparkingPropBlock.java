package wallis_ultimate.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** WORLD-013: broken machinery that still has some power in it, and now and then throws a spark. */
public class SparkingPropBlock extends FacingPropBlock {
    private static final int SPARK_CHANCE = 12;
    private static final int SOUND_CHANCE = 5;

    public SparkingPropBlock(BlockBehaviour.Properties properties, double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        super(properties, fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(SPARK_CHANCE) != 0) {
            return;
        }
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + 0.3 + random.nextDouble() * 0.7;
        double z = pos.getZ() + random.nextDouble();
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, (random.nextDouble() - 0.5) * 0.3, random.nextDouble() * 0.2, (random.nextDouble() - 0.5) * 0.3);
        }
        if (random.nextInt(SOUND_CHANCE) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.2F, 1.6F + random.nextFloat() * 0.4F, false);
        }
    }
}
