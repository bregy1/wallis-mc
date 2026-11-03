package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.describe;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.renderedText;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import com.immersivecinematics.immersive_cinematics.camera.CameraManager;
import com.immersivecinematics.immersive_cinematics.control.ExitReason;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.task.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.client.CleanLanguage;
import wallis_ultimate.client.ClientConversation;
import wallis_ultimate.client.ConversationScreen;
import wallis_ultimate.network.ConversationPacket;
import wallis_ultimate.progression.Tiers;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.story.ChapterOneTalks;
import wallis_ultimate.story.OpeningCutscene;
import wallis_ultimate.story.StoryEvents;
import wallis_ultimate.story.StoryNpcs;
import wallis_ultimate.story.StoryQuests;
import wallis_ultimate.story.StoryState;
import wallis_ultimate.story.Transmissions;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * Chapter 1 played in a real client, from the opening to the moment the boss quest fires and past its end: the cutscene and its
 * promises (STORY-010), Lurleen (STORY-003), conversations and the clean-language option (STORY-002), and every quest with what
 * it unlocks (PROG-003, PROG-004). Run by itself with {@code bash tools/testsnapshot.sh look story}: then the opening starts the
 * way it does for a real player, by joining a new world. On this branch the boss doesn't exist, so its death is simulated.
 */
final class StorySmokeSteps {
    private static final String LURLEEN_NAME = "Lurleen";
    private static final int BUTTONS_WITH_OPTION = 1;
    /** Every run of the boss's command, with permission level 2, as the boss's own command would see it. */
    private static final List<String> BOSS_COMMANDS = new CopyOnWriteArrayList<>();

    private StorySmokeSteps() {
    }

    private static void onCommand(CommandEvent event) {
        String command = event.getParseResults().getReader().getString();
        if (command.equals(ChapterOneTalks.BOSS_START_COMMAND) && event.getParseResults().getContext().getSource().hasPermission(2)) {
            BOSS_COMMANDS.add(command);
        }
    }

    static List<ClientSmokeTest.Step> steps() {
        MinecraftForge.EVENT_BUS.addListener(StorySmokeSteps::onCommand);
        List<ClientSmokeTest.Step> steps = new ArrayList<>();
        opening(steps);
        firstConversation(steps);
        lookAtLurleen(steps);
        firstQuests(steps);
        night(steps);
        radioAndSafeSpot(steps);
        carl(steps);
        boss(steps);
        teams(steps);
        skip(steps);
        steps.add(step("back to normal", 5, player -> {
            command("difficulty peaceful");
            command("gamemode survival");
            command("time set 1000");
            command("effect give @s minecraft:resistance infinite 4 true");
        }));
        return steps;
    }

