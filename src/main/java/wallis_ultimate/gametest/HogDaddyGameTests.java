package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.boss.HogDaddyFight;
import wallis_ultimate.boss.HogFarm;
import wallis_ultimate.entity.GroundMark;
import wallis_ultimate.entity.HogDaddy;
import wallis_ultimate.entity.MudRing;
import wallis_ultimate.entity.ZombiePiglet;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.survival.Downed;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.HogFarmBuild;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * Checks for ENEMY-011: Hog Daddy, his attacks, and the fight around him. Mock players aren't in the level, so they're
 * handed over directly; the fight runs on a small farm of the test's own, because the real one is too far away and a
 * test platform too small for it. The whole fight on the real farm is for the smoke test.
 */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HogDaddyGameTests {
    private static final String PLATFORM = "platform";
    private static final float LETHAL = 1000.0F;
    private static final float PRECISION = 0.01F;

    private HogDaddyGameTests() {
    }

    /** At a place relative to the test. Relative y=1 is the platform's floor; stand at y=2. */
    private static Player mockPlayer(GameTestHelper helper, String name, Vec3 relative) {
        Vec3 at = helper.absoluteVec(relative);
        Player player = new Player(helper.getLevel(), BlockPos.containing(at), 0.0F, new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };
        player.moveTo(at);
        return player;
    }

    /** With enough health not to go down from what a test throws at it. */
    private static Player toughPlayer(GameTestHelper helper, String name, Vec3 relative) {
        Player player = mockPlayer(helper, name, relative);
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(LETHAL);
        player.setHealth(LETHAL);
        return player;
    }

    private static HogDaddy boss(GameTestHelper helper, Vec3 relative) {
        return helper.spawn(ModEntities.HOG_DADDY.get(), relative);
    }

    private static long marksNear(GameTestHelper helper, boolean line) {
        return helper.getLevel().getEntitiesOfClass(GroundMark.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0), mark -> mark.isLine() == line).size();
    }

    // The seam with the quests: the entity id and the command, exactly as the story's quests use them.

    @GameTest(template = PLATFORM)
    public static void theBossHasTheIdTheQuestKills(GameTestHelper helper) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(ModEntities.HOG_DADDY.get());
        helper.assertTrue(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "hog_daddy").equals(id), "the boss is called " + id);
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void theCommandExistsAtPermissionLevelTwo(GameTestHelper helper) {
        CommandSourceStack console = helper.getLevel().getServer().createCommandSourceStack();
        String command = "wallis_boss hog_daddy start";
        ParseResults<CommandSourceStack> allowed = console.getServer().getCommands().getDispatcher().parse(command, console.withPermission(2));
        helper.assertTrue(allowed.getContext().getCommand() != null, "level 2 can't run it");
        ParseResults<CommandSourceStack> refused = console.getServer().getCommands().getDispatcher().parse(command, console.withPermission(1));
        helper.assertTrue(refused.getContext().getCommand() == null, "level 1 can run it");
        helper.succeed();
    }

    /** FTB Quests' kill task only counts a player's damage source, and the fire may finish him off. */
    @GameTest(template = PLATFORM)
    public static void whoeverHurtHimLastGetsTheKillIfTheFireFinishesHim(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        Player player = mockPlayer(helper, "hog_credit", new Vec3(2.5, 2.0, 2.5));
        helper.assertTrue(boss.killCredit(helper.getLevel().damageSources().onFire()).getEntity() == null, "nobody hit him yet");
        boss.hurt(helper.getLevel().damageSources().playerAttack(player), 4.0F);
        helper.assertTrue(boss.killCredit(helper.getLevel().damageSources().onFire()).getEntity() == player, "the kill isn't credited");
        helper.killAllEntities();
        helper.succeed();
    }

    // The rules of ENEMY-011.

    /**
     * PROG-007, spec: taking him down takes as many times the damage as there are players, at any team size, even where
     * a higher max health would pass Minecraft's cap of 1024. The piglet count scales too, one extra per player.
     */
    @GameTest(template = PLATFORM)
    public static void healthAndPigletsScalePerPlayer(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        boss.setYRot(0.0F);
        boss.stun();
        Player player = mockPlayer(helper, "hog_scale", new Vec3(4.5, 2.0, 7.5));
        float hit = 12.0F;
        helper.assertTrue(Math.abs(boss.getMaxHealth() - HogDaddy.HEALTH_PER_PLAYER) < PRECISION, "his health is " + boss.getMaxHealth());
        expectDamage(helper, boss, helper.getLevel().damageSources().playerAttack(player), hit, hit, "a hit with one player");
        for (int players : new int[] {3, 6}) {
            boss.setPlayerCount(players);
            expectDamage(helper, boss, helper.getLevel().damageSources().playerAttack(player), hit, hit / players, "a hit with " + players + " players");
        }
        helper.assertTrue(HogDaddyFight.pigletsPerWave(2) == HogDaddyFight.pigletsPerWave(1) + 1
                && HogDaddyFight.pigletsPerWave(3) == HogDaddyFight.pigletsPerWave(2) + 1, "not one extra piglet per player");
        helper.killAllEntities();
        helper.succeed();
    }

    /** ENEMY-011: longer windows for fewer players; 4 seconds with the three players it's designed for. */
    @GameTest(template = PLATFORM)
    public static void stunIsFourSecondsForThreeAndLongerForFewer(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        boss.setPlayerCount(3);
        helper.assertTrue(boss.stunTicks() == 80, "three players: " + boss.stunTicks());
        boss.setPlayerCount(4);
        helper.assertTrue(boss.stunTicks() == 80, "four players: " + boss.stunTicks());
        boss.setPlayerCount(2);
        int duo = boss.stunTicks();
        boss.setPlayerCount(1);
        int solo = boss.stunTicks();
        helper.assertTrue(solo > duo && duo > 80, "solo " + solo + ", duo " + duo);
        helper.killAllEntities();
        helper.succeed();
    }

    /** Spec: a red line shows for exactly a second, and he doesn't move or turn while it does. */
    @GameTest(template = PLATFORM)
    public static void aChargeIsAnnouncedByARedLineForOneSecond(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(1.5, 2.0, 3.5));
        Player player = toughPlayer(helper, "hog_line", new Vec3(5.5, 2.0, 3.5));
        boss.addTestPlayer(player);
        boss.startWindUp(player);
        Vec3 start = boss.position();
        float yaw = boss.getYRot();
        helper.assertTrue(boss.action() == HogDaddy.Action.WIND_UP, "he doesn't wind up");
        helper.assertTrue(Math.abs(yaw - -90.0F) < 1.0F, "he should face east, faces " + yaw);
        helper.assertTrue(marksNear(helper, true) == 1, "no red line");
        // He starts on the tick the second is over.
        helper.runAfterDelay(HogDaddy.TELEGRAPH_TICKS - 2, () -> {
            helper.assertTrue(boss.action() == HogDaddy.Action.WIND_UP, "he's already doing " + boss.action());
            helper.assertTrue(boss.position().distanceTo(start) < 0.05, "he moved during the telegraph");
            helper.assertTrue(Math.abs(boss.getYRot() - yaw) < 0.5F, "he turned during the telegraph");
        });
        helper.runAfterDelay(HogDaddy.TELEGRAPH_TICKS + 3, () -> {
            helper.assertTrue(boss.action() != HogDaddy.Action.WIND_UP, "he's still winding up");
            helper.assertTrue(boss.getX() > start.x + 0.5, "he isn't charging along the line: " + boss.position() + " from " + start);
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** Spec: slamming into a fence post stuns him for 4 seconds, and in phase 2 the post survives. He hits who's in the way. */
    @GameTest(template = PLATFORM)
    public static void slammingIntoAPostStunsHimAndThePostHolds(GameTestHelper helper) {
        BlockPos post = new BlockPos(6, 2, 3);
        helper.setBlock(post, ModBlocks.PEN_POST.get());
        HogDaddy boss = boss(helper, new Vec3(1.5, 2.0, 3.5));
        Player player = toughPlayer(helper, "hog_slam", new Vec3(4.0, 2.0, 3.5));
        boss.addTestPlayer(player);
        boss.setPlayerCount(3);
        boss.startWindUp(player);
        helper.succeedWhen(() -> {
            helper.assertTrue(boss.action() == HogDaddy.Action.STUNNED, "not stunned yet, " + boss.action());
            helper.assertTrue(player.getHealth() < player.getMaxHealth(), "he went through the player without hurting them");
            helper.assertBlockPresent(ModBlocks.PEN_POST.get(), post);
            boss.discard();
        });
    }

    /** Spec, phase 3: each post breaks after one hit. All of it, not just the block he touched. */
    @GameTest(template = PLATFORM)
    public static void enragedHeBreaksThePostHeHits(GameTestHelper helper) {
        BlockPos post = new BlockPos(6, 2, 3);
        helper.setBlock(post, ModBlocks.PEN_POST.get());
        helper.setBlock(post.above(), ModBlocks.PEN_POST.get());
        helper.setBlock(post.above(2), ModBlocks.PEN_POST.get());
        HogDaddy boss = boss(helper, new Vec3(1.5, 2.0, 3.5));
        Player player = toughPlayer(helper, "hog_break", new Vec3(4.0, 2.0, 3.5));
        boss.addTestPlayer(player);
        boss.setPhase(HogDaddy.Phase.ENRAGED);
        boss.startWindUp(player);
        helper.succeedWhen(() -> {
            helper.assertTrue(boss.action() == HogDaddy.Action.STUNNED, "not stunned yet, " + boss.action());
            helper.assertBlockNotPresent(ModBlocks.PEN_POST.get(), post);
            helper.assertBlockNotPresent(ModBlocks.PEN_POST.get(), post.above(2));
            boss.discard();
        });
    }

    /** The trough stuns him too; anything else just stops him. */
    @GameTest(template = PLATFORM)
    public static void theTroughStunsHimAndAWallDoesNot(GameTestHelper helper) {
        helper.setBlock(new BlockPos(6, 2, 2), ModBlocks.FEEDING_TROUGH.get());
        for (int y = 2; y <= 4; y++) {
            for (int z = 4; z <= 6; z++) {
                helper.setBlock(new BlockPos(6, y, z), ModBlocks.CRUSHED_WRECK.get());
            }
        }
        HogDaddy troughBoss = boss(helper, new Vec3(1.5, 2.0, 2.0));
        Player troughPlayer = toughPlayer(helper, "hog_trough", new Vec3(4.0, 2.0, 2.0));
        troughBoss.addTestPlayer(troughPlayer);
        troughBoss.startWindUp(troughPlayer);
        HogDaddy wallBoss = boss(helper, new Vec3(1.5, 2.0, 5.5));
        Player wallPlayer = toughPlayer(helper, "hog_wall", new Vec3(4.0, 2.0, 5.5));
        wallBoss.addTestPlayer(wallPlayer);
        wallBoss.startWindUp(wallPlayer);
        helper.succeedWhen(() -> {
            helper.assertTrue(troughBoss.action() == HogDaddy.Action.STUNNED, "the trough didn't stun him, " + troughBoss.action());
            helper.assertTrue(wallBoss.action() == HogDaddy.Action.RECOVER || wallBoss.action() == HogDaddy.Action.STALK, "the wall did this: " + wallBoss.action());
            troughBoss.discard();
            wallBoss.discard();
        });
    }

    /** Spec: stuns are when hits really count, double on his exposed back. Fire is his other weakness. */
    @GameTest(template = PLATFORM)
    public static void hitsCountWhenHeIsStunnedAndOnHisBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        boss.setYRot(0.0F); // Facing south, so he has his back to smaller z.
        Player front = mockPlayer(helper, "hog_front", new Vec3(4.5, 2.0, 7.5));
        Player back = mockPlayer(helper, "hog_back", new Vec3(4.5, 2.0, 1.5));
        float hit = 10.0F;
        expectDamage(helper, boss, level.damageSources().playerAttack(front), hit, hit * HogDaddy.ARMORED_MULTIPLIER, "a normal hit");
        boss.setRemainingFireTicks(100);
        expectDamage(helper, boss, level.damageSources().playerAttack(front), hit, hit * HogDaddy.BURNING_MULTIPLIER, "a hit while he burns");
        boss.setRemainingFireTicks(0);
        boss.stun();
        expectDamage(helper, boss, level.damageSources().playerAttack(front), hit, hit, "a hit in the front while stunned");
        expectDamage(helper, boss, level.damageSources().playerAttack(back), hit, hit * HogDaddy.BACK_MULTIPLIER, "a hit in the back while stunned");
        // Two players hitting in the same moment both count.
        expectDamage(helper, boss, level.damageSources().playerAttack(back), hit, hit * HogDaddy.BACK_MULTIPLIER, "a second player's hit");
        boss.invulnerableTime = 0;
        expectDamage(helper, boss, level.damageSources().onFire(), 1.0F, HogDaddy.FIRE_MULTIPLIER, "fire");
        helper.killAllEntities();
        helper.succeed();
    }

    private static void expectDamage(GameTestHelper helper, HogDaddy boss, net.minecraft.world.damagesource.DamageSource source, float amount, float expected, String what) {
        float before = boss.getHealth();
        boss.hurt(source, amount);
        float taken = before - boss.getHealth();
        helper.assertTrue(Math.abs(taken - expected) < PRECISION, what + " did " + taken + ", expected " + expected);
    }

    /** Spec: he can't be hurt during the chase. */
    @GameTest(template = PLATFORM)
    public static void heCannotBeHurtDuringTheChase(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        boss.setPhase(HogDaddy.Phase.CHASE);
        Player player = mockPlayer(helper, "hog_chase", new Vec3(2.5, 2.0, 2.5));
        boolean hurt = boss.hurt(helper.getLevel().damageSources().playerAttack(player), 10.0F);
        helper.assertTrue(!hurt && boss.getHealth() == boss.getMaxHealth(), "he took damage in the chase");
        helper.killAllEntities();
        helper.succeed();
    }

    /** Spec: phase 3 from half health. */
    @GameTest(template = PLATFORM)
    public static void halfHealthEnragesHim(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        boss.setHealth(boss.getMaxHealth() / 2.0F + 1.0F);
        Player player = mockPlayer(helper, "hog_rage", new Vec3(2.5, 2.0, 2.5));
        boss.stun();
        helper.assertTrue(boss.phase() == HogDaddy.Phase.PEN, "enraged before half health");
        boss.hurt(helper.getLevel().damageSources().playerAttack(player), 5.0F);
        helper.assertTrue(boss.phase() == HogDaddy.Phase.ENRAGED, "not enraged at half health");
        helper.killAllEntities();
        helper.succeed();
    }

    /** SURV-001, ENEMY-011: downed players are ignored by him and his piglets. */
    @GameTest(template = PLATFORM)
    public static void downedPlayersAreIgnored(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        ZombiePiglet piglet = helper.spawn(ModEntities.ZOMBIE_PIGLET.get(), new Vec3(1.5, 2.0, 1.5));
        piglet.setNoAi(true);
        Player downed = mockPlayer(helper, "hog_downed", new Vec3(2.5, 2.0, 2.5));
        Player standing = mockPlayer(helper, "hog_standing", new Vec3(6.5, 2.0, 6.5));
        boss.addTestPlayer(downed);
        boss.addTestPlayer(standing);
        downed.hurt(helper.getLevel().damageSources().generic(), LETHAL);
        helper.assertTrue(Downed.isDowned(downed), "the test player should be down");
        helper.assertTrue(boss.fightPlayers().equals(List.of(standing)), "he still counts the downed player: " + boss.fightPlayers());
        boss.setTarget(downed);
        helper.assertTrue(boss.getTarget() == null, "he can target a downed player");
        piglet.setTarget(downed);
        helper.assertTrue(piglet.getTarget() == null, "a piglet can target a downed player");
        helper.assertTrue(!downed.hurt(helper.getLevel().damageSources().mobAttack(boss), 8.0F), "a downed player can be hit");
        helper.killAllEntities();
        helper.succeed();
    }

    /** Spec: the rings cross the pen; jump over them. Only feet on the ground get caught, once per ring. */
    @GameTest(template = PLATFORM)
    public static void mudRingsCatchFeetOnTheGroundOnly(GameTestHelper helper) {
        HogDaddy boss = boss(helper, new Vec3(4.5, 2.0, 4.5));
        boss.setNoAi(true);
        Player grounded = toughPlayer(helper, "hog_ground", new Vec3(4.5, 2.0, 7.5));
        Player jumping = toughPlayer(helper, "hog_jump", new Vec3(4.5, 3.0, 1.5));
        boss.addTestPlayer(grounded);
        boss.addTestPlayer(jumping);
        MudRing.spawn(boss, boss.position());
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(Math.abs(grounded.getMaxHealth() - grounded.getHealth()) >= MudRing.DAMAGE / 2.0F, "the ring missed feet on the ground");
            helper.assertTrue(grounded.getMaxHealth() - grounded.getHealth() <= MudRing.DAMAGE * 2.0F, "the ring hit more than once");
            helper.assertTrue(jumping.getHealth() == jumping.getMaxHealth(), "the ring caught someone in the air");
            helper.killAllEntities();
            helper.succeed();
        });
    }

    // The fight around him.

    private static HogFarm testFarm(GameTestHelper helper) {
        Blueprint b = new Blueprint(1);
        b.set(7, 0, 7, "minecraft:air");
        b.box(3, 0, 6, 3, 1, 6, "minecraft:spruce_planks");
        b.box(6, 0, 0, 7, 1, 0, "minecraft:air");
        b.mark(HogFarmBuild.CHASE_START, 1, 0, 6);
        b.mark(HogFarmBuild.BOSS_START, 6, 0, 6);
        b.mark(HogFarmBuild.WALL_FROM, 3, 0, 6);
        b.mark(HogFarmBuild.WALL_TO, 3, 1, 6);
        b.mark(HogFarmBuild.GATE_FROM, 6, 0, 0);
        b.mark(HogFarmBuild.GATE_TO, 7, 1, 0);
        b.mark(HogFarmBuild.BELL, 0, 0, 0);
        b.mark(HogFarmBuild.PEN_LANDING, 2, 0, 2);
        b.mark(HogFarmBuild.BARN_LINE, 1, 0, 3);
        b.mark(HogFarmBuild.PIGLET + 0, 5, 0, 1);
        BlockPos origin = helper.absolutePos(new BlockPos(0, 2, 0));
        b.place(helper.getLevel(), origin);
        return new HogFarm(origin, b);
    }

    /** All the way through on a small farm: the gate shuts, he comes through the wall, the barn ends the chase, piglets come, he dies, the farm comes back. */
    @GameTest(template = PLATFORM, timeoutTicks = 400)
    public static void aFightRunsFromTheWallToTheEnd(GameTestHelper helper) {
        HogFarm farm = testFarm(helper);
        ServerLevel level = helper.getLevel();
        BlockPos wall = new BlockPos(3, 2, 6);
        BlockPos gate = new BlockPos(6, 2, 0);
        Player player = toughPlayer(helper, "hog_fight", new Vec3(1.5, 2.0, 6.5));
        HogDaddyFight fight = HogDaddyFight.startForTest(level, farm, player);
        HogDaddy boss = fight.boss();
        helper.assertBlockPresent(ModBlocks.RIVETED_STEEL.get(), gate);
        helper.assertTrue(fight.stage() == HogDaddyFight.Stage.INTRO && boss.action() == HogDaddy.Action.WAIT, "he should wait behind the wall");
        helper.assertTrue(fight.playersInside().equals(List.of(player)), "who's inside: " + fight.playersInside());
        helper.runAfterDelay(HogDaddyFight.INTRO_TICKS + 2, () -> {
            helper.assertTrue(fight.stage() == HogDaddyFight.Stage.CHASE, "no chase after the intro: " + fight.stage());
            helper.assertBlockNotPresent(Blocks.SPRUCE_PLANKS, wall);
            helper.assertTrue(boss.phase() == HogDaddy.Phase.CHASE, "he isn't chasing: " + boss.phase());
            player.moveTo(helper.absoluteVec(new Vec3(1.5, 2.0, 2.5)));
        });
        helper.runAfterDelay(HogDaddyFight.INTRO_TICKS + 5, () -> {
            helper.assertTrue(fight.stage() == HogDaddyFight.Stage.PEN, "reaching the barn didn't end the chase: " + fight.stage());
            helper.assertTrue(boss.phase() == HogDaddy.Phase.PEN, "he's still in the chase");
            helper.assertTrue(marksNear(helper, false) >= 1, "no circle where he'll land");
        });
        // Waves come every 10 seconds of the fight, and the first one after he's in the pen is due at 200 ticks.
        helper.runAfterDelay(HogDaddyFight.PIGLET_WAVE_TICKS + 2, () -> {
            helper.assertTrue(fight.piglets().size() == HogDaddyFight.pigletsPerWave(1), "piglets: " + fight.piglets().size());
            boss.setHealth(1.0F);
            boss.hurt(level.damageSources().playerAttack(player), 10.0F);
            helper.assertTrue(boss.isDeadOrDying(), "he should be dead");
            helper.assertTrue(fight.stage() == HogDaddyFight.Stage.OVER && fight.won(), "killing him didn't end the fight");
            helper.assertBlockNotPresent(ModBlocks.RIVETED_STEEL.get(), gate);
            helper.assertTrue(fight.piglets().stream().allMatch(ZombiePiglet::isRemoved), "the piglets stayed");
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(fight.won(), "not over yet");
            helper.assertBlockPresent(Blocks.SPRUCE_PLANKS, wall);
            helper.killAllEntities();
        });
    }

    /** ENEMY-011 with SURV-001: when nobody's left on their feet he's gone, the gate opens, and the farm comes back. */
    @GameTest(template = PLATFORM, timeoutTicks = 300)
    public static void everyoneDownEndsTheFight(GameTestHelper helper) {
        HogFarm farm = testFarm(helper);
        BlockPos gate = new BlockPos(6, 2, 0);
        Player player = mockPlayer(helper, "hog_wipe", new Vec3(1.5, 2.0, 6.5));
        HogDaddyFight fight = HogDaddyFight.startForTest(helper.getLevel(), farm, player);
        HogDaddy boss = fight.boss();
        player.hurt(helper.getLevel().damageSources().generic(), LETHAL);
        helper.assertTrue(Downed.isDowned(player), "the test player should be down");
        helper.succeedWhen(() -> {
            helper.assertTrue(fight.stage() == HogDaddyFight.Stage.OVER && !fight.won(), "the fight goes on: " + fight.stage());
            helper.assertTrue(boss.isRemoved(), "he's still there");
            helper.assertBlockNotPresent(ModBlocks.RIVETED_STEEL.get(), gate);
        });
    }

    /** PROG-007: everyone inside the arena counts, and only them. */
    @GameTest(template = PLATFORM)
    public static void everyoneInsideTheRingCountsForTheScaling(GameTestHelper helper) {
        HogFarm farm = testFarm(helper);
        Player first = mockPlayer(helper, "hog_one", new Vec3(1.5, 2.0, 6.5));
        Player second = mockPlayer(helper, "hog_two", new Vec3(2.5, 2.0, 6.5));
        HogDaddyFight fight = HogDaddyFight.startForTest(helper.getLevel(), farm, first, second);
        helper.succeedWhen(() -> {
            helper.assertTrue(fight.boss().playerCount() == 2, "he counts " + fight.boss().playerCount());
            fight.stopForTest();
        });
    }

    /** Spec: falling hay in the chase, with a mark where it comes down. */
    @GameTest(template = PLATFORM, timeoutTicks = 300)
    public static void hayFallsDuringTheChaseWithAMark(GameTestHelper helper) {
        HogFarm farm = testFarm(helper);
        Player player = toughPlayer(helper, "hog_hay", new Vec3(1.5, 2.0, 6.5));
        HogDaddyFight fight = HogDaddyFight.startForTest(helper.getLevel(), farm, player);
        helper.succeedWhen(() -> {
            List<FallingBlockEntity> bales = helper.getLevel().getEntitiesOfClass(FallingBlockEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(24.0));
            helper.assertTrue(!bales.isEmpty(), "no hay yet");
            helper.assertTrue(bales.get(0).getBlockState().is(Blocks.HAY_BLOCK), "that's not hay: " + bales.get(0).getBlockState());
            helper.assertTrue(marksNear(helper, false) >= 1, "no mark where it lands");
            bales.forEach(bale -> bale.discard());
            fight.stopForTest();
        });
    }

    // The farm itself.

    /** WORLD-006, ENEMY-011: the ring is closed all the way around in unminable blocks, except for the gate that shuts during a fight. */
    @GameTest(template = PLATFORM)
    public static void theRingIsClosedInUnminableBlocks(GameTestHelper helper) {
        Blueprint farm = HogFarmBuild.blueprint();
        ServerLevel level = helper.getLevel();
        BlockPos gateFrom = farm.mark(HogFarmBuild.GATE_FROM);
        BlockPos gateTo = farm.mark(HogFarmBuild.GATE_TO);
        helper.assertTrue(farm.sizeX() == HogFarmBuild.SIZE_X && farm.sizeZ() == HogFarmBuild.SIZE_Z, "size " + farm.sizeX() + " x " + farm.sizeZ());
        for (int x = 0; x < farm.sizeX(); x++) {
            for (int z = 0; z < farm.sizeZ(); z++) {
                boolean edge = x == 0 || z == 0 || x == farm.sizeX() - 1 || z == farm.sizeZ() - 1;
                boolean inGate = x >= gateFrom.getX() && x <= gateTo.getX() && z >= gateFrom.getZ() && z <= gateTo.getZ();
                if (!edge) {
                    continue;
                }
                for (int y = inGate ? 4 : 0; y <= 4; y++) {
                    BlockState state = farm.blocks().get(new BlockPos(x, y, z));
                    helper.assertTrue(state != null && state.getDestroySpeed(level, BlockPos.ZERO) < 0.0F && state.isCollisionShapeFullBlock(level, BlockPos.ZERO),
                            "the ring has a hole at " + x + ", " + y + ", " + z + ": " + state);
                }
            }
        }
        helper.succeed();
    }

    /** ENEMY-011, Q-150: everything the fight needs is on the blueprint, and every block in it exists. */
    @GameTest(template = PLATFORM)
    public static void theFarmHasEverythingTheFightNeeds(GameTestHelper helper) {
        Blueprint farm = HogFarmBuild.blueprint();
        for (String mark : List.of(HogFarmBuild.CHASE_START, HogFarmBuild.BOSS_START, HogFarmBuild.WALL_FROM, HogFarmBuild.WALL_TO, HogFarmBuild.GATE_FROM,
                HogFarmBuild.GATE_TO, HogFarmBuild.BELL, HogFarmBuild.PEN_LANDING, HogFarmBuild.BARN_LINE, HogFarmBuild.PIGLET + 0)) {
            helper.assertTrue(farm.mark(mark) != null, "no mark for " + mark);
        }
        helper.assertTrue(farm.blocks().values().stream().filter(state -> state.is(ModBlocks.PEN_POST.get())).count() >= 8, "not enough posts");
        helper.assertTrue(farm.blocks().values().stream().anyMatch(state -> state.is(ModBlocks.FEEDING_TROUGH.get())), "no trough");
        helper.assertTrue(farm.blocks().values().stream().anyMatch(state -> state.is(ModBlocks.CORN.get())), "no corn");
        helper.assertTrue(farm.blocks().get(farm.mark(HogFarmBuild.BELL)).is(Blocks.BELL), "no bell");
        helper.succeed();
    }

    /** WORLD-005, Q-138: built once, between two safe spots. The server's world has been through its first start by now. */
    @GameTest(template = PLATFORM)
    public static void theFarmStandsBetweenTwoSafeSpots(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        DistrictSites sites = DistrictSites.get(overworld);
        BlockPos origin = sites.hogFarm();
        helper.assertTrue(origin != null, "the farm wasn't built");
        BlockState corner = overworld.getBlockState(origin);
        helper.assertTrue(corner.is(ModBlocks.STRAPPED_HAY.get()) || corner.is(ModBlocks.CRUSHED_WRECK.get()), "the ring's corner is " + corner);
        BlockPos middle = origin.offset(HogFarmBuild.SIZE_X / 2, 0, HogFarmBuild.SIZE_Z / 2);
        List<SafeSpot> spots = sites.spots();
        boolean between = false;
        for (int a = 0; a < spots.size(); a++) {
            for (int b = a + 1; b < spots.size(); b++) {
                double midX = (spots.get(a).center().getX() + spots.get(b).center().getX()) / 2.0;
                double midZ = (spots.get(a).center().getZ() + spots.get(b).center().getZ()) / 2.0;
                between |= Math.abs(middle.getX() - midX) <= 80.0 && Math.abs(middle.getZ() - midZ) <= 80.0;
            }
        }
        helper.assertTrue(between, "the farm at " + middle + " isn't between two safe spots");
        helper.succeed();
    }

    /** ENEMY-011: players can't dig under the ring or pillar over it, in or out of a fight. Outside the footprint nothing changes. */
    @GameTest(template = PLATFORM)
    public static void nothingCanBeMinedOrPlacedOnTheFarm(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        BlockPos origin = DistrictSites.get(overworld).hogFarm();
        Player player = mockPlayer(helper, "hog_miner", new Vec3(2.5, 2.0, 2.5));
        BlockPos inside = origin.offset(20, 0, 60);
        BlockPos underneath = origin.offset(0, -20, 0);
        BlockPos outside = origin.offset(-30, 0, 0);
        helper.assertTrue(breakRefused(overworld, inside, player), "mining in the field works");
        helper.assertTrue(breakRefused(overworld, underneath, player), "digging under the ring works");
        helper.assertTrue(!breakRefused(overworld, outside, player), "mining outside the farm is refused");
        BlockEvent.EntityPlaceEvent place = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(overworld.dimension(), overworld, inside.above(4)),
                Blocks.STONE.defaultBlockState(), player);
        MinecraftForge.EVENT_BUS.post(place);
        helper.assertTrue(place.isCanceled(), "placing a block on the farm works");
        helper.succeed();
    }

    private static boolean breakRefused(ServerLevel level, BlockPos pos, Player player) {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
}
