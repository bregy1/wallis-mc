package wallis_ultimate.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.combat.Noise;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.registry.ModItems;

/**
 * GEAR-007: a thrown Pipe Bomb. It lies where it lands, beeps and lures zombies (ENEMY-006), then explodes. It hurts
 * and pushes whatever is close but never damages blocks (Q-142), so it doesn't use vanilla's explosion.
 */
public class PipeBomb extends ThrowableItemProjectile {
    public static final int FUSE_TICKS = 80;
    public static final double LURE_RADIUS = 24.0;
    public static final double BLAST_RADIUS = 4.0;
    public static final float BLAST_DAMAGE = 12.0F;
    public static final double BLAST_NOISE_RADIUS = 48.0;
    private static final int BEEP_INTERVAL_TICKS = 20;
    private static final double KNOCKBACK = 0.8;
    private static final EntityDataAccessor<Boolean> LANDED = SynchedEntityData.defineId(PipeBomb.class, EntityDataSerializers.BOOLEAN);

    private int fuse;

    public PipeBomb(EntityType<? extends PipeBomb> type, Level level) {
        super(type, level);
    }

    public PipeBomb(Level level, LivingEntity thrower) {
        super(ModEntities.PIPE_BOMB.get(), thrower, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LANDED, false);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PIPE_BOMB.get();
    }

    public void land() {
        entityData.set(LANDED, true);
        setDeltaMovement(Vec3.ZERO);
    }

    public boolean hasLanded() {
        return entityData.get(LANDED);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        setPos(result.getLocation());
        land();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        land();
    }

    @Override
    public void tick() {
        if (!hasLanded()) {
            super.tick();
            return;
        }
        baseTick();
        if (level().isClientSide()) {
            return;
        }
        if (fuse % BEEP_INTERVAL_TICKS == 0) {
            level().playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.PLAYERS, 1.0F, 1.8F);
            Noise.make(level(), position(), LURE_RADIUS);
        }
        if (++fuse >= FUSE_TICKS) {
            explode();
        }
    }

    private void explode() {
        level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0F, 1.0F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        Noise.make(level(), position(), BLAST_NOISE_RADIUS);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(BLAST_RADIUS),
                entity -> distanceTo(entity) <= BLAST_RADIUS)) {
            float falloff = 1.0F - distanceTo(entity) / (float) BLAST_RADIUS;
            entity.hurt(damageSources().explosion(this, getOwner()), BLAST_DAMAGE * falloff);
            entity.knockback(KNOCKBACK * falloff, getX() - entity.getX(), getZ() - entity.getZ());
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Landed", hasLanded());
        tag.putInt("Fuse", fuse);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(LANDED, tag.getBoolean("Landed"));
        fuse = tag.getInt("Fuse");
    }
}
