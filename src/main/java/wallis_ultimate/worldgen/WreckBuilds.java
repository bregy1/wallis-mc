package wallis_ultimate.worldgen;

import net.minecraft.core.Direction;
import wallis_ultimate.block.BillboardBlock;

/** WORLD-013: wrecked vehicles and other multi-block props built from the kit, for use in any structure. */
public final class WreckBuilds {
    private static final String ID = "wallis_ultimate:";
    private static final String BODY = ID + "wreck_body";
    private static final String PANEL = ID + "wreck_panel[type=bottom,waterlogged=false]";
    private static final String GLASS_NORTH = ID + "wreck_window[facing=north]";
    private static final String GLASS_WEST = ID + "wreck_window[facing=west]";
    private static final String GLASS_EAST = ID + "wreck_window[facing=east]";
    private static final String EV_BODY = ID + "ev_body";
    private static final String EV_PANEL = ID + "ev_panel[type=bottom,waterlogged=false]";

    private WreckBuilds() {
    }

    /**
     * A pickup, two wide and six long, its hood pointing north. (x, z) is its north-west corner, without the wheels:
     * those stick out one block on each side.
     */
    static void pickupAlongZ(Blueprint b, int x, int y, int z) {
        b.box(x, y, z, x + 1, y, z + 5, BODY);
        // Hood, cab, and the bed with low sides and junk in it.
        b.box(x, y + 1, z, x + 1, y + 1, z, PANEL);
        b.set(x, y + 1, z + 1, GLASS_NORTH);
        b.set(x + 1, y + 1, z + 1, GLASS_NORTH);
        b.set(x, y + 1, z + 2, GLASS_WEST);
        b.set(x + 1, y + 1, z + 2, GLASS_EAST);
        b.box(x, y + 2, z + 1, x + 1, y + 2, z + 2, PANEL);
        b.box(x, y + 1, z + 3, x + 1, y + 1, z + 3, PANEL);
        b.box(x, y + 1, z + 5, x + 1, y + 1, z + 5, PANEL);
        b.set(x, y + 1, z + 4, ID + "oil_drum");
        b.set(x + 1, y + 1, z + 4, ID + "beer_cans[facing=north]");
        // Wheels.
        b.set(x - 1, y, z + 1, ID + "wreck_wheel[facing=east]");
        b.set(x - 1, y, z + 4, ID + "wreck_wheel[facing=east]");
        b.set(x + 2, y, z + 1, ID + "wreck_wheel[facing=west]");
        b.set(x + 2, y, z + 4, ID + "wreck_wheel[facing=west]");
    }

    /**
     * An electric car, two wide and five long, nose to the north: low, smooth, a glass roof that's mostly gone, its
     * battery spilling out of the side. (x, z) is its north-west corner, without the wheels.
     */
    public static void evCarAlongZ(Blueprint b, int x, int y, int z) {
        b.box(x, y, z, x + 1, y, z + 4, EV_BODY);
        b.set(x + 1, y, z + 2, ID + "ev_battery");
        b.box(x, y + 1, z, x + 1, y + 1, z, EV_PANEL);
        b.set(x, y + 1, z + 1, ID + "ev_window[facing=north]");
        b.set(x + 1, y + 1, z + 1, ID + "ev_window[facing=north]");
        b.set(x, y + 1, z + 2, ID + "ev_window[facing=west]");
        b.set(x + 1, y + 1, z + 2, ID + "ev_window[facing=east]");
        b.set(x, y + 1, z + 3, ID + "ev_window[facing=south]");
        b.box(x, y + 2, z + 1, x + 1, y + 2, z + 1, ID + "ev_window[facing=down]");
        b.box(x, y + 2, z + 2, x + 1, y + 2, z + 3, EV_PANEL);
        b.box(x, y + 1, z + 4, x + 1, y + 1, z + 4, EV_PANEL);
        b.set(x - 1, y, z + 1, ID + "wreck_wheel[facing=east]");
        b.set(x - 1, y, z + 3, ID + "wreck_wheel[facing=east]");
        b.set(x + 2, y, z + 3, ID + "wreck_wheel[facing=west]");
    }

    /** A car seen from the side, nose to the east, six long and two wide, wheels on its south side only. Old rust, or new white. */
    public static void carAlongX(Blueprint b, int x, int z, boolean electric) {
        String body = electric ? EV_BODY : BODY;
        String panel = electric ? EV_PANEL : PANEL;
        b.box(x, 0, z, x + 5, 0, z + 1, body);
        b.box(x + 1, 1, z, x + 3, 1, z + 1, body);
        b.box(x, 1, z, x, 1, z + 1, panel);
        b.box(x + 4, 1, z, x + 5, 1, z + 1, panel);
        b.box(x + 1, 2, z, x + 3, 2, z + 1, panel);
        b.set(x + 1, 0, z + 2, ID + "wreck_wheel[facing=north]");
        b.set(x + 4, 0, z + 2, ID + "wreck_wheel[facing=north]");
    }

    /**
     * A billboard, three wide and two high, looking away from the given side. (x, y, z) is the lower block at the
     * viewer's left, as they look at it. Facing north, it stands against the north side of its blocks.
     */
    public static void billboard(Blueprint b, int x, int y, int z, String facing) {
        Direction side = Direction.byName(facing);
        Direction viewerRight = side.getClockWise();
        for (int row = 0; row < BillboardBlock.ROWS; row++) {
            for (int column = 0; column < BillboardBlock.COLUMNS; column++) {
                int tile = row * BillboardBlock.COLUMNS + column;
                b.set(x + viewerRight.getStepX() * column, y + BillboardBlock.ROWS - 1 - row, z + viewerRight.getStepZ() * column,
                        ID + "billboard[facing=" + facing + ",tile=" + tile + "]");
            }
        }
    }
}
