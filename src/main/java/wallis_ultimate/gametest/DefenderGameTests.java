package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.Defender;
import wallis_ultimate.entity.Dropship;
import wallis_ultimate.entity.NpcUnstuck;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.survival.Garrisons;
import wallis_ultimate.survival.SafeZones;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * Checks for SURV-011. Real safe spots are too far away to test next to, so tests put a little garrison of the same kind
 * around themselves. All in a batch of their own, because zones are global, and other tests would notice.
 */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DefenderGameTests {
    private static final String PLATFORM = "platform";
    private static final String BATCH = "defenders";
    /** The dropship hovers at least 20 blocks over whatever is below it, which takes a while at parachute speed. */
    private static final int MIN_PARACHUTE_TICKS = 60;

    private DefenderGameTests() {
    }

    private static SafeSpot zoneHere(GameTestHelper helper, int guards, Blueprint blueprint) {
        BlockPos center = helper.absolutePos(new BlockPos(3, 2, 3));
        SafeSpot zone = new SafeSpot("test_garrison_" + UUID.randomUUID(), center, 4, center, List.of(), center);
        SafeZones.addTestZone(zone);
        Garrisons.addTestGarrison(zone, guards, blueprint);
        return zone;
    }

    private static void finish(GameTestHelper helper, SafeSpot zone) {
        SafeZones.removeTestZone(zone);
        Garrisons.removeTestGarrison(zone);
        helper.killAllEntities();
        helper.succeed();
    }

    private static Defender defenderAt(GameTestHelper helper, SafeSpot zone, double x, double z) {
        Defender defender = helper.spawn(ModEntities.DEFENDER.get(), new Vec3(x, 2.0, z));
        defender.assign(zone.id(), BlockPos.containing(defender.position()));
        return defender;
    }

    private static Player mockPlayer(GameTestHelper helper, boolean creative) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), "wanted_guy")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return creative;
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(pos));
        return player;
    }

    /** A real shot at a real zombie, across the platform. Hit chance isn't 100%, so a few of them. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 100)
    public static void shotsHurtZombies(GameTestHelper helper) {
        Defender defender = helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(1.5, 2.0, 1.5));
        RegularZombie zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(6.5, 2.0, 6.5));
        helper.runAfterDelay(5, () -> {
            for (int i = 0; i < 12; i++) {
                defender.shoot(zombie);
            }
            if (zombie.getHealth() >= zombie.getMaxHealth() && zombie.isAlive()) {
                helper.fail("twelve shots across the platform all missed");
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** The whole thing: a free defender notices the zombie by itself, and it comes out badly for the zombie. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 200)
    public static void defendersPickFightsWithEnemiesThemselves(GameTestHelper helper) {
        Defender defender = helper.spawn(ModEntities.DEFENDER.get(), new Vec3(1.5, 2.0, 1.5));
        RegularZombie zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(6.5, 2.0, 6.5));
        helper.succeedWhen(() -> {
            if (zombie.getHealth() >= zombie.getMaxHealth() && zombie.isAlive()) {
                helper.fail("the defender should have shot the zombie by now");
            }
            helper.killAllEntities();
        });
    }

    /** Monsters hunt defenders like they hunt players, so a horde at a safe spot means a fight. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 200)
    public static void zombiesHuntDefenders(GameTestHelper helper) {
        helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(1.5, 2.0, 1.5));
        RegularZombie zombie = helper.spawn(ModEntities.ZOMBIE.get(), new Vec3(6.5, 2.0, 6.5));
        helper.succeedWhen(() -> {
            if (!(zombie.getTarget() instanceof Defender)) {
                helper.fail("the zombie should go after the defender");
            }
            helper.killAllEntities();
        });
    }

    /** Q-155: mining what's part of the build makes that player wanted, except in creative. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void miningTurnsThemHostile(GameTestHelper helper) {
        SafeSpot zone = zoneHere(helper, 0, null);
        Defender defender = defenderAt(helper, zone, 3.5, 3.5);
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
        Player miner = mockPlayer(helper, false);
        if (Garrisons.isWantedBy(defender, miner)) {
            helper.fail("nobody's wanted before they do anything");
        }
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), miner));
        if (!Garrisons.isWantedBy(defender, miner)) {
            helper.fail("mining should turn the defenders on the player");
        }
        Player builder = mockPlayer(helper, true);
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), builder));
        if (Garrisons.isWantedBy(defender, builder)) {
            helper.fail("creative players are exempt");
        }
        Garrisons.forgive(miner);
        if (Garrisons.isWantedBy(defender, miner)) {
            helper.fail("dying makes up for it");
        }
        defender.hurt(level.damageSources().playerAttack(miner), 1.0F);
        if (!Garrisons.isWantedBy(defender, miner)) {
            helper.fail("hitting a defender is just as bad");
        }
        finish(helper, zone);
    }

    /** Mining outside of what the blueprint built, like a tree in the area, doesn't count. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void miningOtherThingsIsFine(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint(1).set(0, 0, 0, "minecraft:oak_planks");
        SafeSpot zone = zoneHere(helper, 0, blueprint);
        Defender defender = defenderAt(helper, zone, 3.5, 3.5);
        ServerLevel level = helper.getLevel();
        BlockPos notBuilt = helper.absolutePos(new BlockPos(4, 1, 4));
        Player miner = mockPlayer(helper, false);
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, notBuilt, level.getBlockState(notBuilt), miner));
        if (Garrisons.isWantedBy(defender, miner)) {
            helper.fail("that wasn't part of the safe spot");
        }
        finish(helper, zone);
    }

    /** Main NPCs can't die, and don't count toward the guards. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void mainNpcsCantDie(GameTestHelper helper) {
        Defender main = helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(3.5, 2.0, 3.5));
        main.setMainNpc(true);
        if (main.hurt(helper.getLevel().damageSources().generic(), 1000.0F) || !main.isAlive()) {
            helper.fail("a main NPC took damage");
        }
        Defender ordinary = helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(5.5, 2.0, 5.5));
        ordinary.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        if (ordinary.isAlive()) {
            helper.fail("an ordinary defender can die");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** A broken block from the blueprint comes back, put there by a defender walking over. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 300)
    public static void defendersRepairBrokenBlocks(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint(2).set(0, 0, 0, "minecraft:oak_planks").set(1, 0, 0, "minecraft:spruce_fence");
        SafeSpot zone = zoneHere(helper, 1, blueprint);
        BlockPos center = zone.center();
        blueprint.place(helper.getLevel(), center);
        defenderAt(helper, zone, 1.5, 1.5);
        helper.getLevel().setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(center.east(), Blocks.AIR.defaultBlockState());
        helper.succeedWhen(() -> {
            if (!helper.getLevel().getBlockState(center).is(Blocks.OAK_PLANKS) || !helper.getLevel().getBlockState(center.east()).is(Blocks.SPRUCE_FENCE)) {
                helper.fail("the broken blocks should be back");
            }
            SafeZones.removeTestZone(zone);
            Garrisons.removeTestGarrison(zone);
            helper.killAllEntities();
        });
    }

    /** SURV-011: a guard doesn't stand and snipe from its post. It walks up to the enemy, which is what gets it hurt. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 100)
    public static void guardsWalkUpToTheEnemy(GameTestHelper helper) {
        SafeSpot zone = zoneHere(helper, 0, null);
        Defender guard = defenderAt(helper, zone, 0.5, 0.5);
        RegularZombie zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(7.5, 2.0, 7.5));
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1024.0);
        zombie.setHealth(1024.0F);
        // A shot that knocks it off this small platform leaves no way there, and then a guard rightly stays and keeps shooting.
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
        Vec3 start = guard.position();
        guard.setTarget(zombie);
        helper.runAfterDelay(40, () -> {
            // Its own position: its shots can knock the zombie off this small platform.
            if (guard.position().distanceTo(start) < 2.0) {
                helper.fail("the guard should have walked up to the zombie, but stayed at " + guard.position());
            }
            finish(helper, zone);
        });
    }

    /** Mining doesn't wait for the slow scan: guards know about it at once. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void minedBlocksAreNoticedRightAway(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint(3).set(0, 0, 0, "minecraft:oak_planks");
        SafeSpot zone = zoneHere(helper, 0, blueprint);
        BlockPos center = zone.center();
        blueprint.place(helper.getLevel(), center);
        Player miner = mockPlayer(helper, false);
        MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), center, helper.getLevel().getBlockState(center), miner));
        if (!Garrisons.isWaitingForRepair(helper.getLevel(), zone, center)) {
            helper.fail("a mined block should be on the repair list straight away");
        }
        Garrisons.forgive(miner);
        finish(helper, zone);
    }

    /** Someone standing in the way only delays a repair: the block stays on the list and comes back afterwards. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 60)
    public static void aBlockedRepairIsTriedAgain(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint(4).set(0, 0, 0, "minecraft:oak_planks");
        SafeSpot zone = zoneHere(helper, 0, blueprint);
        BlockPos center = zone.center();
        Defender guard = defenderAt(helper, zone, 4.5, 4.5);
        Pig inTheWay = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(3.5, 2.0, 3.5));
        Garrisons.repair(guard, center);
        if (helper.getLevel().getBlockState(center).is(Blocks.OAK_PLANKS)) {
            helper.fail("the block shouldn't appear inside someone standing there");
        }
        if (!Garrisons.isWaitingForRepair(helper.getLevel(), zone, center)) {
            helper.fail("a repair that was in the way should stay on the list");
        }
        inTheWay.discard();
        helper.runAfterDelay(1, () -> {
            Garrisons.repair(guard, center);
            if (!helper.getLevel().getBlockState(center).is(Blocks.OAK_PLANKS)) {
                helper.fail("the block should come back once the way is clear");
            }
            finish(helper, zone);
        });
    }

    /** With no player around, a garrison that lost someone is simply back to full strength. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 300)
    public static void aGarrisonRestoresItsCount(GameTestHelper helper) {
        SafeSpot zone = zoneHere(helper, 2, null);
        helper.succeedWhen(() -> {
            AABB area = new AABB(zone.center()).inflate(16.0);
            List<Defender> guards = helper.getLevel().getEntitiesOfClass(Defender.class, area, guard -> zone.id().equals(guard.spotId()));
            if (guards.size() != 2) {
                helper.fail("the garrison should fill up to 2, has " + guards.size());
            }
            SafeZones.removeTestZone(zone);
            Garrisons.removeTestGarrison(zone);
            helper.killAllEntities();
        });
    }

    /** The aircraft comes in, its troops jump, land under parachutes and head for their posts. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 500)
    public static void theDropshipDeliversTroops(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(3, 1, 3));
        List<BlockPos> posts = List.of(center.above(), center.above().east());
        String spot = "test_drop_" + UUID.randomUUID();
        Dropship.send(helper.getLevel(), spot, center, posts, 20);
        long[] firstJump = {-1};
        helper.onEachTick(() -> {
            AABB air = new AABB(center).inflate(32.0, 96.0, 32.0);
            if (firstJump[0] < 0 &&!helper.getLevel().getEntitiesOfClass(Defender.class, air, guard -> spot.equals(guard.spotId()) && guard.isParachuting()).isEmpty()) {
                firstJump[0] = helper.getTick();
            }
        });
        helper.succeedWhen(() -> {
            AABB air = new AABB(center).inflate(32.0, 96.0, 32.0);
            List<Defender> troops = helper.getLevel().getEntitiesOfClass(Defender.class, air, guard -> spot.equals(guard.spotId()));
            if (troops.size() != 2 || troops.stream().anyMatch(Defender::isParachuting)) {
                helper.fail("two troops should have landed, have " + troops.size());
            }
            if (firstJump[0] < 0) {
                helper.fail("they should come down under parachutes");
            }
            long fell = helper.getTick() - firstJump[0];
            if (fell < MIN_PARACHUTE_TICKS) {
                helper.fail("they were down after only " + fell + " ticks, at height " + (troops.get(0).getY() - center.getY())
                        + " over the platform, ground " + troops.get(0).onGround() + ", water " + troops.get(0).isInWater());
            }
            troops.forEach(Defender::discard);
            helper.killAllEntities();
        });
    }

    /** No NPC stays trapped: walled in a pit it can't climb out of, it teleports out by itself. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 300)
    public static void stuckNpcsTeleportOut(GameTestHelper helper) {
        BlockPos pit = new BlockPos(3, 2, 3);
        for (int y = 2; y <= 3; y++) {
            helper.setBlock(pit.north().atY(y), Blocks.STONE);
            helper.setBlock(pit.south().atY(y), Blocks.STONE);
            helper.setBlock(pit.east().atY(y), Blocks.STONE);
            helper.setBlock(pit.west().atY(y), Blocks.STONE);
        }
        Defender defender = helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(3.5, 2.0, 3.5));
        Vec3 start = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
        helper.succeedWhen(() -> {
            if (defender.position().distanceTo(start) < 1.0) {
                helper.fail("still stuck in the pit after more than " + NpcUnstuck.STUCK_TICKS + " ticks");
            }
            helper.killAllEntities();
        });
    }

    /** And a free one with room to walk stays where it is. */
    @GameTest(template = PLATFORM, batch = BATCH, timeoutTicks = 200, required = true)
    public static void freeNpcsDontTeleport(GameTestHelper helper) {
        Defender defender = helper.spawnWithNoFreeWill(ModEntities.DEFENDER.get(), new Vec3(3.5, 2.0, 3.5));
        Vec3 start = helper.absoluteVec(new Vec3(3.5, 2.0, 3.5));
        helper.runAfterDelay(NpcUnstuck.STUCK_TICKS + 40, () -> {
            if (defender.position().distanceTo(start) > 1.0) {
                helper.fail("nothing was wrong, but it moved away");
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
