package wallis_ultimate.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * WORLD-013: hurts and slows anything walking through it. There's nothing to collide with, so zombies path right
 * through it instead of walking around, which is what makes it hold them up.
 */
public class BarbedWireBlock extends Block {
    public static final float DAMAGE = 1.0F;
    /** Sweet berry bushes are 0.8, cobwebs 0.25. */
    private static final Vec3 SLOWDOWN = new Vec3(0.4, 0.75, 0.4);
    /** Same threshold as sweet berry bushes: standing still in it doesn't hurt. */
    private static final double MOVING = 0.003;

    public BarbedWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        entity.makeStuckInBlock(state, SLOWDOWN);
        if (!level.isClientSide() && (Math.abs(entity.getX() - entity.xOld) >= MOVING || Math.abs(entity.getZ() - entity.zOld) >= MOVING)) {
            entity.hurt(level.damageSources().cactus(), DAMAGE);
        }
    }
}
