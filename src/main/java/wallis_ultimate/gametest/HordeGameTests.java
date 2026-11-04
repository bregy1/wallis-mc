package wallis_ultimate.gametest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.survival.HordeNights;
import wallis_ultimate.worldgen.SafeSpot;

/** Checks for ENEMY-006 and WORLD-012: horde nights. The waves themselves run in the client smoke test. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HordeGameTests {
    private static final String PLATFORM = "platform";
    private static final long NIGHTFALL = 12000L;
    /** Hourglass's speed by day (SURV-007), which is what the warnings have to be timed in. */
    private static final double DAY_SPEED = 0.677083;

    private HordeGameTests() {
    }

    @GameTest(template = PLATFORM)
    public static void everySeventhNightIsAHordeNight(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        for (int night = 1; night < HordeNights.NIGHTS_PER_HORDE; night++) {
            HordeNights.countNight(player);
            if (HordeNights.isDue(List.of(player))) {
                helper.fail("night " + night + " was a horde night");
            }
        }
        if (!HordeNights.isTomorrow(List.of(player))) {
            helper.fail("no warning the day before the 7th night");
        }
        HordeNights.countNight(player);
        if (!HordeNights.isDue(List.of(player))) {
            helper.fail("the 7th night wasn't a horde night");
        }
        HordeNights.markHorde(List.of(player), 7L);
        if (HordeNights.nightsCounted(player) != 0 || HordeNights.isTomorrow(List.of(player))) {
            helper.fail("the counter didn't start over");
        }
        helper.succeed();
    }

    /** The counter closest to its horde night decides, and it starts over for everyone present, not for anyone else. */
    @GameTest(template = PLATFORM)
    public static void theClosestCounterDecidesForEveryonePresent(GameTestHelper helper) {
        Player closest = helper.makeMockPlayer();
        Player behind = helper.makeMockPlayer();
        Player away = helper.makeMockPlayer();
        HordeNights.setNightsCounted(closest, HordeNights.NIGHTS_PER_HORDE);
        HordeNights.setNightsCounted(behind, 2);
        HordeNights.setNightsCounted(away, 5);
        List<Player> present = List.of(closest, behind);
        if (!HordeNights.isDue(present)) {
            helper.fail("the closest counter didn't decide");
        }
        HordeNights.markHorde(present, 3L);
        if (HordeNights.nightsCounted(closest) != 0 || HordeNights.nightsCounted(behind) != 0) {
            helper.fail("the counters of those present didn't start over");
        }
        if (HordeNights.nightsCounted(away) != 5) {
            helper.fail("the counter of someone not there changed");
        }
        helper.succeed();
    }

    /** "2 minutes before" and "1 minute before" are real minutes, and Hourglass runs the day slower than vanilla. */
    @GameTest(template = PLATFORM)
    public static void warningsComeTwoMinutesAndOneMinuteAhead(GameTestHelper helper) {
        double firstSeconds = (NIGHTFALL - HordeNights.FIRST_WARNING) / DAY_SPEED / 20.0;
        double secondSeconds = (NIGHTFALL - HordeNights.SECOND_WARNING) / DAY_SPEED / 20.0;
        if (Math.abs(firstSeconds - 120.0) > 1.0 || Math.abs(secondSeconds - 60.0) > 1.0) {
            helper.fail("warnings at " + firstSeconds + " and " + secondSeconds + " seconds before nightfall");
        }
        helper.succeed();
    }

    /** Only time going by counts, also across midnight; a /time command doesn't count as a night. */
    @GameTest(template = PLATFORM)
    public static void onlyTimeGoingByCountsANight(GameTestHelper helper) {
        if (!HordeNights.crossed(11999L, 12000L, NIGHTFALL) || !HordeNights.crossed(24000L + 11999L, 24000L + 12001L, NIGHTFALL)) {
            helper.fail("nightfall wasn't noticed");
        }
        if (HordeNights.crossed(12000L, 12001L, NIGHTFALL) || HordeNights.crossed(11000L, 11999L, NIGHTFALL)) {
            helper.fail("nightfall was noticed when it wasn't happening");
        }
        if (!HordeNights.crossed(23999L, 24001L, 0L)) {
            helper.fail("the turn of the day wasn't noticed");
        }
        if (!HordeNights.isTimeGoingBy(11999L, 12000L) || HordeNights.isTimeGoingBy(3000L, 18000L) || HordeNights.isTimeGoingBy(18000L, 1000L)) {
            helper.fail("a /time command counted as time going by");
        }
        helper.succeed();
    }

    /** Q-158: beds refuse from nightfall to dawn of the horde's night, and no other time. */
    @GameTest(template = PLATFORM)
    public static void bedsOnlyRefuseOnTheHordesNight(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        List<Player> present = List.of(player);
        long day = 7L;
        long midnight = day * 24000L + 18000L;
        if (HordeNights.isHordeNight(present, midnight)) {
            helper.fail("a night without a horde counted as one");
        }
        HordeNights.markHorde(present, day);
        if (!HordeNights.isHordeNight(present, day * 24000L + 12000L) || !HordeNights.isHordeNight(present, midnight)) {
            helper.fail("the horde's night didn't count");
        }
        if (HordeNights.isHordeNight(present, day * 24000L + 11999L) || HordeNights.isHordeNight(present, day * 24000L + 23000L)
                || HordeNights.isHordeNight(present, (day + 1L) * 24000L + 18000L)) {
            helper.fail("beds would refuse outside the horde's night");
        }
        helper.succeed();
    }

    /** WORLD-012: when a team plays together, the horde goes for the bed most of them use. */
    @GameTest(template = PLATFORM)
    public static void theHordeGoesForTheBedMostOfTheTeamUses(GameTestHelper helper) {
        BlockPos loner = new BlockPos(500, 70, 500);
        BlockPos shared = new BlockPos(-300, 70, 0);
        BlockPos sharedToo = new BlockPos(-290, 70, 3);
        if (!shared.equals(HordeNights.sharedBase(List.of(loner, shared, sharedToo)))) {
            helper.fail("went for " + HordeNights.sharedBase(List.of(loner, shared, sharedToo)));
        }
        if (!loner.equals(HordeNights.sharedBase(List.of(loner)))) {
            helper.fail("a solo player's bed wasn't the base");
        }
        if (HordeNights.sharedBase(List.of()) != null) {
            helper.fail("nobody has a bed, but there's a base");
        }
        helper.succeed();
    }

    /** Q-157: at the base if anyone's home, otherwise around the players, wherever they are. */
    @GameTest(template = PLATFORM)
    public static void theHordeComesToThePlayersIfNobodyIsHome(GameTestHelper helper) {
        Player here = helper.makeMockPlayer();
        BlockPos pos = here.blockPosition();
        BlockPos nearby = pos.offset(10, 0, 0);
        BlockPos farAway = pos.offset(500, 0, 0);
        List<Player> team = new ArrayList<>(List.of(here));

        List<HordeNights.Fight> atHome = HordeNights.fights(team, nearby);
        if (atHome.size() != 1 || !atHome.get(0).center().equals(nearby)) {
            helper.fail("with someone home, the horde didn't go for the base");
        }
        List<HordeNights.Fight> nobodyHome = HordeNights.fights(team, farAway);
        if (nobodyHome.size() != 1 || !nobodyHome.get(0).center().equals(pos)) {
            helper.fail("with nobody home, the horde didn't come to the player");
        }
        List<HordeNights.Fight> noBed = HordeNights.fights(team, null);
        if (noBed.size() != 1 || !noBed.get(0).center().equals(pos)) {
            helper.fail("without a bed, the horde didn't come to the player");
        }
        helper.succeed();
    }

    /** PROG-007: the same size every time, and bigger with more players in the fight. */
    @GameTest(template = PLATFORM)
    public static void wavesGrowWithThePlayersInTheFight(GameTestHelper helper) {
        if (HordeNights.waveSize(1) != 6 || HordeNights.waveSize(3) != 14) {
            helper.fail("waves of " + HordeNights.waveSize(1) + " and " + HordeNights.waveSize(3));
        }
        // SURV-011: at a safe spot, where the guards fight it too, it comes in force.
        if (HordeNights.safeSpotWaveSize(1) != 15) {
            helper.fail("a wave at a safe spot for one player should be 15, not " + HordeNights.safeSpotWaveSize(1));
        }
        helper.succeed();
    }

    /** SURV-006: no enemy spawns inside a safe spot, so a horde there starts from outside the whole area. */
    @GameTest(template = PLATFORM)
    public static void wavesStartOutsideASafeSpot(GameTestHelper helper) {
        SafeSpot zone = new SafeSpot("test", BlockPos.ZERO, 60, BlockPos.ZERO, List.of());
        if (HordeNights.ringDistance(zone) <= zone.radius()) {
            helper.fail("the ring starts at " + HordeNights.ringDistance(zone));
        }
        helper.succeed();
    }

}
