package wallis_ultimate.worldgen;

/**
 * SURV-006, SURV-011, Q-146: district 1's four safe spots, each closed in by its own kind of wall and each with its own
 * theme from the ruins of an advanced world (World view in the overview): a fort of shipping containers around a junkyard
 * of electric cars, a futuristic charging station turned strongpoint, a failed government evacuation camp, and a pile-up
 * around a driverless semi on a broken highway. Placeholder builds by Claude. Every one has marks for its beds, its travel
 * signpost, its guard posts (guard1, guard2...) and where its named NPCs stand (npc1, npc2...).
 */
public final class SafeSpotBuilds {
    static final String TRAVEL = "travel";
    static final String BED = "bed";
    static final String GUARD = "guard";
    static final String NPC = "npc";
    private static final String ID = "wallis_ultimate:";
    private static final String SANDBAGS = ID + "sandbags";
    private static final String SANDBAG_SLAB = ID + "sandbag_slab[type=bottom,waterlogged=false]";
    private static final String WIRE = ID + "barbed_wire";
    private static final String DRUM = ID + "oil_drum";
    private static final String BURN_BARREL = ID + "oil_drum[lit=true]";
    private static final String CRATE = ID + "supply_crate";
    private static final String WRECK = ID + "wreck_body";
    private static final String WRECK_PANEL = ID + "wreck_panel[type=bottom,waterlogged=false]";
    private static final String EV_BODY = ID + "ev_body";
    private static final String CONTAINER_BLUE = ID + "shipping_container_blue";
    private static final String CONTAINER_ORANGE = ID + "shipping_container_orange";
    private static final String CORRUGATED = ID + "corrugated_metal";
    private static final String SCRAP = ID + "scrap_block";
    private static final String SCRAP_PILE = ID + "scrap_pile";
    private static final String RUBBLE = ID + "rubble_pile";
    private static final String FENCE_LINK = ID + "chain_link_fence";
    private static final String GABION = ID + "gabion_barrier";
    private static final String HAZARD = ID + "hazard_plate";
    private static final String CONCRETE = ID + "cracked_concrete";
    private static final String ASPHALT = ID + "asphalt";
    private static final String COMPOSITE = ID + "composite_panel";
    private static final String COMPOSITE_CRACKED = ID + "composite_panel_cracked";
    private static final String LIGHT_STRIP = ID + "composite_light_strip";
    private static final String GLASS_PANE = ID + "shattered_glass_pane";
    private static final String GIRDER_UP = ID + "steel_girder[axis=y]";
    private static final String GIRDER_X = ID + "steel_girder[axis=x]";
    private static final String GIRDER_Z = ID + "steel_girder[axis=z]";
    private static final String LAMP_POST = ID + "lamp_post[axis=y]";
    private static final String TIRES = ID + "tire_stack[facing=north]";
    private static final String DRONE = ID + "drone_wreck[facing=north]";
    private static final String BEER = ID + "beer_cans[facing=north]";
    private static final String PLANKS = "minecraft:spruce_planks";
    private static final String FENCE = "minecraft:spruce_fence";
    private static final String BARRICADE = "just_barricades:spruce_barricade[facing=north,health=3]";
    private static final String DIRT = "minecraft:coarse_dirt";
    private static final String GRAVEL = "minecraft:gravel";
    private static final String AIR = "minecraft:air";
    private static final String CAMPFIRE = "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]";
    private static final String SUPPLIES = "supplies";
    /** How much of the free ground around a safe spot gets some rubble, blood or a scorch mark. */
    private static final float DECAL_CHANCE = 0.035F;

    private SafeSpotBuilds() {
    }

    /** The signpost. Travellers arrive one block south of it, so that block and the one above stay free in every safe spot. */
    private static void travel(Blueprint b, int x, int y, int z) {
        b.set(x, y, z, ID + "travel_signpost");
        b.set(x, y, z + 1, AIR);
        b.set(x, y + 1, z + 1, AIR);
        b.mark(TRAVEL, x, y, z);
    }

    /** Beds point their heads the way the bed block's facing says; the foot is the block before that. */
    private static void bed(Blueprint b, String mark, String color, int x, int y, int z, String facing, int footX, int footZ) {
        b.set(x, y, z, "minecraft:" + color + "_bed[facing=" + facing + ",part=head,occupied=false]");
        b.set(footX, y, footZ, "minecraft:" + color + "_bed[facing=" + facing + ",part=foot,occupied=false]");
        b.mark(mark, x, y, z);
    }

    private static void guards(Blueprint b, int[][] posts) {
        for (int i = 0; i < posts.length; i++) {
            b.mark(GUARD + (i + 1), posts[i][0], posts[i][1], posts[i][2]);
        }
    }

    /** Scatters the marks of what happened here over ground nothing else stands on. */
    private static void aftermath(Blueprint b, int fromX, int fromZ, int toX, int toZ) {
        b.scatter(fromX, 0, fromZ, toX, toZ, ID + "rubble_decal", DECAL_CHANCE);
        b.scatter(fromX, 0, fromZ, toX, toZ, ID + "blood_decal", DECAL_CHANCE / 2.0F);
        b.scatter(fromX, 0, fromZ, toX, toZ, ID + "scorch_decal", DECAL_CHANCE / 2.0F);
    }

    /** A pole with a working floodlight on top, looking the given way. */
    private static void floodlightMast(Blueprint b, int x, int z, int height, String facing) {
        b.box(x, 0, z, x, height - 1, z, LAMP_POST);
        b.set(x, height, z, ID + "floodlight[facing=" + facing + ",lit=true]");
    }

