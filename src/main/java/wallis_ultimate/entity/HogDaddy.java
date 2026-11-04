package wallis_ultimate.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import wallis_ultimate.boss.HogDaddyFight;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.survival.Downed;

/**
 * ENEMY-011: the Chapter 1 boss, a pig farmer who mutated into a hulking hog-man. His attacks are all announced by red
 * marks on the ground ({@link GroundMark}). He can't be hurt during the chase; in the pen hits barely hurt him until he
 * slams into a fence post or the feeding trough, which stuns him with his back exposed. Below half health he's enraged:
 * belly flops send mud rings across the pen, and each post only holds once. Fire is his other weakness. Runs on a small
 * state machine instead of goals, because he only ever does one thing at a time and every step is timed.
 * {@link HogDaddyFight} runs the fight around him. All numbers are proposals (30-enemies.md).
 */
public class HogDaddy extends Monster {
    public enum Phase { CHASE, PEN, ENRAGED }

    public enum Action { WAIT, STALK, WIND_UP, CHARGE, STUNNED, RECOVER, LEAP, FLOP_WIND_UP, FLOP_AIR }

    /** ENEMY-011: "Scales by: health". Per player in the arena, which gives about the same number of stuns at any team size. */
    public static final float HEALTH_PER_PLAYER = 400.0F;
    /** ENEMY-011, the team size the mechanics are designed for. */
    public static final int FULL_TEAM = 3;
    /** Spec: a red line telegraph, 1 second. */
    public static final int TELEGRAPH_TICKS = 20;
    /** Spec: stunned for 4 seconds. Fewer players get a longer window: half a second per missing player. */
    public static final int STUN_TICKS = 80;
    public static final int STUN_EXTRA_PER_MISSING_PLAYER = 10;
    public static final float CHARGE_DAMAGE = 8.0F;
    public static final float HOOK_DAMAGE = 5.0F;
    public static final float FLOP_DAMAGE = 6.0F;
    /** Normal hits barely hurt him; stunned, hits really count, double on his exposed back. */
    public static final float ARMORED_MULTIPLIER = 0.25F;
    public static final float BURNING_MULTIPLIER = 0.5F;
    public static final float BACK_MULTIPLIER = 2.0F;
    /** Spec: "fire burns his bristles". */
    public static final float FIRE_MULTIPLIER = 3.0F;
    public static final float CHARGE_RANGE = 24.0F;
    public static final float CHARGE_WIDTH = 2.5F;
    public static final float FLOP_RADIUS = 3.0F;
    /** Blocks per tick; a sprinting player does 0.28. */
    private static final double CHASE_SPEED = 0.26;
    private static final double PEN_SPEED = 0.19;
    private static final double ENRAGED_SPEED = RegularZombie.PLAYER_WALK_BLOCKS_PER_TICK;
    private static final double CHARGE_SPEED = 0.6;
    private static final double ENRAGED_CHARGE_SPEED = 0.7;
    private static final int CHASE_STALK_TICKS = 40;
    private static final int PEN_STALK_TICKS = 50;
    private static final int ENRAGED_STALK_TICKS = 30;
    private static final int RECOVER_TICKS = 25;
    private static final int LEAP_TICKS = 30;
    private static final double LEAP_HEIGHT = 12.0;
    private static final int FLOP_AIR_TICKS = 14;
    private static final double FLOP_HEIGHT = 5.0;
    /** Two rings per flop, a second apart. */
    private static final int SECOND_RING_DELAY = 20;
    private static final int FLOP_RECOVER_TICKS = 40;
    private static final float FLOP_REACH = 14.0F;
    private static final double MELEE_REACH = 3.2;
    private static final int MELEE_COOLDOWN_TICKS = 30;
    private static final double CHARGE_MIN_DISTANCE = 4.0;
    private static final int REPATH_TICKS = 10;
    /** A charge steps over anything lower than this. */
    private static final double STEP = 0.6;
    private static final double HIT_PUSH = 1.1;
    private static final double HIT_PUSH_UP = 0.45;

    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(HogDaddy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION = SynchedEntityData.defineId(HogDaddy.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    /** GameTests only: mock players aren't in the level, so they're handed over like this. */
    private final List<Player> testPlayers = new ArrayList<>();
    private final Set<UUID> chargeHits = new HashSet<>();
    @Nullable
    private HogDaddyFight fight;
    @Nullable
    private Player lastPlayerHit;
    private int playerCount = 1;
    private int actionTicks;
    private int meleeCooldown;
    private int stunTicksLeft;
    private boolean flopNext;
    private boolean flopRecovering;
    private Vec3 chargeDirection = Vec3.ZERO;
    private float chargeLength;
    private double charged;
    private Vec3 airFrom = Vec3.ZERO;
    private Vec3 airTo = Vec3.ZERO;

    public HogDaddy(EntityType<? extends HogDaddy> type, Level level) {
        super(type, level);
        setMaxUpStep(1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH_PER_PLAYER).add(Attributes.ATTACK_DAMAGE, HOOK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, RegularZombie.speedAttributeFor(PEN_SPEED)).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    public void joinFight(HogDaddyFight fight) {
        this.fight = fight;
    }

    public Phase phase() {
        return Phase.values()[entityData.get(DATA_PHASE)];
    }

    public void setPhase(Phase phase) {
        entityData.set(DATA_PHASE, phase.ordinal());
        double speed = switch (phase) {
            case CHASE -> CHASE_SPEED;
            case PEN -> PEN_SPEED;
            case ENRAGED -> ENRAGED_SPEED;
        };
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(RegularZombie.speedAttributeFor(speed));
    }

    public Action action() {
        return Action.values()[entityData.get(DATA_ACTION)];
    }

    private void setAction(Action action) {
        entityData.set(DATA_ACTION, action.ordinal());
        actionTicks = 0;
    }

    /**
     * PROG-007: everyone inside the arena counts. Minecraft caps the max health attribute at 1024, which three players
     * would already pass, so his health stays the same and hits do proportionally less. It comes to the same thing, keeps
     * his share of health when the count changes, and works for any team size.
     */
    public void setPlayerCount(int players) {
        playerCount = Math.max(players, 1);
    }

    public int playerCount() {
        return playerCount;
    }

    /** ENEMY-011: longer timing windows for fewer players. */
    public int stunTicks() {
        return STUN_TICKS + STUN_EXTRA_PER_MISSING_PLAYER * Math.max(FULL_TEAM - playerCount, 0);
    }

    /** GameTests only. */
    public void addTestPlayer(Player player) {
        testPlayers.add(player);
    }

    /** Everyone he's fighting who is still standing; downed players are ignored (SURV-001). */
    public List<Player> fightPlayers() {
        List<Player> players = fight != null ? new ArrayList<>(fight.playersInside()) : new ArrayList<>();
        players.addAll(testPlayers);
        players.removeIf(player -> !player.isAlive() || Downed.isDowned(player));
        return players;
    }

    @Nullable
    private Player nearestPlayer() {
        Player nearest = null;
        double best = Double.MAX_VALUE;
        for (Player player : fightPlayers()) {
            double distance = distanceToSqr(player);
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    /** He stands behind the wall, snorting, until the fight lets him go. */
    public void waitBehindWall() {
        setAction(Action.WAIT);
    }

    /** Phase 1 begins: straight through the wall. */
    public void burstThrough(BlockPos from, BlockPos to) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level().destroyBlock(pos, false);
        }
        playSound(SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 2.0F, 0.7F);
        roar();
        setAction(Action.STALK);
    }

    /** The chase is over: over the barn and into the pen. Announced by a circle where he'll come down. */
    public void leapInto(Vec3 landing) {
        setPhase(Phase.PEN);
        startAir(Action.LEAP, landing);
        GroundMark.disc(level(), landing, FLOP_RADIUS, LEAP_TICKS, LEAP_TICKS);
        roar();
    }

    private void roar() {
        playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.75F);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());
        actionTicks++;
        if (meleeCooldown > 0) {
            meleeCooldown--;
        }
        switch (action()) {
            case WAIT -> getNavigation().stop();
            case STALK -> stalk();
            case WIND_UP -> {
                if (actionTicks >= TELEGRAPH_TICKS) {
                    setAction(Action.CHARGE);
                    chargeHits.clear();
                    charged = 0.0;
                    playSound(SoundEvents.ZOGLIN_ATTACK, 2.0F, 0.6F);
                }
            }
            case CHARGE -> charge();
            case STUNNED -> {
                if (--stunTicksLeft <= 0) {
                    setAction(Action.STALK);
                }
            }
            case RECOVER -> recover();
            case FLOP_WIND_UP -> {
                if (actionTicks >= TELEGRAPH_TICKS) {
                    startAir(Action.FLOP_AIR, airTo);
                }
            }
            case LEAP -> fly(LEAP_TICKS, LEAP_HEIGHT);
            case FLOP_AIR -> fly(FLOP_AIR_TICKS, FLOP_HEIGHT);
        }
    }

    private void stalk() {
        Player target = nearestPlayer();
        if (getTarget() != target) {
            setTarget(target);
        }
        if (target == null) {
            getNavigation().stop();
            return;
        }
        if (actionTicks % REPATH_TICKS == 1) {
            getNavigation().moveTo(target, 1.0);
        }
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distance = distanceTo(target);
        if (distance <= MELEE_REACH && meleeCooldown <= 0) {
            swing(InteractionHand.MAIN_HAND);
            doHurtTarget(target);
            meleeCooldown = MELEE_COOLDOWN_TICKS;
        }
        // Someone hugging him gets the next attack too, a bit later.
        int wait = stalkTicks();
        if (actionTicks >= wait && hasLineOfSight(target) && (distance >= CHARGE_MIN_DISTANCE || actionTicks >= 2 * wait)) {
            if (phase() == Phase.ENRAGED && flopNext) {
                startFlop(target);
            } else {
                startWindUp(target);
            }
            flopNext = !flopNext;
        }
    }

    private int stalkTicks() {
        return switch (phase()) {
            case CHASE -> CHASE_STALK_TICKS;
            case PEN -> PEN_STALK_TICKS;
            case ENRAGED -> ENRAGED_STALK_TICKS;
        };
    }

    /** Locks in the line. He doesn't turn after this, which is what makes a charge dodgeable. */
    public void startWindUp(Player target) {
        Vec3 toTarget = target.position().subtract(position()).multiply(1.0, 0.0, 1.0);
        chargeDirection = toTarget.lengthSqr() < 1.0E-4 ? Vec3.directionFromRotation(0.0F, getYRot()) : toTarget.normalize();
        chargeLength = lengthOfCharge(chargeDirection);
        float yaw = (float) (Mth.atan2(chargeDirection.z, chargeDirection.x) * Mth.RAD_TO_DEG) - 90.0F;
        face(yaw);
        getNavigation().stop();
        setAction(Action.WIND_UP);
        int chargeTicks = Mth.ceil(chargeLength / chargeSpeed());
        GroundMark.line(level(), position(), yaw, chargeLength, CHARGE_WIDTH, TELEGRAPH_TICKS, TELEGRAPH_TICKS + chargeTicks);
        playSound(SoundEvents.ZOGLIN_ANGRY, 2.0F, 0.6F);
    }

    private double chargeSpeed() {
        return phase() == Phase.ENRAGED ? ENRAGED_CHARGE_SPEED : CHARGE_SPEED;
    }

    private void face(float yaw) {
        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
        yRotO = yaw;
    }

    /** How far he gets before something stops him: the first wall or stun object on the line, or his range. */
    private float lengthOfCharge(Vec3 direction) {
        AABB body = getBoundingBox();
        for (float distance = 0.5F; distance <= CHARGE_RANGE; distance += 0.5F) {
            AABB moved = body.move(direction.x * distance, 0.0, direction.z * distance);
            if (stunObjectIn(moved) != null || blockedAt(moved)) {
                return distance - 0.5F;
            }
        }
        return CHARGE_RANGE;
    }

    /** Whether something he can't step over or trample is in that space. */
    private boolean blockedAt(AABB box) {
        AABB aboveStep = new AABB(box.minX, box.minY + STEP, box.minZ, box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(aboveStep.minX), Mth.floor(aboveStep.minY), Mth.floor(aboveStep.minZ),
                Mth.floor(aboveStep.maxX), Mth.floor(aboveStep.maxY), Mth.floor(aboveStep.maxZ))) {
            BlockState state = level().getBlockState(pos);
            if (!state.is(Blocks.HAY_BLOCK) && !state.getCollisionShape(level(), pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** The first fence post or trough in that space, including what's at his feet. */
    @Nullable
    private BlockPos stunObjectIn(AABB box) {
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            if (isStunObject(level().getBlockState(pos))) {
                return pos.immutable();
            }
        }
        return null;
    }

    public static boolean isStunObject(BlockState state) {
        return state.is(ModBlocks.PEN_POST.get()) || state.is(ModBlocks.FEEDING_TROUGH.get());
    }

    private void charge() {
        double speed = chargeSpeed();
        face(getYRot());
        AABB ahead = getBoundingBox().move(chargeDirection.x * (speed + 0.2), 0.0, chargeDirection.z * (speed + 0.2)).inflate(0.05);
        trample(ahead);
        hitPlayersInTheWay();
        BlockPos stunObject = stunObjectIn(ahead);
        if (stunObject != null) {
            slam(stunObject);
            return;
        }
        if (charged >= chargeLength || (charged > 0.0 && horizontalCollision)) {
            // The end of the line, or something that doesn't stun him, like the ring.
            setDeltaMovement(Vec3.ZERO);
            setAction(Action.RECOVER);
            return;
        }
        setDeltaMovement(chargeDirection.x * speed, getDeltaMovement().y, chargeDirection.z * speed);
        charged += speed;
    }

    /** Corn and fallen hay don't stop him. Everything comes back with the farm after the fight. */
    private void trample(AABB box) {
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState state = level().getBlockState(pos);
            if (state.is(ModBlocks.CORN.get()) || state.is(Blocks.HAY_BLOCK)) {
                level().destroyBlock(pos, false);
            }
        }
    }

    private void hitPlayersInTheWay() {
        AABB reach = getBoundingBox().inflate(0.4);
        for (Player player : fightPlayers()) {
            if (!chargeHits.contains(player.getUUID()) && reach.intersects(player.getBoundingBox())
                    && player.hurt(damageSources().mobAttack(this), CHARGE_DAMAGE)) {
                chargeHits.add(player.getUUID());
                player.setDeltaMovement(chargeDirection.x * HIT_PUSH, HIT_PUSH_UP, chargeDirection.z * HIT_PUSH);
                player.hurtMarked = true;
            }
        }
    }

    /** Whether his latest charge caught this player. */
    public boolean chargeCaught(Player player) {
        return chargeHits.contains(player.getUUID());
    }

    /** Spec: a slam into a post or the trough stuns him. Enraged, the post doesn't survive it. */
    private void slam(BlockPos stunObject) {
        stun();
        if (phase() == Phase.ENRAGED) {
            breakStunObject(stunObject);
        }
    }

    public void stun() {
        setDeltaMovement(Vec3.ZERO);
        getNavigation().stop();
        stunTicksLeft = stunTicks();
        setAction(Action.STUNNED);
        playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 2.0F, 0.6F);
        playSound(SoundEvents.ZOGLIN_HURT, 2.0F, 0.5F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CRIT, getX(), getEyeY(), getZ(), 20, 0.6, 0.4, 0.6, 0.1);
        }
        if (fight != null) {
            fight.onStunned();
        }
    }

