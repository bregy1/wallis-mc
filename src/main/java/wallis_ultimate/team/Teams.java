package wallis_ultimate.team;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * "Same team" checks, backed by FTB Teams (PROG-006, PACK-004). A solo player is a team of one, so two solo
 * players are never teammates. Server side only: FTB Teams' manager lives on the server.
 */
public final class Teams {
    /** GameTests only: mock players can't join FTB teams (that needs a connected player), so tests group them here. */
    private static final Map<UUID, String> TEST_TEAMS = new ConcurrentHashMap<>();

    private Teams() {
    }

    public static boolean sameTeam(Player a, Player b) {
        if (a == b) {
            return false;
        }
        String testTeamA = TEST_TEAMS.get(a.getUUID());
        String testTeamB = TEST_TEAMS.get(b.getUUID());
        if (testTeamA != null || testTeamB != null) {
            return testTeamA != null && testTeamA.equals(testTeamB);
        }
        return FTBTeamsAPI.api().isManagerLoaded()
                && FTBTeamsAPI.api().getManager().arePlayersInSameTeam(a.getUUID(), b.getUUID());
    }

    /** A name for the player's team that stays the same, for remembering what the team has done. */
    public static String teamKey(Player player) {
        String testTeam = TEST_TEAMS.get(player.getUUID());
        if (testTeam != null) {
            return testTeam;
        }
        if (FTBTeamsAPI.api().isManagerLoaded()) {
            return FTBTeamsAPI.api().getManager().getTeamForPlayerID(player.getUUID()).map(team -> team.getId().toString()).orElse(player.getStringUUID());
        }
        return player.getStringUUID();
    }

    public static List<ServerPlayer> onlineTeammates(ServerPlayer player) {
        return player.server.getPlayerList().getPlayers().stream().filter(other -> sameTeam(player, other)).toList();
    }

    /** GameTests only. */
    public static void joinTestTeam(Player player, String team) {
        TEST_TEAMS.put(player.getUUID(), team);
    }

    /** GameTests only. */
    public static void leaveTestTeam(Player player) {
        TEST_TEAMS.remove(player.getUUID());
    }
}
