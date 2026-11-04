package wallis_ultimate.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.registry.ModEntities;

/**
 * ENEMY-011: a red mark on the ground that says where a big attack will land: a line for a charge, a circle for a
 * landing. It fills up while the attack is coming, so players can read the timing. Only a picture: it can't hurt anyone,
 * and it's an entity because that brings it to every client and removes it again with no extra packets.
 */
public class GroundMark extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_LINE = SynchedEntityData.defineId(GroundMark.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_LENGTH = SynchedEntityData.defineId(GroundMark.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_WIDTH = SynchedEntityData.defineId(GroundMark.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_FILL_TICKS = SynchedEntityData.defineId(GroundMark.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFETIME = SynchedEntityData.defineId(GroundMark.class, EntityDataSerializers.INT);
    /** Just above the ground, so it isn't lost in it. */
    private static final double ABOVE_GROUND = 0.03;
    /** How far down to look for the ground. */
    private static final int FLOOR_SEARCH = 12;

    public GroundMark(EntityType<? extends GroundMark> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    /** A line from {@code start} in the direction of {@code yaw}. */
    public static GroundMark line(Level level, Vec3 start, float yaw, float length, float width, int fillTicks, int lifetime) {
        GroundMark mark = create(level, start, length, width, fillTicks, lifetime);
        mark.entityData.set(DATA_LINE, true);
        mark.setYRot(yaw);
        level.addFreshEntity(mark);
        return mark;
    }

    public static GroundMark disc(Level level, Vec3 center, float radius, int fillTicks, int lifetime) {
        GroundMark mark = create(level, center, radius, 0.0F, fillTicks, lifetime);
        level.addFreshEntity(mark);
        return mark;
    }

    private static GroundMark create(Level level, Vec3 at, float length, float width, int fillTicks, int lifetime) {
        GroundMark mark = new GroundMark(ModEntities.GROUND_MARK.get(), level);
        mark.setPos(at.x, floorBelow(level, at) + ABOVE_GROUND, at.z);
        mark.entityData.set(DATA_LENGTH, length);
        mark.entityData.set(DATA_WIDTH, width);
        mark.entityData.set(DATA_FILL_TICKS, fillTicks);
        mark.entityData.set(DATA_LIFETIME, lifetime);
        return mark;
    }

    /**
     * The top of the block under this place. By block, not by collision: mud looks like a full block, so a mark laid on
     * its slightly lower collision box would be hidden inside it.
     */
    public static double floorBelow(Level level, Vec3 at) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(at.x), Mth.floor(at.y + 0.01), Mth.floor(at.z));
        for (int i = 0; i < FLOOR_SEARCH; i++) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return pos.getY() + 1.0;
            }
            pos.move(0, -1, 0);
        }
        return at.y;
    }

    public boolean isLine() {
        return entityData.get(DATA_LINE);
    }

    /** A line's length, or a circle's radius. */
    public float length() {
        return entityData.get(DATA_LENGTH);
    }

    public float width() {
        return entityData.get(DATA_WIDTH);
    }

    public int fillTicks() {
        return entityData.get(DATA_FILL_TICKS);
    }

    public int lifetime() {
        return entityData.get(DATA_LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount >= lifetime()) {
            discard();
        }
    }

    /** Players have to see it from far away, and it's far smaller than what it draws. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSquared) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_LINE, false);
        entityData.define(DATA_LENGTH, 1.0F);
        entityData.define(DATA_WIDTH, 1.0F);
        entityData.define(DATA_FILL_TICKS, 1);
        entityData.define(DATA_LIFETIME, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
