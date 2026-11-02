package wallis_ultimate.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import wallis_ultimate.survival.Downed;

/**
 * Client-side copy of the downed state (SURV-001/002): the local player's own timer and pistol ammo, the
 * downed teammates with their timers, and the local player's revive progress. Timers count down locally
 * between server updates.
 */
public final class ClientDownedState {
    /** Teammate entries without an update for this long are dropped; the server sends one every second. */
    private static final int STALE_TICKS = 60;

    private static int ticksLeft;
    private static int ammo;
    private static int reviveProgress;
    private static final Map<UUID, Teammate> TEAMMATES = new LinkedHashMap<>();

    public static final class Teammate {
        private final String name;
        private int ticksLeft;
        private int ticksSinceUpdate;

        private Teammate(String name, int ticksLeft) {
            this.name = name;
            this.ticksLeft = ticksLeft;
        }

        public String name() {
            return name;
        }

        public int ticksLeft() {
            return ticksLeft;
        }
    }

    private ClientDownedState() {
    }

    public static void update(int newTicksLeft, int newAmmo) {
        ticksLeft = newTicksLeft;
        ammo = newAmmo;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Downed.applySyncedState(player, ticksLeft, ammo);
        }
    }

    public static void updateTeammate(UUID playerId, String name, int teammateTicksLeft) {
        if (teammateTicksLeft > 0) {
            TEAMMATES.put(playerId, new Teammate(name, teammateTicksLeft));
        } else {
            TEAMMATES.remove(playerId);
        }
    }

    public static void setReviveProgress(int progress) {
        reviveProgress = progress;
    }

    /** Once per client tick. The local timers stop at 1 tick; only the server ends the downed state. */
    public static void tick(LocalPlayer player) {
        if (isDowned()) {
            if (ticksLeft > 1) {
                ticksLeft--;
            }
            // Re-applied every tick: the LocalPlayer object is replaced on respawn and dimension change.
            Downed.applySyncedState(player, ticksLeft, ammo);
        }
        Iterator<Teammate> it = TEAMMATES.values().iterator();
        while (it.hasNext()) {
            Teammate teammate = it.next();
            if (teammate.ticksLeft > 1) {
                teammate.ticksLeft--;
            }
            if (++teammate.ticksSinceUpdate > STALE_TICKS) {
                it.remove();
            }
        }
    }

    public static void reset() {
        ticksLeft = 0;
        ammo = 0;
        reviveProgress = 0;
        TEAMMATES.clear();
    }

    public static boolean isDowned() {
        return ticksLeft > 0;
    }

    public static int ticksLeft() {
        return ticksLeft;
    }

    public static int ammo() {
        return ammo;
    }

    public static int reviveProgress() {
        return reviveProgress;
    }

    public static boolean isTeammateDowned(UUID playerId) {
        return TEAMMATES.containsKey(playerId);
    }

    public static Collection<Teammate> teammates() {
        return Collections.unmodifiableCollection(TEAMMATES.values());
    }
}