    private static void opening(List<ClientSmokeTest.Step> steps) {
        steps.add(step("get ready for the opening", 30, player -> {
            command("effect clear @s minecraft:resistance");
            command("difficulty normal");
            command("time set 1000");
            command("weather clear");
            if (!StoryEvents.openingPlaysOnJoin()) {
                // After the other groups: back where a new player starts, and without the bed the survival group left standing.
                serverPlayer().getPersistentData().remove(WallisUltimate.MOD_ID + ":beds");
                BlockPos spawn = sites().spawn();
                command(String.format("tp @s %.2f %d %.2f 0 0", spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5));
            }
        }));
        steps.add(step("start it, if joining didn't", 10, player -> {
            if (!StoryEvents.openingPlaysOnJoin()) {
                command("clear");
                command("wallis_story opening");
            }
        }));
        steps.add(await("the opening starts", 300, player -> CameraManager.INSTANCE.isActive() && OpeningCutscene.isWatching(serverPlayer())));
        steps.add(step("check the opening started", 0, player -> {
            check("a new world opens with the opening cutscene", CameraManager.INSTANCE.isActive() && OpeningCutscene.isWatching(serverPlayer()),
                    "camera active " + CameraManager.INSTANCE.isActive());
        }));
        steps.add(await("a few seconds in", 300, player -> OpeningCutscene.tick(serverPlayer()) >= 70));
        steps.add(step("picture of the first shot", 0, player -> {
            screenshot("story-opening-fire");
            check("the opening has lines on screen", ClientConversation.caption() != null, "no caption");
            command("summon wallis_ultimate:zombie ~6 ~ ~ {Tags:[\"wu_smoke_zombie\"]}");
        }));
        Vec3[] zombieAt = new Vec3[1];
        float[] health = new float[1];
        steps.add(step("a zombie turns up and something hits the player", 20, player -> {
            Entity zombie = taggedOnServer("wu_smoke_zombie");
            zombieAt[0] = zombie == null ? null : zombie.position();
            health[0] = player.getHealth();
            command("damage @s 4 minecraft:magic");
        }));
        steps.add(await("the porch shot", 300, player -> OpeningCutscene.tick(serverPlayer()) >= 170));
        steps.add(step("check the cutscene's promises", 0, player -> {
            screenshot("story-opening-porch");
            check("nobody in a cutscene can be hurt", player.getHealth() >= health[0], "health " + health[0] + " to " + player.getHealth());
            Entity zombie = taggedOnServer("wu_smoke_zombie");
            boolean still = zombieAt[0] != null && zombie != null && zombie.position().distanceTo(zombieAt[0]) < 0.6;
            check("enemies near a cutscene hold still", still, zombie == null ? "no zombie" : "moved " + zombie.position().distanceTo(zombieAt[0]));
            if (zombie != null) {
                zombie.discard();
            }
            // That was the point; from here on a zombie shouldn't get in the way of the rest of the test.
            command("effect give @s minecraft:resistance infinite 4 true");
        }));
        steps.add(await("Darryl's shot", 300, player -> OpeningCutscene.tick(serverPlayer()) >= 340));
        steps.add(step("picture of Darryl", 0, player -> {
            screenshot("story-opening-darryl");
            check("Darryl walks up", actor() != null, "no actor on the server");
        }));
        steps.add(await("Lurleen's shot", 300, player -> OpeningCutscene.tick(serverPlayer()) >= 470));
        steps.add(step("picture of Lurleen in the opening", 0, player -> {
            screenshot("story-opening-lurleen");
            check("the shot took Darryl out", actor() == null, "still there");
        }));
        steps.add(await("the first conversation starts by itself", 300, player -> ClientConversation.current() != null));
    }

