package wallis_ultimate.story;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;

/**
 * PROG-003, STORY-003: the connection between our story and Chapter 1's quests in FTB Quests
 * (pack/config/ftbquests/quests/chapters/scavenger.snbt). Custom tasks there carry a tag that says what completes them. The
 * ids here match that file. Everything a quest unlocks comes as a normal reward in the file: History Stages shares it across
 * the team.
 */
public final class StoryQuests {
    public static final long CHAPTER = 0x5A11000000000001L;
    public static final long QUEST_INTRO = 0x5A11000000000100L;
    public static final long QUEST_SWING = 0x5A11000000000200L;
    public static final long QUEST_HOME = 0x5A11000000000300L;
    public static final long QUEST_DINNER = 0x5A11000000000400L;
    public static final long QUEST_ZOMBIES = 0x5A11000000000500L;
    public static final long QUEST_NIGHT = 0x5A11000000000600L;
    public static final long QUEST_RADIO = 0x5A11000000000700L;
    public static final long QUEST_SAFE = 0x5A11000000000800L;
    public static final long QUEST_SCRAP = 0x5A11000000000900L;
    public static final long QUEST_CARL = 0x5A11000000000A00L;
    public static final long QUEST_READY = 0x5A11000000000B00L;
    public static final long QUEST_HOG = 0x5A11000000000C00L;
    public static final long TASK_HOG_KILL = 0x5A11000000000C01L;
    /** The other side of the seam with the boss (ENEMY-011). */
    public static final ResourceLocation HOG_DADDY = ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "hog_daddy");
    public static final TagKey<Item> COOKED_FOOD = ItemTags.create(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "cooked_food"));

    private static final String FLAG_TAG = "wu_flag:";
    private static final String CHECK_TAG = "wu_check:";
    private static final int CHECK_EVERY_TICKS = 20;
    private static final int HOG_KILL_RADIUS = 128;

    private StoryQuests() {
    }

    public static void register() {
        CustomTaskEvent.EVENT.register(StoryQuests::onCustomTask);
    }

    /** FTB Quests announces every custom task when it loads the quest file. */
    private static EventResult onCustomTask(CustomTaskEvent event) {
        CustomTask task = event.getTask();
        if (!task.getQuestFile().isServerSide()) {
            return EventResult.pass();
        }
        BiPredicate<ServerPlayer, String> check = checkFor(task);
        if (check != null) {
            task.setCheckTimer(CHECK_EVERY_TICKS);
            task.setCheck((data, player) -> {
                if (check.test(player, Teams.teamKey(player))) {
                    data.setProgress(data.task().getMaxProgress());
                }
            });
        }
        return EventResult.pass();
    }

    @Nullable
    private static BiPredicate<ServerPlayer, String> checkFor(CustomTask task) {
        for (String tag : task.getTags()) {
            if (tag.startsWith(FLAG_TAG)) {
                String flag = tag.substring(FLAG_TAG.length());
                return (player, team) -> StoryState.get(player.server).hasFlag(team, flag);
            }
            if (tag.equals(CHECK_TAG + "home")) {
                return (player, team) -> StoryState.get(player.server).home(team) != null;
            }
            if (tag.equals(CHECK_TAG + "safe_spot")) {
                return StoryQuests::foundASafeSpot;
            }
            if (tag.equals(CHECK_TAG + "cooked_food")) {
                return (player, team) -> holdsCookedFood(player.getInventory());
            }
        }
        WallisUltimate.LOGGER.warn("Custom task {} in the quest file has no check", task.getCodeString());
        return null;
    }

    public static boolean foundASafeSpot(ServerPlayer player, String team) {
        DistrictSites sites = DistrictSites.get(player.server.overworld());
        return sites.spots().stream().anyMatch(spot -> sites.hasDiscovered(team, spot.id()));
    }

    public static boolean holdsCookedFood(Inventory inventory) {
        return inventory.items.stream().anyMatch(stack -> stack.is(COOKED_FOOD));
    }

    @Nullable
    public static TeamData data(ServerPlayer player) {
        return ServerQuestFile.INSTANCE == null ? null : ServerQuestFile.INSTANCE.getOrCreateTeamData(player);
    }

    /** Is this quest done for the player's team? */
    public static boolean completed(ServerPlayer player, long questId) {
        TeamData data = data(player);
        Quest quest = ServerQuestFile.INSTANCE == null ? null : ServerQuestFile.INSTANCE.getQuest(questId);
        return data != null && quest != null && data.isCompleted(quest);
    }

    /** Is this quest open for the player's team right now: everything before it is done, and it isn't? */
    public static boolean open(ServerPlayer player, long questId) {
        TeamData data = data(player);
        Quest quest = ServerQuestFile.INSTANCE == null ? null : ServerQuestFile.INSTANCE.getQuest(questId);
        return data != null && quest != null && !data.isCompleted(quest) && data.canStartTasks(quest);
    }

    /**
     * FTB Quests only counts a kill if a player's own hit lands the last blow, but Hog Daddy's weakness is fire and stunning
     * himself on obstacles. So his death counts for every team with someone near it, whatever killed him.
     */
    public static void bossDied(ServerPlayer nearby) {
        TeamData data = data(nearby);
        Task kill = ServerQuestFile.INSTANCE == null ? null : ServerQuestFile.INSTANCE.getTask(TASK_HOG_KILL);
        if (data != null && kill != null && data.canStartTasks(kill.getQuest())) {
            data.setProgress(kill, kill.getMaxProgress());
        }
    }

    public static boolean isHogDaddy(net.minecraft.world.entity.Entity entity) {
        return HOG_DADDY.equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
    }

    public static int hogKillRadius() {
        return HOG_KILL_RADIUS;
    }
}
