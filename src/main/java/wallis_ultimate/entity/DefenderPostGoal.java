package wallis_ultimate.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * SURV-011: while nothing's going on, a defender goes back to its post and shuffles around it. If it can't find a way
 * back for a while, it teleports there rather than stand around somewhere it can't leave.
 */
public class DefenderPostGoal extends Goal {
    private static final double SPEED = 0.8;
    private static final double AT_POST_SQR = 5.0 * 5.0;
    private static final int STROLL_CHANCE = 240;
    private static final int STROLL_RADIUS = 3;
    private static final int GIVE_UP_TICKS = 300;

    private final Defender defender;
    private long noWaySince = -1;

    public DefenderPostGoal(Defender defender) {
        this.defender = defender;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private boolean isAwayFromPost() {
        BlockPos post = defender.post();
        return defender.distanceToSqr(post.getX() + 0.5, post.getY(), post.getZ() + 0.5) > AT_POST_SQR;
    }

    @Override
    public boolean canUse() {
        if (defender.post() == null || defender.getTarget() != null || defender.isParachuting()) {
            return false;
        }
        return isAwayFromPost() || defender.getRandom().nextInt(STROLL_CHANCE) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return defender.getTarget() == null && !defender.getNavigation().isDone();
    }

    @Override
    public void start() {
        BlockPos post = defender.post();
        int dx = defender.getRandom().nextInt(STROLL_RADIUS * 2 + 1) - STROLL_RADIUS;
        int dz = defender.getRandom().nextInt(STROLL_RADIUS * 2 + 1) - STROLL_RADIUS;
        boolean found = defender.getNavigation().moveTo(post.getX() + dx + 0.5, post.getY(), post.getZ() + dz + 0.5, SPEED);
        if (found || !isAwayFromPost()) {
            noWaySince = -1;
            return;
        }
        long now = defender.level().getGameTime();
        if (noWaySince < 0) {
            noWaySince = now;
        } else if (now - noWaySince >= GIVE_UP_TICKS) {
            noWaySince = -1;
            BlockPos free = NpcUnstuck.freeSpotNear(defender.level(), post);
            NpcUnstuck.moveTo(defender, free != null ? free : post);
        }
    }
}
