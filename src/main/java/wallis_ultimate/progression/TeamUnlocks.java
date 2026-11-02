package wallis_ultimate.progression;

import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.bananemdnsa.historystages.data.saveddata.IndividualStageData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * PROG-006: a player who joins a team loses the unlocks the team doesn't have (Q-126), so all members always match.
 * History Stages' "shareOnJoin" (pack/config) already hands the joiner every stage the team has; it never passes
 * the joiner's stages on to the team, so the order of the two doesn't matter. Leaving a team keeps all stages.
 */
public final class TeamUnlocks {
    private TeamUnlocks() {
    }

    public static void register() {
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamUnlocks::onJoinedParty);
    }

    private static void onJoinedParty(PlayerJoinedPartyTeamEvent event) {
        ServerPlayer player = event.getPlayer();
        Set<UUID> others = new HashSet<>(event.getTeam().getMembers());
        others.remove(player.getUUID());
        removeUnlocksTheTeamLacks(player.getUUID(), others, player.server);
    }

    /**
     * Relocks every team-shared stage the joiner has that none of the other members has. A team without other
     * members (just created) keeps everything. Returns the relocked stage ids.
     */
    public static List<String> removeUnlocksTheTeamLacks(UUID joiner, Collection<UUID> others, MinecraftServer server) {
        List<String> relocked = new ArrayList<>();
        if (others.isEmpty()) {
            return relocked;
        }
        // History Stages' API only answers "is it unlocked" for online players; teammates may be offline.
        // Its own team sharing reads this saved data the same way.
        IndividualStageData data = IndividualStageData.get(server.overworld());
        for (String stage : List.copyOf(data.getUnlockedStages(joiner))) {
            boolean teamHasIt = others.stream().anyMatch(member -> data.hasStage(member, stage));
            if (net.bananemdnsa.historystages.api.team.Teams.isSharedWithTeam(stage) && !teamHasIt
                    && StageStates.relockIndividual(stage, joiner, server)) {
                relocked.add(stage);
            }
        }
        return relocked;
    }
}
