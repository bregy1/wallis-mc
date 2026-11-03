package wallis_ultimate.gametest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.quest.task.KillTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.progression.Tiers;
import wallis_ultimate.story.ChapterOneTalks;
import wallis_ultimate.story.ConversationSession;
import wallis_ultimate.story.StoryNpcs;
import wallis_ultimate.story.StoryQuests;
import wallis_ultimate.story.StoryState;
import wallis_ultimate.story.SwearFilter;
import wallis_ultimate.story.Talk;

/** Checks for STORY-002, STORY-003, PROG-001, PROG-003, PROG-004 and the seam with the boss. The whole path is played in StorySmokeSteps. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StoryGameTests {
    private static final String PLATFORM = "platform";
    private static final Pattern TRANSLATE_KEY = Pattern.compile("translate\\\\\":\\\\\"([a-z0-9_.]+)");
    /** Coarser than the filter on purpose: anything this finds in cleaned text slipped through. */
    private static final Pattern SWEAR_LEFT = Pattern.compile("fuck|shit|damn|bitch|bastard|asshole|dumbass|jackass|piss|\\bass\\b|\\bhell\\b|\\bcrap|\\bdick|\\bballs\\b|\\bturd|\\bprick|\\bcock\\b", Pattern.CASE_INSENSITIVE);

    private StoryGameTests() {
    }

    private static ConversationSession threePlayerTalk(RandomSource random, List<UUID> players) {
        Talk talk = Talk.of("test")
                .line("ask", ChapterOneTalks.talk(ChapterOneTalks.INTRO).node("start").speaker(), "?")
                .reply("yes", "after")
                .reply("no", null)
                .line("after", ChapterOneTalks.talk(ChapterOneTalks.INTRO).node("start").speaker(), "ok")
                .end()
                .build();
        ConversationSession session = new ConversationSession(1, talk, "team", false, players.get(0), players);
        session.enter(talk.first(), random);
        return session;
    }

    /** STORY-002: each time a reply is needed, one present member is picked at random; the others can't choose. */
    @GameTest(template = PLATFORM)
    public static void oneRandomMemberChoosesEachReply(GameTestHelper helper) {
        RandomSource random = RandomSource.create(7);
        List<UUID> players = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        ConversationSession session = threePlayerTalk(random, players);
        Set<UUID> picked = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            session.enter(session.talk().first(), random);
            UUID chooser = session.chooser();
            if (!players.contains(chooser)) {
                helper.fail("picked someone who isn't there");
            }
            picked.add(chooser);
            for (UUID player : players) {
                if (session.mayAnswer(player, 0) != player.equals(chooser)) {
                    helper.fail("someone other than the picked member may answer");
                }
            }
        }
        if (picked.size() != players.size()) {
            helper.fail("100 picks only ever hit " + picked.size() + " of 3 members");
        }
        if (session.mayAnswer(session.chooser(), 5)) {
            helper.fail("a reply that doesn't exist counts");
        }
        session.enter("after", random);
        if (session.chooser() != null || session.mayAnswer(players.get(0), 0)) {
            helper.fail("a line without replies has someone choosing");
        }
        helper.succeed();
    }

    /** Someone leaving doesn't hang the talk: the choice moves on, and a talk nobody is left in ends. */
    @GameTest(template = PLATFORM)
    public static void aConversationSurvivesPeopleLeaving(GameTestHelper helper) {
        RandomSource random = RandomSource.create(11);
        List<UUID> players = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        ConversationSession session = threePlayerTalk(random, players);
        UUID first = session.chooser();
        session.remove(first, random);
        if (session.chooser() == null || session.chooser().equals(first) || !session.participants().contains(session.chooser())) {
            helper.fail("the choice didn't move on to someone still there");
        }
        UUID lead = session.lead();
        session.remove(lead, random);
        if (session.lead().equals(lead) || session.ended()) {
            helper.fail("the talk's lead didn't move on");
        }
        session.remove(session.participants().get(0), random);
        if (!session.ended()) {
            helper.fail("a talk with nobody left didn't end");
        }
        helper.succeed();
    }

    /** Whoever was picked and lets the clock run out loses the turn, but a solo player is never timed out. */
    @GameTest(template = PLATFORM)
    public static void aSlowChooserPassesTheTurn(GameTestHelper helper) {
        RandomSource random = RandomSource.create(13);
        List<UUID> players = List.of(UUID.randomUUID(), UUID.randomUUID());
        ConversationSession session = threePlayerTalk(random, players);
        for (int i = 0; i < ConversationSession.CHOOSER_TIMEOUT_TICKS; i++) {
            session.tick();
        }
        if (!session.chooserTimedOut()) {
            helper.fail("the chooser didn't time out");
        }
        UUID slow = session.chooser();
        session.passChooser(random);
        if (slow.equals(session.chooser())) {
            helper.fail("the turn didn't pass");
        }
        ConversationSession solo = threePlayerTalk(random, List.of(players.get(0)));
        for (int i = 0; i < ConversationSession.CHOOSER_TIMEOUT_TICKS; i++) {
            solo.tick();
        }
        if (solo.chooserTimedOut()) {
            helper.fail("a solo player got timed out");
        }
        helper.succeed();
    }

    /** STORY-002: the clean-language option. Whole words only, and no "%" that would upset lang formatting. */
    @GameTest(template = PLATFORM)
    public static void theSwearFilterReplacesSwearWordsOnly(GameTestHelper helper) {
        String cleaned = SwearFilter.clean("What the fuck, you dumbass. Goddamn shit, hell!");
        if (SWEAR_LEFT.matcher(cleaned).find() || cleaned.contains("%")) {
            helper.fail("not clean: " + cleaned);
        }
        String harmless = "A class of scrap in the shell: hello, Dickson's assistant passed.";
        String expected = harmless.replace("Dickson's", SwearFilter.clean("Dickson") + "'s");
        if (!SwearFilter.clean(harmless).equals(expected)) {
            helper.fail("too eager: " + SwearFilter.clean(harmless));
        }
        helper.succeed();
    }

    private static JsonObject lang() throws Exception {
        try (InputStream stream = WallisUltimate.class.getResourceAsStream("/assets/" + WallisUltimate.MOD_ID + "/lang/en_us.json")) {
            return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
        }
    }

    /** All story text is in the lang file, the filter gets every swear word in it, and nothing in the quest file points at nothing. */
    @GameTest(template = PLATFORM)
    public static void allStoryTextIsInTheLangFileAndCanBeCleaned(GameTestHelper helper) throws Exception {
        JsonObject lang = lang();
        for (Talk talk : ChapterOneTalks.all()) {
            for (Talk.Node node : talk.nodes()) {
                if (!lang.has(node.textKey())) {
                    helper.fail("not in the lang file: " + node.textKey() + " (run runData)");
                }
                node.replies().forEach(reply -> {
                    if (!lang.has(reply.textKey())) {
                        helper.fail("not in the lang file: " + reply.textKey() + " (run runData)");
                    }
                });
            }
        }
        for (String key : lang.keySet()) {
            if (key.startsWith("story." + WallisUltimate.MOD_ID)) {
                String cleaned = SwearFilter.clean(lang.get(key).getAsString());
                Matcher left = SWEAR_LEFT.matcher(cleaned);
                if (left.find()) {
                    helper.fail("the filter misses '" + left.group() + "' in " + key);
                }
            }
        }
        String quests = Files.readString(Path.of("config/ftbquests/quests/chapters/scavenger.snbt"));
        Matcher keys = TRANSLATE_KEY.matcher(quests);
        int found = 0;
        while (keys.find()) {
            found++;
            if (!lang.has(keys.group(1))) {
                helper.fail("the quest file uses a key that's not in the lang file: " + keys.group(1));
            }
        }
        if (found == 0) {
            helper.fail("no text keys found in the quest file");
        }
        helper.succeed();
    }

    /** Every conversation can be read to its end: every line can be reached, and every path stops. */
    @GameTest(template = PLATFORM)
    public static void everyLineOfEveryTalkCanBeReached(GameTestHelper helper) {
        if (ChapterOneTalks.all().size() < 10) {
            helper.fail("only " + ChapterOneTalks.all().size() + " talks");
        }
        for (Talk talk : ChapterOneTalks.all()) {
            if (talk.id().equals(ChapterOneTalks.BANTER)) {
                continue; // banter is single lines, each its own way in
            }
            Set<String> reached = new HashSet<>();
            List<String> open = new ArrayList<>(List.of(talk.first()));
            while (!open.isEmpty()) {
                String id = open.remove(open.size() - 1);
                if (reached.add(id)) {
                    Talk.Node node = talk.node(id);
                    if (node.next() != null) {
                        open.add(node.next());
                    }
                    node.replies().stream().map(Talk.Reply::next).filter(next -> next != null).forEach(open::add);
                }
            }
            for (Talk.Node node : talk.nodes()) {
                if (!reached.contains(node.id())) {
                    helper.fail("talk " + talk.id() + ": line " + node.id() + " can't be reached");
                }
            }
        }
        helper.succeed();
    }

    /** PROG-003: the chapter loaded from pack/config, and everything it gives unlocks a stage that exists. */
    @GameTest(template = PLATFORM)
    public static void chapterOneLoadsWithRealStages(GameTestHelper helper) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Chapter chapter = file == null ? null : file.getChapter(StoryQuests.CHAPTER);
        if (chapter == null) {
            helper.fail("chapter 1 didn't load");
            return;
        }
        if (chapter.getQuests().size() != 12) {
            helper.fail("chapter 1 has " + chapter.getQuests().size() + " quests");
        }
        Set<String> stages = new HashSet<>();
        for (Quest quest : chapter.getQuests()) {
            for (Task task : quest.getTasks()) {
                if (task instanceof CustomTask && task.autoSubmitOnPlayerTick() == 0) {
                    helper.fail("custom task " + task.getCodeString() + " has no check, so it can never complete");
                }
            }
            for (Reward reward : quest.getRewards()) {
                CompoundTag data = new CompoundTag();
                reward.writeData(data);
                if (data.contains("stage")) {
                    stages.add(data.getString("stage"));
                    if (StageStates.findStage(data.getString("stage")) == null) {
                        helper.fail("quest " + quest.getCodeString() + " unlocks a stage that doesn't exist: " + data.getString("stage"));
                    }
                }
            }
        }
        if (!stages.equals(Set.of("scavenger_weapons", "cooking", "iron", "settler"))) {
            helper.fail("the chapter unlocks " + stages);
        }
        helper.succeed();
    }

    /**
     * The seam with the boss. Neither side has to exist for this to hold: the kill task on the id loads and waits, and the quest
     * that sets the fight off runs the agreed command, quietly, once for the team.
     */
    @GameTest(template = PLATFORM)
    public static void theBossQuestsCarryTheAgreedSeam(GameTestHelper helper) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        Task kill = file.getTask(StoryQuests.TASK_HOG_KILL);
        if (!(kill instanceof KillTask) || kill.getMaxProgress() != 1) {
            helper.fail("the last quest isn't a kill task on one boss: " + kill);
            return;
        }
        CompoundTag killData = new CompoundTag();
        kill.writeData(killData);
        if (!StoryQuests.HOG_DADDY.toString().equals(killData.getString("entity"))) {
            helper.fail("the kill task is on " + killData.getString("entity"));
        }
        Reward start = file.getQuest(StoryQuests.QUEST_READY).getRewards().iterator().next();
        CompoundTag command = new CompoundTag();
        start.writeData(command);
        if (!command.getString("command").equals("/" + ChapterOneTalks.BOSS_START_COMMAND) || !command.getBoolean("elevate_perms") || !command.getBoolean("silent")) {
            helper.fail("the fight's command reward is " + command);
        }
        if (!start.isTeamReward()) {
            helper.fail("the fight would start once per team member");
        }
        // A command that doesn't exist (yet) fails without an exception.
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(), "wallis_no_such_command go");
        helper.succeed();
    }

    /** The bed and the Rabbit Foot go to every player of a team; quests that set things in motion happen only once. */
    @GameTest(template = PLATFORM)
    public static void everyPlayerGetsTheirOwnBedAndFoot(GameTestHelper helper) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        for (long questId : new long[] {StoryQuests.QUEST_INTRO, StoryQuests.QUEST_NIGHT}) {
            for (Reward reward : file.getQuest(questId).getRewards()) {
                if (reward.isTeamReward()) {
                    helper.fail("quest " + file.getQuest(questId).getCodeString() + " gives its reward to one member only");
                }
            }
        }
        helper.succeed();
    }

    /** PROG-001, PROG-004: tiers come from data, in order; a team's tier follows the stages it has unlocked. */
    @GameTest(template = PLATFORM)
    public static void tiersComeFromDataInOrder(GameTestHelper helper) {
        List<Tiers.Tier> tiers = Tiers.all();
        List<String> names = tiers.stream().map(tier -> tier.id().getPath().toLowerCase(Locale.ROOT)).toList();
        if (!names.equals(List.of("scavenger", "settler", "gunsmith", "launch"))) {
            helper.fail("tiers: " + names);
        }
        if (Tiers.current(stage -> false) != tiers.get(0)) {
            helper.fail("a new team doesn't start in the first tier");
        }
        if (Tiers.current("iron"::equals) != tiers.get(0)) {
            helper.fail("a stage that's no tier's changed the tier");
        }
        if (Tiers.current("settler"::equals) != tiers.get(1)) {
            helper.fail("the Settler stage doesn't open tier 2");
        }
        if (!tiers.get(0).milestone().equals(file(StoryQuests.QUEST_HOG))) {
            helper.fail("tier 1's milestone isn't the Hog Daddy quest: " + tiers.get(0).milestone());
        }
        helper.succeed();
    }

    private static String file(long id) {
        return String.format("%016X", id);
    }

    /** PROG-006: a joiner's story comes along; a leaver keeps a copy. */
    @GameTest(template = PLATFORM)
    public static void storyStateFollowsTheTeam(GameTestHelper helper) {
        StoryState state = StoryState.get(helper.getLevel().getServer());
        String solo = "wu_t_solo_" + UUID.randomUUID();
        String party = "wu_t_party_" + UUID.randomUUID();
        String left = "wu_t_left_" + UUID.randomUUID();
        state.setFlag(solo, "a");
        state.setFlag(party, "b");
        state.setHome(party, new BlockPos(1, 2, 3));
        state.setHome(solo, new BlockPos(9, 9, 9));
        state.mergeInto(solo, party);
        if (!state.hasFlag(party, "a") || !state.hasFlag(party, "b") || !new BlockPos(1, 2, 3).equals(state.home(party))) {
            helper.fail("joining mixed up the team's state");
        }
        state.copyTeam(party, left);
        if (!state.hasFlag(left, "a") || !state.hasFlag(left, "b") || !new BlockPos(1, 2, 3).equals(state.home(left))) {
            helper.fail("the leaver didn't keep a copy");
        }
        state.setFlag(left, "c");
        if (state.hasFlag(party, "c")) {
            helper.fail("the copy still points at the team's own state");
        }
        helper.succeed();
    }

    /** STORY-003: Lurleen stands where she's put, wears her skin through Easy NPC, and can't die (SURV-011). */
    @GameTest(template = PLATFORM)
    public static void lurleenIsOnEasyNpcAndCantDie(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        Optional<Mob> spawned = StoryNpcs.spawnLurleen(level, pos, false);
        if (spawned.isEmpty()) {
            helper.fail("Easy NPC's entity isn't there");
            return;
        }
        Mob lurleen = spawned.get();
        CompoundTag data = lurleen.saveWithoutId(new CompoundTag());
        CompoundTag skin = data.getCompound("SkinData");
        boolean rightSkin = "RESOURCE_LOCATION".equals(skin.getString("Type")) && skin.getString("Texture").endsWith("textures/entity/npc/lurleen.png");
        boolean hurt = lurleen.hurt(level.damageSources().generic(), 1000.0F);
        boolean alive = lurleen.isAlive();
        boolean tagged = lurleen.getTags().contains(StoryNpcs.TAG_LURLEEN) && StoryNpcs.isStoryNpc(lurleen);
        boolean persistent = data.getBoolean("PersistenceRequired");
        lurleen.discard();
        if (!rightSkin) {
            helper.fail("her skin data is " + skin);
        }
        if (hurt || !alive) {
            helper.fail("Lurleen can be hurt");
        }
        if (!tagged || !persistent) {
            helper.fail("tags or persistence missing");
        }
        helper.succeed();
    }

    /** The "Hot Meal" check: raw food doesn't count, cooked food does. */
    @GameTest(template = PLATFORM)
    public static void onlyCookedFoodCountsAsAHotMeal(GameTestHelper helper) {
        Inventory inventory = helper.makeMockPlayer().getInventory();
        inventory.setItem(0, new ItemStack(Items.BEEF));
        if (StoryQuests.holdsCookedFood(inventory)) {
            helper.fail("raw beef counted");
        }
        inventory.setItem(1, new ItemStack(Items.BAKED_POTATO));
        if (!StoryQuests.holdsCookedFood(inventory)) {
            helper.fail("a baked potato didn't count");
        }
        helper.succeed();
    }
}
