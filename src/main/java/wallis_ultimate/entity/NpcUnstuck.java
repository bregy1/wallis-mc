package wallis_ultimate.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import wallis_ultimate.registry.ModBlocks;

/**
 * SURV-011: no NPC stays trapped in a hole (your words: "I dont want npcs to be trapped in holes and stuff"). Any NPC can
 * use this: give it a field of this class and call {@link #tick} from its customServerAiStep. If it sits inside a block,
 * sits in a hole it can't climb out of, or is trying to walk but not getting anywhere for a few seconds, it teleports to
 * the closest free spot to stand on.
 */
public final class NpcUnstuck {
    public static final int STUCK_TICKS = 100;
    public static final int SEARCH_RADIUS = 16;
    private static final int CHECK_TICKS = 10;
    private static final int SEARCH_HEIGHT = 5;
    private static final double MOVED_SQR = 0.25;

    private int stuckTicks;
    private double lastX;
    private double lastY;
    private double lastZ;

    public void tick(Mob npc) {
        if (npc.tickCount % CHECK_TICKS != 0 || npc.isPassenger()) {
            return;
        }
        boolean moved = npc.distanceToSqr(lastX, lastY, lastZ) > MOVED_SQR;
        lastX = npc.getX();
        lastY = npc.getY();
        lastZ = npc.getZ();
        boolean tryingToWalk = !npc.getNavigation().isDone();
        boolean stuck = npc.isInWall() || isInHole(npc.level(), npc.blockPosition()) || tryingToWalk && !moved;
        stuckTicks = stuck ? stuckTicks + CHECK_TICKS : 0;
        if (stuckTicks >= STUCK_TICKS) {
            stuckTicks = 0;
            BlockPos free = freeSpotNear(npc.level(), npc.blockPosition());
            if (free != null) {
                moveTo(npc, free);
            }
        }
    }

    /** Teleports an NPC, with a small puff where it was and where it lands, and makes it forget where it was walking. */
    public static void moveTo(Mob npc, BlockPos pos) {
        puff(npc);
        npc.getNavigation().stop();
        npc.teleportTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        npc.fallDistance = 0.0F;
        puff(npc);
    }

    private static void puff(Mob npc) {
        if (npc.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.POOF, npc.getX(), npc.getY() + 1.0, npc.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
        }
    }

    /** The closest place near here where an NPC can stand and walk away from, or null if there's none in reach. */
    @Nullable
    public static BlockPos freeSpotNear(BlockGetter level, BlockPos around) {
        for (int radius = 1; radius <= SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    for (int step = 0; step <= SEARCH_HEIGHT * 2; step++) {
                        // 0, +1, -1, +2, -2, ...: stay as close to its height as possible.
                        int dy = step % 2 == 0 ? step / 2 : -(step + 1) / 2;
                        BlockPos pos = around.offset(dx, dy, dz);
                        if (canStandAt(level, pos) && !isInHole(level, pos)) {
                            return pos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean canStandAt(BlockGetter level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState ground = level.getBlockState(below);
        return !ground.getCollisionShape(level, below).isEmpty() && ground.getFluidState().isEmpty() && !ground.is(BlockTags.FIRE)
                && isOpen(level, pos) && isOpen(level, pos.above());
    }

    private static boolean isOpen(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty() && !state.is(BlockTags.FIRE)
                && !state.is(ModBlocks.BARBED_WIRE.get());
    }

    /** All four sides are blocked: too high to step up, nothing open to walk into. */
    private static boolean isInHole(BlockGetter level, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos next = pos.relative(side);
            boolean walk = isOpen(level, next) && isOpen(level, next.above());
            boolean climb = isOpen(level, next.above()) && isOpen(level, next.above(2));
            if (walk || climb) {
                return false;
            }
        }
        return true;
    }
}
