package wallis_ultimate.survival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * SURV-001: players go down instead of dying. Downed players crawl, defend themselves with a 10-shot pistol,
 * are ignored by enemies, and die for real after 90 seconds or when they give up. State lives server-side in
 * the player's persistent data; the client is told through {@link DownedSync}.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Downed {
    public static final int DOWNED_TICKS = 90 * 20;
    public static final int PISTOL_SHOTS = 10;
    /** "OK-ish for the early game": 2 hearts per shot. Trinkets and items may raise it later. */
    public static final float PISTOL_DAMAGE = 4.0F;
    public static final double PISTOL_RANGE = 24.0;
    public static final int HELP_SOUND_INTERVAL_TICKS = 100;
    private static final double ENEMY_FORGET_RADIUS = 48.0;

    private static final String TICKS_LEFT = WallisUltimate.MOD_ID + ":downed_ticks_left";
    private static final String AMMO = WallisUltimate.MOD_ID + ":downed_pistol_ammo";

    private Downed() {
    }

    public static boolean isDowned(Player player) {
        return player.getPersistentData().getInt(TICKS_LEFT) > 0;
    }

    public static int ticksLeft(Player player) {
        return player.getPersistentData().getInt(TICKS_LEFT);
    }

    public static int ammo(Player player) {
        return player.getPersistentData().getInt(AMMO);
    }

    /** Void, lava and /kill skip the downed state (SURV-001). */
    public static boolean killsInstantly(DamageSource source, LivingEntity entity) {
        return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || source.is(DamageTypes.LAVA) || entity.isInLava();
    }

    public static void goDown(Player player) {
        CompoundTag data = player.getPersistentData();
        data.putInt(TICKS_LEFT, DOWNED_TICKS);
        data.putInt(AMMO, PISTOL_SHOTS);
        player.setHealth(1.0F);
        player.clearFire();
        player.setForcedPose(Pose.SWIMMING);
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, DOWNED_TICKS, 0, false, false));
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(ENEMY_FORGET_RADIUS),
                mob -> mob.getTarget() == player)) {
            mob.setTarget(null);
        }
        DownedSync.sendState(player);
    }

    /** Ends the downed state and gets the player back up with half health (SURV-002). */
    public static void revive(Player player) {
        clear(player);
        player.setHealth(player.getMaxHealth() / 2.0F);
        DownedSync.sendState(player);
    }

    /** The give-up button: a downed player dies right away (SURV-001). */
    public static void giveUp(Player player) {
        if (isDowned(player)) {
            finalDeath(player);
        }
    }

    /** Gives up or runs out of time: the player dies for real. */
    public static void finalDeath(Player player) {
        clear(player);
        DownedSync.sendState(player);
        player.kill();
    }

    private static void clear(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(TICKS_LEFT);
        data.remove(AMMO);
        player.setForcedPose(null);
        player.removeEffect(MobEffects.GLOWING);
    }

    /** One server tick of the downed timer. */
    public static void tickDowned(Player player) {
        int left = ticksLeft(player) - 1;
        if (left <= 0) {
            finalDeath(player);
            return;
        }
        player.getPersistentData().putInt(TICKS_LEFT, left);
        player.setForcedPose(Pose.SWIMMING);
        if (left % HELP_SOUND_INTERVAL_TICKS == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        if (left % 20 == 0) {
            DownedSync.sendState(player);
        }
    }

    /** Fires the downed pistol along the player's view. Returns the entity hit, or null. Uses one shot either way. */
    public static LivingEntity firePistol(Player player) {
        if (!isDowned(player) || ammo(player) <= 0) {
            return null;
        }
        player.getPersistentData().putInt(AMMO, ammo(player) - 1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.6F);
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(PISTOL_RANGE));
        Vec3 blockHit = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, start, blockHit,
                new AABB(start, blockHit).inflate(1.0), entity -> entity instanceof LivingEntity && entity != player && !entity.isSpectator());
        DownedSync.sendState(player);
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            target.hurt(player.damageSources().playerAttack(player), PISTOL_DAMAGE);
            return target;
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()
                && !isDowned(player) && !killsInstantly(event.getSource(), player)) {
            event.setCanceled(true);
            goDown(player);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isDowned(player) && !killsInstantly(event.getSource(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getNewTarget() instanceof Player player && isDowned(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide() && isDowned(event.player)) {
            tickDowned(event.player);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isDowned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isDowned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (isDowned(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isDowned(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
