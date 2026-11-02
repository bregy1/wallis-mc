package wallis_ultimate.worldgen;

/**
 * SURV-006, Q-146: district 1's four safe spots, each different. Two big fortified bases built from found materials
 * (a junkyard, a gas station), two small ones (a tent camp, a stranded truck). Placeholder builds by Claude. Every one
 * has marks for its beds and its travel signpost.
 */
public final class SafeSpotBuilds {
    static final String TRAVEL = "travel";
    static final String BED = "bed";
    private static final String ID = "wallis_ultimate:";
    private static final String SANDBAGS = ID + "sandbags";
    private static final String SANDBAG_SLAB = ID + "sandbag_slab[type=bottom,waterlogged=false]";
    private static final String WIRE = ID + "barbed_wire";
    private static final String DRUM = ID + "oil_drum";
    private static final String BURN_BARREL = ID + "oil_drum[lit=true]";
    private static final String CRATE = ID + "supply_crate";
    private static final String BODY = ID + "wreck_body";
    private static final String PANEL = ID + "wreck_panel[type=bottom,waterlogged=false]";
    private static final String PALISADE = "decorative_blocks:spruce_palisade";
    private static final String PLANKS = "minecraft:spruce_planks";
    private static final String LOG_UP = "minecraft:spruce_log[axis=y]";
    private static final String FENCE = "minecraft:spruce_fence";
    private static final String BARRICADE = "just_barricades:spruce_barricade[facing=north,health=3]";
    private static final String DIRT = "minecraft:coarse_dirt";
    private static final String GRAVEL = "minecraft:gravel";
    private static final String SUPPLIES = "supplies";

    private SafeSpotBuilds() {
    }

    /** The signpost. Travellers arrive one block south of it, so that block stays free in every safe spot. */
    private static void travel(Blueprint b, int x, int y, int z) {
        b.set(x, y, z, ID + "travel_signpost");
        b.mark(TRAVEL, x, y, z);
    }

    /** Beds point their heads the way the bed block's facing says; the foot is the block before that. */
    private static void bed(Blueprint b, String mark, String color, int x, int y, int z, String facing, int footX, int footZ) {
        b.set(x, y, z, "minecraft:" + color + "_bed[facing=" + facing + ",part=head,occupied=false]");
        b.set(footX, y, footZ, "minecraft:" + color + "_bed[facing=" + facing + ",part=foot,occupied=false]");
        b.mark(mark, x, y, z);
    }

    /**
     * Earl's Salvage, north of the cabin: a junkyard turned into a fort. A palisade with wrecks stacked along it, one
     * gate facing south to the cabin between sandbags and wire, two lookout towers, a sleeping shed in the yard.
     */
    public static Blueprint earlsSalvage() {
        Blueprint b = new Blueprint(2001);
        int size = 40;
        b.mottled(0, -1, 0, size, -1, size, DIRT, GRAVEL, 0.3F);
        b.box(0, 0, 0, size, 2, 0, PALISADE);
        b.box(0, 0, size, size, 2, size, PALISADE);
        b.box(0, 0, 0, 0, 2, size, PALISADE);
        b.box(size, 0, 0, size, 2, size, PALISADE);
        // The gate, in the south wall.
        b.box(18, 0, size, 22, 2, size, "minecraft:air");
        b.box(17, 0, size - 1, 17, 3, size + 1, LOG_UP);
        b.box(23, 0, size - 1, 23, 3, size + 1, LOG_UP);
        b.box(17, 4, size, 23, 4, size, "minecraft:spruce_log[axis=x]");
        b.box(13, 0, size + 2, 16, 1, size + 2, SANDBAGS);
        b.box(24, 0, size + 2, 27, 1, size + 2, SANDBAGS);
        b.box(13, 2, size + 2, 16, 2, size + 2, SANDBAG_SLAB);
        b.box(24, 2, size + 2, 27, 2, size + 2, SANDBAG_SLAB);
        b.box(4, 0, size + 4, 15, 0, size + 4, WIRE);
        b.box(25, 0, size + 4, 36, 0, size + 4, WIRE);
        b.set(16, 1, size + 1, BURN_BARREL);
        b.set(24, 1, size + 1, BURN_BARREL);
        b.set(16, 0, size + 1, DRUM);
        b.set(24, 0, size + 1, DRUM);
        tower(b, 2, 2);
        tower(b, size - 6, 2);
        wreckStacks(b, size);
        shed(b);
        yard(b);
        return b;
    }

