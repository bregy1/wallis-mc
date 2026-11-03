package wallis_ultimate.story;

import dev.ftb.mods.ftbquests.net.SyncTeamDataMessage;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * PROG-006: the story belongs to the team. FTB Quests copies its progress when a party is created and adds a joiner's finished
 * quests to the party; our own story state does the same (creating a party counts as joining it), so conversations and quests stay
 * in step. When a player leaves, FTB Quests would put them back on their old solo progress and let them claim everything again;
 * they keep a copy of what the team had done instead (Q-009).
 */
public final class StoryTeams {
    private StoryTeams() {
    }

    public static void register() {
        TeamEvent.PLAYER_JOINED_PARTY.register(StoryTeams::onJoined);
        TeamEvent.PLAYER_LEFT_PARTY.register(StoryTeams::onLeft);
    }

    private static void onJoined(PlayerJoinedPartyTeamEvent event) {
        StoryState.get(event.getPlayer().server).mergeInto(event.getPreviousTeam().getId().toString(), event.getTeam().getId().toString());
    }

    private static void onLeft(PlayerLeftPartyTeamEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        Team party = event.getTeam();
        Team solo = event.getPlayerTeam();
        StoryState.get(server).copyTeam(party.getId().toString(), solo.getId().toString());
        if (ServerQuestFile.INSTANCE != null) {
            TeamData from = ServerQuestFile.INSTANCE.getOrCreateTeamData(party);
            TeamData to = ServerQuestFile.INSTANCE.getOrCreateTeamData(solo);
            to.copyData(from);
            to.clearCachedProgress();
            to.markDirty();
            new SyncTeamDataMessage(to, true).sendTo(solo.getOnlineMembers());
        }
    }
}
