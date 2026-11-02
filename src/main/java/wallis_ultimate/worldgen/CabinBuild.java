package wallis_ultimate.worldgen;

/**
 * STORY-002, Q-147: the rednecks' run-down hunting cabin at world spawn. Everyone wakes up in front of it, by the
 * campfire on its north side, looking south at the porch (players always spawn looking south). Porch with a sagging
 * couch and beer cans, antlers over the door, moonshine still out back, rusty pickup on the east side. Placeholder
 * build by Claude; the look is the user's to change in words.
 */
public final class CabinBuild {
    public static final String SPAWN = "spawn";
    private static final long SEED = 1001;
    private static final String PLANKS = "minecraft:spruce_planks";
    private static final String LOG_UP = "minecraft:spruce_log[axis=y]";
    private static final String LOG_X = "minecraft:spruce_log[axis=x]";
    private static final String LOG_Z = "minecraft:spruce_log[axis=z]";
    private static final String ID = "wallis_ultimate:";

    private CabinBuild() {
    }

    public static Blueprint blueprint() {
        Blueprint b = new Blueprint(SEED);
        ground(b);
        floorAndWalls(b);
        roof(b);
        porch(b);
        inside(b);
        campfire(b);
        still(b);
        WreckBuilds.pickupAlongZ(b, 22, 0, 9);
        b.set(21, 0, 15, ID + "wreck_wheel[facing=east]"); // the spare, leaning on the truck
        outhouse(b);
        woodpile(b);
        return b;
    }

    /** Trodden earth around the cabin: from the fire to the steps, and to the truck. */
    private static void ground(Blueprint b) {
        b.mottled(11, -1, 1, 15, -1, 9, "minecraft:coarse_dirt", "minecraft:rooted_dirt", 0.2F);
        b.mottled(15, -1, 6, 24, -1, 9, "minecraft:coarse_dirt", "minecraft:gravel", 0.1F);
        b.mottled(21, -1, 8, 25, -1, 16, "minecraft:coarse_dirt", "minecraft:gravel", 0.1F);
        b.mottled(7, -1, 20, 18, -1, 27, "minecraft:coarse_dirt", "minecraft:rooted_dirt", 0.2F);
    }

    private static void floorAndWalls(Blueprint b) {
        b.box(8, 0, 13, 18, 0, 19, PLANKS);
        b.mottled(9, 1, 13, 17, 5, 13, PLANKS, "minecraft:stripped_spruce_log[axis=x]", 0.15F);
        b.mottled(9, 1, 19, 17, 5, 19, PLANKS, "minecraft:stripped_spruce_log[axis=x]", 0.15F);
        b.mottled(8, 1, 14, 8, 5, 18, PLANKS, "minecraft:stripped_spruce_log[axis=z]", 0.15F);
        b.mottled(18, 1, 14, 18, 5, 18, PLANKS, "minecraft:stripped_spruce_log[axis=z]", 0.15F);
        // Corner logs on a stone footing.
        for (int x : new int[] {8, 18}) {
            for (int z : new int[] {13, 19}) {
                b.set(x, -1, z, "minecraft:mossy_cobblestone");
                b.set(x, 0, z, "minecraft:cobblestone");
                b.box(x, 1, z, x, 5, z, LOG_UP);
            }
        }
        // The triangles under the roof, at both ends.
        for (int x : new int[] {8, 18}) {
            b.box(x, 6, 14, x, 6, 18, PLANKS);
            b.box(x, 7, 15, x, 7, 17, PLANKS);
            b.set(x, 8, 16, PLANKS);
        }
        // Front door, with a window on each side: one with cracked glass, one boarded up.
        b.set(13, 1, 13, "minecraft:spruce_door[facing=south,half=lower,hinge=left,open=false]");
        b.set(13, 2, 13, "minecraft:spruce_door[facing=south,half=upper,hinge=left,open=false]");
        b.box(10, 2, 13, 11, 3, 13, ID + "wreck_window[facing=south]");
        b.box(15, 2, 13, 16, 3, 13, "just_barricades:spruce_barricade[facing=north,health=3]");
        // West and east windows, the back door.
        b.box(8, 2, 15, 8, 3, 17, ID + "wreck_window[facing=west]");
        b.box(18, 2, 15, 18, 3, 16, "just_barricades:spruce_barricade[facing=west,health=3]");
        b.set(11, 1, 19, "minecraft:spruce_door[facing=north,half=lower,hinge=right,open=false]");
        b.set(11, 2, 19, "minecraft:spruce_door[facing=north,half=upper,hinge=right,open=false]");
        // Rafters across the room.
        b.box(9, 5, 15, 17, 5, 15, LOG_X);
        b.box(9, 5, 17, 17, 5, 17, LOG_X);
    }

