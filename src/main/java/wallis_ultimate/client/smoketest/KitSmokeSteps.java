package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.block.PanelBlock;
import wallis_ultimate.registry.ModKit;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.WreckBuilds;

/**
 * WORLD-013: the apocalypse kit in a real client. Every block on a platform in the sky, row by row, a picture of each
 * row by day, and one at night for the ones that glow. Also an electric car wreck and a billboard, put together.
 */
final class KitSmokeSteps {
    private static final String ID = "wallis_ultimate:";
    private static final int COLUMNS = 6;
    private static final int COLUMN_STEP = 3;
    private static final int ROW_STEP = 5;
    /** Blocks that need more than their default state to show themselves. */
    private static final Map<String, String> STATES = Map.of(
            "street_lamp", "[facing=south,lit=true]",
            "floodlight", "[facing=north,lit=true]",
            "billboard", "[facing=north,tile=0]");

    private static BlockPos origin;
    private static Vec3 home;

    private KitSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        List<RegistryObject<? extends Block>> blocks = List.copyOf(ModKit.names().keySet());
        int rows = (blocks.size() + COLUMNS - 1) / COLUMNS;
        List<ClientSmokeTest.Step> steps = new ArrayList<>();
        steps.add(step("build the kit platform", 40, player -> {
            home = player.position();
            origin = new BlockPos(player.getBlockX() + 40, 220, player.getBlockZ());
            Minecraft.getInstance().options.hideGui = true;
            command("gamerule sendCommandFeedback false");
            command("time set noon");
            command("gamemode spectator");
            Blueprint blueprint = showcase(blocks, rows);
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            BlockPos at = origin;
            server.execute(() -> blueprint.place(server.overworld(), at));
            tpTo(COLUMNS * COLUMN_STEP / 2.0, 4.0, rows * ROW_STEP + 8.0, 180.0F, 10.0F);
        }));
        steps.add(step("check that every block is in place", 0, player -> {
            int missing = 0;
            for (int i = 0; i < blocks.size(); i++) {
                BlockPos at = origin.offset(i % COLUMNS * COLUMN_STEP, 1, i / COLUMNS * ROW_STEP);
                if (player.level().getBlockState(at).getBlock() != blocks.get(i).get()) {
                    missing++;
                }
            }
            check("all " + blocks.size() + " kit blocks stay where they're put", missing == 0, missing + " missing");
        }));
        for (int row = 0; row < rows; row++) {
            int z = row * ROW_STEP;
            int name = row;
            steps.add(step("go to kit row " + row, 15, player -> tpTo(COLUMNS * COLUMN_STEP / 2.0, 3.2, z + 5.5, 180.0F, 18.0F)));
            steps.add(step("picture of kit row " + row, 2, player -> screenshot("kit-row" + name)));
        }
        steps.add(step("look at it from afar by night", 40, player -> {
            command("time set midnight");
            tpTo(COLUMNS * COLUMN_STEP / 2.0, 9.0, rows * ROW_STEP + 12.0, 180.0F, 18.0F);
        }));
        steps.add(step("picture of the kit by night", 2, player -> screenshot("kit-night")));
        steps.add(step("back to where the player was", 20, player -> {
            Minecraft.getInstance().options.hideGui = false;
            command("time set noon");
            command("gamemode survival");
            command("gamerule sendCommandFeedback true");
            command(String.format("tp @s %.2f %.2f %.2f", home.x, home.y, home.z));
        }));
        return steps;
    }

    private static Blueprint showcase(List<RegistryObject<? extends Block>> blocks, int rows) {
        Blueprint b = new Blueprint(1);
        b.box(-6, 0, -8, COLUMNS * COLUMN_STEP + 4, 0, rows * ROW_STEP + 2, "minecraft:gray_concrete");
        for (int i = 0; i < blocks.size(); i++) {
            String id = blocks.get(i).getId().getPath();
            int x = i % COLUMNS * COLUMN_STEP;
            int z = i / COLUMNS * ROW_STEP;
            b.set(x, 1, z, ID + id + STATES.getOrDefault(id, stateFor(blocks.get(i).get())));
            if (blocks.get(i).get() instanceof IronBarsBlock || blocks.get(i).get() instanceof WallBlock) {
                b.set(x + 1, 1, z, ID + id);
            }
        }
        // Put together, behind the rows: a billboard over a car wreck.
        WreckBuilds.billboard(b, 2, 2, -7, "north");
        WreckBuilds.evCarAlongZ(b, 8, 1, -7);
        return b;
    }

    private static String stateFor(Block block) {
        return block instanceof PanelBlock ? "[facing=north]" : "";
    }

    private static void tpTo(double x, double y, double z, float yaw, float pitch) {
        command(String.format("tp @s %.2f %.2f %.2f %.1f %.1f", origin.getX() + x, origin.getY() + y, origin.getZ() + z, yaw, pitch));
    }
}
