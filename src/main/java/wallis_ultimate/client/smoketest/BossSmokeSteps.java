package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.walk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.boss.HogDaddyFight;
import wallis_ultimate.boss.HogDaddyProgress;
import wallis_ultimate.boss.HogFarm;
import wallis_ultimate.entity.GroundMark;
import wallis_ultimate.entity.HogDaddy;
import wallis_ultimate.entity.MudRing;
import wallis_ultimate.entity.ZombiePiglet;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.RollRequestPacket;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.survival.Downed;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.HogFarmBuild;

/**
 * The Chapter 1 boss in a real client (ENEMY-011), from the command to his death. First a look at the farm and at him.
 * Then a lost fight: going down makes him lose interest and ends it, and the bell at the gate starts it again. Then the
 * real thing: the wall, a run through the corn, the barn, a charge into a post, hits on his back, half health, mud rings,
 * a post that breaks, and the kill. Getting him to half health and nearly dead takes a real team minutes, so those are
 * set directly; every hit that decides something is a real swing.
 */
final class BossSmokeSteps {
    /** He walks about 10 blocks before he charges. From further than this he's still 4 blocks off by then, and his line is nearly straight. */
    private static final double BAIT_MIN_DISTANCE = 16.0;
    /** His width plus a bit, each way from the middle of the line. */
    private static final double CHARGE_CLEARANCE = 2.0;
    private static final int TROUGH_LENGTH = 8;

    private static float healthBeforeHit;
    private static HogDaddy.Action lastBaitAction;
    private static Vec3 baitSpot;
    private static final double STILL_ON_THE_LINE = 1.5;