    /** A lookout on four legs, with a ladder, a sandbagged platform and a tarp over it. */
    private static void tower(Blueprint b, int x, int z) {
        for (int legX : new int[] {x, x + 4}) {
            for (int legZ : new int[] {z, z + 4}) {
                b.box(legX, 0, legZ, legX, 6, legZ, LOG_UP);
            }
        }
        b.box(x, 7, z, x + 4, 7, z + 4, PLANKS);
        b.box(x, 8, z, x + 4, 8, z, SANDBAG_SLAB);
        b.box(x, 8, z + 4, x + 4, 8, z + 4, SANDBAG_SLAB);
        b.box(x, 8, z, x, 8, z + 4, SANDBAG_SLAB);
        b.box(x + 4, 8, z, x + 4, 8, z + 4, SANDBAG_SLAB);
        // The ladder runs up a plank column and over the edge, through a gap in the sandbags.
        b.box(x + 2, 0, z + 4, x + 2, 6, z + 4, PLANKS);
        b.box(x + 2, 0, z + 5, x + 2, 7, z + 5, "minecraft:ladder[facing=south,waterlogged=false]");
        b.set(x + 2, 8, z + 4, "minecraft:air");
        for (int legX : new int[] {x, x + 4}) {
            for (int legZ : new int[] {z, z + 4}) {
                b.box(legX, 8, legZ, legX, 10, legZ, FENCE);
            }
        }
        b.box(x, 11, z, x + 4, 11, z + 4, ID + "green_tarp[facing=down]");
        b.set(x + 1, 8, z + 1, CRATE);
        b.set(x + 3, 8, z + 1, ID + "beer_cans[facing=north]");
    }

    /** Wrecks stacked two high along the east and west walls: the fort is made of what it salvages. */
    private static void wreckStacks(Blueprint b, int size) {
        for (int z = 10; z < size - 4; z += 7) {
            b.box(1, 0, z, 3, 0, z + 5, BODY);
            b.box(1, 1, z, 3, 1, z + 1, PANEL);
            b.box(1, 1, z + 2, 3, 2, z + 3, BODY);
            b.box(1, 3, z + 2, 3, 3, z + 3, PANEL);
            b.set(4, 0, z + 1, ID + "wreck_wheel[facing=west]");
            b.set(4, 0, z + 4, ID + "wreck_wheel[facing=west]");
        }
        for (int z = 10; z < size - 4; z += 7) {
            b.box(size - 3, 0, z, size - 1, 0, z + 5, BODY);
            b.box(size - 3, 1, z + 3, size - 1, 1, z + 5, PANEL);
            b.box(size - 3, 1, z, size - 1, 2, z + 2, BODY);
            b.set(size - 4, 0, z + 1, ID + "wreck_wheel[facing=east]");
            b.set(size - 4, 0, z + 4, ID + "wreck_wheel[facing=east]");
        }
    }

    /** Beds under a tin roof, in the middle of the yard. */
    private static void shed(Blueprint b) {
        int x = 11;
        int z = 12;
        b.box(x, 0, z, x + 12, 0, z + 7, PLANKS);
        b.box(x, 1, z, x + 12, 3, z, PLANKS);
        b.box(x, 1, z + 7, x + 12, 3, z + 7, PLANKS);
        b.box(x, 1, z, x, 3, z + 7, PLANKS);
        b.box(x + 12, 1, z, x + 12, 3, z + 7, PLANKS);
        b.box(x - 1, 4, z - 1, x + 13, 4, z + 8, "minecraft:iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]");
        b.box(x + 2, 2, z + 7, x + 3, 2, z + 7, BARRICADE);
        b.box(x + 9, 2, z + 7, x + 10, 2, z + 7, BARRICADE);
        b.set(x + 6, 1, z + 7, "minecraft:spruce_door[facing=north,half=lower,hinge=left,open=false]");
        b.set(x + 6, 2, z + 7, "minecraft:spruce_door[facing=north,half=upper,hinge=left,open=false]");
        bed(b, BED + "1", "red", x + 2, 1, z + 1, "north", x + 2, z + 2);
        bed(b, BED + "2", "brown", x + 5, 1, z + 1, "north", x + 5, z + 2);
        bed(b, BED + "3", "green", x + 8, 1, z + 1, "north", x + 8, z + 2);
        bed(b, BED + "4", "gray", x + 10, 1, z + 1, "north", x + 10, z + 2);
        b.set(x + 1, 1, z + 6, "minecraft:lantern[hanging=false,waterlogged=false]");
        b.lootChest(x + 11, 1, z + 4, "west", "earls_salvage");
        b.set(x + 11, 1, z + 6, CRATE);
        b.set(x + 11, 2, z + 6, ID + "beer_cans[facing=north]");
    }

