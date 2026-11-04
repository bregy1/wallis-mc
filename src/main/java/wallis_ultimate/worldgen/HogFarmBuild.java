package wallis_ultimate.worldgen;

import java.util.List;

/**
 * ENEMY-011, Q-150: Hog Daddy's farm, the arena of the Chapter 1 boss. A pig pen with posts and a feeding trough in the
 * north, a red barn as the only way through, and a cornfield in the south that makes the team run a long U from the
 * little cabin at its far end. All of it inside a ring of crushed cars and hay bales that can't be broken. Placeholder
 * build by Claude; the look is the user's to change in words. Nothing can be mined or placed on the footprint
 * (boss/HogFarmRules), so the normal blocks in it are safe too.
 */
public final class HogFarmBuild {
    public static final int SIZE_X = 64;
    public static final int SIZE_Z = 100;
    /** Where the team starts: inside the cabin, its door to the west. */
    public static final String CHASE_START = "chase_start";
    /** Where he waits: outside the cabin's east wall. */
    public static final String BOSS_START = "boss_start";
    public static final String WALL_FROM = "wall_from";
    public static final String WALL_TO = "wall_to";
    public static final String GATE_FROM = "gate_from";
    public static final String GATE_TO = "gate_to";
    public static final String BELL = "bell";
    /** Where he comes down in the pen. */
    public static final String PEN_LANDING = "pen_landing";
    /** Its z is the barn's south door: anyone north of it has reached the barn. */
    public static final String BARN_LINE = "barn_line";
    /** Where piglets come out, numbered from 0. */
    public static final String PIGLET = "piglet";

    private static final long SEED = 1101;
    private static final String ID = "wallis_ultimate:";
    private static final String HAY_RING = ID + "strapped_hay";
    private static final String WRECK_RING = ID + "crushed_wreck";
    private static final String POST = ID + "pen_post[facing=north]";
    private static final String TROUGH = ID + "feeding_trough[facing=north]";
    private static final String CORN_LOWER = ID + "corn[half=lower]";
    private static final String CORN_UPPER = ID + "corn[half=upper]";
    private static final String BURN_BARREL = ID + "oil_drum[lit=true]";
    private static final String AIR = "minecraft:air";
    private static final String PLANKS = "minecraft:spruce_planks";
    private static final String BARN_WALL = "minecraft:red_terracotta";
    private static final String BARN_TRIM = "minecraft:white_terracotta";
    private static final String HAY = "minecraft:hay_block[axis=x]";

    private static final int RING = 2;
    private static final int RING_SEGMENT = 7;
    private static final int RING_LOW = 5;
    private static final int RING_HIGH = 7;
    private static final int PEN_END_Z = 37;
    private static final int PEN_WALL_Z = 38;
    private static final int PEN_WALL_HEIGHT = 4;
    private static final int BARN_WEST = 40;
    private static final int BARN_EAST = 55;
    private static final int BARN_NORTH = 34;
    private static final int BARN_SOUTH = 54;
    private static final int BARN_DOOR_WEST = 45;
    private static final int BARN_DOOR_EAST = 50;
    private static final int BARN_WALL_TOP = 5;
    /** The hay wall down the middle of the cornfield, open at the west end: that's the U. */
    private static final int DIVIDER_NORTH = 75;
    private static final int DIVIDER_SOUTH = 77;
    private static final int DIVIDER_WEST = 14;
    private static final int DIVIDER_TOP = 2;
    private static final int NORTH_LANE_START = 55;
    private static final int SOUTH_LANE_START = 78;
    private static final int LANE_DEPTH = 19;
    private static final int CORN_ROW_SPACING = 3;
    private static final int CABIN_WEST = 46;
    private static final int CABIN_EAST = 56;
    private static final int CABIN_NORTH = 82;
    private static final int CABIN_SOUTH = 92;
    private static final int CABIN_WALL_TOP = 3;
    private static final int CABIN_DOOR_NORTH = 86;
    private static final int CABIN_DOOR_SOUTH = 88;
    private static final int POST_TOP = 2;
    private static final List<int[]> POSTS = List.of(new int[] {11, 11}, new int[] {23, 11}, new int[] {39, 11}, new int[] {51, 11},
            new int[] {11, 27}, new int[] {23, 27}, new int[] {39, 27}, new int[] {51, 27});
    private static final int TROUGH_WEST = 28;
    private static final int TROUGH_EAST = 35;
    private static final int TROUGH_Z = 19;
    private static final int[] HUTCHES = {8, 22, 40};
    private static final int HUTCH_WIDTH = 5;
    private static final int HUTCH_DEPTH = 4;