    private BossSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        List<ClientSmokeTest.Step> steps = new ArrayList<>();
        Vec3[] home = new Vec3[1];
        steps.add(step("get ready for the boss", 5, player -> {
            home[0] = player.position();
            command("difficulty normal");
            command("time set noon");
            command("weather clear");
            command("gamerule doMobSpawning false");
            command("gamerule sendCommandFeedback false");
            check("the farm was built when the world was created", farm() != null, "");
        }));
        lookAtTheFarm(steps);
        lookAtHim(steps);
        lostFight(steps);
        realFight(steps);
        steps.add(step("back to where the player was", 20, player -> {
            command("difficulty peaceful");
            command("gamerule doMobSpawning true");
            command("gamerule sendCommandFeedback true");
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f", home[0].x, home[0].y, home[0].z));
        }));
        return steps;
    }

    /** Where to stand, relative to the farm's corner, and which way to look. */
    private static void lookAtTheFarm(List<ClientSmokeTest.Step> steps) {
        steps.add(step("hide the HUD for the pictures", 2, player -> Minecraft.getInstance().options.hideGui = true));
        look(steps, "farm-air", 120, -20.0, 45.0, -20.0, -45.0F, 35.0F);
        look(steps, "farm-pen", 30, 8.0, 4.0, 3.0, -55.0F, 14.0F);
        look(steps, "farm-barn", 30, 47.5, 3.0, 74.0, 180.0F, 8.0F);
        look(steps, "farm-corn", 30, 6.0, 3.0, 87.5, -90.0F, 12.0F);
        look(steps, "farm-cabin", 30, 38.0, 3.0, 87.5, -90.0F, 8.0F);
        look(steps, "farm-gate", 30, 70.0, 3.0, 18.5, 90.0F, 5.0F);
        steps.add(step("show the HUD again", 2, player -> Minecraft.getInstance().options.hideGui = false));
    }

    private static void look(List<ClientSmokeTest.Step> steps, String name, int wait, double x, double y, double z, float yaw, float pitch) {
        steps.add(step("go to " + name, wait, player -> {
            BlockPos origin = farm().origin();
            command("gamemode spectator");
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", origin.getX() + x, origin.getY() + y, origin.getZ() + z, yaw, pitch));
        }));
        steps.add(step("picture of " + name, 2, player -> {
            screenshot(name);
            command("gamemode survival");
        }));
    }

    /** Q-150: him and a piglet close up, on a platform in the sky. They can't move, so this shows the pose. */
    private static void lookAtHim(List<ClientSmokeTest.Step> steps) {
        steps.add(step("line him up with a piglet", 40, player -> {
            int x = player.getBlockX() + 60;
            int y = 240;
            int z = player.getBlockZ();
            command(String.format("fill %d %d %d %d %d %d minecraft:grass_block", x - 8, y - 1, z - 8, x + 8, y - 1, z + 8));
            command(String.format("summon wallis_ultimate:hog_daddy %d %d %d {NoAI:1b,Rotation:[0f,0f]}", x - 2, y, z));
            command(String.format("summon wallis_ultimate:zombie_piglet %d %d %d {NoAI:1b,Rotation:[0f,0f]}", x + 3, y, z));
            command(String.format(Locale.ROOT, "tp @s %.2f %d %.2f 180 4", x + 0.5, y, z + 7.5));
        }));
        steps.add(step("picture of him", 2, player -> screenshot("hog-lineup")));
        steps.add(step("from the other side", 10, player -> {
            // He stands 7 blocks north of where the last picture was taken, and this is 6 blocks north of him.
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 0 4", player.getX(), player.getY(), player.getZ() - 13.0));
        }));
        steps.add(step("picture of his back", 2, player -> screenshot("hog-lineup-back")));
        steps.add(step("clear them away", 5, player -> {
            command("kill @e[type=wallis_ultimate:hog_daddy]");
            command("kill @e[type=wallis_ultimate:zombie_piglet]");
        }));
    }

    /** SURV-001, ENEMY-011: going down makes him lose interest and ends the fight, and the bell at the gate starts it again. */
    private static void lostFight(List<ClientSmokeTest.Step> steps) {
        steps.add(step("the quest's command starts the fight", 5, player -> command("wallis_boss hog_daddy start")));
        steps.add(await("the fight starts", 60, player -> HogDaddyFight.current() != null));
        steps.add(await("the player arrives in the cabin", 60, player -> farm().contains(player.getX(), player.getZ())));
        steps.add(step("check the start", 20, player -> {
            check("the command starts the fight", HogDaddyFight.current() != null, "");
            BlockPos start = farm().at(HogFarmBuild.CHASE_START);
            check("it brings the player into the cabin", player.blockPosition().distManhattan(start) <= 3, "at " + player.blockPosition() + ", cabin " + start);
            check("the gate has shut", level().getBlockState(farm().at(HogFarmBuild.GATE_FROM)).is(ModBlocks.RIVETED_STEEL.get()),
                    String.valueOf(level().getBlockState(farm().at(HogFarmBuild.GATE_FROM))));
            check("he waits outside the wall", fight().boss().position().distanceTo(farm().standingAt(HogFarmBuild.BOSS_START)) < 2.0,
                    String.valueOf(fight().boss().position()));
            command("tp @s ~ ~ ~ -90 0");
        }));
        steps.add(step("picture of the cabin from inside", 2, player -> screenshot("hog-cabin")));
        steps.add(await("he comes through the wall", HogDaddyFight.INTRO_TICKS + 40, player -> fight().stage() == HogDaddyFight.Stage.CHASE));
        steps.add(step("check the wall", 4, player -> {
            check("he comes through the wall", fight().stage() == HogDaddyFight.Stage.CHASE, String.valueOf(fight().stage()));
            check("the wall has a hole in it", !level().getBlockState(farm().at(HogFarmBuild.WALL_FROM)).is(Blocks.SPRUCE_PLANKS)
                    && level().getBlockState(farm().at(HogFarmBuild.WALL_FROM)).isAir(), String.valueOf(level().getBlockState(farm().at(HogFarmBuild.WALL_FROM))));
        }));
        steps.add(step("picture of the hole he made", 2, player -> {
            screenshot("hog-burst");
            // Backing off to the door, to have a line to look at.
            Vec3 door = farm().standingAt(HogFarmBuild.CHASE_START);
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f -90 22", door.x - 7.0, door.y, door.z));
        }));
        steps.add(await("the first red line", 200, player -> marks(true) > 0));
        steps.add(step("up where the whole line shows", 5, player -> {
            check("a charge is announced by a red line", marks(true) > 0, "");
            HogDaddy boss = fight().boss();
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", boss.getX() - 16.0, boss.getY() + 6.0, boss.getZ(), boss.getX() - 6.0, boss.getY(), boss.getZ()));
        }));
        steps.add(step("picture of the red line", 2, player -> screenshot("hog-red-line")));
        steps.add(step("go down", 5, player -> server().execute(() -> Downed.goDown(serverPlayer(player)))));
        steps.add(await("he loses interest", 40, player -> Downed.isDowned(serverPlayer(player)) && (HogDaddyFight.current() == null || fight().boss().getTarget() == null)));
        steps.add(step("check he lost interest", 0, player -> check("he ignores a downed player",
                HogDaddyFight.current() == null || fight().boss().getTarget() == null, "still after " + (HogDaddyFight.current() == null ? null : fight().boss().getTarget()))));
        steps.add(await("the fight is lost", HogDaddyFight.WIPE_TICKS + 80, player -> HogDaddyFight.current() == null));
        steps.add(step("check the lost fight", 10, player -> {
            check("with nobody standing, the fight ends", HogDaddyFight.current() == null, "");
            check("the gate opens again", level().getBlockState(farm().at(HogFarmBuild.GATE_FROM)).isAir(), String.valueOf(level().getBlockState(farm().at(HogFarmBuild.GATE_FROM))));
            check("the wall comes back", !level().getBlockState(farm().at(HogFarmBuild.WALL_FROM)).isAir(), "");
            check("the team hasn't beaten him", !progress().hasBeaten(Teams.teamKey(serverPlayer(player))), "");
            server().execute(() -> Downed.revive(serverPlayer(player)));
        }));
        steps.add(step("go to the bell", 40, player -> {
            Vec3 bell = farm().standingAt(HogFarmBuild.BELL);
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f -90 10", bell.x - 2.0, bell.y, bell.z));
        }));
        steps.add(step("picture of the gate and the bell", 2, player -> screenshot("hog-bell")));
        steps.add(step("ring the bell", 5, player -> {
            BlockPos bell = farm().at(HogFarmBuild.BELL);
            Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(bell), Direction.WEST, bell, false));
        }));
        steps.add(await("the bell starts it again", 60, player -> HogDaddyFight.current() != null));
        steps.add(step("check the bell", 0, player -> check("after a lost fight, the bell at the gate starts it again", HogDaddyFight.current() != null, "")));
    }

    private static void realFight(List<ClientSmokeTest.Step> steps) {
        steps.add(await("he comes through the wall again", HogDaddyFight.INTRO_TICKS + 60, player -> fight().stage() == HogDaddyFight.Stage.CHASE));
        // A real run: out of the door, into the corn, looking west.
        Vec3[] runStart = new Vec3[1];
        steps.add(step("run for it", 45, player -> {
            runStart[0] = player.position();
            player.setYRot(90.0F);
            player.setXRot(0.0F);
            walk(40, 0);
        }));
        steps.add(step("check the run", 0, player -> {
            check("running gets the player through the door and into the corn", player.getX() < runStart[0].x - 6.0, "from " + runStart[0] + " to " + player.position());
            check("he comes after them", fight().boss().getX() < farm().at(HogFarmBuild.BOSS_START).getX() - 2.0, String.valueOf(fight().boss().position()));
            screenshot("hog-chase");
        }));
        steps.add(await("a bale comes down", HogDaddyFight.PIGLET_WAVE_TICKS, player -> marks(false) > 0));
        steps.add(step("check the bale", 3, player -> {
            // In the air or already down: the client can see the circle late, and a bale only falls for a second.
            boolean bale = !level().getEntitiesOfClass(FallingBlockEntity.class, serverPlayer(player).getBoundingBox().inflate(32.0)).isEmpty() || hayOnTheField();
            check("hay falls during the chase, with a red circle where it lands", marks(false) > 0 && bale, "marks: " + marks(false) + ", bale: " + bale);
            command("tp @s ~ ~ ~ facing entity @e[type=wallis_ultimate:ground_mark,limit=1,sort=nearest] feet");
        }));
        steps.add(step("picture of the bale", 2, player -> screenshot("hog-hay")));
        steps.add(step("the player gets to the barn", 2, player -> {
            Vec3 barn = farm().standingAt(HogFarmBuild.BARN_LINE);
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 0 0", barn.x, barn.y, barn.z - 6.0));
        }));
        steps.add(await("the chase ends", 40, player -> fight().stage() == HogDaddyFight.Stage.PEN));
        steps.add(step("check the end of the chase", 10, player -> {
            check("reaching the barn ends the chase", fight().stage() == HogDaddyFight.Stage.PEN && fight().boss().phase() == HogDaddy.Phase.PEN, String.valueOf(fight().stage()));
            check("he comes over the roof", fight().boss().action() == HogDaddy.Action.LEAP, String.valueOf(fight().boss().action()));
            // Far off with a clear view, so there's time for a roll even on a client with a slow frame.
            Vec3 at = farm().standingAt(HogFarmBuild.PEN_LANDING);
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", at.x - 18.0, at.y, at.z - 16.0, at.x, at.y + 3.0, at.z));
        }));
        steps.add(step("picture of the leap", 0, player -> screenshot("hog-leap")));
        steps.add(await("he lands in the pen", 80, player -> fight().boss().action() == HogDaddy.Action.STALK));
        // ENEMY-011, CMBT-002: a real roll, sideways out of the red line. He's looking along it, so a quarter turn from there.
        steps.add(await("he winds up his first charge in the pen", 200, player -> fight().boss().action() == HogDaddy.Action.WIND_UP));
        Vec3[] rollStart = new Vec3[1];
        // No teleport or picture before this: a picture stalls the client for up to a second, and the charge doesn't wait.
        steps.add(step("turn sideways to the line", 2, player -> {
            rollStart[0] = player.position();
            player.setYRot(fight().boss().getYRot() + 90.0F);
        }));
        // Like a player hitting the key until it works: the server refuses a roll unless it knows the player is on the ground.
        steps.add(await("the roll takes the player out of the way", 18, player -> {
            if (player.onGround()) {
                ModNetwork.CHANNEL.sendToServer(new RollRequestPacket());
            }
            return player.position().distanceTo(rollStart[0]) > 1.5;
        }));
        steps.add(await("the charge is over", 80, player -> fight().boss().action() != HogDaddy.Action.WIND_UP && fight().boss().action() != HogDaddy.Action.CHARGE));
        steps.add(step("check the roll", 2, player -> {
            check("a dodge roll gets the player out of the way of a charge", !fight().boss().chargeCaught(serverPlayer(player)),
                    "caught at " + player.position() + ", " + player.position().distanceTo(rollStart[0]) + " blocks from where the roll started");
        }));
        // On the next one nobody has to dodge: the line along the ground, then from above, in the open, ending at whatever will stop him.
        steps.add(await("he winds up another charge", 200, player -> fight().boss().action() == HogDaddy.Action.WIND_UP));
        steps.add(step("look down the line", 2, player -> command("tp @s ~ ~ ~ facing entity @e[type=wallis_ultimate:hog_daddy,limit=1] feet")));
        steps.add(step("picture of the line in the pen", 1, player -> screenshot("hog-pen-line")));
        steps.add(step("up above the line", 4, player -> {
            HogDaddy boss = fight().boss();
            Vec3 along = boss.position().add(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(6.0));
            command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing %.2f %.2f %.2f", along.x, boss.getY() + 16.0, along.z - 4.0, along.x, boss.getY(), along.z));
        }));
        steps.add(step("picture of the line from above", 2, player -> screenshot("hog-line-above")));
        // Or the next step would take this same wind-up for a new one, with the player still in the air.
        steps.add(await("that charge is over too", 80, player -> fight().boss().action() != HogDaddy.Action.WIND_UP && fight().boss().action() != HogDaddy.Action.CHARGE));
        steps.add(step("back down to the pen", 2, player -> lastBaitAction = null));
        steps.add(await("he winds up a charge at the player in front of a post", 200, player -> {
            bait(player);
            return fight().boss().action() == HogDaddy.Action.WIND_UP;
        }));
        steps.add(step("check the red line in the pen", 2, player -> check("in the pen too, a charge is announced by a red line", marks(true) > 0, String.valueOf(fight().boss().action()))));
        steps.add(await("he slams into the post", 60, player -> fight().boss().action() == HogDaddy.Action.STUNNED));
        steps.add(step("check the slam", 0, player -> {
            check("slamming into a post stuns him", fight().boss().action() == HogDaddy.Action.STUNNED, String.valueOf(fight().boss().action()));
            // Nobody steers this player, so a piglet can knock it off the line first. A GameTest covers the rule itself either way.
            boolean stayed = baitSpot != null && player.position().subtract(baitSpot).horizontalDistance() < STILL_ON_THE_LINE;
            log("at the slam the player was " + (stayed ? "still on the line" : "knocked off the line") + ", caught: " + fight().boss().chargeCaught(serverPlayer(player)));
            check("staying on the line gets you hit", !stayed || fight().boss().chargeCaught(serverPlayer(player)), "not caught at " + player.position());
            check("in phase 2 the post holds", stunBlocksLeft() == stunBlocksOnTheFarm(), stunBlocksLeft() + " of " + stunBlocksOnTheFarm() + " left");
            standBehindHim();
        }));
        steps.add(await("the player gets behind him", 20, player -> player.distanceTo(fight().boss()) < 5.5F));
        steps.add(step("picture of his back while he's stunned", 2, player -> screenshot("hog-stunned")));
        // A half-recharged swing does less, and by then the player may be holding anything from the other steps.
        steps.add(await("the swing is recharged", 30, player -> player.getAttackStrengthScale(0.0F) >= 1.0F));
        steps.add(step("hit him in the back", 5, player -> {
            healthBeforeHit = fight().boss().getHealth();
            hitHim(player);
        }));
        steps.add(step("check the hit", 8, player -> {
            float taken = healthBeforeHit - fight().boss().getHealth();
            double swing = serverPlayer(player).getAttributeValue(Attributes.ATTACK_DAMAGE);
            check("a hit on his back while he's stunned does double", Math.abs(taken - swing * HogDaddy.BACK_MULTIPLIER) < 0.5, "took " + taken + " from a swing of " + swing);
            check("he's on the health of one player", fight().boss().playerCount() == 1, String.valueOf(fight().boss().playerCount()));
        }));
        steps.add(step("bring him near half health", 2, player -> {
            HogDaddy boss = fight().boss();
            server().execute(() -> boss.setHealth(boss.getMaxHealth() / 2.0F + 0.1F));
        }));
        steps.add(step("hit him again", 5, player -> hitHim(player)));
        steps.add(await("half health enrages him", 20, player -> fight().boss().phase() == HogDaddy.Phase.ENRAGED));
        steps.add(step("check the enrage", 4, player -> {
            check("half health enrages him", fight().boss().phase() == HogDaddy.Phase.ENRAGED, "health " + fight().boss().getHealth());
            screenshot("hog-enraged");
            lastBaitAction = null;
        }));
        steps.add(await("a belly flop sends a mud ring", 500, player -> {
            bait(player);
            return rings() > 0;
        }));
        steps.add(step("check the ring", 12, player -> {
            check("enraged, belly flops send mud rings", rings() > 0, String.valueOf(fight().boss().action()));
            standBack(player);
        }));
        steps.add(step("picture of the mud ring", 2, player -> screenshot("hog-mud-ring")));
        steps.add(await("the piglets come", HogDaddyFight.PIGLET_WAVE_TICKS + 40, player -> {
            bait(player);
            return !fight().piglets().isEmpty();
        }));
        steps.add(step("check the piglets", 0, player -> {
            check("zombie piglets keep coming", !fight().piglets().isEmpty(), "");
            log("piglets: " + fight().piglets().size());
        }));
        steps.add(await("he slams into a post, enraged", 500, player -> {
            bait(player);
            return fight().boss().action() == HogDaddy.Action.STUNNED;
        }));
        steps.add(step("check the broken post", 0, player -> {
            check("enraged, he slams into a post too", fight().boss().action() == HogDaddy.Action.STUNNED, String.valueOf(fight().boss().action()));
            // Usually the one the player stands at, but anything else on the line stops him first.
            check("each post only holds once now", stunBlocksLeft() < stunBlocksOnTheFarm(), stunBlocksLeft() + " of " + stunBlocksOnTheFarm() + " left");
            standBehindHim();
        }));
        steps.add(await("the player gets behind him again", 20, player -> player.distanceTo(fight().boss()) < 5.5F));
        steps.add(step("picture of the broken post", 6, player -> screenshot("hog-broken-post")));
        steps.add(step("bring him near death", 2, player -> {
            HogDaddy boss = fight().boss();
            server().execute(() -> boss.setHealth(0.5F));
        }));
        HogDaddy[] dying = new HogDaddy[1];
        steps.add(step("land the last hit", 6, player -> {
            dying[0] = fight().boss();
            hitHim(player);
        }));
        steps.add(step("check his death", 0, player -> {
            check("the last hit kills him", dying[0].isDeadOrDying(), "health " + dying[0].getHealth());
            screenshot("hog-beaten");
        }));
        steps.add(await("the farm is put back", 80, player -> HogDaddyFight.current() == null));
        steps.add(step("check the end", 5, player -> {
            check("the fight is over", HogDaddyFight.current() == null, "");
            check("the team has beaten him", progress().hasBeaten(Teams.teamKey(serverPlayer(player))), "");
            check("the gate is open", level().getBlockState(farm().at(HogFarmBuild.GATE_FROM)).isAir(), String.valueOf(level().getBlockState(farm().at(HogFarmBuild.GATE_FROM))));
            check("the broken post comes back", stunBlocksLeft() == stunBlocksOnTheFarm(), stunBlocksLeft() + " of " + stunBlocksOnTheFarm());
            check("the wall comes back", !level().getBlockState(farm().at(HogFarmBuild.WALL_FROM)).isAir(), "");
            check("the piglets are gone", level().getEntitiesOfClass(ZombiePiglet.class, player.getBoundingBox().inflate(80.0)).isEmpty(), "");
            command("wallis_boss hog_daddy start");
        }));
        steps.add(step("check he stays dead", 10, player -> {
            String told = Component.translatable("story." + WallisUltimate.MOD_ID + ".hog_daddy.already_beaten").getString();
            check("he can't be fought twice", HogDaddyFight.current() == null && chatContains(told), "");
        }));
    }

    /**
     * Puts the player between him and a post whenever he starts walking at someone, so his charge goes through them and
     * ends at the post. Only then: he walks straight at someone who stands still, so they stay on the line. Moving them
     * again could land after he has locked in his line, on a slow client, and take them off it.
     */
    private static void bait(LocalPlayer player) {
        if (HogDaddyFight.current() == null) {
            return;
        }
        HogDaddy.Action action = fight().boss().action();
        boolean startedWalking = action == HogDaddy.Action.STALK && lastBaitAction != HogDaddy.Action.STALK;
        lastBaitAction = action;
        if (!startedWalking) {
            return;
        }
        Vec3 at = Vec3.atBottomCenterOf(baitTarget());
        Vec3 toHim = fight().boss().position().subtract(at).multiply(1.0, 0.0, 1.0);
        Vec3 spot = toHim.lengthSqr() < 1.0E-4 ? at : at.add(toHim.normalize().scale(2.5));
        baitSpot = spot;
        // Looking at his feet, which also shows the red line.
        command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing entity @e[type=wallis_ultimate:hog_daddy,limit=1] feet", spot.x, at.y, spot.z));
    }

    /**
     * The nearest fence post or trough that's far enough for a charge and has nothing else on the way to it. Anything on
     * the line stops him, so the charge would end before it gets to the player otherwise.
     */
    private static BlockPos baitTarget() {
        Vec3 him = fight().boss().position();
        List<BlockPos> standing = farm().blueprint().blocks().entrySet().stream()
                .filter(block -> block.getKey().getY() == 0 && HogDaddy.isStunObject(block.getValue()))
                .map(block -> farm().origin().offset(block.getKey()))
                .filter(pos -> HogDaddy.isStunObject(level().getBlockState(pos)))
                .sorted(Comparator.comparingDouble(pos -> Vec3.atBottomCenterOf(pos).distanceToSqr(him))).toList();
        return standing.stream().filter(pos -> Vec3.atBottomCenterOf(pos).distanceTo(him) >= BAIT_MIN_DISTANCE && clearRun(him, pos, standing))
                .findFirst().orElse(standing.get(standing.size() - 1));
    }

    private static boolean clearRun(Vec3 him, BlockPos target, List<BlockPos> others) {
        Vec3 to = Vec3.atBottomCenterOf(target).subtract(him).multiply(1.0, 0.0, 1.0);
        double length = to.length();
        Vec3 along = to.normalize();
        for (BlockPos other : others) {
            // The rest of the trough is the same thing he's aiming at.
            if (other.distManhattan(target) > TROUGH_LENGTH) {
                Vec3 toOther = Vec3.atBottomCenterOf(other).subtract(him).multiply(1.0, 0.0, 1.0);
                double ahead = toOther.dot(along);
                if (ahead > 0.0 && ahead < length && toOther.subtract(along.scale(ahead)).length() < CHARGE_CLEARANCE) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Behind him, close enough to hit him and far enough to see all of him. */
    private static void standBehindHim() {
        HogDaddy boss = fight().boss();
        Vec3 behind = boss.position().subtract(Vec3.directionFromRotation(0.0F, boss.getYRot()).scale(4.5));
        command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing entity @e[type=wallis_ultimate:hog_daddy,limit=1] feet", behind.x, boss.getY(), behind.z));
    }

    private static void standBack(LocalPlayer player) {
        HogDaddy boss = fight().boss();
        Vec3 away = player.position().subtract(boss.position()).multiply(1.0, 0.0, 1.0);
        Vec3 spot = boss.position().add((away.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize()).scale(12.0));
        command(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f facing entity @e[type=wallis_ultimate:hog_daddy,limit=1] feet", spot.x, boss.getY() + 4.0, spot.z));
    }

    /** A real swing, the way a click would. */
    private static void hitHim(LocalPlayer player) {
        Entity onClient = Minecraft.getInstance().level.getEntity(fight().boss().getId());
        if (onClient != null) {
            Minecraft.getInstance().gameMode.attack(player, onClient);
        }
    }

    /** Any hay on the ground that the farm's own blueprint didn't put there. */
    private static boolean hayOnTheField() {
        HogFarm farm = farm();
        for (int x = 0; x < HogFarmBuild.SIZE_X; x++) {
            for (int z = 0; z < HogFarmBuild.SIZE_Z; z++) {
                BlockPos relative = new BlockPos(x, 0, z);
                if (level().getBlockState(farm.origin().offset(relative)).is(Blocks.HAY_BLOCK) && !farm.blueprint().blocks().containsKey(relative)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static long stunBlocksOnTheFarm() {
        return farm().blueprint().blocks().values().stream().filter(HogDaddy::isStunObject).count();
    }

    private static long stunBlocksLeft() {
        return farm().blueprint().blocks().entrySet().stream().filter(block -> HogDaddy.isStunObject(block.getValue()))
                .filter(block -> HogDaddy.isStunObject(level().getBlockState(farm().origin().offset(block.getKey())))).count();
    }

    private static long marks(boolean line) {
        return StreamSupport.stream(Minecraft.getInstance().level.entitiesForRendering().spliterator(), false)
                .filter(entity -> entity instanceof GroundMark mark && mark.isLine() == line).count();
    }

    private static long rings() {
        return StreamSupport.stream(Minecraft.getInstance().level.entitiesForRendering().spliterator(), false).filter(MudRing.class::isInstance).count();
    }

    private static MinecraftServer server() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    private static ServerLevel level() {
        return server().overworld();
    }

    private static ServerPlayer serverPlayer(LocalPlayer player) {
        return server().getPlayerList().getPlayer(player.getUUID());
    }

    private static HogFarm farm() {
        return HogFarm.of(level());
    }

    private static HogDaddyFight fight() {
        return HogDaddyFight.current();
    }

    private static HogDaddyProgress progress() {
        return HogDaddyProgress.get(level());
    }
}