    /** A whole post, or the whole trough: everything of the same kind that's connected to the block he hit. */
    private void breakStunObject(BlockPos hit) {
        BlockState kind = level().getBlockState(hit);
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> open = new ArrayList<>(List.of(hit));
        while (!open.isEmpty()) {
            BlockPos pos = open.remove(open.size() - 1);
            if (seen.add(pos) && level().getBlockState(pos).is(kind.getBlock())) {
                for (Direction direction : Direction.values()) {
                    open.add(pos.relative(direction));
                }
                level().destroyBlock(pos, false);
            }
        }
    }

    private void recover() {
        getNavigation().stop();
        if (flopRecovering && actionTicks == SECOND_RING_DELAY) {
            MudRing.spawn(this, position());
            playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.6F);
        }
        if (actionTicks >= (flopRecovering ? FLOP_RECOVER_TICKS : RECOVER_TICKS)) {
            flopRecovering = false;
            setAction(Action.STALK);
        }
    }

    /** Spec, phase 3: the belly flop. Comes down where the target stood, or as near as he gets. */
    private void startFlop(Player target) {
        Vec3 toTarget = target.position().subtract(position()).multiply(1.0, 0.0, 1.0);
        double reach = Math.min(toTarget.length(), FLOP_REACH);
        Vec3 landing = toTarget.lengthSqr() < 1.0E-4 ? position() : position().add(toTarget.normalize().scale(reach));
        airTo = new Vec3(landing.x, target.getY(), landing.z);
        face((float) (Mth.atan2(toTarget.z, toTarget.x) * Mth.RAD_TO_DEG) - 90.0F);
        getNavigation().stop();
        setAction(Action.FLOP_WIND_UP);
        GroundMark.disc(level(), airTo, FLOP_RADIUS, TELEGRAPH_TICKS, TELEGRAPH_TICKS + FLOP_AIR_TICKS);
        playSound(SoundEvents.ZOGLIN_ANGRY, 2.0F, 0.6F);
    }

    private void startAir(Action action, Vec3 to) {
        airFrom = position();
        airTo = to;
        noPhysics = true;
        setNoGravity(true);
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        setAction(action);
    }

    private void fly(int ticks, double height) {
        float progress = Math.min((float) actionTicks / ticks, 1.0F);
        double x = Mth.lerp(progress, airFrom.x, airTo.x);
        double z = Mth.lerp(progress, airFrom.z, airTo.z);
        double y = Mth.lerp(progress, airFrom.y, airTo.y) + Mth.sin(progress * Mth.PI) * height;
        setPos(x, y, z);
        setDeltaMovement(Vec3.ZERO);
        if (actionTicks >= ticks) {
            land();
        }
    }

    private void land() {
        noPhysics = false;
        setNoGravity(false);
        setPos(airTo.x, airTo.y, airTo.z);
        playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 0.8F);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MUD.defaultBlockState()), getX(), getY() + 0.1, getZ(), 60, 1.5, 0.2, 1.5, 0.2);
        }
        if (action() == Action.FLOP_AIR) {
            for (Player player : fightPlayers()) {
                if (player.position().subtract(position()).horizontalDistance() <= FLOP_RADIUS) {
                    player.hurt(damageSources().mobAttack(this), FLOP_DAMAGE);
                }
            }
            MudRing.spawn(this, position());
            flopRecovering = true;
            setAction(Action.RECOVER);
        } else {
            setAction(Action.STALK);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        if (phase() == Phase.CHASE) {
            return false;
        }
        boolean byPlayer = source.getEntity() instanceof Player;
        if (byPlayer) {
            // A team's hits all count: vanilla would drop most of them as landing within one hit's moment of immunity.
            invulnerableTime = 0;
        }
        boolean hit = super.hurt(source, amount * damageMultiplier(source) / playerCount);
        if (hit && byPlayer) {
            invulnerableTime = 0;
            lastPlayerHit = (Player) source.getEntity();
        }
        if (hit && phase() == Phase.PEN && getHealth() <= getMaxHealth() / 2.0F) {
            enrage();
        }
        return hit;
    }

    public float damageMultiplier(DamageSource source) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return FIRE_MULTIPLIER;
        }
        if (action() == Action.STUNNED) {
            return isBehindHim(source.getSourcePosition()) ? BACK_MULTIPLIER : 1.0F;
        }
        return isOnFire() ? BURNING_MULTIPLIER : ARMORED_MULTIPLIER;
    }

    private boolean isBehindHim(@Nullable Vec3 from) {
        return from != null && Vec3.directionFromRotation(0.0F, getYRot()).dot(from.subtract(position()).multiply(1.0, 0.0, 1.0)) < 0.0;
    }

    /** Spec, phase 3: from half health on. */
    private void enrage() {
        setPhase(Phase.ENRAGED);
        roar();
        if (fight != null) {
            fight.onEnraged();
        }
    }

    /**
     * Whoever hurt him last gets the kill even if the fire finishes him, because FTB Quests' kill task only counts a kill
     * whose damage source is a player, and the team's last Chapter 1 quest is that task.
     */
    @Override
    public void die(DamageSource source) {
        super.die(killCredit(source));
        // Not if something called off the death.
        if (dead && fight != null) {
            fight.onBossDied();
        }
    }

    public DamageSource killCredit(DamageSource source) {
        return !(source.getEntity() instanceof Player) && lastPlayerHit != null ? damageSources().playerAttack(lastPlayerHit) : source;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            // Flies buzzing around him (Q-150).
            level().addParticle(new DustParticleOptions(new Vector3f(0.04F, 0.04F, 0.05F), 0.5F),
                    getRandomX(1.2), getRandomY(), getRandomZ(1.2), 0.0, 0.0, 0.0);
            if (action() == Action.STUNNED && tickCount % 3 == 0) {
                level().addParticle(ParticleTypes.CRIT, getX(), getEyeY() + 0.4, getZ(), random.nextGaussian() * 0.3, 0.2, random.nextGaussian() * 0.3);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    /** Peaceful would take him out of the fight, and the fight out of the story. */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_PHASE, Phase.PEN.ordinal());
        entityData.define(DATA_ACTION, Action.STALK.ordinal());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ZOGLIN_STEP, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    @Override
    public float getVoicePitch() {
        return 0.6F;
    }
}
