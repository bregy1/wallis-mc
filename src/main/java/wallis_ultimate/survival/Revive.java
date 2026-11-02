package wallis_ultimate.survival;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * SURV-002: a teammate revives a downed player by holding the use key on them for 3.5 seconds while standing
 * still. Taking damage cancels the revive and loses its progress. The client reports "still holding" once per
 * tick ({@link #hold}); progress advances at most once per game tick.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Revive {
    public static final int REVIVE_TICKS = 70;
    public static final double REVIVE_RANGE = 3.0;
    /** Released if no "holding" report arrived for this many ticks. */
    private static final long RELEASE_TICKS = 3;
    private static final UUID FREEZE_ID = UUID.fromString("6c1f8a52-3f0e-4c2b-9a7e-2d4b1f0c9e11");

    private static final Map<Player, Session> SESSIONS = new WeakHashMap<>();

    private static final class Session {
        final Player target;
        int progress;
        long lastHoldTick;

        Session(Player target) {
            this.target = target;
        }
    }

    private Revive() {
    }

    /** Only team members can revive each other. Uses vanilla scoreboard teams until FTB Teams is in the dev setup. */
    public static boolean sameTeam(Player a, Player b) {
        return a != b && a.isAlliedTo(b);
    }

    /** Called once per tick while the reviver holds the use key on the target. */
    public static void hold(Player reviver, Player target) {
        if (Downed.isDowned(reviver) || !Downed.isDowned(target) || !sameTeam(reviver, target)
                || reviver.distanceTo(target) > REVIVE_RANGE) {
            cancel(reviver);
            return;
        }
        long now = reviver.level().getGameTime();
        Session session = SESSIONS.get(reviver);
        if (session == null || session.target != target) {
            cancel(reviver);
            session = new Session(target);
            session.lastHoldTick = now - 1;
            SESSIONS.put(reviver, session);
            freeze(reviver, true);
        }
        if (session.lastHoldTick == now) {
            return;
        }
        session.lastHoldTick = now;
        session.progress++;
        if (session.progress >= REVIVE_TICKS) {
            cancel(reviver);
            Downed.revive(target);
        }
    }

    public static int progress(Player reviver) {
        Session session = SESSIONS.get(reviver);
        return session == null ? 0 : session.progress;
    }

    public static boolean isReviving(Player player) {
        return SESSIONS.containsKey(player);
    }

    public static void cancel(Player reviver) {
        if (SESSIONS.remove(reviver) != null) {
            freeze(reviver, false);
        }
    }

    /** While reviving, the reviver can't move. */
    private static void freeze(Player reviver, boolean frozen) {
        AttributeInstance speed = reviver.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        speed.removeModifier(FREEZE_ID);
        if (frozen) {
            speed.addTransientModifier(new AttributeModifier(FREEZE_ID, "wallis_ultimate:reviving", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Session session = SESSIONS.get(event.player);
        if (session != null && event.player.level().getGameTime() - session.lastHoldTick > RELEASE_TICKS) {
            cancel(event.player);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            cancel(player);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (isReviving(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isReviving(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
