package wallis_ultimate.survival;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * SURV-006, ENEMY-007: what a safe spot's area does by itself. No enemy spawns inside it, so a fight there always comes
 * from outside (Q-156). A team that comes here for the first time has found the spot, and can travel to it from then on
 * (WORLD-004).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SafeZones {
    private static final int DISCOVERY_CHECK_TICKS = 20;
    /** GameTests only: real safe spots are too far away to test on, so tests put a zone where they are. */
    private static final List<SafeSpot> TEST_ZONES = new ArrayList<>();

    private SafeZones() {
    }

    /** The safe spot at this place, or null. Safe spots only exist in the Overworld for now. */
    @Nullable
    public static SafeSpot spotAt(Level level, double x, double z) {
        if (!(level instanceof ServerLevel serverLevel) || level.dimension() != Level.OVERWORLD) {
            return null;
        }
        for (SafeSpot spot : TEST_ZONES) {
            if (spot.contains(x, z)) {
                return spot;
            }
        }
        for (SafeSpot spot : DistrictSites.get(serverLevel).spots()) {
            if (spot.contains(x, z)) {
                return spot;
            }
        }
        return null;
    }

    @Nullable
    public static SafeSpot spotAt(Entity entity) {
        return spotAt(entity.level(), entity.getX(), entity.getZ());
    }

    /** GameTests only. */
    public static void addTestZone(SafeSpot zone) {
        TEST_ZONES.add(zone);
    }

    /** GameTests only. */
    public static void removeTestZone(SafeSpot zone) {
        TEST_ZONES.remove(zone);
    }

    @SubscribeEvent
    public static void onSpawnCheck(MobSpawnEvent.PositionCheck event) {
        if (event.getEntity() instanceof Enemy && spotAt(event.getEntity()) != null) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player && player.tickCount % DISCOVERY_CHECK_TICKS == 0) {
            SafeSpot spot = spotAt(player);
            if (spot != null) {
                discover(player, spot);
            }
        }
    }

    /** The whole team gets told when one of them finds a new safe spot. */
    private static void discover(ServerPlayer player, SafeSpot spot) {
        DistrictSites sites = DistrictSites.get(player.server.overworld());
        if (sites.discover(Teams.teamKey(player), spot.id())) {
            Component message = Component.translatable("message." + WallisUltimate.MOD_ID + ".safe_spot.found", Component.translatable(spot.nameKey()))
                    .withStyle(ChatFormatting.GREEN);
            player.sendSystemMessage(message);
            Teams.onlineTeammates(player).forEach(teammate -> teammate.sendSystemMessage(message));
        }
    }
}
