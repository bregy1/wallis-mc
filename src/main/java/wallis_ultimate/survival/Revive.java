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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;

/**
 * SURV-002: a teammate revives a downed player by holding the use key on them for 3.5 seconds while standing
 * still. Taking damage cancels the revive and loses its progress. While the key is held, the client reports
 * {@link #hold} every tick and {@link #cancel} on release; progress runs on the server tick.
 * Server-side only: the session map must never be touched from the client thread.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Revive {
    public static final int REVIVE_TICKS = 70;
    public static final double REVIVE_RANGE = 3.0;
    /** The revive stops if the client's "still holding" reports stop for this many ticks. */
    public static final long RELEASE_TICKS = 10;
    private static final UUID FREEZE_ID = UUID.fromString("6c1f8a52-3f0e-4c2b-9a7e-2d4b1f0c9e11");

    private static final Map<Player, Session> SESSIONS = new WeakHashMap<>();

    private static final class Session {
        final Player target;
        int progress;
        long lastHoldTick;
        long lastProgressTick = -1;

        Session(Player target) {
            this.target = target;
        }
    }

    private Revive() {
    }

    public static boolean canRevive(Player reviver, Player target) {
        return !Downed.isDowned(reviver) && Downed.isDowned(target) && Teams.sameTeam(reviver, target)
                && target.isAlive() && reviver.level() == target.level() && reviver.distanceTo(target) <= REVIVE_RANGE;
    }

    /** The reviver holds the use key on the target. Starts a revive or keeps the current one going. */
    public static void hold(Player reviver, Player target) {
        Session session = SESSIONS.get(reviver);
        if (session != null && session.target == target) {
            session.lastHoldTick = reviver.level().getGameTime();
            return;
        }
        cancel(reviver);
        if (canRevive(reviver, target)) {
            session = new Session(target);
            session.lastHoldTick = reviver.level().getGameTime();
            SESSIONS.put(reviver, session);
            freeze(reviver, true);
        }
    }

    /** One server tick of the reviver's revive, if any: at most one step of progress per game tick. */
    public static void tickSession(Player reviver) {
        Session session = SESSIONS.get(reviver);
        if (session == null) {
            return;
        }
        long now = reviver.level().getGameTime();
        if (now - session.lastHoldTick > RELEASE_TICKS || !canRevive(reviver, session.target)) {
            cancel(reviver);
            return;
        }
        if (session.lastProgressTick == now) {
            return;
        }
        session.lastProgressTick = now;
        session.progress++;
        if (session.progress >= REVIVE_TICKS) {
            cancel(reviver);
            Downed.revive(session.target);
        } else {
            DownedSync.sendReviveProgress(reviver, session.progress);
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
            DownedSync.sendReviveProgress(reviver, 0);
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

    private static boolean isServerReviver(Player player) {
        return !player.level().isClientSide() && isReviving(player);
    }

    private static void cancelIfReviving(Player player, Event event) {
        if (isServerReviver(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            tickSession(event.player);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            cancel(player);
        }
    }

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        cancel(event.getEntity());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        cancelIfReviving(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        cancelIfReviving(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        cancelIfReviving(event.getEntity(), event);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        cancelIfReviving(event.getEntity(), event);
    }
}
