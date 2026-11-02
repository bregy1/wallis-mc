package wallis_ultimate.survival;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.TravelMenuPacket;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/** WORLD-004, TRANS-001: from a safe spot's signpost to any other one the team has found. Free, but not in a fight. */
public final class Travel {
    /** Hit or attacking in the last 10 seconds. */
    public static final int COMBAT_TICKS = 200;

    private Travel() {
    }

    public static boolean inCombat(Player player) {
        return isRecent(player, player.getLastHurtByMobTimestamp()) || isRecent(player, player.getLastHurtMobTimestamp());
    }

    /** Timestamps start at 0, which means "never". */
    private static boolean isRecent(Player player, int timestamp) {
        return timestamp > 0 && player.tickCount - timestamp < COMBAT_TICKS;
    }

    /** Right-clicking a signpost: the list of places, each with its name. The one the player is at comes first. */
    public static void openMenu(ServerPlayer player, SafeSpot here) {
        DistrictSites sites = DistrictSites.get(player.server.overworld());
        String team = Teams.teamKey(player);
        List<String> known = sites.spots().stream()
                .filter(spot -> !spot.id().equals(here.id()) && sites.hasDiscovered(team, spot.id()))
                .map(SafeSpot::id)
                .toList();
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TravelMenuPacket(here.id(), known));
    }

    /** The player picked a place. The server checks everything again: the client only asks. */
    public static void travel(ServerPlayer player, String spotId) {
        DistrictSites sites = DistrictSites.get(player.server.overworld());
        SafeSpot here = SafeZones.spotAt(player);
        SafeSpot there = sites.spot(spotId).orElse(null);
        if (here == null || there == null || here == there || Downed.isDowned(player) || !sites.hasDiscovered(Teams.teamKey(player), spotId)) {
            return;
        }
        if (inCombat(player)) {
            player.displayClientMessage(Component.translatable("message." + WallisUltimate.MOD_ID + ".travel.in_combat"), true);
            return;
        }
        player.stopRiding();
        player.teleportTo(player.server.overworld(), there.arrival().getX() + 0.5, there.arrival().getY(), there.arrival().getZ() + 0.5, 180.0F, 0.0F);
    }
}
