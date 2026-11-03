package wallis_ultimate.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.registry.ModEntities;

/**
 * ENEMY-011, phase 3: the mud shockwave from one of Hog Daddy's belly flops, a ring that grows across the pen. It only
 * catches feet on the ground, so players jump over it, or roll through it (CMBT-002 makes them immune for a moment).
 */
public class MudRing extends Entity {
    public static final float GROWTH_PER_TICK = 0.3F;
    public static final float MAX_RADIUS = 16.0F;
    /** How far on each side of its radius it reaches: at its speed, it takes 6 ticks to pass someone. */
    public static final float HALF_WIDTH = 0.9F;
    /** Feet this far above the ground are over it. Half a jump, which lasts longer than the ring takes to pass. */
    public static final double JUMP_CLEARANCE = 0.5;
    public static final float DAMAGE = 4.0F;
    private static final double PUSH = 0.5;
    private static final double PUSH_UP = 0.3;
    private static final int PARTICLES_PER_TICK = 6;

    @Nullable
    private HogDaddy owner;
    private final Set<UUID> caught = new HashSet<>();

    public MudRing(EntityType<? extends MudRing> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public static MudRing spawn(HogDaddy owner, Vec3 center) {
        MudRing ring = new MudRing(ModEntities.MUD_RING.get(), owner.level());
        ring.owner = owner;
        ring.setPos(center.x, GroundMark.floorBelow(owner.level(), center), center.z);
        owner.level().addFreshEntity(ring);
        return ring;
    }

    public float radius() {
        return tickCount * GROWTH_PER_TICK;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            float radius = radius();
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                double angle = random.nextDouble() * Math.PI * 2.0;
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), getX() + Math.cos(angle) * radius,
                        getY() + 0.1, getZ() + Math.sin(angle) * radius, 0.0, 0.2, 0.0);
            }
            return;
        }
        if (radius() > MAX_RADIUS) {
            discard();
            return;
        }
        if (owner == null) {
            return;
        }
        for (Player player : owner.fightPlayers()) {
            double distance = player.position().subtract(position()).horizontalDistance();
            if (Math.abs(distance - radius()) > HALF_WIDTH || player.getY() > getY() + JUMP_CLEARANCE || caught.contains(player.getUUID())) {
                continue;
            }
            if (player.hurt(damageSources().mobAttack(owner), DAMAGE)) {
                caught.add(player.getUUID());
                Vec3 out = player.position().subtract(position()).multiply(1.0, 0.0, 1.0);
                out = out.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : out.normalize();
                player.setDeltaMovement(out.x * PUSH, PUSH_UP, out.z * PUSH);
                player.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSquared) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