    /** Ridge along x; the north side runs on down over the porch. Worn shingles, and a tarp over the worst hole. */
    private static void roof(Blueprint b) {
        String south = "minecraft:spruce_stairs[facing=south]";
        String north = "minecraft:spruce_stairs[facing=north]";
        String southWorn = "minecraft:dark_oak_stairs[facing=south]";
        String northWorn = "minecraft:dark_oak_stairs[facing=north]";
        int[][] northSide = {{15, 8}, {14, 7}, {13, 6}, {12, 5}, {11, 4}, {10, 3}};
        for (int[] row : northSide) {
            b.mottled(7, row[1], row[0], 19, row[1], row[0], south, southWorn, 0.3F);
        }
        int[][] southSide = {{17, 8}, {18, 7}, {19, 6}, {20, 5}};
        for (int[] row : southSide) {
            b.mottled(7, row[1], row[0], 19, row[1], row[0], north, northWorn, 0.3F);
        }
        b.mottled(7, 9, 16, 19, 9, 16, "minecraft:spruce_slab[type=bottom]", "minecraft:dark_oak_slab[type=bottom]", 0.3F);
        b.box(14, 7, 18, 16, 7, 18, "minecraft:air");
        b.box(14, 6, 19, 16, 6, 19, "minecraft:air");
        b.box(13, 8, 18, 17, 8, 18, ID + "blue_tarp[facing=down]");
        b.box(13, 7, 19, 17, 7, 19, ID + "blue_tarp[facing=down]");
    }

    private static void porch(Blueprint b) {
        b.mottled(8, 0, 10, 18, 0, 12, PLANKS, "minecraft:spruce_slab[type=top]", 0.12F);
        b.box(12, 0, 9, 14, 0, 9, "minecraft:spruce_stairs[facing=south]");
        for (int x : new int[] {8, 11, 15, 18}) {
            b.box(x, 1, 10, x, 2, 10, "minecraft:spruce_fence");
        }
        // The sagging couch, an old chair, and what they drank.
        b.box(9, 1, 12, 11, 1, 12, "another_furniture:brown_sofa[facing=north,type=single]");
        b.set(16, 1, 12, "another_furniture:spruce_chair[facing=north,tucked=false,variant=1]");
        b.set(10, 1, 11, ID + "beer_cans[facing=north]");
        b.set(15, 1, 11, ID + "beer_cans[facing=east]");
        b.set(9, 1, 10, ID + "beer_cans[facing=west]");
        b.set(17, 1, 12, ID + "oil_drum");
        b.set(13, 4, 12, ID + "antlers[facing=south]");
        b.set(12, 3, 12, "minecraft:wall_torch[facing=north]");
    }

    private static void inside(Blueprint b) {
        b.set(13, 1, 16, "another_furniture:spruce_table");
        b.set(12, 1, 16, "another_furniture:spruce_chair[facing=east,tucked=false,variant=1]");
        b.set(14, 1, 16, "another_furniture:spruce_chair[facing=west,tucked=false,variant=1]");
        b.set(13, 2, 16, ID + "beer_cans[facing=north]");
        b.box(15, 1, 18, 17, 1, 18, "another_furniture:spruce_drawer[facing=north,open=false]");
        b.set(16, 2, 18, ID + "beer_cans[facing=south]");
        b.set(9, 1, 18, "minecraft:furnace[facing=east,lit=false]");
        b.set(9, 1, 14, "minecraft:crafting_table");
        b.box(9, 3, 18, 10, 3, 18, "another_furniture:spruce_shelf[facing=north,horizontal=single]");
        b.mottled(11, 1, 14, 12, 1, 15, "minecraft:brown_carpet", "minecraft:air", 0.2F);
        b.box(17, 1, 14, 17, 1, 15, "minecraft:barrel[facing=up,open=false]");
        b.set(17, 2, 14, ID + "supply_crate");
        b.lootChest(17, 1, 16, "west", "cabin");
        b.set(9, 4, 14, "minecraft:cobweb");
        b.set(17, 4, 18, "minecraft:cobweb");
        b.set(13, 4, 16, "minecraft:lantern[hanging=true,waterlogged=false]");
        b.set(13, 5, 16, "minecraft:chain[axis=y,waterlogged=false]");
    }

