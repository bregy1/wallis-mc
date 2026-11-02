package wallis_ultimate.combat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.survival.Downed;

/** CMBT-002: forward dodge roll. Server-side; the client only sends a request (RollRequestPacket). */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DodgeRoll {
    /** Ground speed that carries the player about 3 blocks on flat ground (tune in playtesting). */
    public static final double ROLL_SPEED = 1.36;
    public static final long COOLDOWN_TICKS = 40;
    public static final long IMMUNITY_TICKS = 5;
    /** Same hunger cost as a sprint jump. */
    public static final float EXHAUSTION = 0.2F;

    private static final String LAST_ROLL = WallisUltimate.MOD_ID + ":last_roll";
    private static final String IMMUNE_UNTIL = WallisUltimate.MOD_ID + ":roll_immune_until";

    private DodgeRoll() {
    }

    /** Rolls the player forward if allowed. Returns whether the roll happened. */
    public static boolean tryRoll(Player player) {
        long now = player.level().getGameTime();
        CompoundTag data = player.getPersistentData();
        boolean onCooldown = data.contains(LAST_ROLL) && now - data.getLong(LAST_ROLL) < COOLDOWN_TICKS;
        if (!player.onGround() || onCooldown || Downed.isDowned(player)) {
            return false;
        }
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        player.setDeltaMovement(-Mth.sin(yaw) * ROLL_SPEED, player.getDeltaMovement().y, Mth.cos(yaw) * ROLL_SPEED);
        player.hurtMarked = true;
        player.causeFoodExhaustion(EXHAUSTION);
        data.putLong(LAST_ROLL, now);
        data.putLong(IMMUNE_UNTIL, now + IMMUNITY_TICKS);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }

    public static boolean isRollImmune(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.contains(IMMUNE_UNTIL) && player.level().getGameTime() <= data.getLong(IMMUNE_UNTIL);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && isRollImmune(player)
                && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }
}
