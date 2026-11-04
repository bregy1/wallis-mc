package wallis_ultimate.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

/**
 * SURV-011: a defender's fight. It shoots whenever it can see the target, walks up to it from its post, and hits what's
 * right next to it. Lookouts high up stay where they are.
 */
public class DefenderAttackGoal extends Goal {
    private static final double MELEE_RANGE_SQR = 2.8 * 2.8;
    private static final double SHOT_RANGE_SQR = Defender.SHOT_RANGE * Defender.SHOT_RANGE;
    private static final int MELEE_COOLDOWN = 20;
    private static final int SHOT_COOLDOWN = 30;
    private static final int SHOT_COOLDOWN_SPREAD = 11;
    private static final int PATH_REFRESH_TICKS = 20;
    private static final double SPEED = 1.0;
    /** How close a guard walks up before it just stands and shoots. Close enough to get hurt: the fights cost lives (SURV-011). */
    private static final double STAND_OFF_SQR = 5.0 * 5.0;
    /** A guard this far above its target is a lookout, on a tower or a roof. Wall tops are lower, and walls get left. */
    private static final double LOOKOUT_HEIGHT = 5.0;
    /** How long a guard with no way to its target and no shot at it stands around before it goes back up. */
    private static final int BACK_TO_POST_TICKS = 100;

    private final Defender defender;
    private int cooldown;
    private int pathTicks;
    private int noWayTicks;

    public DefenderAttackGoal(Defender defender) {
        this.defender = defender;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = defender.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        defender.setAggressive(true);
        pathTicks = 0;
    }

    @Override
    public void stop() {
        defender.setAggressive(false);
        defender.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = defender.getTarget();
        if (target == null) {
            return;
        }
        defender.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distanceSqr = defender.distanceToSqr(target);
        boolean sees = defender.getSensing().hasLineOfSight(target) || defender.canFireDownAt(target);
        cooldown = Math.max(cooldown - 1, 0);
        if (distanceSqr <= MELEE_RANGE_SQR) {
            defender.getNavigation().stop();
            if (cooldown == 0) {
                defender.swing(defender.getUsedItemHand());
                defender.doHurtTarget(target);
                cooldown = MELEE_COOLDOWN;
            }
            return;
        }
        boolean inRange = sees && distanceSqr <= SHOT_RANGE_SQR && !defender.isParachuting();
        if (inRange && cooldown == 0) {
            defender.shoot(target);
            cooldown = SHOT_COOLDOWN + defender.getRandom().nextInt(SHOT_COOLDOWN_SPREAD);
        }
        // High up, a guard stays put and keeps watch: a tower is no use empty. Everyone else comes down and meets them.
        boolean holdsPosition = defender.getY() - target.getY() >= LOOKOUT_HEIGHT;
        if (holdsPosition || inRange && distanceSqr <= STAND_OFF_SQR) {
            defender.getNavigation().stop();
        } else if (--pathTicks <= 0) {
            pathTicks = PATH_REFRESH_TICKS;
            walkUpTo(target, inRange);
        }
    }

    /**
     * Only if there's a way there. Enemies pressing against the far side of a wall can't be reached: a guard with a clear
     * shot stays where it is and keeps shooting, and one without goes back to its post on the wall to get one.
     */
    private void walkUpTo(LivingEntity target, boolean hasShot) {
        Path path = defender.getNavigation().createPath(target, 0);
        if (path != null && path.canReach()) {
            noWayTicks = 0;
            defender.getNavigation().moveTo(path, SPEED);
            return;
        }
        defender.getNavigation().stop();
        noWayTicks = hasShot ? 0 : noWayTicks + PATH_REFRESH_TICKS;
        BlockPos post = defender.post();
        if (noWayTicks >= BACK_TO_POST_TICKS && post != null) {
            noWayTicks = 0;
            BlockPos free = NpcUnstuck.freeSpotNear(defender.level(), post);
            NpcUnstuck.moveTo(defender, free != null ? free : post);
        }
    }
}