    /** Where everyone wakes up: a fire ring with logs to sit on. */
    private static void campfire(Blueprint b) {
        b.set(13, 0, 3, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]");
        int[][] ring = {{12, 2}, {13, 2}, {14, 2}, {12, 3}, {14, 3}, {12, 4}, {13, 4}, {14, 4}};
        for (int[] stone : ring) {
            b.set(stone[0], -1, stone[1], "minecraft:cobblestone");
        }
        b.box(10, 0, 2, 10, 0, 4, LOG_Z);
        b.box(16, 0, 2, 16, 0, 4, LOG_Z);
        b.set(11, 0, 5, ID + "beer_cans[facing=north]");
        b.set(15, 0, 1, ID + "beer_cans[facing=east]");
        // North of the fire, so the first thing anyone sees is the fire, the steps and the porch behind it.
        b.mark(SPAWN, 13, 0, 0);
    }

    /** Out back: a copper pot over a fire, a pipe to the barrel, jugs and barrels all around. */
    private static void still(Blueprint b) {
        b.set(11, 0, 24, "minecraft:campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]");
        b.set(11, 1, 24, "minecraft:waxed_copper_block");
        b.set(11, 2, 24, "minecraft:waxed_cut_copper_slab[type=bottom,waterlogged=false]");
        b.set(11, 3, 24, "minecraft:lightning_rod[facing=up,powered=false,waterlogged=false]");
        b.set(12, 2, 24, "minecraft:lightning_rod[facing=east,powered=false,waterlogged=false]");
        b.set(13, 2, 24, "minecraft:lightning_rod[facing=east,powered=false,waterlogged=false]");
        b.set(14, 0, 24, "minecraft:barrel[facing=up,open=false]");
        b.set(14, 1, 24, "minecraft:cauldron");
        b.set(15, 0, 23, "minecraft:barrel[facing=up,open=false]");
        b.set(15, 0, 25, "minecraft:barrel[facing=up,open=false]");
        b.set(15, 1, 25, "minecraft:barrel[facing=up,open=false]");
        b.set(13, 0, 26, "minecraft:decorated_pot[facing=north,waterlogged=false]");
        b.set(12, 0, 26, "minecraft:flower_pot");
        b.set(9, 0, 23, ID + "supply_crate");
        b.set(9, 0, 24, ID + "beer_cans[facing=north]");
        // A tarp on poles, to keep the rain out of the mash.
        for (int x : new int[] {8, 17}) {
            for (int z : new int[] {21, 27}) {
                b.box(x, 0, z, x, 3, z, "minecraft:spruce_fence");
            }
        }
        b.box(8, 4, 21, 17, 4, 27, ID + "green_tarp[facing=down]");
    }

    /** Every cabin in the woods has one. */
    private static void outhouse(Blueprint b) {
        b.box(2, 0, 20, 4, 0, 22, PLANKS);
        b.box(2, 1, 20, 2, 3, 22, PLANKS);
        b.box(4, 1, 20, 4, 3, 22, PLANKS);
        b.box(3, 1, 22, 3, 3, 22, PLANKS);
        b.box(2, 4, 20, 4, 4, 22, "minecraft:spruce_slab[type=bottom]");
        b.set(3, 1, 20, "minecraft:spruce_door[facing=south,half=lower,hinge=left,open=false]");
        b.set(3, 2, 20, "minecraft:spruce_door[facing=south,half=upper,hinge=left,open=false]");
        b.set(3, 1, 21, "minecraft:spruce_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]");
    }

    private static void woodpile(Blueprint b) {
        b.box(4, 0, 12, 6, 0, 16, LOG_X);
        b.box(4, 1, 13, 6, 1, 15, LOG_X);
        b.set(5, 2, 14, LOG_X);
        b.set(5, 0, 18, "minecraft:spruce_log[axis=y]");
    }
}
