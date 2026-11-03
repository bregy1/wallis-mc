package wallis_ultimate.worldgen;

import java.util.List;
import java.util.function.LongFunction;
import net.minecraft.util.RandomSource;

/**
 * WORLD-014: Earth's small ruins, scattered across the landscape as it generates: what's left of a very advanced
 * civilisation (World view in the overview). Each look is drawn with a seed, so no two of them come out quite the same.
 * All of them fit in 15 by 15 blocks, so a feature can place them from the middle of a chunk. Placeholder builds by Claude.
 */
public final class RuinBuilds {
    private static final String ID = "wallis_ultimate:";
    private static final String ASPHALT = ID + "asphalt";
    private static final String CONCRETE = ID + "cracked_concrete";
    private static final String CONCRETE_SLAB = ID + "cracked_concrete_slab[type=bottom,waterlogged=false]";
    private static final String COMPOSITE = ID + "composite_panel";
    private static final String COMPOSITE_CRACKED = ID + "composite_panel_cracked";
    private static final String GLASS_PANE = ID + "shattered_glass_pane";
    private static final String RUBBLE_PILE = ID + "rubble_pile[facing=north]";
    private static final String RUBBLE = ID + "rubble_decal";
    private static final String BLOOD = ID + "blood_decal";
    private static final String SCORCH = ID + "scorch_decal";
    private static final String REBAR = ID + "exposed_rebar";
    private static final String DRONE = ID + "drone_wreck[facing=north]";
    private static final String ROBOT = ID + "dead_robot[facing=north]";
    private static final String ROBOT_SCRAP = ID + "robot_scrap[facing=north]";
    private static final String SCRAP_PILE = ID + "scrap_pile[facing=north]";
    private static final String CONE = ID + "traffic_cone[facing=north]";
    private static final String JERSEY = ID + "jersey_barrier[facing=north]";
    private static final String GIRDER_UP = ID + "steel_girder[axis=y]";
    private static final String GIRDER_X = ID + "steel_girder[axis=x]";
    private static final String CHROME = ID + "chrome_block";
    private static final String CHROME_CRACKED = ID + "chrome_cracked";
    private static final String CHROME_SHARD = ID + "chrome_shard[facing=north]";
    private static final String LAMP_POST = ID + "lamp_post[axis=y]";
    private static final String SERVER_RACK = ID + "server_rack[facing=north]";
    private static final String CRATE = ID + "supply_crate";

    /** Every look. The rare, big one is on the list once; the small ones several times. */
    private static final List<LongFunction<Blueprint>> LOOKS = List.of(
            RuinBuilds::carPileUp, RuinBuilds::carPileUp,
            RuinBuilds::droneCrash, RuinBuilds::droneCrash,
            RuinBuilds::chargingPosts, RuinBuilds::chargingPosts,
            RuinBuilds::billboard,
            RuinBuilds::facade, RuinBuilds::facade,
            RuinBuilds::sculpture,
            RuinBuilds::solarArray, RuinBuilds::solarArray,
            RuinBuilds::streetCorner, RuinBuilds::streetCorner,
            RuinBuilds::dataCube,
            RuinBuilds::holoKiosk,
            RuinBuilds::robotGraveyard, RuinBuilds::robotGraveyard,
            RuinBuilds::lastStand,
            RuinBuilds::spilledContainers, RuinBuilds::spilledContainers,
            RuinBuilds::skeletonTower);

    private RuinBuilds() {
    }

    /** One of every look, for tests and pictures. */
    public static List<LongFunction<Blueprint>> allLooks() {
        return List.of(RuinBuilds::carPileUp, RuinBuilds::droneCrash, RuinBuilds::chargingPosts, RuinBuilds::billboard, RuinBuilds::facade,
                RuinBuilds::sculpture, RuinBuilds::solarArray, RuinBuilds::streetCorner, RuinBuilds::dataCube, RuinBuilds::holoKiosk,
                RuinBuilds::robotGraveyard, RuinBuilds::lastStand, RuinBuilds::spilledContainers, RuinBuilds::skeletonTower);
    }

    public static Blueprint random(RandomSource random) {
        return LOOKS.get(random.nextInt(LOOKS.size())).apply(random.nextLong());
    }

    private static void debris(Blueprint b, int toX, int toZ) {
        b.scatter(0, 0, 0, toX, toZ, RUBBLE, 0.08F);
        b.scatter(0, 0, 0, toX, toZ, SCORCH, 0.04F);
    }