    private static void yard(Blueprint b) {
        b.set(20, 0, 25, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]");
        b.box(18, 0, 24, 18, 0, 26, "minecraft:spruce_log[axis=z]");
        b.box(22, 0, 24, 22, 0, 26, "minecraft:spruce_log[axis=z]");
        travel(b, 20, 0, 31);
        b.set(9, 0, 28, BURN_BARREL);
        b.set(31, 0, 28, BURN_BARREL);
        b.box(26, 0, 24, 29, 0, 26, CRATE);
        b.box(26, 1, 25, 28, 1, 26, CRATE);
        b.set(27, 2, 26, CRATE);
        b.box(10, 0, 23, 12, 1, 24, DRUM);
        b.set(13, 0, 25, DRUM);
        b.set(26, 2, 26, ID + "beer_cans[facing=west]");
        b.lootChest(29, 1, 25, "west", SUPPLIES);
        // A tarp on poles over the crates.
        for (int x : new int[] {25, 30}) {
            for (int z : new int[] {23, 27}) {
                b.box(x, 0, z, x, 3, z, FENCE);
            }
        }
        b.box(25, 4, 23, 30, 4, 27, ID + "blue_tarp[facing=down]");
    }

    /**
     * Gas-N-Guns, west of the cabin: a roadside gas station made into a strongpoint. Canopy over two pumps out front,
     * sandbags and wire around the lot, windows boarded up, beds in the shop.
     */
    public static Blueprint gasNGuns() {
        Blueprint b = new Blueprint(2002);
        b.mottled(0, -1, 0, 30, -1, 26, "minecraft:gray_concrete", "minecraft:gravel", 0.2F);
        // The shop, along the north side of the lot, facing south.
        int x = 6;
        int z = 2;
        b.box(x, 0, z, x + 18, 0, z + 8, "minecraft:polished_andesite");
        b.box(x, 1, z, x + 18, 4, z, "minecraft:bricks");
        b.box(x, 1, z + 8, x + 18, 4, z + 8, "minecraft:bricks");
        b.box(x, 1, z, x, 4, z + 8, "minecraft:bricks");
        b.box(x + 18, 1, z, x + 18, 4, z + 8, "minecraft:bricks");
        b.box(x - 1, 5, z - 1, x + 19, 5, z + 9, "minecraft:smooth_stone_slab[type=bottom,waterlogged=false]");
        b.box(x, 5, z + 9, x + 18, 5, z + 9, "minecraft:red_concrete");
        b.box(x + 3, 2, z + 8, x + 6, 3, z + 8, BARRICADE);
        b.box(x + 12, 2, z + 8, x + 15, 3, z + 8, BARRICADE);
        b.set(x + 9, 1, z + 8, "minecraft:spruce_door[facing=north,half=lower,hinge=left,open=false,powered=false]");
        b.set(x + 9, 2, z + 8, "minecraft:spruce_door[facing=north,half=upper,hinge=left,open=false,powered=false]");
        bed(b, BED + "1", "blue", x + 2, 1, z + 1, "north", x + 2, z + 2);
        bed(b, BED + "2", "red", x + 4, 1, z + 1, "north", x + 4, z + 2);
        bed(b, BED + "3", "gray", x + 6, 1, z + 1, "north", x + 6, z + 2);
        b.box(x + 10, 1, z + 1, x + 16, 2, z + 1, "another_furniture:spruce_shelf[facing=north,horizontal=single]");
        b.box(x + 10, 1, z + 4, x + 15, 1, z + 4, "another_furniture:spruce_drawer[facing=north,open=false]");
        b.set(x + 12, 2, z + 4, ID + "beer_cans[facing=south]");
        b.set(x + 16, 1, z + 7, CRATE);
        b.set(x + 1, 1, z + 7, CRATE);
        b.lootChest(x + 17, 1, z + 4, "west", "gas_n_guns");
        b.lootChest(3, 0, 22, "east", SUPPLIES);
        b.set(x + 9, 4, z + 4, "minecraft:lantern[hanging=true,waterlogged=false]");
        // The canopy with its two pumps.
        for (int legX : new int[] {9, 21}) {
            for (int legZ : new int[] {14, 19}) {
                b.box(legX, 0, legZ, legX, 4, legZ, "minecraft:iron_block");
            }
        }
        b.box(7, 5, 13, 23, 5, 20, "minecraft:white_concrete");
        b.box(7, 5, 13, 23, 5, 13, "minecraft:red_concrete");
        b.box(7, 5, 20, 23, 5, 20, "minecraft:red_concrete");
        for (int pumpX : new int[] {13, 17}) {
            b.box(pumpX, 0, 16, pumpX, 0, 17, "minecraft:polished_andesite");
            b.box(pumpX, 1, 16, pumpX, 2, 16, "minecraft:red_concrete");
            b.set(pumpX, 3, 16, "minecraft:white_concrete");
        }
        travel(b, 15, 0, 12);
        // The defences around the lot, open at the south where the road comes in.
        b.box(0, 0, 0, 30, 1, 0, SANDBAGS);
        b.box(0, 0, 0, 0, 1, 26, SANDBAGS);
        b.box(30, 0, 0, 30, 1, 26, SANDBAGS);
        b.box(0, 2, 0, 30, 2, 0, SANDBAG_SLAB);
        b.box(0, 2, 0, 0, 2, 26, SANDBAG_SLAB);
        b.box(30, 2, 0, 30, 2, 26, SANDBAG_SLAB);
        b.box(0, 0, 26, 11, 1, 26, SANDBAGS);
        b.box(19, 0, 26, 30, 1, 26, SANDBAGS);
        b.box(0, 2, 26, 11, 2, 26, SANDBAG_SLAB);
        b.box(19, 2, 26, 30, 2, 26, SANDBAG_SLAB);
        b.box(0, 0, 28, 11, 0, 28, WIRE);
        b.box(19, 0, 28, 30, 0, 28, WIRE);
        b.set(12, 0, 25, BURN_BARREL);
        b.set(18, 0, 25, BURN_BARREL);
        b.set(2, 0, 24, DRUM);
        b.set(3, 0, 24, DRUM);
        b.set(2, 0, 23, DRUM);
        b.set(2, 1, 24, DRUM);
        b.set(27, 0, 23, CRATE);
        b.set(28, 0, 23, CRATE);
        b.set(28, 0, 24, CRATE);
        b.set(28, 1, 24, CRATE);
        WreckBuilds.pickupAlongZ(b, 3, 0, 12);
        return b;
    }

