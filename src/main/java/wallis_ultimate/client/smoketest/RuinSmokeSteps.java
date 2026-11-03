package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.worldgen.Blueprint;
import wallis_ultimate.worldgen.RuinBuilds;
import wallis_ultimate.worldgen.RuinFeature;

/**
 * WORLD-014 in a real client: the terrain really generates ruins, one of them from up close, then every look on a
 * platform in the sky.
 */
final class RuinSmokeSteps {
    private static final int SPACING = 18;
    private static final int COLUMNS = 4;
    private static final int BUILDS_TO_TIME = 1000;
    /** Well under what a chunk takes to generate. */
    private static final double MOST_MS_PER_BUILD = 1.0;

    private static BlockPos origin;
    private static Vec3 home;

    private RuinSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        List<LongFunction<Blueprint>> looks = RuinBuilds.allLooks();
        int rows = (looks.size() + COLUMNS - 1) / COLUMNS;
        List<ClientSmokeTest.Step> steps = new ArrayList<>();
        steps.add(step("go and find a ruin the terrain generated", 120, player -> {
            home = player.position();
            Minecraft.getInstance().options.hideGui = true;
            command("gamerule sendCommandFeedback false");
            command("time set noon");
            command("gamemode spectator");
            BlockPos ruin = RuinFeature.lastPlaced();
            check("the terrain generates ruins", ruin != null, String.valueOf(ruin));
            if (ruin != null) {
                command(String.format("tp @s %d %d %d -45 30", ruin.getX() - 9, ruin.getY() + 9, ruin.getZ() - 9));
            }
        }));
        steps.add(step("picture of the generated ruin", 2, player -> screenshot("ruin-natural")));
        steps.add(step("time building ruins", 0, player -> {
            RandomSource random = RandomSource.create(1L);
            long start = System.nanoTime();
            for (int i = 0; i < BUILDS_TO_TIME; i++) {
                RuinBuilds.random(random);
            }
            double each = (System.nanoTime() - start) / 1.0E6 / BUILDS_TO_TIME;
            log(String.format("building a ruin takes %.3f ms", each));
            check("drawing up a ruin costs next to nothing", each < MOST_MS_PER_BUILD, String.format("%.3f ms each", each));
        }));
        steps.add(step("lay out every look on a platform", 60, player -> {
            origin = new BlockPos(player.getBlockX() + 80, 200, player.getBlockZ());
            Blueprint platform = new Blueprint(1);
            platform.box(-4, 0, -4, COLUMNS * SPACING, 0, rows * SPACING, "minecraft:grass_block");
            MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
            BlockPos at = origin;
            server.execute(() -> {
                platform.place(server.overworld(), at);
                for (int i = 0; i < looks.size(); i++) {
                    looks.get(i).apply(i).place(server.overworld(), at.offset(i % COLUMNS * SPACING, 1, i / COLUMNS * SPACING));
                }
            });
        }));
        for (int i = 0; i < looks.size(); i++) {
            int x = i % COLUMNS * SPACING;
            int z = i / COLUMNS * SPACING;
            int name = i;
            steps.add(step("go to look " + i, 25, player -> tpTo(x + 7.0, 11.0, z + 21.0, 180.0F, 24.0F)));
            steps.add(step("picture of look " + i, 2, player -> screenshot("ruin-look" + name)));
        }
        steps.add(step("all of them by night", 30, player -> {
            command("time set midnight");
            tpTo(COLUMNS * SPACING / 2.0, 34.0, rows * SPACING + 22.0, 180.0F, 34.0F);
        }));
        steps.add(step("picture of the ruins by night", 2, player -> screenshot("ruin-night")));
        steps.add(step("back to where the player was", 20, player -> {
            Minecraft.getInstance().options.hideGui = false;
            command("time set noon");
            command("gamemode survival");
            command("gamerule sendCommandFeedback true");
            command(String.format("tp @s %.2f %.2f %.2f", home.x, home.y, home.z));
        }));
        return steps;
    }

    private static void tpTo(double x, double y, double z, float yaw, float pitch) {
        command(String.format("tp @s %.2f %.2f %.2f %.1f %.1f", origin.getX() + x, origin.getY() + y, origin.getZ() + z, yaw, pitch));
    }
}
