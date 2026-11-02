package wallis_ultimate.worldgen;

/** WORLD-013: wrecked vehicles built from the wreck blocks, for use in any structure. */
final class WreckBuilds {
    private static final String ID = "wallis_ultimate:";
    private static final String BODY = ID + "wreck_body";
    private static final String PANEL = ID + "wreck_panel[type=bottom,waterlogged=false]";
    private static final String GLASS_NORTH = ID + "wreck_window[facing=north]";
    private static final String GLASS_WEST = ID + "wreck_window[facing=west]";
    private static final String GLASS_EAST = ID + "wreck_window[facing=east]";

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
}