    /** Camp Tarp, east of the cabin: three tents around a fire, a loose ring of wire, not much else. */
    public static Blueprint campTarp() {
        Blueprint b = new Blueprint(2003);
        b.mottled(1, -1, 1, 23, -1, 21, "minecraft:grass_block", DIRT, 0.45F);
        tent(b, 2, 3, "blue", BED + "1", "red");
        tent(b, 16, 3, "green", BED + "2", "blue");
        tent(b, 9, 14, "green", BED + "3", "brown");
        b.set(12, 0, 8, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]");
        b.box(10, 0, 7, 10, 0, 9, "minecraft:spruce_log[axis=z]");
        b.box(14, 0, 7, 14, 0, 9, "minecraft:spruce_log[axis=z]");
        b.set(11, 0, 5, CRATE);
        b.set(12, 0, 5, CRATE);
        b.set(11, 1, 5, ID + "beer_cans[facing=north]");
        b.set(13, 0, 5, DRUM);
        b.lootChest(10, 0, 5, "south", "camp_tarp");
        travel(b, 12, 0, 11);
        b.set(5, 0, 12, BURN_BARREL);
        b.set(19, 0, 12, BURN_BARREL);
        for (int x = 0; x <= 24; x += 2) {
            b.set(x, 0, 0, WIRE);
            b.set(x, 0, 23, WIRE);
        }
        for (int z = 2; z <= 21; z += 2) {
            b.set(0, 0, z, WIRE);
            b.set(24, 0, z, WIRE);
        }
        return b;
    }