    /** Two cars on a piece of road that isn't going anywhere any more. */
    private static Blueprint carPileUp(long seed) {
        Blueprint b = new Blueprint(seed);
        b.scatter(0, -1, 0, 12, 10, ASPHALT, 0.8F);
        for (int z = 0; z <= 10; z += 2) {
            b.set(6, -1, z, ID + "asphalt_dashed[facing=north]");
        }
        WreckBuilds.evCarAlongZ(b, 3, 0, 1);
        WreckBuilds.carAlongX(b, 5, 7, b.roll(0.5F));
        b.set(9, 0, 2, CONE);
        b.set(10, 0, 4, CONE);
        b.set(1, 0, 8, CONE);
        b.set(2, 0, 7, ID + "tire_stack[facing=north]");
        debris(b, 12, 10);
        return b;
    }

    /** A delivery drone that came down hard, and the two that came to see why. */
    private static Blueprint droneCrash(long seed) {
        Blueprint b = new Blueprint(seed);
        b.set(5, 0, 5, DRONE);
        b.set(3, 0, 7, ID + "drone_wreck[facing=east]");
        b.set(8, 0, 3, ID + "drone_wreck[facing=south]");
        b.set(5, 0, 4, ID + "ev_battery");
        b.set(6, 0, 6, ROBOT_SCRAP);
        b.set(4, 0, 4, SCRAP_PILE);
        b.scatter(1, 0, 1, 9, 9, SCORCH, 0.3F);
        b.scatter(0, 0, 0, 10, 10, RUBBLE, 0.12F);
        return b;
    }

    /** A row of charging posts on a concrete pad, one car still plugged in, waiting. */
    private static Blueprint chargingPosts(long seed) {
        Blueprint b = new Blueprint(seed);
        b.mottled(0, -1, 0, 11, -1, 6, CONCRETE, ASPHALT, 0.2F);
        for (int x : new int[] {2, 5, 8}) {
            b.set(x, 0, 1, ID + "charging_station[facing=north]");
        }
        WreckBuilds.evCarAlongZ(b, 5, 0, 2);
        b.box(0, 0, 0, 0, 3, 0, LAMP_POST);
        b.set(0, 3, 1, ID + "street_lamp[facing=north,lit=false]");
        b.set(11, 0, 5, CONE);
        debris(b, 11, 6);
        return b;
    }

    /** The ad is still running. */
    private static Blueprint billboard(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(3, 0, 4, 3, 3, 4, GIRDER_UP);
        b.box(5, 0, 4, 5, 3, 4, GIRDER_UP);
        b.box(2, 3, 4, 6, 3, 4, GIRDER_X);
        WreckBuilds.billboard(b, 3, 4, 4, "north");
        b.set(8, 0, 5, RUBBLE_PILE);
        b.box(0, 0, 6, 3, 0, 6, ID + "steel_girder[axis=x]");
        debris(b, 9, 8);
        return b;
    }

    /** One corner of something white and expensive, standing in a field. */
    private static Blueprint facade(long seed) {
        Blueprint b = new Blueprint(seed);
        int[] heights = {7, 7, 6, 6, 5, 3, 2, 1, 1};
        b.box(0, -1, 0, 8, -1, 6, CONCRETE);
        for (int x = 0; x < heights.length; x++) {
            for (int y = 0; y < heights[x]; y++) {
                boolean window = x % 3 == 1 && y % 3 == 1;
                b.set(x, y, 0, window ? GLASS_PANE : b.roll(0.15F) ? COMPOSITE_CRACKED : COMPOSITE);
            }
            if (b.roll(0.5F)) {
                b.set(x, heights[x], 0, REBAR);
            }
        }
        for (int z = 1; z < 6; z++) {
            int height = 7 - z;
            for (int y = 0; y < height; y++) {
                boolean window = z % 3 == 2 && y % 3 == 1;
                b.set(0, y, z, window ? GLASS_PANE : b.roll(0.15F) ? COMPOSITE_CRACKED : COMPOSITE);
            }
        }
        b.set(1, 3, 0, ID + "composite_light_strip");
        b.box(4, 1, 1, 4, 3, 1, ID + "neon_tube_pink[axis=y]");
        b.set(3, 0, 2, RUBBLE_PILE);
        b.set(5, 0, 2, RUBBLE_PILE);
        b.set(6, 0, 3, RUBBLE_PILE);
        b.set(2, 0, 4, ID + "dead_screen[facing=down]");
        b.scatter(1, 0, 1, 10, 7, RUBBLE, 0.25F);
        return b;
    }

