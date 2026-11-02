package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.walk;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * The WORLD-013 props in a real client. Builds a little scene on a platform in the sky (a pickup wreck, sandbags, wire,
 * drums, crates and a tent), takes pictures of it from two sides, and walks through the wire.
 */
final class PropSmokeSteps {
    private static final String ID = "wallis_ultimate:";
    /** Off to the side of the player, so it doesn't shade or block the zombie platform later on. */
    private static final int SCENE_OFFSET = 40;
    private static final int WALK_TICKS = 20;

    private static Vec3 home;
    private static BlockPos origin;
    private static double start;
    private static double clearWalk;

    private PropSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("build the prop scene", 20, player -> {
                    home = player.position();
                    origin = new BlockPos(player.getBlockX() + SCENE_OFFSET, 220, player.getBlockZ());
                    fill(-14, 0, -12, 14, 0, 14, "minecraft:grass_block");
                    pickup();
                    sandbagWall();
                    fill(-6, 1, 3, 6, 1, 6, ID + "barbed_wire");
                    drumsAndCrates();
                    tent();
                    place(3, 1, 1, ID + "beer_cans");
                    fill(9, 1, 1, 9, 3, 1, "minecraft:spruce_planks");
                    place(9, 3, 2, ID + "antlers[facing=north]");
                    command("time set noon");
                    Minecraft.getInstance().options.hideGui = true;
                    tpTo(0.5, 2.0, 13.5, 180.0F, 12.0F);
                }),
                step("picture from the front", 2, player -> screenshot("props-front")),
                step("look from the other side", 20, player -> tpTo(-9.5, 3.5, -10.5, -30.0F, 20.0F)),
                step("picture from behind", 2, player -> screenshot("props-behind")),
                step("look at the burn barrel by night", 20, player -> {
                    command("time set midnight");
                    tpTo(3.5, 2.0, -9.5, -20.0F, 15.0F);
                }),
                step("picture of the fire", 2, player -> screenshot("props-fire")),
                step("get ready to walk where there is no wire", 10, player -> {
                    Minecraft.getInstance().options.hideGui = false;
                    command("time set noon");
                    command("effect clear @s minecraft:resistance");
                    tpTo(11.5, 1.0, 12.5, 180.0F, 0.0F);
                }),
                step("walk without wire", WALK_TICKS + 2, player -> {
                    start = player.getZ();
                    walk(WALK_TICKS, 0.0F);
                }),
                step("get into the wire", 10, player -> {
                    clearWalk = Math.abs(player.getZ() - start);
                    tpTo(0.5, 1.0, 6.9, 180.0F, 0.0F);
                }),
                step("walk through the wire", WALK_TICKS + 2, player -> {
                    start = player.getZ();
                    walk(WALK_TICKS, 0.0F);
                }),
                step("check the wire", 0, player -> {
                    double wireWalk = Math.abs(player.getZ() - start);
                    check("barbed wire slows the player down", wireWalk < clearWalk * 0.6,
                            String.format("%.1f blocks in the wire, %.1f without", wireWalk, clearWalk));
                    check("barbed wire hurts the player", player.getHealth() < player.getMaxHealth(), "health " + player.getHealth());
                }),
                step("back to the ground", 20, player -> {
                    command("effect give @s minecraft:instant_health 1 4 true");
                    command("effect give @s minecraft:resistance infinite 4 true");
                    command(String.format("tp @s %.2f %.2f %.2f", home.x, home.y, home.z));
                }));
    }

    /** Two wide, five long: a bed at the back, a cab, a hood, four wheels. */
    private static void pickup() {
        fill(-3, 1, -6, 1, 1, -5, ID + "wreck_body");
        fill(-3, 2, -6, -2, 2, -6, ID + "wreck_panel[type=bottom]");
        fill(-3, 2, -5, -2, 2, -5, ID + "wreck_panel[type=bottom]");
        place(-1, 2, -6, ID + "wreck_window[facing=north]");
        place(-1, 2, -5, ID + "wreck_window[facing=south]");
        place(0, 2, -6, ID + "wreck_window[facing=north]");
        place(0, 2, -5, ID + "wreck_window[facing=south]");
        fill(-1, 3, -6, 0, 3, -5, ID + "wreck_panel[type=bottom]");
        fill(1, 2, -6, 1, 2, -5, ID + "wreck_panel[type=bottom]");
        place(-2, 1, -7, ID + "wreck_wheel[facing=south]");
        place(0, 1, -7, ID + "wreck_wheel[facing=south]");
        place(-2, 1, -4, ID + "wreck_wheel[facing=north]");
        place(0, 1, -4, ID + "wreck_wheel[facing=north]");
    }

    private static void sandbagWall() {
        fill(-6, 1, 1, -1, 1, 1, ID + "sandbags");
        fill(-6, 2, 1, -1, 2, 1, ID + "sandbag_slab");
        place(-1, 2, 1, "minecraft:air");
        fill(-6, 1, 0, -6, 1, -2, ID + "sandbag_slab");
    }

    private static void drumsAndCrates() {
        place(4, 1, -6, ID + "oil_drum");
        place(5, 1, -6, ID + "oil_drum[lit=true]");
        place(4, 1, -7, ID + "oil_drum");
        place(4, 2, -7, ID + "oil_drum");
        fill(7, 1, -7, 8, 1, -6, ID + "supply_crate");
        place(7, 2, -7, ID + "supply_crate");
    }

    /** Sloped tarps for a roof, a flat one for the ground, one hanging down as the back wall. */
    private static void tent() {
        for (int z = -4; z <= -1; z++) {
            place(-11, 1, z, ID + "blue_tarp_slope[facing=east]");
            place(-10, 2, z, ID + "blue_tarp_slope[facing=east]");
            place(-9, 2, z, ID + "blue_tarp_slope[facing=west]");
            place(-8, 1, z, ID + "blue_tarp_slope[facing=west]");
        }
        fill(-10, 1, -4, -9, 1, -1, ID + "green_tarp[facing=down]");
        place(-10, 1, -5, ID + "green_tarp[facing=south]");
        place(-9, 1, -5, ID + "green_tarp[facing=south]");
        place(-10, 2, -5, ID + "green_tarp[facing=south]");
        place(-9, 2, -5, ID + "green_tarp[facing=south]");
    }

    private static void fill(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, String block) {
        command(String.format("fill %d %d %d %d %d %d %s", origin.getX() + fromX, origin.getY() + fromY, origin.getZ() + fromZ,
                origin.getX() + toX, origin.getY() + toY, origin.getZ() + toZ, block));
    }

    private static void place(int x, int y, int z, String block) {
        command(String.format("setblock %d %d %d %s", origin.getX() + x, origin.getY() + y, origin.getZ() + z, block));
    }

    private static void tpTo(double x, double y, double z, float yaw, float pitch) {
        command(String.format("tp @s %.2f %.2f %.2f %.1f %.1f", origin.getX() + x, origin.getY() + y, origin.getZ() + z, yaw, pitch));
    }
}
