package wallis_ultimate.combat;

import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModEffects;
import wallis_ultimate.registry.ModItems;

/** GEAR-007: what the Chapter 1 melee weapons do besides plain damage. Starting values, tuned with balancing. */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WeaponEffects {
    public static final int BLEEDING_TICKS = 80;
    public static final double SHOVEL_KNOCKBACK = 1.0;
    public static final int SHOVEL_STUN_TICKS = 20;
    /** How far behind the target the Pitchfork's tines reach, and how wide. */
    public static final double PITCHFORK_BEHIND = 1.5;
    public static final double PITCHFORK_WIDTH = 1.0;
    public static final double PITCHFORK_DAMAGE = 4.5;
    /** The Frying Pan blocks one hit, then has to wait. */
    public static final int FRYING_PAN_COOLDOWN_TICKS = 100;
    public static final double FRYING_PAN_NOISE_RADIUS = 12.0;

    private WeaponEffects() {
    }

    /** Nail Bat: the target bleeds. Hitting again only restarts the bleeding. */
    public static void causeBleeding(LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(ModEffects.BLEEDING.get(), BLEEDING_TICKS, 0), attacker);
    }

    /** Heavy Shovel: knocks back and stuns for a moment. */
    public static void heavySwing(LivingEntity target, LivingEntity attacker) {
        target.knockback(SHOVEL_KNOCKBACK, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SHOVEL_STUN_TICKS, 3), attacker);
    }

    /** Pitchfork: also stabs the closest enemy right behind the target. */
    public static void stabBehind(LivingEntity target, LivingEntity attacker) {
        Vec3 direction = target.position().subtract(attacker.position()).normalize();
        Vec3 behind = target.position().add(direction.scale(PITCHFORK_BEHIND));
        List<LivingEntity> enemies = target.level().getEntitiesOfClass(LivingEntity.class, new AABB(behind, behind).inflate(PITCHFORK_WIDTH),
                entity -> entity != target && entity != attacker && entity instanceof Enemy && entity.isAlive());
        enemies.stream().min((a, b) -> Double.compare(a.distanceToSqr(behind), b.distanceToSqr(behind))).ifPresent(second -> {
            DamageSource source = attacker instanceof Player player ? attacker.damageSources().playerAttack(player)
                    : attacker.damageSources().mobAttack(attacker);
            second.hurt(source, (float) PITCHFORK_DAMAGE);
        });
    }

    /** Frying Pan: in the offhand it blocks one hit from the front, loudly. */
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        ItemStack pan = player.getOffhandItem();
        DamageSource source = event.getSource();
        if (!pan.is(ModItems.FRYING_PAN.get()) || player.getCooldowns().isOnCooldown(pan.getItem())
                || source.is(DamageTypeTags.BYPASSES_SHIELD) || !comesFromTheFront(player, source)) {
            return;
        }
        event.setCanceled(true);
        pan.hurtAndBreak(1, player, entity -> entity.broadcastBreakEvent(EquipmentSlot.OFFHAND));
        player.getCooldowns().addCooldown(pan.getItem(), FRYING_PAN_COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.6F);
        Noise.make(player.level(), player.position(), FRYING_PAN_NOISE_RADIUS);
    }

    /** The same check a shield uses. */
    private static boolean comesFromTheFront(Player player, DamageSource source) {
        Vec3 from = source.getSourcePosition();
        if (from == null) {
            return false;
        }
        Vec3 toPlayer = from.vectorTo(player.position()).normalize();
        return new Vec3(toPlayer.x, 0.0, toPlayer.z).dot(player.getViewVector(1.0F)) < 0.0;
    }
}
