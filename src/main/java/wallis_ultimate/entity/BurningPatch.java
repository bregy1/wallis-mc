package wallis_ultimate.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * GEAR-007: the flames a Moonshine Molotov leaves behind. It's an entity, not fire blocks, so it never lights
 * anything or spreads (Q-142). Burns whatever stands in it and pushes enemies back out, so it blocks a path.
 */
public class BurningPatch extends Entity {
    public static final int LIFETIME_TICKS = 100;
    public static final double RADIUS = 1.5;
    /** Per hit, at two hits a second: 2 damage per second. */
    public static final float DAMAGE = 1.0F;
    private static final int BURN_SECONDS = 3;
    private static final double PUSH = 0.25;
    private static final int PARTICLES_PER_TICK = 4;

    private int age;

    public BurningPatch(EntityType<? extends BurningPatch> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                double distance = Math.sqrt(random.nextDouble()) * RADIUS;
                level().addParticle(ParticleTypes.FLAME, getX() + Math.cos(angle) * distance, getY() + 0.1,
                        getZ() + Math.sin(angle) * distance, 0.0, 0.03, 0.0);
            }
            return;
        }
        if (++age >= LIFETIME_TICKS) {
            discard();
            return;
        }
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(RADIUS, 1.0, RADIUS),
                entity -> horizontalDistanceTo(entity) <= RADIUS)) {
            // Every tick, so touching the flames burns right away; the usual hit timer limits it to two hits a second.
            entity.hurt(damageSources().inFire(), DAMAGE);
            entity.setSecondsOnFire(BURN_SECONDS);
            if (entity instanceof Enemy) {
                Vec3 out = entity.position().subtract(position()).multiply(1.0, 0.0, 1.0);
                entity.setDeltaMovement(out.lengthSqr() < 1.0E-4 ? new Vec3(PUSH, 0.0, 0.0) : out.normalize().scale(PUSH));
                entity.hurtMarked = true;
            }
        }
    }

    private double horizontalDistanceTo(Entity entity) {
        return entity.position().subtract(position()).horizontalDistance();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
    }
}
