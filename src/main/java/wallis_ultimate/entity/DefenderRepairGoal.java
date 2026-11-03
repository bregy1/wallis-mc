package wallis_ultimate.entity;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import wallis_ultimate.survival.Garrisons;

/**
 * SURV-011: the defenders are the builders. In quiet moments, one walks over to something broken and puts it back. If
 * it can't get there in time it fixes it anyway, so nothing stays broken just because the way there is blocked.
 */
public class DefenderRepairGoal extends Goal {
    private static final double SPEED = 0.9;
    private static final double REACH_SQR = 4.0 * 4.0;
    private static final int GIVE_UP_WALKING_TICKS = 200;

    private final Defender defender;
    @Nullable
    private BlockPos broken;
    private int ticks;

    public DefenderRepairGoal(Defender defender) {
        this.defender = defender;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (defender.getTarget() != null || defender.isParachuting()) {
            return false;
        }
        broken = Garrisons.nextRepair(defender);
        return broken != null;
    }

    @Override
    public boolean canContinueToUse() {
        return broken != null && defender.getTarget() == null;
    }

    @Override
    public void start() {
        ticks = 0;
        defender.getNavigation().moveTo(broken.getX() + 0.5, broken.getY(), broken.getZ() + 0.5, SPEED);
    }

    @Override
    public void stop() {
        defender.getNavigation().stop();
        broken = null;
    }

    @Override
    public void tick() {
        ticks++;
        defender.getLookControl().setLookAt(broken.getX() + 0.5, broken.getY() + 0.5, broken.getZ() + 0.5);
        boolean close = defender.distanceToSqr(broken.getX() + 0.5, broken.getY() + 0.5, broken.getZ() + 0.5) <= REACH_SQR;
        if (close || ticks >= GIVE_UP_WALKING_TICKS) {
            Garrisons.repair(defender, broken);
            broken = null;
        } else if (defender.getNavigation().isDone() && ticks % 40 == 0) {
            defender.getNavigation().moveTo(broken.getX() + 0.5, broken.getY(), broken.getZ() + 0.5, SPEED);
        }
    }
}