    private HogFarmBuild() {
    }

    public static Blueprint blueprint() {
        Blueprint b = new Blueprint(SEED);
        ground(b);
        ring(b);
        penWall(b);
        cornfield(b);
        divider(b);
        pen(b);
        barn(b);
        cabin(b);
        gate(b);
        b.mark(BOSS_START, CABIN_EAST + 3, 0, (CABIN_DOOR_NORTH + CABIN_DOOR_SOUTH) / 2);
        b.mark(BARN_LINE, (BARN_DOOR_WEST + BARN_DOOR_EAST) / 2, 0, BARN_SOUTH);
        b.mark(PEN_LANDING, (TROUGH_WEST + TROUGH_EAST) / 2, 0, TROUGH_Z + 5);
        return b;
    }

    private static void ground(Blueprint b) {
        b.mottled(0, -1, 0, SIZE_X - 1, -1, SIZE_Z - 1, "minecraft:coarse_dirt", "minecraft:rooted_dirt", 0.25F);
        b.mottled(RING, -1, RING, SIZE_X - RING - 1, -1, PEN_END_Z, "minecraft:mud", "minecraft:muddy_mangrove_roots[axis=y]", 0.15F);
    }

    /** Spec: wrecked cars and hay bales, of blocks that can't be broken. Never lower than can be jumped from a hutch. */
    private static void ring(Blueprint b) {
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                boolean inRing = x < RING || x >= SIZE_X - RING || z < RING || z >= SIZE_Z - RING;
                if (inRing) {
                    ringColumn(b, x, z);
                }
            }
        }
    }

    private static void ringColumn(Blueprint b, int x, int z) {
        int segment = (x + z) / RING_SEGMENT;
        boolean wreck = segment % 2 == 0;
        // Stacks of different height, so it doesn't read as a wall. The same for both rows of a stack.
        int top = RING_LOW + Math.floorMod(segment * 31, RING_HIGH - RING_LOW + 1);
        b.box(x, 0, z, x, top, z, wreck ? WRECK_RING : HAY_RING);
    }

    /** Between pen and cornfield: the barn is the only way through. */
    private static void penWall(Blueprint b) {
        b.box(RING, 0, PEN_WALL_Z, SIZE_X - RING - 1, PEN_WALL_HEIGHT, PEN_WALL_Z + 1, HAY_RING);
    }

    private static void cornfield(Blueprint b) {
        cornRows(b, DIVIDER_WEST, SIZE_X - RING - 1, NORTH_LANE_START);
        cornRows(b, DIVIDER_WEST, CABIN_WEST - 2, SOUTH_LANE_START);
    }

    /** Rows along the lane with room to run between them, and the odd plant missing. */
    private static void cornRows(Blueprint b, int fromX, int toX, int fromZ) {
        Blueprint gaps = new Blueprint(SEED + fromZ);
        for (int z = fromZ + 1; z <= fromZ + LANE_DEPTH - 1; z += CORN_ROW_SPACING) {
            gaps.mottled(fromX, 0, z, toX, 0, z, CORN_LOWER, AIR, 0.12F);
        }
        gaps.blocks().forEach((pos, state) -> {
            if (!state.isAir()) {
                b.set(pos.getX(), 0, pos.getZ(), CORN_LOWER);
                b.set(pos.getX(), 1, pos.getZ(), CORN_UPPER);
            }
        });
    }

    private static void divider(Blueprint b) {
        b.box(DIVIDER_WEST, 0, DIVIDER_NORTH, SIZE_X - RING - 1, DIVIDER_TOP, DIVIDER_SOUTH, HAY_RING);
    }

    /** Spec: the pen, with fence posts and a feeding trough to slam into. Burn barrels light it, and hutches let the piglets out. */
    private static void pen(Blueprint b) {
        for (int[] post : POSTS) {
            b.box(post[0], 0, post[1], post[0], POST_TOP, post[1], POST);
        }
        b.box(TROUGH_WEST, 0, TROUGH_Z, TROUGH_EAST, 0, TROUGH_Z, TROUGH);
        for (int[] barrel : new int[][] {{3, 3}, {60, 3}, {3, 36}, {35, 36}}) {
            b.set(barrel[0], 0, barrel[1], BURN_BARREL);
        }
        for (int i = 0; i < HUTCHES.length; i++) {
            hutch(b, HUTCHES[i], i);
        }
    }

    private static void hutch(Blueprint b, int x, int number) {
        b.box(x, 0, RING, x + HUTCH_WIDTH - 1, 1, RING + HUTCH_DEPTH - 1, PLANKS);
        b.box(x + 1, 0, RING, x + HUTCH_WIDTH - 2, 0, RING + HUTCH_DEPTH - 1, AIR);
        b.box(x, 2, RING, x + HUTCH_WIDTH - 1, 2, RING + HUTCH_DEPTH - 1, "minecraft:spruce_slab[type=bottom]");
        b.mark(PIGLET + number, x + HUTCH_WIDTH / 2, 0, RING + HUTCH_DEPTH);
    }

    /** Red, with a white frame around both doors, stuffed with hay, and open to both sides. */
    private static void barn(Blueprint b) {
        b.box(BARN_WEST, 0, BARN_NORTH, BARN_EAST, BARN_WALL_TOP, BARN_SOUTH, BARN_WALL);
        b.box(BARN_WEST + 1, 0, BARN_NORTH + 1, BARN_EAST - 1, BARN_WALL_TOP, BARN_SOUTH - 1, AIR);
        b.box(BARN_WEST + 1, -1, BARN_NORTH + 1, BARN_EAST - 1, -1, BARN_SOUTH - 1, "minecraft:coarse_dirt");
        for (int z : new int[] {BARN_NORTH, BARN_SOUTH}) {
            b.box(BARN_DOOR_WEST - 1, 0, z, BARN_DOOR_EAST + 1, 4, z, BARN_TRIM);
            b.box(BARN_DOOR_WEST, 0, z, BARN_DOOR_EAST, 3, z, AIR);
        }
        // The roof, its ridge along z, with the wall's triangle under it at both ends.
        int halfWidth = (BARN_EAST - BARN_WEST + 1) / 2;
        for (int i = 0; i < halfWidth; i++) {
            int y = BARN_WALL_TOP + 1 + i;
            b.box(BARN_WEST + i, y, BARN_NORTH - 1, BARN_WEST + i, y, BARN_SOUTH + 1, "minecraft:dark_oak_stairs[facing=east]");
            b.box(BARN_EAST - i, y, BARN_NORTH - 1, BARN_EAST - i, y, BARN_SOUTH + 1, "minecraft:dark_oak_stairs[facing=west]");
            for (int z : new int[] {BARN_NORTH, BARN_SOUTH}) {
                b.box(BARN_WEST + i + 1, y, z, BARN_EAST - i - 1, y, z, BARN_WALL);
            }
        }
        // Hay along the walls, low enough that nobody climbs out on it.
        b.box(BARN_WEST + 1, 0, BARN_NORTH + 2, BARN_WEST + 3, 0, BARN_SOUTH - 2, HAY);
        b.box(BARN_EAST - 3, 0, BARN_NORTH + 2, BARN_EAST - 1, 0, BARN_SOUTH - 2, HAY);
        for (int z = BARN_NORTH + 4; z < BARN_SOUTH - 2; z += 6) {
            b.set(BARN_DOOR_WEST, BARN_WALL_TOP, z, "minecraft:lantern[hanging=true,waterlogged=false]");
            b.set(BARN_DOOR_EAST, BARN_WALL_TOP, z, "minecraft:lantern[hanging=true,waterlogged=false]");
        }
        for (int z = BARN_NORTH + 1; z < BARN_SOUTH; z++) {
            b.set(BARN_DOOR_WEST, BARN_WALL_TOP + 1, z, "minecraft:dark_oak_planks");
            b.set(BARN_DOOR_EAST, BARN_WALL_TOP + 1, z, "minecraft:dark_oak_planks");
        }
    }

    /** The little cabin at the far end of the cornfield. He comes through its east wall; the door faces the corn. */
    private static void cabin(Blueprint b) {
        b.box(CABIN_WEST, -1, CABIN_NORTH, CABIN_EAST, -1, CABIN_SOUTH, PLANKS);
        b.mottled(CABIN_WEST, 0, CABIN_NORTH, CABIN_EAST, CABIN_WALL_TOP, CABIN_SOUTH, PLANKS, "minecraft:stripped_spruce_log[axis=y]", 0.12F);
        b.box(CABIN_WEST + 1, 0, CABIN_NORTH + 1, CABIN_EAST - 1, CABIN_WALL_TOP, CABIN_SOUTH - 1, AIR);
        // As high as the walls: he only fits through what's a block taller than he is, as far as his path finding goes.
        b.box(CABIN_WEST, 0, CABIN_DOOR_NORTH, CABIN_WEST, CABIN_WALL_TOP, CABIN_DOOR_SOUTH, AIR);
        b.box(CABIN_WEST, CABIN_WALL_TOP + 1, CABIN_NORTH + 1, CABIN_EAST, CABIN_WALL_TOP + 1, CABIN_SOUTH - 1, PLANKS);
        for (int i = 0; i < 6; i++) {
            int y = CABIN_WALL_TOP + 1 + i;
            b.box(CABIN_WEST - 1 + i, y, CABIN_NORTH - 1, CABIN_WEST - 1 + i, y, CABIN_SOUTH + 1, "minecraft:spruce_stairs[facing=east]");
            b.box(CABIN_EAST + 1 - i, y, CABIN_NORTH - 1, CABIN_EAST + 1 - i, y, CABIN_SOUTH + 1, "minecraft:spruce_stairs[facing=west]");
            for (int z : new int[] {CABIN_NORTH, CABIN_SOUTH}) {
                b.box(CABIN_WEST + i, y, z, CABIN_EAST - i, y, z, PLANKS);
            }
        }
        b.box((CABIN_WEST + CABIN_EAST) / 2, CABIN_WALL_TOP + 7, CABIN_NORTH - 1, (CABIN_WEST + CABIN_EAST) / 2, CABIN_WALL_TOP + 7, CABIN_SOUTH + 1, "minecraft:spruce_slab[type=bottom]");
        // A farmer's mess along the north and south walls, leaving the middle free.
        b.set(CABIN_WEST + 2, 0, CABIN_NORTH + 1, "another_furniture:spruce_table");
        b.set(CABIN_WEST + 3, 0, CABIN_NORTH + 1, "another_furniture:spruce_chair[facing=west,tucked=false,variant=1]");
        b.set(CABIN_EAST - 1, 0, CABIN_NORTH + 1, "minecraft:barrel[facing=up,open=false]");
        b.set(CABIN_EAST - 2, 0, CABIN_NORTH + 1, "minecraft:barrel[facing=up,open=false]");
        b.set(CABIN_WEST + 2, 0, CABIN_SOUTH - 1, "minecraft:furnace[facing=north,lit=false]");
        b.set(CABIN_EAST - 1, 0, CABIN_SOUTH - 1, ID + "supply_crate");
        b.set(CABIN_EAST - 3, 0, CABIN_SOUTH - 1, ID + "beer_cans[facing=north]");
        int middleX = (CABIN_WEST + CABIN_EAST) / 2;
        int middleZ = (CABIN_NORTH + CABIN_SOUTH) / 2;
        b.set(middleX, CABIN_WALL_TOP, middleZ, "minecraft:lantern[hanging=true,waterlogged=false]");
        b.mark(CHASE_START, middleX, 0, middleZ);
        b.mark(WALL_FROM, CABIN_EAST, 0, CABIN_DOOR_NORTH);
        b.mark(WALL_TO, CABIN_EAST, CABIN_WALL_TOP, CABIN_DOOR_SOUTH);
    }

    /** Open until a fight starts. The bell beside it is for trying again. */
    private static void gate(Blueprint b) {
        int fromZ = 17;
        int toZ = 20;
        b.box(SIZE_X - RING, 0, fromZ, SIZE_X - 1, 3, toZ, AIR);
        b.mark(GATE_FROM, SIZE_X - RING, 0, fromZ);
        b.mark(GATE_TO, SIZE_X - 1, 3, toZ);
        b.set(SIZE_X - RING - 2, 0, fromZ - 2, "minecraft:bell[attachment=floor,facing=east,powered=false]");
        b.mark(BELL, SIZE_X - RING - 2, 0, fromZ - 2);
    }
}
