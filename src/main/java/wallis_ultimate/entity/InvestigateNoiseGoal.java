package wallis_ultimate.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

/**
 * ENEMY-006: a zombie without a target walks to the last noise it heard, then looks around from there. It may not
 * find a path right away (say, while falling), so it keeps trying until the zombie forgets the noise.
 */
public class InvestigateNoiseGoal extends Goal {
    private static final double SPEED = 1.0;

    private final RegularZombie zombie;
    private Path path;

    public InvestigateNoiseGoal(RegularZombie zombie) {
        this.zombie = zombie;
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos noise = zombie.heardNoise();
        if (noise == null || zombie.getTarget() != null) {
            return false;
        }
        path = zombie.getNavigation().createPath(noise, 1);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return zombie.heardNoise() != null && zombie.getTarget() == null && !zombie.getNavigation().isDone();
    }

    @Override
    public void start() {
        zombie.getNavigation().moveTo(path, SPEED);
    }

    @Override
    public void stop() {
        zombie.forgetNoise();
        zombie.getNavigation().stop();
    }
}
