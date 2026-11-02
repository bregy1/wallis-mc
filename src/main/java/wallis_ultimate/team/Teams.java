package wallis_ultimate.team;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** "Same team" checks. Uses vanilla scoreboard teams until FTB Teams is part of the dev setup (PACK-001). */
public final class Teams {
    private Teams() {
    }

    public static boolean sameTeam(Player a, Player b) {
        return a != b && a.isAlliedTo(b);
    }

    public static List<ServerPlayer> onlineTeammates(ServerPlayer player) {
        return player.server.getPlayerList().getPlayers().stream().filter(other -> sameTeam(player, other)).toList();
    }
}
