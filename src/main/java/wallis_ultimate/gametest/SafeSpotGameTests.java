package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModEntities;
import wallis_ultimate.survival.SafeZones;
import wallis_ultimate.survival.Travel;
import wallis_ultimate.team.Teams;
import wallis_ultimate.worldgen.DistrictSites;
import wallis_ultimate.worldgen.SafeSpot;

/**
 * Checks for SURV-006, ENEMY-007 and WORLD-004. Real safe spots are too far away to test next to, so tests that need
 * one put a zone of the same kind around themselves. They share a batch: zones are global, and other tests would notice.
 */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SafeSpotGameTests {
    private static final String PLATFORM = "platform";
    private static final String BATCH = "safe_spots";
    private static final int NEAREST = 300;
    private static final int FARTHEST = 650;

    private SafeSpotGameTests() {
    }

    private static SafeSpot zoneHere(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(3, 2, 3));
        SafeSpot zone = new SafeSpot("test_zone", center, 4, center, List.of());
        SafeZones.addTestZone(zone);
        return zone;
    }

    private static void finish(GameTestHelper helper, SafeSpot zone) {
        SafeZones.removeTestZone(zone);
        helper.killAllEntities();
        helper.succeed();
    }

    private static Player mockPlayer(GameTestHelper helper, BlockPos relative, boolean creative) {
        BlockPos pos = helper.absolutePos(relative);
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), "safe_spot_guy")) {
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

    /** Q-138: four of them, about 350–600 blocks away, one in each direction, each with beds and a signpost. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void fourSafeSpotsAroundTheCabin(GameTestHelper helper) {
        ServerLevel overworld = helper.getLevel().getServer().overworld();
        DistrictSites sites = DistrictSites.get(overworld);
        if (sites.spots().size() != 4) {
            helper.fail("expected four safe spots, not " + sites.spots().size());
        }
        boolean north = false;
        boolean south = false;
        boolean east = false;
        boolean west = false;
        for (SafeSpot spot : sites.spots()) {
            int dx = spot.center().getX() - sites.spawn().getX();
            int dz = spot.center().getZ() - sites.spawn().getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < NEAREST || distance > FARTHEST) {
                helper.fail(spot.id() + " is " + distance + " blocks from spawn");
            }
            north |= dz < 0 && Math.abs(dz) > Math.abs(dx);
            south |= dz > 0 && Math.abs(dz) > Math.abs(dx);
            west |= dx < 0 && Math.abs(dx) > Math.abs(dz);
            east |= dx > 0 && Math.abs(dx) > Math.abs(dz);
            if (spot.beds().isEmpty()) {
                helper.fail(spot.id() + " has no beds");
            }
            for (BlockPos bed : spot.beds()) {
                if (!(overworld.getBlockState(bed).getBlock() instanceof BedBlock)) {
                    helper.fail(spot.id() + " has no bed at " + bed + ": " + overworld.getBlockState(bed));
                }
            }
            if (!overworld.getBlockState(spot.signpost()).is(ModBlocks.TRAVEL_SIGNPOST.get())) {
                helper.fail(spot.id() + " has no signpost at " + spot.signpost());
            }
            if (!overworld.getBlockState(spot.arrival()).isAir() || !overworld.getBlockState(spot.arrival().above()).isAir()) {
                helper.fail(spot.id() + ": something is in the way at the arrival " + spot.arrival());
            }
            if (!spot.contains(spot.signpost().getX(), spot.signpost().getZ()) || !spot.contains(spot.beds().get(0).getX(), spot.beds().get(0).getZ())) {
                helper.fail(spot.id() + ": its bed or signpost is outside its area");
            }
        }
        if (!(north && south && east && west)) {
            helper.fail("a direction has no safe spot");
        }
        helper.succeed();
    }

    /** A fight at a safe spot always comes from outside: enemies don't appear in its middle. Animals may be there. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void enemiesDontSpawnInside(GameTestHelper helper) {
        SafeSpot zone = zoneHere(helper);
        RegularZombie zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(3.5, 2.0, 3.5));
        MobSpawnEvent.PositionCheck check = new MobSpawnEvent.PositionCheck(zombie, helper.getLevel(), MobSpawnType.NATURAL, null);
        MinecraftForge.EVENT_BUS.post(check);
        if (check.getResult() != Event.Result.DENY) {
            helper.fail("no enemy should spawn in a safe spot");
        }
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new Vec3(3.5, 2.0, 3.5));
        MobSpawnEvent.PositionCheck animal = new MobSpawnEvent.PositionCheck(pig, helper.getLevel(), MobSpawnType.NATURAL, null);
        MinecraftForge.EVENT_BUS.post(animal);
        if (animal.getResult() == Event.Result.DENY) {
            helper.fail("animals may be there");
        }
        finish(helper, zone);
    }

    /** WORLD-004: hit or attacking in the last 10 seconds stops you. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void noTravelInCombat(GameTestHelper helper) {
        Player player = mockPlayer(helper, new BlockPos(3, 2, 3), false);
        RegularZombie zombie = helper.spawnWithNoFreeWill(ModEntities.ZOMBIE.get(), new Vec3(5.5, 2.0, 5.5));
        player.tickCount = 1000;
        if (Travel.inCombat(player)) {
            helper.fail("nobody has been in a fight yet");
        }
        player.setLastHurtByMob(zombie);
        if (!Travel.inCombat(player)) {
            helper.fail("just got hit: that's combat");
        }
        player.tickCount += Travel.COMBAT_TICKS;
        if (Travel.inCombat(player)) {
            helper.fail("10 seconds later it's over");
        }
        player.setLastHurtMob(zombie);
        if (!Travel.inCombat(player)) {
            helper.fail("attacking counts too");
        }
        helper.killAllEntities();
        helper.succeed();
    }

    /** WORLD-004: discovery belongs to the team, and is only news once. */
    @GameTest(template = PLATFORM, batch = BATCH)
    public static void aTeamRemembersWhatItFound(GameTestHelper helper) {
        DistrictSites sites = DistrictSites.get(helper.getLevel().getServer().overworld());
        Player finder = mockPlayer(helper, new BlockPos(3, 2, 3), false);
        Player teammate = mockPlayer(helper, new BlockPos(3, 2, 3), false);
        String team = "safe_spot_test_" + UUID.randomUUID();
        Teams.joinTestTeam(finder, team);
        Teams.joinTestTeam(teammate, team);
        if (!sites.discover(Teams.teamKey(finder), "somewhere")) {
            helper.fail("it should be new the first time");
        }
        if (sites.discover(Teams.teamKey(teammate), "somewhere")) {
            helper.fail("the teammate's team already knows it");
        }
        if (!sites.hasDiscovered(Teams.teamKey(teammate), "somewhere") || sites.hasDiscovered(UUID.randomUUID().toString(), "somewhere")) {
            helper.fail("only the team should know it");
        }
        Teams.leaveTestTeam(finder);
        Teams.leaveTestTeam(teammate);
        helper.succeed();
    }
}