    /** Six long, four wide, with a bed inside. The ridge runs along z; the opening faces south. */
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

    /**
     * The Stranded Hauler, south of the cabin: a semi that ran out of road. Beds in the open-backed trailer, a burn
     * barrel and a few sandbags. Small, and the first place most teams find.
     */
    public static Blueprint strandedHauler() {
        Blueprint b = new Blueprint(2004);
        b.mottled(0, -1, 0, 26, -1, 18, "minecraft:grass_block", GRAVEL, 0.4F);
        // The cab, with its nose to the east.
        int y = 0;
        b.box(20, y, 6, 24, y, 9, BODY);
        b.box(20, y + 1, 6, 22, y + 3, 9, BODY);
        b.box(23, y + 1, 6, 24, y + 1, 9, PANEL);
        b.box(20, y + 4, 6, 22, y + 4, 9, PANEL);
        b.box(22, y + 2, 7, 22, y + 3, 8, ID + "wreck_window[facing=east]");
        b.box(21, y + 2, 6, 21, y + 2, 6, ID + "wreck_window[facing=north]");
        b.box(21, y + 2, 9, 21, y + 2, 9, ID + "wreck_window[facing=south]");
        b.set(24, y, 5, ID + "wreck_wheel[facing=south]");
        b.set(24, y, 10, ID + "wreck_wheel[facing=north]");
        b.set(21, y, 5, ID + "wreck_wheel[facing=south]");
        b.set(21, y, 10, ID + "wreck_wheel[facing=north]");
        // The trailer, a long box you walk into from the west.
        b.box(3, y, 5, 18, y, 10, "minecraft:iron_block");
        b.mottled(3, y + 1, 5, 18, y + 3, 5, "minecraft:white_concrete", "minecraft:light_gray_concrete", 0.2F);
        b.mottled(3, y + 1, 10, 18, y + 3, 10, "minecraft:white_concrete", "minecraft:light_gray_concrete", 0.2F);
        b.mottled(18, y + 1, 5, 18, y + 3, 10, "minecraft:white_concrete", "minecraft:light_gray_concrete", 0.2F);
        b.mottled(3, y + 4, 5, 18, y + 4, 10, "minecraft:white_concrete", ID + "blue_tarp[facing=up]", 0.15F);
        for (int x : new int[] {5, 7, 14, 16}) {
            b.set(x, y, 4, ID + "wreck_wheel[facing=south]");
            b.set(x, y, 11, ID + "wreck_wheel[facing=north]");
        }
        bed(b, BED + "1", "gray", 16, y + 1, 6, "east", 15, 6);
        bed(b, BED + "2", "brown", 16, y + 1, 9, "east", 15, 9);
        b.box(10, y + 1, 6, 12, y + 1, 6, CRATE);
        b.set(11, y + 2, 6, CRATE);
        b.lootChest(13, y + 1, 6, "south", "stranded_hauler");
        b.set(8, y + 1, 9, DRUM);
        b.set(9, y + 1, 9, ID + "beer_cans[facing=north]");
        b.set(7, y + 3, 7, "minecraft:lantern[hanging=true,waterlogged=false]");
        // Out by the open end, with a step up into the trailer.
        b.box(2, y, 7, 2, y, 8, "minecraft:spruce_stairs[facing=east]");
        travel(b, 0, y, 6);
        b.set(1, y, 12, BURN_BARREL);
        b.box(0, y, 2, 4, y, 2, SANDBAGS);
        b.box(0, y + 1, 2, 4, y + 1, 2, SANDBAG_SLAB);
        b.box(0, y, 14, 4, y, 14, SANDBAGS);
        b.box(0, y + 1, 14, 4, y + 1, 14, SANDBAG_SLAB);
        return b;
    }
}