    /** A wall of stacked shipping containers: the color changes every six blocks, and between the two layers. */
    private static void containerWall(Blueprint b, int fromX, int fromZ, int toX, int toZ, int height) {
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                for (int y = 0; y < height; y++) {
                    int along = x - fromX + z - fromZ;
                    b.set(x, y, z, ((along / 6) + (y / 2)) % 2 == 0 ? CONTAINER_BLUE : CONTAINER_ORANGE);
                }
            }
        }
    }

    /**
     * Earl's Salvage, north of the cabin: a junkyard turned into a fort. Its wall is shipping containers two rows deep,
     * with a walkway on the inner row. Inside, what's left of everyone's electric cars and delivery drones: heaps of it,
     * a crane still holding one up, a workshop, a barracks made from containers. The gate faces south, to the cabin.
     */
    public static Blueprint earlsSalvage() {
        Blueprint b = new Blueprint(2001);
        int w = 42;
        int d = 44;
        b.mottled(0, -1, 0, w, -1, d, DIRT, "minecraft:packed_mud", 0.25F);
        b.speckle(0, -1, 0, w, d, GRAVEL, 0.06F);
        earlsWalls(b, w, d);
        earlsGate(b, d);
        tower(b, 3, 3);
        tower(b, w - 7, 3);
        crane(b);
        barracks(b);
        workshop(b);
        heaps(b, w, d);
        earlsYard(b);
        guards(b, new int[][] {{5, 9, 5}, {w - 5, 9, 5}, {12, 3, 1}, {30, 3, 1}, {1, 3, 24}, {w - 1, 3, 24}, {12, 3, d - 1}, {30, 3, d - 1}});
        b.mark(NPC + "1", 21, 0, 28);
        aftermath(b, 2, 2, w - 2, d + 5);
        return b;
    }

    private static void earlsWalls(Blueprint b, int w, int d) {
        containerWall(b, 0, 0, w, 0, 4);
        containerWall(b, 0, 1, w, 1, 3);
        containerWall(b, 0, 0, 0, d, 4);
        containerWall(b, 1, 1, 1, d, 3);
        containerWall(b, w, 0, w, d, 4);
        containerWall(b, w - 1, 1, w - 1, d, 3);
        containerWall(b, 0, d, w, d, 4);
        containerWall(b, 0, d - 1, w, d - 1, 3);
        // On top of the outer row: sandbags where people stand, wire and patches of sheet metal in between.
        for (int i = 0; i <= w; i++) {
            b.set(i, 4, 0, i % 12 < 6 ? SANDBAG_SLAB : WIRE);
            b.set(i, 4, d, i % 12 < 6 ? SANDBAG_SLAB : WIRE);
        }
        for (int i = 0; i <= d; i++) {
            b.set(0, 4, i, i % 12 < 6 ? SANDBAG_SLAB : WIRE);
            b.set(w, 4, i, i % 12 < 6 ? SANDBAG_SLAB : WIRE);
        }
        // Ladders up to the walkway, one per side.
        b.box(14, 0, 2, 14, 2, 2, "minecraft:ladder[facing=south,waterlogged=false]");
        b.box(28, 0, d - 2, 28, 2, d - 2, "minecraft:ladder[facing=north,waterlogged=false]");
        b.box(2, 0, 30, 2, 2, 30, "minecraft:ladder[facing=east,waterlogged=false]");
        b.box(w - 2, 0, 30, w - 2, 2, 30, "minecraft:ladder[facing=west,waterlogged=false]");
    }

    private static void earlsGate(Blueprint b, int d) {
        b.box(18, 0, d - 1, 24, 4, d, AIR);
        // A girder arch with the name in pink neon, salvaged from something fancier.
        b.box(17, 0, d, 17, 7, d, GIRDER_UP);
        b.box(25, 0, d, 25, 7, d, GIRDER_UP);
        b.box(17, 8, d, 25, 8, d, GIRDER_X);
        b.box(19, 7, d, 23, 7, d, ID + "neon_tube_pink[axis=x]");
        b.set(17, 5, d + 1, ID + "sign_safe_zone[facing=north]");
        b.set(25, 5, d + 1, ID + "sign_shot_on_site[facing=north]");
        // A chain-link gate that slid half open and stayed there.
        b.box(18, 0, d, 20, 2, d, FENCE_LINK);
        b.mottled(19, -1, 33, 23, -1, d + 5, ASPHALT, CONCRETE, 0.06F);
        // Outside: a chicane of jersey barriers, tires and wire.
        b.box(9, 0, d + 3, 16, 0, d + 3, ID + "jersey_barrier[facing=north]");
        b.box(26, 0, d + 3, 33, 0, d + 3, ID + "jersey_barrier[facing=north]");
        b.box(17, 0, d + 5, 21, 0, d + 5, ID + "jersey_barrier[facing=north]");
        b.box(2, 0, d + 5, 15, 0, d + 5, WIRE);
        b.box(27, 0, d + 5, 40, 0, d + 5, WIRE);
        b.set(16, 0, d + 2, BURN_BARREL);
        b.set(26, 0, d + 2, BURN_BARREL);
        b.set(15, 0, d + 2, TIRES);
        b.set(27, 0, d + 2, TIRES);
        b.set(14, 0, d + 2, ID + "traffic_cone[facing=north]");
        b.set(28, 0, d + 2, ID + "traffic_cone[facing=north]");
    }

    /** A lookout on four girders, with a ladder, a sandbagged platform, floodlights and a tarp over it. */
    private static void tower(Blueprint b, int x, int z) {
        for (int legX : new int[] {x, x + 4}) {
            for (int legZ : new int[] {z, z + 4}) {
                b.box(legX, 0, legZ, legX, 7, legZ, GIRDER_UP);
                b.box(legX, 9, legZ, legX, 11, legZ, FENCE);
            }
        }
        b.box(x + 1, 4, z, x + 3, 4, z, GIRDER_X);
        b.box(x + 1, 4, z + 4, x + 3, 4, z + 4, GIRDER_X);
        b.box(x, 4, z + 1, x, 4, z + 3, GIRDER_Z);
        b.box(x + 4, 4, z + 1, x + 4, 4, z + 3, GIRDER_Z);
        b.box(x, 8, z, x + 4, 8, z + 4, CORRUGATED);
        b.box(x + 1, 9, z, x + 3, 9, z, SANDBAG_SLAB);
        b.box(x + 1, 9, z + 4, x + 3, 9, z + 4, SANDBAG_SLAB);
        b.box(x, 9, z + 1, x, 9, z + 3, SANDBAG_SLAB);
        b.box(x + 4, 9, z + 1, x + 4, 9, z + 3, SANDBAG_SLAB);
        // The ladder runs up a plank column and over the edge, through a gap in the sandbags.
        b.box(x + 2, 0, z + 4, x + 2, 7, z + 4, PLANKS);
        b.box(x + 2, 0, z + 5, x + 2, 8, z + 5, "minecraft:ladder[facing=south,waterlogged=false]");
        b.set(x + 2, 9, z + 4, AIR);
        b.box(x, 12, z, x + 4, 12, z + 4, ID + "green_tarp[facing=down]");
        b.set(x + 1, 11, z, ID + "floodlight[facing=north,lit=true]");
        b.set(x + 3, 11, z, ID + "floodlight[facing=north,lit=true]");
        b.set(x + 1, 9, z + 1, CRATE);
        b.set(x + 3, 9, z + 1, BEER);
    }

    /** The landmark you see from far off: a salvage crane with a car still hanging from it. */
    private static void crane(Blueprint b) {
        b.box(32, 0, 25, 34, 0, 27, HAZARD);
        b.box(33, 1, 26, 33, 12, 26, GIRDER_UP);
        b.box(22, 13, 26, 37, 13, 26, GIRDER_X);
        b.box(36, 12, 26, 37, 12, 26, SCRAP);
        b.box(36, 11, 26, 37, 11, 26, SCRAP);
        b.box(32, 9, 25, 34, 11, 27, CONTAINER_ORANGE);
        b.set(33, 10, 28, ID + "ev_window[facing=north]");
        b.box(33, 9, 26, 33, 11, 26, GIRDER_UP);
        b.box(24, 10, 26, 24, 12, 26, "minecraft:chain[axis=y,waterlogged=false]");
        // The car, nose down at the far end of the boom.
        b.box(23, 8, 24, 25, 9, 28, EV_BODY);
        b.set(24, 9, 24, ID + "ev_battery");
        b.box(23, 9, 25, 25, 9, 26, ID + "ev_panel[type=bottom,waterlogged=false]");
        b.set(23, 7, 25, ID + "wreck_wheel[facing=east]");
        b.set(25, 7, 27, ID + "wreck_wheel[facing=west]");
    }

    /** Beds in a long container with a sheet metal roof and scavenged car windows. */
    private static void barracks(Blueprint b) {
        int x = 10;
        int z = 11;
        b.box(x, 0, z, x + 14, 0, z + 7, PLANKS);
        containerWall(b, x, z, x + 14, z, 4);
        containerWall(b, x, z + 7, x + 14, z + 7, 4);
        containerWall(b, x, z, x, z + 7, 4);
        containerWall(b, x + 14, z, x + 14, z + 7, 4);
        b.box(x - 1, 4, z - 1, x + 15, 4, z + 8, CORRUGATED);
        b.box(x, 1, z + 1, x + 13, 3, z + 6, AIR);
        b.box(x + 2, 2, z + 7, x + 3, 2, z + 7, ID + "ev_window[facing=south]");
        b.box(x + 11, 2, z + 7, x + 12, 2, z + 7, ID + "ev_window[facing=south]");
        b.set(x + 7, 1, z + 7, "minecraft:spruce_door[facing=north,half=lower,hinge=left,open=false,powered=false]");
        b.set(x + 7, 2, z + 7, "minecraft:spruce_door[facing=north,half=upper,hinge=left,open=false,powered=false]");
        bed(b, BED + "1", "red", x + 2, 1, z + 1, "north", x + 2, z + 2);
        bed(b, BED + "2", "brown", x + 5, 1, z + 1, "north", x + 5, z + 2);
        bed(b, BED + "3", "green", x + 9, 1, z + 1, "north", x + 9, z + 2);
        bed(b, BED + "4", "gray", x + 12, 1, z + 1, "north", x + 12, z + 2);
        b.set(x + 7, 1, z + 1, ID + "server_rack[facing=north]");
        b.set(x + 7, 2, z + 1, ID + "flickering_screen[facing=north]");
        b.set(x + 1, 1, z + 6, "minecraft:lantern[hanging=false,waterlogged=false]");
        b.lootChest(x + 13, 1, z + 4, "west", "earls_salvage");
        b.set(x + 13, 1, z + 6, CRATE);
        b.set(x + 13, 2, z + 6, BEER);
        // Salvaged neon strung along the walls, under the eaves.
        b.box(x + 15, 3, z, x + 15, 3, z + 7, ID + "neon_tube_cyan[axis=z]");
        b.box(x, 3, z + 8, x + 14, 3, z + 8, ID + "neon_tube_cyan[axis=x]");
    }

    /** Sheet metal over poles: what Earl is taking apart right now. */
    private static void workshop(Blueprint b) {
        for (int x : new int[] {27, 33}) {
            for (int z : new int[] {11, 19}) {
                b.box(x, 0, z, x, 3, z, FENCE);
            }
        }
        b.box(26, 4, 10, 34, 4, 20, ID + "corrugated_sheet[facing=down]");
        b.mottled(27, -1, 11, 33, -1, 19, CONCRETE, ID + "hazard_plate", 0.08F);
        b.set(32, 0, 12, ID + "dead_robot[facing=north]");
        b.set(30, 0, 12, ID + "charging_station[facing=north]");
        b.set(29, 0, 12, ID + "server_rack[facing=north]");
        b.set(29, 1, 12, ID + "server_rack[facing=north]");
        b.box(30, 0, 15, 32, 0, 15, "another_furniture:spruce_table");
        b.set(30, 1, 15, DRONE);
        b.set(32, 1, 15, ID + "robot_scrap[facing=east]");
        b.set(28, 0, 18, ID + "robot_scrap[facing=south]");
        b.set(32, 0, 18, SCRAP_PILE + "[facing=west]");
        b.lootChest(27, 0, 15, "east", SUPPLIES);
    }

    /** What's piled against the walls: cars, drones, tires and scrap. */
    private static void heaps(Blueprint b, int w, int d) {
        // West side: cars stacked two high, a mountain of scrap behind them.
        WreckBuilds.evCarAlongZ(b, 5, 0, 21);
        WreckBuilds.evCarAlongZ(b, 5, 0, 27);
        b.box(5, 2, 28, 6, 2, 30, EV_BODY);
        b.set(5, 3, 29, DRONE);
        WreckBuilds.evCarAlongZ(b, 5, 0, 33);
        b.box(2, 0, 10, 8, 0, 20, SCRAP);
        b.box(2, 1, 11, 7, 1, 18, SCRAP);
        b.box(2, 2, 12, 5, 2, 16, SCRAP);
        b.set(3, 3, 14, SCRAP_PILE + "[facing=north]");
        b.set(6, 2, 13, DRONE);
        b.set(7, 2, 17, SCRAP_PILE + "[facing=east]");
        b.set(8, 1, 15, ID + "robot_scrap[facing=west]");
        // East side: the old rust the first cars were replaced by, and tires.
        b.box(w - 5, 0, 22, w - 3, 2, 24, WRECK);
        b.box(w - 5, 3, 22, w - 3, 3, 24, WRECK_PANEL);
        b.set(w - 4, 4, 23, DRONE);
        b.box(w - 5, 0, 30, w - 3, 1, 33, WRECK);
        b.set(w - 3, 2, 31, SCRAP_PILE + "[facing=north]");
        b.box(w - 3, 0, 36, w - 3, 0, 40, TIRES);
        b.box(w - 3, 1, 37, w - 3, 1, 39, TIRES);
        // South-west corner: fuel, under guard of a very old sign.
        b.box(3, 0, d - 8, 7, 0, d - 4, DRUM);
        b.box(3, 1, d - 7, 6, 1, d - 5, DRUM);
        b.set(8, 1, d - 3, ID + "sign_keep_out[facing=west]");
        // A piece of the art someone hauled in and never sold.
        b.box(10, 0, 37, 11, 2, 38, ID + "chrome_block");
        b.set(11, 3, 38, ID + "chrome_cracked");
        b.set(12, 0, 37, ID + "chrome_shard[facing=east]");
    }

    private static void earlsYard(Blueprint b) {
        b.set(21, 0, 24, CAMPFIRE);
        b.box(19, 0, 23, 19, 0, 25, "minecraft:spruce_log[axis=z]");
        b.box(23, 0, 23, 23, 0, 25, "minecraft:spruce_log[axis=z]");
        b.set(21, 0, 21, CRATE);
        b.set(22, 0, 21, BEER);
        travel(b, 21, 0, 31);
        b.set(9, 0, 28, BURN_BARREL);
        b.set(31, 0, 30, BURN_BARREL);
        floodlightMast(b, 14, 34, 5, "south");
        floodlightMast(b, 28, 34, 5, "south");
    }

    /**
     * Gas-N-Guns, west of the cabin: a Halcyon fuel and charging station, the kind that was everywhere, made into a
     * strongpoint. White composite panels and light strips under the grime, a sleek canopy over charging posts and two
     * old pumps, the billboard still promising tomorrow. Jersey barriers and chain-link around the lot, sandbag nests at
     * the corners, and a redneck's pickup parked at the only pumps anyone still uses.
     */
    public static Blueprint gasNGuns() {
        Blueprint b = new Blueprint(2002);
        int w = 36;
        int d = 30;
        b.mottled(0, -1, 0, w, -1, d, ASPHALT, ID + "cracked_concrete", 0.08F);
        stationShop(b);
        canopy(b);
        lot(b, w, d);
        lotDefences(b, w, d);
        guards(b, new int[][] {{2, 2, 2}, {w - 2, 2, 2}, {2, 2, d - 2}, {w - 2, 2, d - 2}, {12, 6, 7}, {24, 6, 7}, {13, 0, d - 1}, {23, 0, d - 1}});
        b.mark(NPC + "1", 18, 1, 9);
        aftermath(b, 1, 1, w - 1, d + 4);
        return b;
    }

    private static void stationShop(Blueprint b) {
        int x = 6;
        int z = 2;
        int width = 24;
        int depth = 9;
        b.box(x, 0, z, x + width, 0, z + depth, "minecraft:polished_andesite");
        b.mottled(x, 1, z, x + width, 4, z, COMPOSITE, COMPOSITE_CRACKED, 0.12F);
        b.mottled(x, 1, z + depth, x + width, 4, z + depth, COMPOSITE, COMPOSITE_CRACKED, 0.12F);
        b.mottled(x, 1, z, x, 4, z + depth, COMPOSITE, COMPOSITE_CRACKED, 0.12F);
        b.mottled(x + width, 1, z, x + width, 4, z + depth, COMPOSITE, COMPOSITE_CRACKED, 0.12F);
        b.box(x, 4, z + depth, x + width, 4, z + depth, LIGHT_STRIP);
        b.mottled(x - 1, 5, z - 1, x + width + 1, 5, z + depth + 1, COMPOSITE, COMPOSITE_CRACKED, 0.08F);
        // Big shop windows, shot out and half boarded up, with sandbags under them.
        b.box(x + 3, 1, z + depth, x + 9, 3, z + depth, GLASS_PANE);
        b.box(x + 15, 1, z + depth, x + 21, 3, z + depth, GLASS_PANE);
        b.box(x + 3, 2, z + depth, x + 6, 3, z + depth, BARRICADE);
        b.box(x + 18, 2, z + depth, x + 21, 3, z + depth, BARRICADE);
        b.box(x + 3, 0, z + depth + 1, x + 9, 0, z + depth + 1, SANDBAGS);
        b.box(x + 15, 0, z + depth + 1, x + 21, 0, z + depth + 1, SANDBAGS);
        b.set(x + 12, 1, z + depth, "minecraft:spruce_door[facing=north,half=lower,hinge=left,open=false,powered=false]");
        b.set(x + 12, 2, z + depth, "minecraft:spruce_door[facing=north,half=upper,hinge=left,open=false,powered=false]");
        // Inside: beds where the stock room was, an empty shop, the screens still trying.
        b.box(x + 9, 1, z + 1, x + 9, 4, z + 5, COMPOSITE);
        bed(b, BED + "1", "blue", x + 2, 1, z + 1, "north", x + 2, z + 2);
        bed(b, BED + "2", "red", x + 5, 1, z + 1, "north", x + 5, z + 2);
        bed(b, BED + "3", "gray", x + 8, 1, z + 1, "north", x + 8, z + 2);
        b.box(x + 11, 1, z + 1, x + 21, 2, z + 1, "another_furniture:spruce_shelf[facing=north,horizontal=single]");
        b.box(x + 13, 1, z + 5, x + 20, 1, z + 5, "another_furniture:spruce_drawer[facing=north,open=false]");
        b.set(x + 15, 2, z + 5, BEER);
        b.set(x + 12, 3, z + 1, ID + "flickering_screen[facing=north]");
        b.set(x + 18, 3, z + 1, ID + "dead_screen[facing=north]");
        b.set(x + 23, 1, z + 1, ID + "server_rack[facing=north]");
        b.set(x + 23, 2, z + 1, ID + "server_rack[facing=north]");
        b.lootChest(x + 23, 1, z + 4, "west", "gas_n_guns");
        b.set(x + 12, 4, z + 4, "minecraft:lantern[hanging=true,waterlogged=false]");
        b.set(x + 15, 1, z + 7, CRATE);
        b.set(x + 1, 1, z + 7, CRATE);
        // The roof: a nest, a ladder up the side, and the billboard.
        b.box(x + width + 1, 0, z + 4, x + width + 1, 5, z + 4, "minecraft:ladder[facing=east,waterlogged=false]");
        b.box(x + 4, 6, z + 7, x + 8, 6, z + 7, SANDBAG_SLAB);
        b.box(x + 16, 6, z + 7, x + 20, 6, z + 7, SANDBAG_SLAB);
        b.box(x + 16, 6, z + 8, x + 16, 6, z + 8, SANDBAG_SLAB);
        WreckBuilds.billboard(b, x + 11, 7, z + 4, "north");
        b.box(x + 11, 6, z + 4, x + 13, 6, z + 4, GIRDER_X);
        b.box(x + 10, 6, z + 4, x + 10, 8, z + 4, GIRDER_UP);
        b.box(x + 14, 6, z + 4, x + 14, 8, z + 4, GIRDER_UP);
    }

    private static void canopy(Blueprint b) {
        for (int x : new int[] {10, 26}) {
            for (int z : new int[] {15, 23}) {
                b.box(x, 0, z, x, 4, z, LAMP_POST);
            }
        }
        b.mottled(8, 5, 14, 28, 5, 24, COMPOSITE, COMPOSITE_CRACKED, 0.06F);
        b.box(8, 5, 14, 28, 5, 14, LIGHT_STRIP);
        b.box(8, 5, 24, 28, 5, 24, LIGHT_STRIP);
        // A piece of the canopy came down and nobody put it back.
        b.box(22, 5, 20, 28, 5, 24, AIR);
        b.box(23, 0, 22, 27, 0, 23, ID + "composite_panel_cracked");
        b.set(25, 1, 22, RUBBLE + "[facing=north]");
        // Charging posts, one car still plugged in. Two old pumps, the only ones still in use.
        for (int x : new int[] {12, 16, 20, 24}) {
            b.box(x - 1, -1, 16, x + 1, -1, 17, HAZARD);
            b.set(x, 0, 16, ID + "charging_station[facing=north]");
        }
        WreckBuilds.evCarAlongZ(b, 12, 0, 18);
        for (int pumpX : new int[] {16, 20}) {
            b.box(pumpX, 0, 21, pumpX, 0, 22, "minecraft:polished_andesite");
            b.box(pumpX, 1, 21, pumpX, 2, 21, "minecraft:red_concrete");
            b.set(pumpX, 3, 21, "minecraft:white_concrete");
        }
    }

    private static void lot(Blueprint b, int w, int d) {
        // Road markings: a lane in, parking bays along the front.
        for (int z = 13; z <= d + 3; z += 2) {
            b.set(18, -1, z, ID + "asphalt_dashed[facing=north]");
        }
        for (int x = 2; x <= 5; x++) {
            b.set(x, -1, 12, ID + "asphalt_line[facing=east]");
            b.set(w - x, -1, 12, ID + "asphalt_line[facing=east]");
        }
        travel(b, 18, 0, 12);
        WreckBuilds.pickupAlongZ(b, 31, 0, 18);
        // Drums of what's left of the fuel, and crates.
        b.box(2, 0, 16, 4, 0, 18, DRUM);
        b.box(2, 1, 16, 3, 1, 17, DRUM);
        b.set(5, 0, 16, BURN_BARREL);
        b.set(2, 0, 21, CRATE);
        b.set(3, 0, 21, CRATE);
        b.set(2, 0, 22, CRATE);
        b.set(2, 1, 21, CRATE);
        b.lootChest(3, 0, 23, "east", SUPPLIES);
        b.set(31, 0, 9, TIRES);
        b.set(32, 0, 9, TIRES);
        b.set(33, 0, 9, TIRES);
        b.set(32, 1, 9, TIRES);
        b.set(9, 0, 26, ID + "traffic_cone[facing=north]");
        b.set(11, 0, 28, ID + "traffic_cone[facing=east]");
    }

    private static void lotDefences(Blueprint b, int w, int d) {
        // The back: sheet metal and a fence. The sides: barriers with chain-link on top. A gap at the front.
        b.box(0, 0, 0, w, 2, 0, CORRUGATED);
        b.box(0, 3, 0, w, 3, 0, WIRE);
        for (int z = 1; z < d; z++) {
            b.set(0, 0, z, ID + "jersey_barrier[facing=east]");
            b.set(w, 0, z, ID + "jersey_barrier[facing=east]");
            b.box(0, 1, z, 0, 2, z, FENCE_LINK);
            b.box(w, 1, z, w, 2, z, FENCE_LINK);
        }
        for (int x = 0; x <= w; x++) {
            if (x < 14 || x > 22) {
                b.set(x, 0, d, ID + "jersey_barrier[facing=north]");
                b.box(x, 1, d, x, 2, d, FENCE_LINK);
            }
        }
        b.set(5, 1, d + 1, ID + "sign_shot_on_site[facing=north]");
        b.set(30, 1, d + 1, ID + "sign_keep_out[facing=north]");
        // Nests at the corners, floodlights over them.
        for (int[] nest : new int[][] {{1, 1}, {w - 3, 1}, {1, d - 3}, {w - 3, d - 3}}) {
            b.box(nest[0], 0, nest[1], nest[0] + 2, 1, nest[1] + 2, GABION);
            b.box(nest[0], 2, nest[1], nest[0] + 2, 2, nest[1], SANDBAG_SLAB);
            b.box(nest[0], 2, nest[1] + 2, nest[0] + 2, 2, nest[1] + 2, SANDBAG_SLAB);
            b.set(nest[0] + 1, 2, nest[1] + 1, AIR);
        }
        floodlightMast(b, 7, 28, 5, "south");
        floodlightMast(b, 29, 28, 5, "south");
        // The way in: tires and barrels, wire to either side, a staggered barrier so nothing comes straight through.
        b.set(13, 0, d + 1, TIRES);
        b.set(23, 0, d + 1, TIRES);
        b.set(13, 1, d + 1, BURN_BARREL);
        b.set(23, 1, d + 1, BURN_BARREL);
        b.box(14, 0, d + 3, 18, 0, d + 3, ID + "jersey_barrier[facing=north]");
        b.box(0, 0, d + 3, 11, 0, d + 3, WIRE);
        b.box(25, 0, d + 3, w, 0, d + 3, WIRE);
        b.box(w + 2, 0, 2, w + 2, 0, d, WIRE);
    }

    /**
     * Camp Tarp, east of the cabin: a government evacuation camp, the kind that was going to save everyone. Gabion walls,
     * chain-link and floodlights, tents in rows, a field of smashed solar panels, a hologram at the gate still telling
     * people where to queue. The evacuation didn't come. The rednecks moved in.
     */
    public static Blueprint campTarp() {
        Blueprint b = new Blueprint(2003);
        int w = 34;
        int d = 34;
        b.mottled(2, -1, 2, w, -1, d, "minecraft:grass_block", DIRT, 0.3F);
        b.speckle(2, -1, 2, w, d, "minecraft:packed_mud", 0.1F);
        campWalls(b, w, d);
        tent(b, 7, 6, "blue", BED + "1", "red");
        tent(b, 13, 6, "green", BED + "2", "blue");
        tent(b, 19, 6, "blue", BED + "3", "brown");
        commandTent(b);
        solarField(b);
        campMiddle(b);
        guards(b, new int[][] {{3, 2, 9}, {3, 2, 27}, {w - 1, 2, 9}, {w - 1, 2, 27}, {12, 2, 3}, {24, 2, 3}, {12, 2, d - 1}, {24, 2, d - 1}});
        b.mark(NPC + "1", 21, 0, 17);
        aftermath(b, 3, 3, w - 1, d - 1);
        return b;
    }

    /** Two blocks of gabion all around with a walkway on top, chain-link above it, wire outside. The gate faces the cabin. */
    private static void campWalls(Blueprint b, int w, int d) {
        b.box(2, 0, 2, w, 1, 3, GABION);
        b.box(2, 0, d - 1, w, 1, d, GABION);
        b.box(2, 0, 2, 3, 1, d, GABION);
        b.box(w - 1, 0, 2, w, 1, d, GABION);
        b.box(2, 2, 2, w, 3, 2, FENCE_LINK);
        b.box(2, 2, d, w, 3, d, FENCE_LINK);
        b.box(2, 2, 2, 2, 3, d, FENCE_LINK);
        b.box(w, 2, 2, w, 3, d, FENCE_LINK);
        // The gate: a gap, with barriers making people slow down.
        b.box(2, 0, 16, 3, 3, 20, AIR);
        b.box(0, 0, 13, 0, 0, 16, ID + "jersey_barrier[facing=east]");
        b.box(0, 0, 20, 0, 0, 23, ID + "jersey_barrier[facing=east]");
        b.box(2, 4, 15, 2, 6, 15, GIRDER_UP);
        b.box(2, 4, 21, 2, 6, 21, GIRDER_UP);
        b.box(2, 0, 15, 2, 3, 15, GIRDER_UP);
        b.box(2, 0, 21, 2, 3, 21, GIRDER_UP);
        b.box(2, 7, 15, 2, 7, 21, GIRDER_Z);
        b.set(1, 2, 15, ID + "sign_safe_zone[facing=east]");
        b.set(1, 2, 21, ID + "sign_keep_out[facing=east]");
        // Ladders to the walkway.
        b.box(4, 0, 10, 4, 1, 10, "minecraft:ladder[facing=east,waterlogged=false]");
        b.box(w - 2, 0, 26, w - 2, 1, 26, "minecraft:ladder[facing=west,waterlogged=false]");
        // Wire outside, loosely.
        for (int i = 2; i <= d; i += 2) {
            b.set(w + 2, 0, i, WIRE);
        }
        for (int i = 4; i <= w; i += 2) {
            b.set(i, 0, 0, WIRE);
            b.set(i, 0, d + 2, WIRE);
        }
        for (int[] mast : new int[][] {{5, 5, 0}, {w - 3, 5, 0}, {5, d - 3, 1}, {w - 3, d - 3, 1}}) {
            floodlightMast(b, mast[0], mast[1], 6, mast[2] == 0 ? "north" : "south");
        }
    }

    /** Four wide, six long, a bed inside. The ridge runs along z; the opening faces south. */
    private static void tent(Blueprint b, int x, int z, String tarp, String bedMark, String bedColor) {
        String slopeEast = ID + tarp + "_tarp_slope[facing=east]";
        String slopeWest = ID + tarp + "_tarp_slope[facing=west]";
        for (int row = z; row < z + 6; row++) {
            b.set(x, 0, row, slopeEast);
            b.set(x + 1, 1, row, slopeEast);
            b.set(x + 2, 1, row, slopeWest);
            b.set(x + 3, 0, row, slopeWest);
        }
        b.box(x + 1, 0, z, x + 2, 0, z, ID + tarp + "_tarp[facing=north]");
        b.box(x + 1, -1, z, x + 2, -1, z + 5, DIRT);
        bed(b, bedMark, bedColor, x + 1, 0, z + 1, "north", x + 1, z + 2);
        b.set(x + 2, 0, z + 1, CRATE);
    }

    /** Where the evacuation was run from: dead screens on a table, a server rack, the map nobody needed. */
    private static void commandTent(Blueprint b) {
        int x = 24;
        int z = 6;
        b.box(x, -1, z, x + 6, -1, z + 7, CONCRETE);
        for (int poleX : new int[] {x, x + 6}) {
            for (int poleZ : new int[] {z, z + 7}) {
                b.box(poleX, 0, poleZ, poleX, 2, poleZ, FENCE);
            }
        }
        b.box(x, 3, z, x + 6, 3, z + 7, ID + "green_tarp[facing=down]");
        b.box(x, 0, z, x + 6, 2, z, ID + "green_tarp[facing=north]");
        b.box(x + 6, 0, z, x + 6, 2, z + 7, ID + "green_tarp[facing=east]");
        b.box(x + 1, 0, z + 2, x + 4, 0, z + 2, "another_furniture:spruce_table");
        b.set(x + 1, 1, z + 2, ID + "dead_screen[facing=down]");
        b.set(x + 2, 1, z + 2, ID + "flickering_screen[facing=down]");
        b.set(x + 4, 1, z + 2, BEER);
        b.set(x + 5, 0, z + 1, ID + "server_rack[facing=north]");
        b.set(x + 5, 1, z + 1, ID + "server_rack[facing=north]");
        b.set(x + 1, 0, z + 1, CRATE);
        b.set(x + 2, 0, z + 1, CRATE);
        b.lootChest(x + 3, 0, z + 1, "south", "camp_tarp");
    }

    /** Solar panels in rows, most of them smashed, feeding one charging post someone hot-wired. */
    private static void solarField(Blueprint b) {
        for (int z = 22; z <= 29; z += 3) {
            for (int x = 7; x <= 14; x += 2) {
                b.set(x, 0, z, ID + "broken_solar_panel[facing=north]");
            }
        }
        b.set(16, 0, 23, ID + "charging_station[facing=north]");
        b.set(17, 0, 23, ID + "ev_battery");
        b.set(9, 0, 25, ID + "drone_wreck[facing=east]");
    }

    private static void campMiddle(Blueprint b) {
        b.set(23, 0, 22, CAMPFIRE);
        b.box(21, 0, 21, 21, 0, 23, "minecraft:spruce_log[axis=z]");
        b.box(25, 0, 21, 25, 0, 23, "minecraft:spruce_log[axis=z]");
        b.set(22, 0, 25, BEER);
        travel(b, 14, 0, 17);
        // The hologram at the gate, still directing people to a queue that's long gone.
        b.set(7, 0, 18, ID + "holo_projector[facing=north]");
        b.set(5, 0, 14, BURN_BARREL);
        b.set(5, 0, 22, BURN_BARREL);
        // Evacuation supplies nobody came for.
        b.box(27, 0, 25, 30, 0, 28, CRATE);
        b.box(27, 1, 26, 29, 1, 28, CRATE);
        b.set(28, 2, 27, CRATE);
        b.box(26, 0, 29, 30, 0, 29, DRUM);
        b.lootChest(26, 0, 24, "east", SUPPLIES);
    }

    /**
     * The Stranded Hauler, south of the cabin: a driverless semi that stopped on a broken stretch of highway, and the
     * pile-up behind it, which became the wall. Beds in its container, a lookout on its roof. The street lamps are the
     * only ones for miles with power, because someone wired them to the truck's batteries.
     */
    public static Blueprint strandedHauler() {
        Blueprint b = new Blueprint(2004);
        int w = 36;
        int d = 28;
        b.mottled(0, -1, 0, w, -1, d, "minecraft:grass_block", DIRT, 0.2F);
        b.speckle(0, -1, 0, w, d, GRAVEL, 0.05F);
        highway(b, w);
        semi(b);
        wreckWall(b, w, d);
        haulerCamp(b, w, d);
        guards(b, new int[][] {{10, 5, 12}, {18, 5, 13}, {4, 1, 4}, {w - 4, 1, 4}, {4, 1, d - 4}, {w - 4, 1, d - 4}});
        b.mark(NPC + "1", 12, 0, 20);
        aftermath(b, 1, 1, w - 1, d - 1);
        return b;
    }

    /** The road runs east-west straight through, cracked and heaved, with its markings still on it. */
    private static void highway(Blueprint b, int w) {
        b.mottled(0, -1, 8, w, -1, 19, ASPHALT, ID + "cracked_concrete", 0.05F);
        for (int x = 0; x <= w; x++) {
            b.set(x, -1, 13, ID + "asphalt_line[facing=east]");
            if (x % 4 < 2) {
                b.set(x, -1, 9, ID + "asphalt_dashed[facing=east]");
                b.set(x, -1, 18, ID + "asphalt_dashed[facing=east]");
            }
        }
        // Where it broke: slabs at an angle, a hole with rebar in it.
        b.box(29, 0, 16, 31, 0, 17, ID + "asphalt_slab[type=bottom,waterlogged=false]");
        b.box(30, 0, 18, 32, 0, 18, ID + "cracked_concrete_stairs[facing=south]");
        b.set(28, 0, 15, RUBBLE + "[facing=north]");
        b.set(31, 1, 16, ID + "exposed_rebar");
        b.set(32, 1, 17, ID + "exposed_rebar");
        b.set(12, 0, 8, ID + "traffic_cone[facing=north]");
        b.set(15, 0, 8, ID + "traffic_cone[facing=east]");
        b.set(18, 0, 8, ID + "traffic_cone[facing=south]");
    }

    /** No cab windows to speak of, it never needed a driver: a smooth white nose with a sensor bar. */
    private static void semi(Blueprint b) {
        int y = 0;
        // The tractor, nose to the east.
        b.box(24, y, 10, 28, y, 15, EV_BODY);
        b.box(24, y + 1, 10, 26, y + 3, 15, EV_BODY);
        b.box(27, y + 1, 10, 28, y + 1, 15, ID + "ev_panel[type=bottom,waterlogged=false]");
        b.box(24, y + 4, 10, 26, y + 4, 15, ID + "ev_panel[type=bottom,waterlogged=false]");
        b.box(26, y + 3, 11, 26, y + 3, 14, LIGHT_STRIP);
        b.box(26, y + 2, 11, 26, y + 2, 14, ID + "ev_window[facing=east]");
        b.set(25, y + 2, 10, ID + "ev_window[facing=north]");
        b.set(25, y + 2, 15, ID + "ev_window[facing=south]");
        b.set(28, y, 9, ID + "wreck_wheel[facing=south]");
        b.set(28, y, 16, ID + "wreck_wheel[facing=north]");
        b.set(25, y, 9, ID + "wreck_wheel[facing=south]");
        b.set(25, y, 16, ID + "wreck_wheel[facing=north]");
        b.box(22, y + 1, 12, 23, y + 1, 13, "minecraft:chain[axis=x,waterlogged=false]");
        // The trailer: a container you walk into from the west.
        b.box(6, y, 10, 21, y, 15, HAZARD);
        b.box(6, y, 11, 21, y, 14, "minecraft:iron_block");
        containerWall(b, 6, 10, 21, 10, 4);
        containerWall(b, 6, 15, 21, 15, 4);
        containerWall(b, 21, 10, 21, 15, 4);
        b.box(6, y, 10, 21, y, 10, HAZARD);
        b.box(6, y, 15, 21, y, 15, HAZARD);
        b.box(6, y + 4, 10, 21, y + 4, 15, CONTAINER_ORANGE);
        b.mottled(7, y + 5, 11, 20, y + 5, 14, ID + "blue_tarp[facing=down]", AIR, 0.5F);
        for (int x : new int[] {8, 10, 17, 19}) {
            b.set(x, y, 9, ID + "wreck_wheel[facing=south]");
            b.set(x, y, 16, ID + "wreck_wheel[facing=north]");
        }
        bed(b, BED + "1", "gray", 19, y + 1, 11, "east", 18, 11);
        bed(b, BED + "2", "brown", 19, y + 1, 14, "east", 18, 14);
        b.box(11, y + 1, 11, 13, y + 1, 11, CRATE);
        b.set(12, y + 2, 11, CRATE);
        b.lootChest(14, y + 1, 11, "south", "stranded_hauler");
        b.set(8, y + 1, 14, DRUM);
        b.set(9, y + 1, 14, BEER);
        b.set(20, y + 1, 12, ID + "server_rack[facing=east]");
        b.set(7, y + 3, 12, "minecraft:lantern[hanging=true,waterlogged=false]");
        b.box(5, y, 12, 5, y, 13, "minecraft:spruce_stairs[facing=east]");
        // A lookout on the roof: sandbags, and a ladder up the side.
        b.box(9, y + 5, 11, 12, y + 5, 11, SANDBAG_SLAB);
        b.box(16, y + 5, 14, 19, y + 5, 14, SANDBAG_SLAB);
        b.box(20, y, 16, 20, y + 4, 16, "minecraft:ladder[facing=south,waterlogged=false]");
    }

    /** Cars nose to tail, with barriers and sheet metal filling the gaps. A way in on the west, where the road comes from. */
    private static void wreckWall(Blueprint b, int w, int d) {
        for (int x = 1; x < w - 2; x += 7) {
            WreckBuilds.carAlongX(b, x, 1, x % 2 == 1);
            WreckBuilds.carAlongX(b, x, d - 2, x % 2 == 0);
        }
        for (int x = 0; x <= w; x++) {
            if (x % 7 == 6) {
                b.box(x, 0, 2, x, 2, 2, FENCE_LINK);
                b.box(x, 0, d - 1, x, 2, d - 1, FENCE_LINK);
            }
        }
        b.box(0, 0, 1, 0, 2, 7, CORRUGATED);
        b.box(0, 0, 20, 0, 2, d - 1, CORRUGATED);
        b.box(0, 3, 1, 0, 3, 7, WIRE);
        b.box(0, 3, 20, 0, 3, d - 1, WIRE);
        for (int z = 1; z < d; z++) {
            b.set(w, 0, z, ID + "jersey_barrier[facing=east]");
            b.box(w, 1, z, w, 2, z, FENCE_LINK);
        }
        // The west gate: the highway comes in between two barriers.
        b.box(1, 0, 7, 3, 0, 7, ID + "jersey_barrier[facing=north]");
        b.box(1, 0, 20, 3, 0, 20, ID + "jersey_barrier[facing=north]");
        b.set(-1, 1, 7, ID + "sign_keep_out[facing=east]");
        b.set(-1, 1, 20, ID + "sign_shot_on_site[facing=east]");
        b.set(0, 0, 8, TIRES);
        b.set(0, 0, 19, TIRES);
        b.set(0, 1, 8, BURN_BARREL);
        b.set(0, 1, 19, BURN_BARREL);
        // Nests on top of the cars in the corners.
        for (int[] nest : new int[][] {{3, 3}, {w - 5, 3}, {3, d - 5}, {w - 5, d - 5}}) {
            b.box(nest[0], 0, nest[1], nest[0] + 2, 0, nest[1] + 2, GABION);
        }
    }

    private static void haulerCamp(Blueprint b, int w, int d) {
        travel(b, 3, 0, 11);
        b.set(12, 0, 22, CAMPFIRE);
        b.box(10, 0, 21, 10, 0, 23, "minecraft:spruce_log[axis=z]");
        b.box(14, 0, 21, 14, 0, 23, "minecraft:spruce_log[axis=z]");
        b.set(11, 0, 24, BEER);
        // Street lamps left over from the highway. One still works.
        b.box(5, 0, 21, 5, 5, 21, LAMP_POST);
        b.set(5, 5, 22, ID + "street_lamp[facing=north,lit=true]");
        b.box(30, 0, 21, 30, 5, 21, LAMP_POST);
        b.set(30, 5, 22, ID + "street_lamp[facing=north,lit=false]");
        // The ad that used to overlook the road.
        b.box(20, 0, 5, 20, 3, 5, GIRDER_UP);
        b.box(22, 0, 5, 22, 3, 5, GIRDER_UP);
        WreckBuilds.billboard(b, 20, 4, 5, "north");
        // Junk from the pile-up.
        b.set(25, 0, 6, DRONE);
        b.set(31, 0, 11, SCRAP_PILE + "[facing=west]");
        b.set(18, 0, 22, ID + "robot_scrap[facing=south]");
        b.set(21, 0, 24, ID + "dead_robot[facing=north]");
        b.box(23, 0, 22, 25, 0, 23, CRATE);
        b.set(24, 1, 23, DRUM);
    }
}
