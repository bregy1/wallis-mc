package wallis_ultimate.gametest;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;

/** Checks for PROG-006: teams come from FTB Teams. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TeamGameTests {
    private static final String PLATFORM = "platform";

    private TeamGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void playersWithoutATeamAreNotTeammates(GameTestHelper helper) {
        if (!FTBTeamsAPI.api().isManagerLoaded()) {
            helper.fail("FTB Teams isn't running");
        }
        Player a = helper.makeMockPlayer();
        Player b = helper.makeMockPlayer();
        if (Teams.sameTeam(a, b) || Teams.sameTeam(a, a)) {
            helper.fail("players without a team count as teammates");
        }
        Teams.joinTestTeam(a, "wu_t_pair");
        Teams.joinTestTeam(b, "wu_t_pair");
        boolean together = Teams.sameTeam(a, b);
        Teams.leaveTestTeam(a);
        Teams.leaveTestTeam(b);
        if (!together) {
            helper.fail("test team members aren't teammates");
        }
        helper.succeed();
    }
}
