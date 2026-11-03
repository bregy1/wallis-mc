package wallis_ultimate.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.combat.Noise;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.survival.Garrisons;

/**
 * SURV-011: an armed NPC holding a safe spot. Shoots any enemy that comes near, hits what gets too close, and patches
 * up what got broken while it's quiet. Ordinary ones can die and get replaced; main NPCs (the named ones) can't. It
 * turns on players who mine in its safe spot or hit it, and leaves everyone else alone.
 */
public class Defender extends PathfinderMob {
    public static final float SHOT_DAMAGE = 4.0F;
    public static final double SHOT_RANGE = 20.0;
    /** ENEMY-006: gunshots carry about this far. So fights at a safe spot pull in more zombies. */
    public static final double SHOT_NOISE = 32.0;
    private static final float SHOT_HIT_CHANCE = 0.85F;
    private static final double TRACER_STEP = 1.5;
    public static final double WALK_BLOCKS_PER_TICK = 1.1 * RegularZombie.PLAYER_WALK_BLOCKS_PER_TICK;
    /** How far from its post it still picks fights. */
    private static final int GUARD_RADIUS = 32;
    private static final int LOOKS = 4;
    /** Falling under a parachute, in blocks per tick; sideways, it steers to its post. */
    private static final double PARACHUTE_FALL = 0.18;
    private static final double PARACHUTE_STEER = 0.12;
    private static final EntityDataAccessor<Boolean> PARACHUTING = SynchedEntityData.defineId(Defender.class, EntityDataSerializers.BOOLEAN);

    private final NpcUnstuck unstuck = new NpcUnstuck();
    private String spotId = "";
    @Nullable
    private BlockPos post;
    private boolean mainNpc;

    public Defender(EntityType<? extends Defender> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        setCanPickUpLoot(false);
        getNavigation().setCanFloat(true);
        if (!level.isClientSide()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.DEFENDER_RIFLE.get()));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, RegularZombie.speedAttributeFor(WALK_BLOCKS_PER_TICK));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PARACHUTING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new DefenderAttackGoal(this));
        goalSelector.addGoal(3, new DefenderRepairGoal(this));
        goalSelector.addGoal(4, new DefenderPostGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, Defender.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                player -> Garrisons.isWantedBy(this, (Player) player)));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false, mob -> mob instanceof Enemy));
    }

    /** Which of the looks this one has. From its UUID, which the client knows and never changes. */
    public int look() {
        return Math.floorMod(getUUID().hashCode(), LOOKS);
    }

    public String spotId() {
        return spotId;
    }

    @Nullable
    public BlockPos post() {
        return post;
    }

    /** Assigns it to a safe spot and a place to stand. */
    public void assign(String spotId, BlockPos post) {
        this.spotId = spotId;
        this.post = post;
        restrictTo(post, GUARD_RADIUS);
    }

    public boolean isMainNpc() {
        return mainNpc;
    }

    /** Main NPCs, the named ones who give quests, can't die. They don't count toward a safe spot's guards. */
    public void setMainNpc(boolean mainNpc) {
        this.mainNpc = mainNpc;
    }

    public boolean isParachuting() {
        return entityData.get(PARACHUTING);
    }

    public void setParachuting(boolean parachuting) {
        entityData.set(PARACHUTING, parachuting);
    }

    /** One rifle shot at the target, right away: a straight line, stopped by blocks. Returns whether it hit. */
    public boolean shoot(LivingEntity target) {
        if (!(level() instanceof ServerLevel server)) {
            return false;
        }
        swing(getUsedItemHand());
        Vec3 from = getEyePosition();
        Vec3 aim = target.getEyePosition().subtract(0.0, 0.3, 0.0);
        BlockHitResult wall = server.clip(new ClipContext(from, aim, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = wall.getType() == HitResult.Type.MISS ? aim : wall.getLocation();
        server.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7F, 1.9F);
        Noise.make(server, position(), SHOT_NOISE);
        server.sendParticles(ParticleTypes.SMOKE, from.x, from.y, from.z, 3, 0.05, 0.05, 0.05, 0.01);
        Vec3 direction = end.subtract(from);
        double length = direction.length();
        for (double along = TRACER_STEP; along < length; along += TRACER_STEP) {
            Vec3 at = from.add(direction.scale(along / length));
            server.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (wall.getType() != HitResult.Type.MISS || getRandom().nextFloat() >= SHOT_HIT_CHANCE) {
            return false;
        }
        // Bullets don't care about the moment of protection after a hit, so several guards firing at once all count.
        // Except on players: being shot at by a whole safe spot should hurt, not be over in a second.
        if (!(target instanceof Player)) {
            target.invulnerableTime = 0;
        }
        return target.hurt(damageSources().mobAttack(this), SHOT_DAMAGE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (mainNpc) {
            return false;
        }
        if (!level().isClientSide() && source.getEntity() instanceof Player player) {
            Garrisons.anger(this, player);
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (isParachuting()) {
            steerParachute();
        } else {
            unstuck.tick(this);
        }
    }

    private void steerParachute() {
        fallDistance = 0.0F;
        if (onGround() || isInWater()) {
            setParachuting(false);
            return;
        }
        Vec3 motion = getDeltaMovement();
        double steerX = 0.0;
        double steerZ = 0.0;
        if (post != null) {
            Vec3 toPost = Vec3.atBottomCenterOf(post).subtract(position()).multiply(1.0, 0.0, 1.0);
            if (toPost.lengthSqr() > 1.0) {
                toPost = toPost.normalize().scale(PARACHUTE_STEER);
                steerX = toPost.x;
                steerZ = toPost.z;
            }
        }
        setDeltaMovement(steerX, Math.max(motion.y, -PARACHUTE_FALL), steerZ);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public int getExperienceReward() {
        return 0;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SpotId", spotId);
        tag.putBoolean("MainNpc", mainNpc);
        if (post != null) {
            tag.put("Post", NbtUtils.writeBlockPos(post));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        spotId = tag.getString("SpotId");
        mainNpc = tag.getBoolean("MainNpc");
        if (tag.contains("Post")) {
            assign(spotId, NbtUtils.readBlockPos(tag.getCompound("Post")));
        }
    }
}
