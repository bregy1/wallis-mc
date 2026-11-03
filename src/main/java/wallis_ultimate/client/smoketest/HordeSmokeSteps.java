package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.entity.RegularZombie;
import wallis_ultimate.survival.HordeNights;

/**
 * A horde night in a real client (ENEMY-006): the two warnings, the start, the first two waves coming for the player,
 * and a bed that refuses. Time is set to just before each moment and then runs by itself, because a /time command
 * doesn't count as a night going by.
 */
final class HordeSmokeSteps {
    /** The warnings are at these times of day, or nightfall. Hourglass needs about 22 ticks for the last 15 of them. */
    private static final int LEAD_IN = 15;
    private static final int PAST_IT_TICKS = 40;

    private static BlockPos bed;
    private static int firstWave;

    private HordeSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                // Earlier steps leave the player high up among test platforms, and zombies over 128 blocks away despawn at once.
                step("get away from the test platforms", 60, player -> command("tp @s ~100 ~ ~100")),
                step("get ready for a horde night", 20, player -> {
                    command("execute positioned over motion_blocking_no_leaves run tp @s ~ ~ ~");
                    command("difficulty normal");
                    command("kill @e[type=wallis_ultimate:zombie]");
                    command("effect give @s minecraft:resistance infinite 4 true");
                    command("gamerule doMobSpawning false");
                    onServer(server -> HordeNights.setNightsCounted(serverPlayer(server, player), HordeNights.NIGHTS_PER_HORDE - 1));
                    command("time set " + (HordeNights.FIRST_WARNING - LEAD_IN));
                }),
                step("two minutes before", PAST_IT_TICKS, player -> {
                }),
                step("check the first warning", 0, player -> {
                    check("two minutes before: an uneasy feeling", chatContains("really uneasy feeling"), "no first warning");
                    command("time set " + (HordeNights.SECOND_WARNING - LEAD_IN));
                }),
                step("one minute before", PAST_IT_TICKS, player -> {
                }),
                step("check the second warning", 0, player -> {
                    check("one minute before: it gets worse", chatContains("something is about to happen"), "no second warning");
                    screenshot("horde-warning");
                    command("time set " + (12000 - LEAD_IN));
                }),
                await("the first wave arrives", 200, player -> zombies(player) >= 4),
                step("check the first wave", 20, player -> {
                    firstWave = zombies(player);
                    check("the horde starts at nightfall, with word from Lurleen", chatContains("Lurleen, on the radio"), "no message");
                    check("a wave of zombies comes for the player", firstWave >= 4, firstWave + " zombies");
                    onServer(server -> check("the counter starts over", HordeNights.nightsCounted(serverPlayer(server, player)) == 0,
                            "counted " + HordeNights.nightsCounted(serverPlayer(server, player))));
                    screenshot("horde-first-wave");
                }),
                step("get a bed", 20, player -> {
                    command("kill @e[type=item]");
                    command("fill ~-2 ~-1 ~-2 ~2 ~-1 ~2 minecraft:smooth_stone");
                    command("fill ~-2 ~ ~-2 ~2 ~1 ~2 minecraft:air");
                    bed = player.blockPosition().east();
                    command(String.format("setblock %d %d %d minecraft:red_bed[facing=east,part=foot]", bed.getX(), bed.getY(), bed.getZ()));
                    command(String.format("setblock %d %d %d minecraft:red_bed[facing=east,part=head]", bed.getX() + 1, bed.getY(), bed.getZ()));
                    command("time set 14000");
                }),
                step("try to sleep through it", 30, player -> Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(bed), Direction.UP, bed, false))),
                step("check the bed", 0, player -> {
                    check("the bed is there", player.level().getBlockState(bed).getBlock() instanceof BedBlock, "no bed");
                    check("a horde night can't be slept through", !player.isSleeping() && chatContains("Nobody's sleeping through this"),
                            "sleeping: " + player.isSleeping());
                    screenshot("horde-bed-refused");
                }),
                await("the second wave arrives a minute later", 1400, player -> zombies(player) > firstWave),
                step("check the second wave", 0, player -> {
                    check("another wave comes", zombies(player) > firstWave, zombies(player) + " zombies, first wave " + firstWave);
                    screenshot("horde-second-wave");
                }),
                // A real death, because the counter and the bed record have to outlive one.
                step("count some nights and die", 10, player -> {
                    onServer(server -> HordeNights.setNightsCounted(serverPlayer(server, player), 3));
                    command("kill @s");
                }),
                // /kill puts a standing player down first; a second one finishes it.
                step("make sure the player is dead", 30, player -> {
                    if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
                        command("kill @s");
                    }
                }),
                await("the death screen", 100, player -> Minecraft.getInstance().screen instanceof DeathScreen),
                step("respawn", 40, player -> {
                    player.respawn();
                    Minecraft.getInstance().setScreen(null);
                }),
                step("check the counter after dying", 10, player -> {
                    onServer(server -> check("the night counter survives dying", HordeNights.nightsCounted(serverPlayer(server, player)) == 3,
                            "counted " + HordeNights.nightsCounted(serverPlayer(server, player))));
                    command("effect give @s minecraft:resistance infinite 4 true");
                }),
                step("clean up after the horde", 20, player -> {
                    command("kill @e[type=wallis_ultimate:zombie]");
                    command(String.format("setblock %d %d %d minecraft:air", bed.getX(), bed.getY(), bed.getZ()));
                    command("gamerule doMobSpawning true");
                    command("time set day");
                }));
    }

    private static int zombies(LocalPlayer player) {
        return player.level().getEntitiesOfClass(RegularZombie.class, player.getBoundingBox().inflate(64.0)).size();
    }

    private static void onServer(java.util.function.Consumer<MinecraftServer> action) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> action.accept(server));
    }

    private static ServerPlayer serverPlayer(MinecraftServer server, LocalPlayer player) {
        return server.getPlayerList().getPlayer(player.getUUID());
    }
}