    /** Public art: a great chrome ring on a plinth, torn open, glowing from the inside where it broke. */
    private static Blueprint sculpture(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(3, -1, 2, 10, -1, 7, CONCRETE);
        b.box(4, 0, 3, 9, 0, 6, COMPOSITE);
        double radius = 5.0;
        int from = 15 + (int) (seed & 15);
        int to = 215;
        for (int degrees = from; degrees <= to; degrees += 6) {
            double angle = Math.toRadians(degrees);
            int x = 7 + (int) Math.round(Math.cos(angle) * radius);
            int y = 6 + (int) Math.round(Math.sin(angle) * radius);
            if (y >= 1) {
                boolean broken = degrees - from < 18 || to - degrees < 18;
                b.set(x, y, 4, broken ? CHROME_CRACKED : CHROME);
                b.set(x, y, 5, broken ? CHROME_CRACKED : CHROME);
            }
        }
        // What fell off.
        b.box(10, 0, 8, 11, 0, 9, CHROME);
        b.set(10, 1, 8, CHROME_CRACKED);
        b.set(12, 0, 7, CHROME_SHARD);
        b.set(9, 0, 10, ID + "chrome_shard[facing=east]");
        b.set(2, 0, 7, ID + "chrome_shard[facing=south]");
        b.set(3, 0, 9, CHROME);
        b.scatter(0, 0, 0, 13, 11, RUBBLE, 0.06F);
        return b;
    }

    /** A field of solar panels, most of them smashed, and the robot that was servicing them. */
    private static Blueprint solarArray(long seed) {
        Blueprint b = new Blueprint(seed);
        for (int z = 1; z <= 9; z += 4) {
            for (int x = 1; x <= 11; x += 2) {
                if (b.roll(0.8F)) {
                    b.set(x, 0, z, ID + "broken_solar_panel[facing=north]");
                }
            }
        }
        b.set(5, 0, 3, ROBOT);
        b.set(9, 0, 7, ROBOT_SCRAP);
        b.scatter(0, 0, 0, 12, 10, RUBBLE, 0.05F);
        return b;
    }

    /** A street corner without its street. */
    private static Blueprint streetCorner(long seed) {
        Blueprint b = new Blueprint(seed);
        b.mottled(0, -1, 0, 9, -1, 4, CONCRETE, ASPHALT, 0.1F);
        b.scatter(0, -1, 5, 11, 11, ASPHALT, 0.7F);
        b.box(0, 0, 4, 4, 0, 4, JERSEY);
        b.box(1, 0, 1, 1, 4, 1, LAMP_POST);
        b.set(1, 4, 2, ID + "street_lamp[facing=north,lit=" + b.roll(0.2F) + "]");
        b.box(7, 0, 0, 9, 1, 0, ID + "chain_link_fence");
        b.set(5, 0, 2, ID + "sign_keep_out[facing=down]");
        b.set(8, 0, 6, CONE);
        b.set(9, 0, 8, CONE);
        b.set(3, 0, 8, ID + "tire_stack[facing=north]");
        b.set(6, 0, 3, ROBOT_SCRAP);
        debris(b, 11, 11);
        return b;
    }

    /** A little data center, cracked like an egg. It's still trying to serve something. */
    private static Blueprint dataCube(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(0, -1, 0, 8, -1, 7, CONCRETE);
        b.mottled(0, 0, 0, 8, 4, 0, COMPOSITE, COMPOSITE_CRACKED, 0.15F);
        b.mottled(0, 0, 0, 0, 4, 7, COMPOSITE, COMPOSITE_CRACKED, 0.15F);
        b.mottled(8, 0, 0, 8, 2, 4, COMPOSITE, COMPOSITE_CRACKED, 0.25F);
        b.mottled(1, 0, 7, 3, 1, 7, COMPOSITE, COMPOSITE_CRACKED, 0.25F);
        b.mottled(1, 4, 1, 4, 4, 3, COMPOSITE, COMPOSITE_CRACKED, 0.15F);
        for (int x = 2; x <= 6; x += 2) {
            b.set(x, 0, 2, SERVER_RACK);
            b.set(x, 1, 2, SERVER_RACK);
            if (x != 4) {
                b.set(x, 2, 2, SERVER_RACK);
            }
        }
        b.set(2, 0, 5, ID + "server_rack[facing=east]");
        b.set(5, 0, 5, RUBBLE_PILE);
        b.set(7, 1, 1, ID + "flickering_screen[facing=north]");
        b.scatter(1, 0, 3, 11, 10, RUBBLE, 0.2F);
        return b;
    }

    /** A kiosk's hologram, still welcoming nobody. These can be seen from far away at night. */
    private static Blueprint holoKiosk(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(1, -1, 1, 8, -1, 8, CONCRETE);
        b.set(5, 0, 5, ID + "holo_projector[facing=north]");
        for (int[] corner : new int[][] {{2, 2}, {8, 2}, {2, 8}}) {
            b.box(corner[0], 0, corner[1], corner[0], 2, corner[1], LAMP_POST);
            b.set(corner[0], 3, corner[1], ID + "neon_tube_cyan[axis=y]");
        }
        b.box(3, 0, 8, 6, 0, 8, CONCRETE_SLAB);
        b.set(8, 0, 8, RUBBLE_PILE);
        b.scatter(1, 0, 1, 9, 9, BLOOD, 0.03F);
        b.scatter(1, 0, 1, 9, 9, RUBBLE, 0.1F);
        return b;
    }