    private static void firstConversation(List<ClientSmokeTest.Step> steps) {
        steps.add(step("check the end of the opening", 5, player -> {
            check("after the opening the camera is back", !CameraManager.INSTANCE.isActive(), "still active");
            check("Lurleen speaks first", ClientConversation.current() != null && ClientConversation.current().textKey().endsWith("lurleen_intro.start"),
                    String.valueOf(ClientConversation.current()));
            check("the screen opens for the one who chooses", Minecraft.getInstance().screen instanceof ConversationScreen, "screen " + Minecraft.getInstance().screen);
            check("a player alone is the one picked to choose", ClientConversation.current() != null && ClientConversation.current().youChoose(), "");
            check("the world keeps running behind a conversation", !Minecraft.getInstance().isPaused(), "paused");
        }));
        steps.add(step("picture of the first reply choice", 2, player -> {
            screenshot("story-conversation");
            String text = renderedText(Component.translatable(ClientConversation.current().textKey()));
            check("swearing is on by default", text.contains("dipshit") && !CleanLanguage.isOn(), text);
            check("the replies are buttons", buttons().size() == 3 + BUTTONS_WITH_OPTION, buttons().size() + " buttons");
        }));
        steps.add(step("switch to clean language", 4, player -> {
            buttons().get(0).onPress();
        }));
        steps.add(step("picture of clean language", 2, player -> {
            screenshot("story-clean-language");
            String text = renderedText(Component.translatable(ClientConversation.current().textKey()));
            check("the clean-language option replaces swear words with symbols", !text.contains("dipshit") && text.contains("#@!$&*#"), text);
            check("the option reaches quest text too", !renderedText(Component.translatable("story." + WallisUltimate.MOD_ID + ".quest.zombies.text1")).contains("shit"), "");
            buttons().get(0).onPress();
        }));
        steps.add(step("back to swearing", 4, player -> {
            String text = renderedText(Component.translatable(ClientConversation.current().textKey()));
            check("the option can be switched back", text.contains("dipshit"), text);
            buttons().get(1).onPress();
        }));
        steps.add(await("Lurleen answers", 60, player -> !ClientConversation.current().textKey().endsWith("lurleen_intro.start")));
        steps.add(step("check that the reply counted", 2, player -> {
            check("clicking a reply moves the talk on", ClientConversation.current().textKey().endsWith("lurleen_intro.better"), ClientConversation.current().textKey());
            screenshot("story-next-line");
        }));
        steps.add(drive("the rest of the first talk", 0));
        steps.add(await("the list and the rewards arrive", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_INTRO) && player.getInventory().countItem(Items.RED_BED) > 0));
        steps.add(step("check the first talk's results", 5, player -> {
            check("the first talk finishes the first quest", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_INTRO), "");
            check("Lurleen hands out her list (the quest book)", hasList(player), describe(player));
            check("the first quest gives every player a bed", player.getInventory().countItem(Items.RED_BED) == 1, describe(player));
            check("no conversation is left open", ClientConversation.current() == null && Minecraft.getInstance().screen == null, "");
            command("ftbquests open_book 5A11000000000300");
        }));
        steps.add(await("the quest book opens", 100, player -> Minecraft.getInstance().screen != null));
        steps.add(step("let the book draw", 20, player -> {
        }));
        steps.add(step("picture of the quest book", 2, player -> screenshot("story-quests-start")));
        steps.add(step("close the book", 10, player -> Minecraft.getInstance().setScreen(null)));
    }

    private static boolean hasList(LocalPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == dev.ftb.mods.ftbquests.item.FTBQuestsItems.BOOK.get()) {
                return stack.getHoverName().getString().equals("Lurleen's List");
            }
        }
        return false;
    }

    private static void lookAtLurleen(List<ClientSmokeTest.Step> steps) {
        steps.add(step("stand in front of Lurleen", 30, player -> {
            Entity lurleen = lurleen(true);
            command("gamemode spectator");
            command(String.format("tp @s %.2f %.2f %.2f 0 8", lurleen.getX(), lurleen.getY() + 0.2, lurleen.getZ() - 2.6));
        }));
        steps.add(step("picture of Lurleen from the front", 2, player -> screenshot("story-lurleen-front")));
        steps.add(step("walk behind her", 20, player -> {
            Entity lurleen = lurleen(true);
            command(String.format("tp @s %.2f %.2f %.2f 180 8", lurleen.getX(), lurleen.getY() + 0.2, lurleen.getZ() + 2.6));
        }));
        steps.add(step("picture of Lurleen from behind", 2, player -> screenshot("story-lurleen-back")));
        steps.add(step("back in front of her", 10, player -> {
            Entity lurleen = lurleen(true);
            command("gamemode survival");
            command(String.format("tp @s %.2f %.2f %.2f 0 0", lurleen.getX(), lurleen.getY(), lurleen.getZ() - 2.0));
        }));
        steps.add(step("check Lurleen", 5, player -> {
            Entity lurleen = clientEntity(lurleen(true));
            check("Lurleen goes by her name", lurleen != null && lurleen.getDisplayName().getString().equals(LURLEEN_NAME),
                    lurleen == null ? "no client entity" : lurleen.getDisplayName().getString());
            check("she's an Easy NPC", lurleen(true).getType().builtInRegistryHolder().key().location().getNamespace().equals("easy_npc"), "");
        }));
        steps.add(talkTo("what Lurleen says next", () -> lurleen(true)));
        steps.add(await("she talks", 60, player -> ClientConversation.current() != null));
        steps.add(step("check her topic", 0, player -> {
            check("Lurleen talks about what's next on the list", ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_swing"),
                    String.valueOf(ClientConversation.current()));
            screenshot("story-lurleen-talk");
        }));
        steps.add(drive("that talk", 0));
    }

    private static void firstQuests(List<ClientSmokeTest.Step> steps) {
        steps.add(step("make a stone sword", 5, player -> command("give @s minecraft:stone_sword")));
        steps.add(await("Somethin' to Swing finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_SWING)));
        steps.add(step("check the weapons stage", 10, player -> {
            check("the quest finishes", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_SWING), "");
            check("the first quest unlocks the Scavenger weapons for the team", StageStates.isUnlockedIndividual("scavenger_weapons", serverPlayer()), "");
        }));
        steps.add(step("let the sun go down, with no home yet", 40, player -> command("time set 11500")));
        steps.add(step("check the dusk warning", 5, player -> {
            check("Lurleen warns a team with no home at dusk", ClientConversation.caption() != null && ClientConversation.caption().textKey().endsWith("transmission.dusk"),
                    String.valueOf(ClientConversation.caption()));
            command("time set 1000");
        }));
        BlockPos[] bed = new BlockPos[1];
        steps.add(step("go somewhere to live", 20, player -> {
            BlockPos cabin = sites().cabin();
            bed[0] = cabin.offset(-40, 30, 13);
            command(String.format("fill %d %d %d %d %d %d minecraft:smooth_stone", bed[0].getX() - 6, bed[0].getY() - 1, bed[0].getZ() - 6, bed[0].getX() + 6, bed[0].getY() - 1, bed[0].getZ() + 6));
            command(String.format("tp @s %d %d %d 0 0", bed[0].getX() - 2, bed[0].getY(), bed[0].getZ()));
            moveToHotbar(player, Items.RED_BED);
        }));
        steps.add(step("put the bed down", 10, player -> placeBed(player, bed[0])));
        steps.add(await("Create First Home finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOME)));
        steps.add(step("check the first home", 20, player -> {
            check("placing a bed creates the first home", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOME)
                    && bed[0].equals(StoryState.get(server()).home(Teams.teamKey(serverPlayer()))), String.valueOf(StoryState.get(server()).home(Teams.teamKey(serverPlayer()))));
            check("the bed stands", player.level().getBlockState(bed[0]).getBlock() instanceof BedBlock, "");
            check("Lurleen moves into the first home", lurleen(false) != null, "no Lurleen at home");
            check("she says so over the radio", ClientConversation.caption() != null, "no caption");
            screenshot("story-first-home");
        }));
        steps.add(talkTo("Lurleen at home", () -> lurleen(false)));
        steps.add(await("she talks at home", 60, player -> ClientConversation.current() != null));
        steps.add(step("check what she says at home", 0, player -> {
            check("Lurleen reacts to the new home", ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_moved_in"), String.valueOf(ClientConversation.current()));
            screenshot("story-lurleen-home");
        }));
        steps.add(drive("that talk too", 0));

        steps.add(step("cook something", 5, player -> command("give @s minecraft:baked_potato")));
        steps.add(await("Hot Meal finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_DINNER)));
        steps.add(step("check cooking", 5, player -> {
            check("Lurleen's early quest unlocks the Cooking stage (SURV-008)", StageStates.isUnlockedIndividual("cooking", serverPlayer()), "");
        }));

        steps.add(step("line up a zombie", 10, player -> {
            command("effect give @s minecraft:strength 30 200 true");
            command("summon wallis_ultimate:zombie ~ ~ ~1.5 {NoAI:1b,Tags:[\"wu_smoke_target\"]}");
        }));
        long killTask = 0x5A11000000000501L;
        steps.add(await("the zombie is there", 40, player -> clientEntity(taggedOnServer("wu_smoke_target")) != null));
        steps.add(step("hit the zombie", 10, player -> Minecraft.getInstance().gameMode.attack(player, clientEntity(taggedOnServer("wu_smoke_target")))));
        steps.add(step("check the kill", 5, player -> {
            Task task = ServerQuestFile.INSTANCE.getTask(killTask);
            check("a killed zombie counts toward the quest", StoryQuests.data(serverPlayer()).getProgress(task) == 1, "progress " + StoryQuests.data(serverPlayer()).getProgress(task));
            command("kill @e[tag=wu_smoke_target]");
            command("ftbquests change_progress @s complete 5A11000000000500");
        }));
        steps.add(await("Darryl's Friends finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_ZOMBIES)));
        steps.add(step("check the bandages", 5, player -> {
            check("the zombie quest pays out", player.getInventory().countItem(ModItems.BANDAGE.get()) >= 3, describe(player));
        }));
    }

    private static void night(List<ClientSmokeTest.Step> steps) {
        // The game only notices it's night on its next tick, so a bed clicked right after this would still say it's day.
        steps.add(step("make it night", 20, player -> command("time set 14000")));
        steps.add(step("lie down", 5, player -> {
            BlockPos bed = StoryState.get(server()).home(Teams.teamKey(serverPlayer()));
            Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(bed), Direction.UP, bed, false));
        }));
        steps.add(await("the player falls asleep", 100, LocalPlayer::isSleeping));
        steps.add(await("the night quest finishes", 200, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_NIGHT)));
        steps.add(await("morning comes", 600, player -> !player.isSleeping()));
        steps.add(step("check the foot", 10, player -> {
            check("sleeping finishes the night quest (ENEMY-003)", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_NIGHT), "");
            check("every player gets Granny's Rabbit Foot", player.getInventory().countItem(ModItems.GRANNYS_RABBIT_FOOT.get()) == 1, describe(player));
        }));
        steps.add(step("go up to Lurleen", 10, player -> {
            Entity lurleen = lurleen(false);
            command(String.format("tp @s %.2f %.2f %.2f", lurleen.getX() + 1.5, lurleen.getY(), lurleen.getZ()));
        }));
        steps.add(talkTo("Lurleen about the foot", () -> lurleen(false)));
        steps.add(await("she talks about the foot", 60, player -> ClientConversation.current() != null));
        steps.add(step("check the foot talk", 0, player -> check("Lurleen tells about Granny",
                ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_foot"), String.valueOf(ClientConversation.current()))));
        steps.add(drive("the Granny talk", 0));
    }

    private static void radioAndSafeSpot(List<ClientSmokeTest.Step> steps) {
        steps.add(step("go to the radio in the cabin", 20, player -> {
            BlockPos cabin = sites().cabin();
            command(String.format("tp @s %d %d %d 0 0", cabin.getX() + 15, cabin.getY() + 1, cabin.getZ() + 16));
        }));
        steps.add(step("switch the radio on", 5, player -> {
            BlockPos radio = sites().cabin().offset(15, 2, 18);
            Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(radio), Direction.NORTH, radio, false));
        }));
        steps.add(await("the radio crackles", 60, player -> ClientConversation.current() != null));
        steps.add(step("listen to the radio", 2, player -> {
            check("the radio plays Dusty Holler", ClientConversation.current() != null && ClientConversation.current().textKey().contains("radio_holler"), String.valueOf(ClientConversation.current()));
            ClientConversation.next();
        }));
        steps.add(await("Dusty talks", 60, player -> ClientConversation.current() != null && ClientConversation.current().textKey().contains(".back")));
        steps.add(step("picture of Dusty on the radio", 0, player -> screenshot("story-radio")));
        steps.add(drive("the broadcast", 0));
        steps.add(await("The Holler Hour finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_RADIO)));
        steps.add(step("go to Camp Tarp", 60, player -> {
            check("listening finishes the radio quest", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_RADIO), "");
            BlockPos arrival = campTarp().arrival();
            command(String.format("tp @s %.2f %d %.2f 180 0", arrival.getX() + 0.5, arrival.getY(), arrival.getZ() + 0.5));
        }));
        steps.add(await("Folks With Walls finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_SAFE)));
        steps.add(step("check the Iron stage", 0, player -> {
            check("finding a safe spot unlocks the Iron stage (PROG-002)", StageStates.isUnlockedIndividual("iron", serverPlayer()), "");
            check("Lurleen comments over the radio", StoryState.get(server()).hasFlag(Teams.teamKey(serverPlayer()), StoryEvents.FLAG_TOLD_SAFE_SPOT), "she didn't");
            screenshot("story-transmission");
            command("give @s minecraft:iron_ingot 8");
        }));
        steps.add(await("Eight Bars finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_SCRAP)));
        steps.add(step("check the frying pan", 5, player -> check("eight iron bars earn the frying pan", player.getInventory().contains(new ItemStack(ModItems.FRYING_PAN.get())), describe(player))));
    }

    private static void carl(List<ClientSmokeTest.Step> steps) {
        steps.add(await("Carl is at Camp Tarp", 100, player -> taggedOnServer(StoryNpcs.TAG_CARL) != null));
        steps.add(step("go up to Carl", 30, player -> {
            Entity carl = taggedOnServer(StoryNpcs.TAG_CARL);
            check("Conspiracy Carl is at Camp Tarp", carl != null, "no Carl");
            command("gamemode spectator");
            command(String.format("tp @s %.2f %.2f %.2f 0 8", carl.getX(), carl.getY() + 0.2, carl.getZ() - 2.6));
        }));
        steps.add(step("picture of Carl", 2, player -> screenshot("story-carl")));
        steps.add(step("stand by Carl", 10, player -> {
            Entity carl = taggedOnServer(StoryNpcs.TAG_CARL);
            command("gamemode survival");
            command(String.format("tp @s %.2f %.2f %.2f 0 0", carl.getX(), carl.getY(), carl.getZ() - 2.0));
        }));
        steps.add(talkTo("Carl", () -> taggedOnServer(StoryNpcs.TAG_CARL)));
        steps.add(await("Carl talks", 60, player -> ClientConversation.current() != null));
        steps.add(step("picture of talking to Carl", 0, player -> {
            check("Carl questions you first", ClientConversation.current() != null && ClientConversation.current().textKey().contains("carl_first"), String.valueOf(ClientConversation.current()));
            screenshot("story-carl-talk");
        }));
        steps.add(drive("Carl's rant", 0));
        steps.add(await("Turn It Down, Carl finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_CARL)));
        steps.add(step("check Carl's quest", 0, player -> {
            check("talking to Carl finishes his quest", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_CARL), "");
            check("a reply can be a story choice the team remembers", StoryState.get(server()).hasFlag(Teams.teamKey(serverPlayer()), ChapterOneTalks.BELIEVED_CARL), "");
        }));
    }

    private static void boss(List<ClientSmokeTest.Step> steps) {
        // Her chunk unloaded while the player was away, so she can't be looked up yet: she's where the cabin says.
        steps.add(step("back to the cabin", 60, player -> {
            BlockPos cabin = sites().cabin();
            command(String.format("tp @s %d %d %d 0 0", cabin.getX() + StoryNpcs.LURLEEN_CABIN_X + 1, cabin.getY(), cabin.getZ() + StoryNpcs.LURLEEN_CABIN_Z - 1));
        }));
        steps.add(step("open the quest book at the boss quest", 0, player -> {
            check("everything before the boss is done, so the ready quest is open", StoryQuests.open(serverPlayer(), StoryQuests.QUEST_READY), "");
            command("ftbquests open_book 5A11000000000B00");
        }));
        steps.add(await("the book opens", 100, player -> Minecraft.getInstance().screen != null));
        steps.add(step("picture of the ready quest", 10, player -> screenshot("story-quests-ready")));
        steps.add(step("close the book again", 5, player -> Minecraft.getInstance().setScreen(null)));
        steps.add(talkTo("Lurleen at the cabin", () -> lurleen(true)));
        steps.add(await("she asks if you're ready", 60, player -> ClientConversation.current() != null));
        steps.add(step("say not yet", 10, player -> {
            check("at the cabin, Lurleen asks if the team is ready", ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_ready"), String.valueOf(ClientConversation.current()));
            screenshot("story-ready");
            ClientConversation.reply(1);
        }));
        steps.add(drive("the wait line", 0));
        steps.add(step("check waiting", 5, player -> check("saying not yet doesn't set anything off", !StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_READY), "")));
        steps.add(talkTo("Lurleen again", () -> lurleen(true)));
        steps.add(await("she asks again", 60, player -> ClientConversation.current() != null));
        steps.add(step("say let's kill it", 5, player -> ClientConversation.reply(0)));
        steps.add(drive("the last line before the fight", 0));
        steps.add(await("the boss quest fires", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_READY)));
        steps.add(step("check the moment the boss quest fires", 10, player -> {
            check("the team's choice fires the boss quest", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_READY), "");
            check("the Hog Daddy quest opens and waits for the kill", StoryQuests.open(serverPlayer(), StoryQuests.QUEST_HOG), "");
            check("the quest runs the boss's command once, with permission level 2", BOSS_COMMANDS.size() == 1, BOSS_COMMANDS.toString());
            check("a boss command that doesn't exist yet stays silent", !chatContains("Unknown or incomplete command") && !chatContains("wallis_boss"), "");
            check("the team is still in tier 1", Tiers.current(serverPlayer()).order() == 1, String.valueOf(Tiers.current(serverPlayer())));
            command("ftbquests open_book 5A11000000000C00");
        }));
        steps.add(await("the book opens at Hog Daddy", 100, player -> Minecraft.getInstance().screen != null));
        steps.add(step("picture of the Hog Daddy quest, on an entity that doesn't exist here", 10, player -> {
            screenshot("story-quests-hog");
            log("hog quest open in the book");
        }));
        steps.add(step("close the book for the last time", 5, player -> Minecraft.getInstance().setScreen(null)));
        steps.add(talkTo("Lurleen after a failed try", () -> lurleen(true)));
        steps.add(await("she offers another go", 60, player -> ClientConversation.current() != null));
        steps.add(step("check another go", 2, player -> check("while the boss lives, Lurleen offers another go", ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_again"), String.valueOf(ClientConversation.current()))));
        steps.add(drive("another go", 0));
        // The boss's own branch isn't here, so its death is the part that's simulated. Everything after it is real.
        steps.add(step("Hog Daddy dies", 5, player -> StoryEvents.bossDied(serverPlayer().serverLevel(), serverPlayer())));
        steps.add(await("the last quest finishes", 100, player -> StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOG)));
        steps.add(await("the chapter's last talk starts", 60, player -> ClientConversation.current() != null));
        steps.add(step("check the milestone", 0, player -> {
            check("the boss's death finishes the last quest, whatever killed it", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOG), "");
            check("beating the milestone advances the team to tier 2 (PROG-004)", Tiers.current(serverPlayer()).order() == 2, String.valueOf(Tiers.current(serverPlayer())));
            check("the Settler stage is unlocked", StageStates.isUnlockedIndividual("settler", serverPlayer()), "");
            check("every player gets Earth's one shulker box (WORLD-007)", player.getInventory().countItem(Items.SHULKER_BOX) == 1, describe(player));
            check("the chapter ends with Lurleen's last talk", ClientConversation.current() != null && ClientConversation.current().textKey().contains("lurleen_ending"), String.valueOf(ClientConversation.current()));
            screenshot("story-ending");
        }));
        steps.add(drive("the chapter's end", 0));
        steps.add(step("check the end of the chapter", 5, player -> check("the cliffhanger plays to its end", StoryState.get(server()).hasFlag(Teams.teamKey(serverPlayer()), ChapterOneTalks.HEARD_ENDING), "")));
    }

    /** PROG-006 with real teams: the story carries over into a party, and a leaver keeps a copy instead of starting over. */
    private static void teams(List<ClientSmokeTest.Step> steps) {
        steps.add(step("create a party", 20, player -> command("ftbteams party create wu_smoke_party")));
        steps.add(step("check the party", 0, player -> {
            check("the party takes over the story", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOG)
                    && StoryState.get(server()).hasFlag(Teams.teamKey(serverPlayer()), ChapterOneTalks.HEARD_ENDING), "team " + Teams.teamKey(serverPlayer()));
            command("ftbteams party leave");
        }));
        steps.add(step("wait for leaving", 20, player -> {
        }));
        steps.add(step("check leaving", 5, player -> {
            check("a player who leaves keeps a copy of the quest progress", StoryQuests.completed(serverPlayer(), StoryQuests.QUEST_HOG), "team " + Teams.teamKey(serverPlayer()));
            check("and of the story", StoryState.get(server()).hasFlag(Teams.teamKey(serverPlayer()), ChapterOneTalks.HEARD_ENDING), "");
            check("and of the tier", Tiers.current(serverPlayer()).order() == 2, String.valueOf(Tiers.current(serverPlayer())));
        }));
    }

    /** STORY-010: alone, one vote is everyone's vote. And the two other things she says over the radio. */
    private static void skip(List<ClientSmokeTest.Step> steps) {
        steps.add(step("the horde code makes Lurleen speak", 5, player -> Transmissions.hordeStarting(serverPlayer())));
        steps.add(await("her warning arrives", 40, player -> ClientConversation.caption() != null && ClientConversation.caption().textKey().endsWith("transmission.horde_starting")));
        steps.add(step("check the warning, then die", 20, player -> {
            check("horde nights can make Lurleen speak (ENEMY-006)", ClientConversation.caption() != null && ClientConversation.caption().textKey().endsWith("transmission.horde_starting"),
                    String.valueOf(ClientConversation.caption()));
            screenshot("story-horde-warning");
            command("kill @s");
        }));
        steps.add(step("make sure of it", 20, player -> {
            if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
                command("kill @s");
            }
        }));
        steps.add(await("the death screen comes up", 100, player -> Minecraft.getInstance().screen instanceof DeathScreen));
        steps.add(step("come back", 5, player -> {
            player.respawn();
            Minecraft.getInstance().setScreen(null);
        }));
        steps.add(await("she has something to say about it", 60, player -> ClientConversation.caption() != null && ClientConversation.caption().textKey().endsWith("transmission.respawn")));
        steps.add(step("check the respawn line", 10, player -> {
            check("after a first death Lurleen points at the grave", ClientConversation.caption() != null && ClientConversation.caption().textKey().endsWith("transmission.respawn"),
                    String.valueOf(ClientConversation.caption()));
            command("effect give @s minecraft:resistance infinite 4 true");
        }));
        steps.add(step("play the opening again", 5, player -> command("wallis_story opening")));
        steps.add(await("it starts again", 200, player -> CameraManager.INSTANCE.isActive() && OpeningCutscene.tick(serverPlayer()) > 10));
        steps.add(step("vote to skip", 5, player -> CameraManager.INSTANCE.requestExit(ExitReason.USER_SKIP)));
        steps.add(await("it ends", 100, player -> !OpeningCutscene.isWatching(serverPlayer())));
        steps.add(step("check skipping", 5, player -> {
            check("a cutscene ends when everyone watching votes to skip", !OpeningCutscene.isWatching(serverPlayer()) && OpeningCutscene.tick(serverPlayer()) < 0, "");
            check("the camera is back", !CameraManager.INSTANCE.isActive(), "");
        }));
    }

    /** Plays a conversation to its end: the given reply every time one is asked for, Next for the rest. */
    private static ClientSmokeTest.Step drive(String name, int reply) {
        boolean[] started = new boolean[1];
        int[] ticks = new int[1];
        return await(name + " plays out", 1200, player -> {
            ConversationPacket conversation = ClientConversation.current();
            if (conversation == null) {
                return started[0] || ++ticks[0] > 40;
            }
            started[0] = true;
            if (++ticks[0] % 4 == 0) {
                if (conversation.youChoose()) {
                    ClientConversation.reply(Math.min(reply, conversation.replyKeys().size() - 1));
                } else if (conversation.youLead()) {
                    ClientConversation.next();
                }
            }
            return false;
        });
    }

    private interface EntitySource {
        Entity get();
    }

    private static ClientSmokeTest.Step talkTo(String name, EntitySource npc) {
        return step("talk to " + name, 4, player -> {
            Entity target = clientEntity(npc.get());
            if (target == null) {
                check("there is someone to talk to: " + name, false, "no client entity for " + npc.get());
                return;
            }
            Minecraft.getInstance().gameMode.interact(player, target, InteractionHand.MAIN_HAND);
        });
    }

    private static List<Button> buttons() {
        return Minecraft.getInstance().screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private static void moveToHotbar(LocalPlayer player, net.minecraft.world.item.Item item) {
        int from = ClientSmokeTest.find(player, item, 0, 36);
        if (from >= 9) {
            ClientSmokeTest.click(player, ClientSmokeTest.inventoryMenuSlot(from), 0, net.minecraft.world.inventory.ClickType.SWAP);
        }
    }

    private static void placeBed(LocalPlayer player, BlockPos spot) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.RED_BED)) {
                player.getInventory().selected = i;
                break;
            }
        }
        BlockPos floor = spot.below();
        Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(floor).add(0.0, 0.5, 0.0), Direction.UP, floor, false));
    }

    private static MinecraftServer server() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    private static ServerPlayer serverPlayer() {
        return server().getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
    }

    private static DistrictSites sites() {
        return DistrictSites.get(server().overworld());
    }

    private static SafeSpot campTarp() {
        return sites().spot("camp_tarp").orElseThrow();
    }

    @Nullable
    private static Entity clientEntity(@Nullable Entity serverEntity) {
        return serverEntity == null ? null : Minecraft.getInstance().level.getEntity(serverEntity.getId());
    }

    @Nullable
    private static Entity findOnServer(Predicate<Entity> test) {
        ServerLevel level = server().overworld();
        for (Entity entity : level.getAllEntities()) {
            if (test.test(entity)) {
                return entity;
            }
        }
        return null;
    }

    @Nullable
    private static Entity taggedOnServer(String tag) {
        return findOnServer(entity -> entity.getTags().contains(tag));
    }

    /** The zombie playing Darryl, while he's on stage. */
    @Nullable
    private static Entity actor() {
        return findOnServer(entity -> entity instanceof Monster && entity.getTags().contains(OpeningCutscene.TAG_ACTOR));
    }

    @Nullable
    private static Entity lurleen(boolean atCabin) {
        return findOnServer(entity -> entity.getTags().contains(StoryNpcs.TAG_LURLEEN) && entity.getTags().contains(StoryNpcs.TAG_LURLEEN_CABIN) == atCabin);
    }
}
