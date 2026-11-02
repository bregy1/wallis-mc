package wallis_ultimate.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.entity.RegularZombie;

/**
 * ENEMY-006: loud things pull nearby zombies toward the noise. Walking, sprinting and mining are quiet; only things
 * that call this make noise.
 */
public final class Noise {
    private Noise() {
    }

    public static void make(Level level, Vec3 pos, double radius) {
        if (level.isClientSide()) {
            return;
        }
        BlockPos noise = BlockPos.containing(pos);
        for (RegularZombie zombie : level.getEntitiesOfClass(RegularZombie.class, new AABB(pos, pos).inflate(radius),
                zombie -> zombie.distanceToSqr(pos) <= radius * radius)) {
            zombie.hearNoise(noise);
        }
    }
}
