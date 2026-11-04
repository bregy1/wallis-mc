package wallis_ultimate.story;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import wallis_ultimate.team.Teams;

/**
 * STORY-003: what Lurleen talks about when a player walks up to her. She reacts to where the team is in the story: the intro
 * first, then what's next on the list, once per quest, and after that she just gives them a hard time.
 */
public final class LurleenTopics {
    /** A quest and what she says about it the first time. In the order the quests come. */
    private record Topic(long quest, String talk) {
    }

    private static final List<Topic> TOPICS = List.of(
            new Topic(StoryQuests.QUEST_SWING, ChapterOneTalks.NUDGE_SWING),
            new Topic(StoryQuests.QUEST_HOME, ChapterOneTalks.NUDGE_HOME),
            new Topic(StoryQuests.QUEST_DINNER, ChapterOneTalks.NUDGE_DINNER),
            new Topic(StoryQuests.QUEST_ZOMBIES, ChapterOneTalks.NUDGE_ZOMBIES),
            new Topic(StoryQuests.QUEST_NIGHT, ChapterOneTalks.NUDGE_NIGHT),
            new Topic(StoryQuests.QUEST_RADIO, ChapterOneTalks.NUDGE_RADIO),
            new Topic(StoryQuests.QUEST_SAFE, ChapterOneTalks.NUDGE_SAFE),
            new Topic(StoryQuests.QUEST_SCRAP, ChapterOneTalks.NUDGE_SCRAP),
            new Topic(StoryQuests.QUEST_CARL, ChapterOneTalks.NUDGE_CARL),
            new Topic(StoryQuests.QUEST_READY, ChapterOneTalks.NUDGE_HOG));

    private static final List<String> BANTER = List.of("smell", "swept", "septic", "beer", "granny", "eyes", "dale", "army", "barrel");

    private LurleenTopics() {
    }

    /** Starts whatever she has to say next. {@code atCabin}: this is the Lurleen at the old cabin, not the one at home. */
    public static void talkTo(ServerPlayer player, boolean atCabin) {
        StoryState state = StoryState.get(player.server);
        String team = Teams.teamKey(player);
        if (!state.hasFlag(team, ChapterOneTalks.HEARD_INTRO)) {
            Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.INTRO));
            return;
        }
        if (StoryQuests.completed(player, StoryQuests.QUEST_HOG)) {
            if (!state.hasFlag(team, ChapterOneTalks.HEARD_ENDING)) {
                Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.ENDING));
            } else {
                banter(player, state, team);
            }
            return;
        }
        if (StoryQuests.completed(player, StoryQuests.QUEST_READY)) {
            Conversations.start(player, ChapterOneTalks.talk(atCabin ? ChapterOneTalks.AGAIN : ChapterOneTalks.NUDGE_HOG));
            return;
        }
        if (atCabin && StoryQuests.open(player, StoryQuests.QUEST_READY)) {
            Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.READY));
            return;
        }
        if (reactToWhatHappened(player, state, team)) {
            return;
        }
        for (Topic topic : TOPICS) {
            if (StoryQuests.open(player, topic.quest()) && state.setFlag(team, "asked_" + topic.talk())) {
                Conversations.start(player, ChapterOneTalks.talk(topic.talk()));
                return;
            }
        }
        if (anyQuestOpen(player) && player.getRandom().nextBoolean()) {
            Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.NUDGE_LIST));
            return;
        }
        banter(player, state, team);
    }

    /** Things she has an opinion on, once each. */
    private static boolean reactToWhatHappened(ServerPlayer player, StoryState state, String team) {
        if (StoryQuests.completed(player, StoryQuests.QUEST_HOME) && state.setFlag(team, "said_moved_in")) {
            return Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.HOME_REACTION));
        }
        if (StoryQuests.completed(player, StoryQuests.QUEST_NIGHT) && state.setFlag(team, "said_foot")) {
            return Conversations.start(player, ChapterOneTalks.talk(ChapterOneTalks.FOOT_REACTION));
        }
        return false;
    }

    private static boolean anyQuestOpen(ServerPlayer player) {
        return TOPICS.stream().anyMatch(topic -> StoryQuests.open(player, topic.quest()));
    }

    private static void banter(ServerPlayer player, StoryState state, String team) {
        List<String> lines = new ArrayList<>(BANTER);
        if (state.hasFlag(team, ChapterOneTalks.TALKED_CARL)) {
            lines.add(state.hasFlag(team, ChapterOneTalks.BELIEVED_CARL) ? "believed" : "doubted");
        }
        Talk talk = ChapterOneTalks.talk(ChapterOneTalks.BANTER);
        Conversations.start(player, talk, lines.get(player.getRandom().nextInt(lines.size())), false);
    }
}