    /** A place robots came to, or were taken to. Nobody knows. */
    private static Blueprint robotGraveyard(long seed) {
        Blueprint b = new Blueprint(seed);
        b.set(2, 0, 2, ROBOT);
        b.set(6, 0, 3, ID + "dead_robot[facing=east]");
        b.set(4, 0, 7, ID + "dead_robot[facing=south]");
        b.set(8, 0, 7, ROBOT_SCRAP);
        b.set(1, 0, 6, ID + "robot_scrap[facing=east]");
        b.set(9, 0, 2, ID + "robot_scrap[facing=south]");
        b.set(7, 0, 9, SCRAP_PILE);
        debris(b, 10, 10);
        return b;
    }

    /** Somebody tried to hold out here, in a very old-fashioned way. It didn't go well. */
    private static Blueprint lastStand(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(2, 0, 2, 8, 0, 2, ID + "sandbags");
        b.box(2, 1, 2, 7, 1, 2, ID + "sandbag_slab[type=bottom,waterlogged=false]");
        b.box(2, 0, 3, 2, 0, 6, ID + "sandbags");
        b.set(5, 0, 5, ID + "oil_drum");
        b.set(6, 0, 7, ID + "beer_cans[facing=north]");
        b.set(3, 0, 8, ID + "barbed_wire");
        b.set(4, 0, 9, ID + "barbed_wire");
        b.set(9, 0, 5, ID + "sign_shot_on_site[facing=down]");
        WreckBuilds.pickupAlongZ(b, 10, 0, 1);
        b.scatter(0, 0, 0, 12, 10, BLOOD, 0.12F);
        b.scatter(0, 0, 0, 12, 10, SCORCH, 0.06F);
        return b;
    }

    /** Containers that came off something. What was in them is now everywhere. */
    private static Blueprint spilledContainers(long seed) {
        Blueprint b = new Blueprint(seed);
        String paint = b.roll(0.5F) ? ID + "shipping_container_blue" : ID + "shipping_container_orange";
        b.box(1, 0, 2, 9, 2, 2, paint);
        b.box(1, 0, 5, 9, 2, 5, paint);
        b.box(9, 0, 2, 9, 2, 5, paint);
        b.box(1, 3, 2, 9, 3, 5, paint);
        b.box(1, 0, 3, 1, 1, 4, ID + "corrugated_sheet[facing=west]");
        b.set(4, 0, 3, CRATE);
        b.set(5, 0, 4, CRATE);
        b.set(6, 0, 3, ID + "server_rack[facing=south]");
        b.set(0, 0, 3, CRATE);
        b.set(0, 0, 6, CRATE);
        b.set(3, 0, 7, SCRAP_PILE);
        b.set(6, 0, 8, ID + "dead_screen[facing=down]");
        b.set(11, 0, 4, ID + "corrugated_sheet[facing=down]");
        debris(b, 12, 10);
        return b;
    }

    /** The landmark, and the rare one: the stump of a tower. Bare girders and what's left of two floors. */
    private static Blueprint skeletonTower(long seed) {
        Blueprint b = new Blueprint(seed);
        b.box(1, -1, 1, 12, -1, 12, CONCRETE);
        int[][] legs = {{2, 2, 14}, {11, 2, 11}, {2, 11, 9}, {11, 11, 6}};
        for (int[] leg : legs) {
            b.box(leg[0], 0, leg[1], leg[0], leg[2], leg[1], GIRDER_UP);
            b.set(leg[0], leg[2] + 1, leg[1], REBAR);
        }
        for (int x = 3; x <= 10; x++) {
            b.set(x, 5, 2, GIRDER_X);
            if (x < 8) {
                b.set(x, 10, 2, GIRDER_X);
            }
        }
        b.box(3, 5, 3, 10, 5, 5, CONCRETE_SLAB);
        b.box(3, 5, 6, 6, 5, 9, CONCRETE_SLAB);
        b.box(3, 10, 3, 6, 10, 6, CONCRETE_SLAB);
        b.set(3, 6, 3, ID + "composite_panel_cracked");
        b.set(4, 6, 3, GLASS_PANE);
        b.set(2, 6, 7, ID + "composite_panel");
        b.set(8, 0, 8, RUBBLE_PILE);
        b.set(9, 0, 9, RUBBLE_PILE);
        b.set(7, 0, 9, RUBBLE_PILE);
        b.set(9, 0, 7, ID + "cracked_concrete");
        b.scatter(0, 0, 0, 13, 13, RUBBLE, 0.15F);
        return b;
    }
}
