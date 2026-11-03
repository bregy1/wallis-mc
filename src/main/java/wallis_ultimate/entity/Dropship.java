package wallis_ultimate.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.registry.ModEntities;

/**
 * SURV-011: supporting troops flying in. A VTOL from before the fall flies to a safe spot, hovers over it while its
 * troops jump out under parachutes, and leaves. Placeholder model by Claude. Only seen by the players around, not saved:
 * if the server stops before it's done, the safe spot just counts its guards again.
 */
public class Dropship extends Entity {
    /** How high above the safe spot it hovers. */
    public static final int ALTITUDE = 30;
    private static final int CLEARANCE = 20;
    private static final double SPEED = 0.9;
    private static final double LEAVE_SPEED = 1.1;
    private static final int DROP_TICKS = 15;
    private static final int LIFETIME_TICKS = 900;
    private static final int LEAVE_TICKS = 120;
    private static final int ENGINE_SOUND_TICKS = 50;
    private static final double ARRIVED_SQR = 2.0 * 2.0;

    private final List<BlockPos> posts = new ArrayList<>();
    private String spotId = "";
    private Vec3 hover = Vec3.ZERO;
    private int age;
    private int leavingTicks = -1;

    public Dropship(EntityType<? extends Dropship> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    /** Sends one over the given place. It starts where it comes into view, so what's there has to be loaded. */
    public static Dropship send(ServerLevel level, String spotId, BlockPos center, List<BlockPos> posts, int approachDistance) {
        Dropship dropship = ModEntities.DROPSHIP.get().create(level);
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        dropship.spotId = spotId;
        dropship.posts.addAll(posts);
        // High over the safe spot, and well clear of whatever stands there, so nobody jumps out into a tower or a hill.
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ());
        dropship.hover = new Vec3(center.getX() + 0.5, Math.max(center.getY() + ALTITUDE, top + CLEARANCE), center.getZ() + 0.5);
        Vec3 start = dropship.hover.add(Math.cos(angle) * approachDistance, 0.0, Math.sin(angle) * approachDistance);
        dropship.moveTo(start.x, start.y, start.z, dropship.yawTowards(start, dropship.hover), 0.0F);
        level.addFreshEntity(dropship);
        return dropship;
    }

    private float yawTowards(Vec3 from, Vec3 to) {
        return (float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (random.nextInt(2) == 0) {
                level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.5, getZ(), 0.0, 0.04, 0.0);
            }
            return;
        }
        if (++age > LIFETIME_TICKS) {
            discard();
            return;
        }
        if (age % ENGINE_SOUND_TICKS == 1) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.HOSTILE, 8.0F, 0.5F);
        }
        if (leavingTicks >= 0) {
            leave();
        } else if (position().distanceToSqr(hover) > ARRIVED_SQR) {
            Vec3 step = hover.subtract(position()).normalize().scale(SPEED);
            setPos(getX() + step.x, getY() + step.y, getZ() + step.z);
        } else if (age % DROP_TICKS == 0) {
            dropOne();
        }
    }

    private void dropOne() {
        if (posts.isEmpty()) {
            leavingTicks = 0;
            return;
        }
        BlockPos post = posts.remove(posts.size() - 1);
        Defender trooper = ModEntities.DEFENDER.get().create(level());
        trooper.moveTo(getX(), getY() - 2.0, getZ(), random.nextFloat() * 360.0F, 0.0F);
        trooper.assign(spotId, post);
        trooper.setParachuting(true);
        level().addFreshEntity(trooper);
    }

    private void leave() {
        if (++leavingTicks > LEAVE_TICKS) {
            discard();
            return;
        }
        double yaw = (getYRot() + 90.0F) * Mth.DEG_TO_RAD;
        setPos(getX() + Math.cos(yaw) * LEAVE_SPEED, getY() + 0.25, getZ() + Math.sin(yaw) * LEAVE_SPEED);
    }

    public boolean isDone() {
        return isRemoved() || leavingTicks >= 0;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSqr) {
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
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
