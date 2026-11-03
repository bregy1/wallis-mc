package wallis_ultimate.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * SURV-011: a defender's fight. It shoots from where it stands while it can see the target, walks closer when it can't
 * or the target is too far, and hits what's right next to it.
 */
public class DefenderAttackGoal extends Goal {
    private static final double MELEE_RANGE_SQR = 2.8 * 2.8;
    private static final double SHOT_RANGE_SQR = Defender.SHOT_RANGE * Defender.SHOT_RANGE;
    private static final int MELEE_COOLDOWN = 20;
    private static final int SHOT_COOLDOWN = 30;
    private static final int SHOT_COOLDOWN_SPREAD = 11;
    private static final int PATH_REFRESH_TICKS = 20;
    private static final double SPEED = 1.0;

    private final Defender defender;
    private int cooldown;
    private int pathTicks;

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
        boolean sees = defender.getSensing().hasLineOfSight(target);
        cooldown = Math.max(cooldown - 1, 0);
        if (distanceSqr <= MELEE_RANGE_SQR) {
            defender.getNavigation().stop();
            if (cooldown == 0) {
                defender.swing(defender.getUsedItemHand());
                defender.doHurtTarget(target);
                cooldown = MELEE_COOLDOWN;
            }
        } else if (sees && distanceSqr <= SHOT_RANGE_SQR && !defender.isParachuting()) {
            defender.getNavigation().stop();
            if (cooldown == 0) {
                defender.shoot(target);
                cooldown = SHOT_COOLDOWN + defender.getRandom().nextInt(SHOT_COOLDOWN_SPREAD);
            }
        } else if (--pathTicks <= 0) {
            defender.getNavigation().moveTo(target, SPEED);
            pathTicks = PATH_REFRESH_TICKS;
        }
    }
}
