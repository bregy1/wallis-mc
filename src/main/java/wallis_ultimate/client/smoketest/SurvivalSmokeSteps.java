package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.click;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.describe;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import wallis_ultimate.block.GraveBlockEntity;
import wallis_ultimate.block.GraveMenu;

/**
 * SURV-004, SURV-005 and SURV-007 in a real client: a death leaves a take-only grave and says where it is (Q-122); a second
 * bed can't be placed while your first one stands (Q-144); day and night run at the configured speeds; sleeping heals.
 */
final class SurvivalSmokeSteps {
    private static BlockPos deathPos;
    private static BlockPos firstBed;
    private static BlockPos secondBed;
    private static final int CLOCK_TICKS = 300;
    private static final double CLOCK_TOLERANCE = 0.08;
    private static final int RESPAWN_NEAR_BED = 4;
    private static long clockStart;

    private SurvivalSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("pack a few things and die", 30, player -> {
                    command("clear");
                    command("give @s minecraft:bread 5");
                    command("give @s minecraft:stick 3");
                    deathPos = player.blockPosition();
                    command("kill @s");
                }),
                // /kill puts a standing player down first; a second one finishes it.
                step("make sure the player is dead", 30, player -> {
                    if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
                        command("kill @s");
                    }
                }),
                await("the death screen", 100, player -> Minecraft.getInstance().screen instanceof DeathScreen),
                step("check the death", 0, player -> {
                    check("a death ends on the death screen", Minecraft.getInstance().screen instanceof DeathScreen,
                            "screen " + Minecraft.getInstance().screen);
                    check("the chat says where the grave is", chatContains("in your grave at"), "no grave message");
                    player.respawn();
                    Minecraft.getInstance().setScreen(null);
                }),
                step("go back to the grave", 40, player -> {
                    command("effect give @s minecraft:resistance infinite 4 true");
                    command(String.format("tp @s %d %d %d", deathPos.getX() + 2, deathPos.getY(), deathPos.getZ()));
                    command("give @s minecraft:dirt 4");
                }),
                await("the dirt arrives", 100, player -> player.getInventory().countItem(Items.DIRT) == 4),
                step("open the grave", 5, player -> {
                    BlockPos grave = findGrave(player);
                    Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(grave), Direction.UP, grave, false));
                }),
                await("the grave screen opens", 100, player -> player.containerMenu instanceof GraveMenu),
                step("try to put dirt into the grave", 10, player -> {
                    check("the grave opens its take-only screen", player.containerMenu instanceof GraveMenu,
                            "menu " + player.containerMenu);
                    screenshot("grave");
                    click(player, menuSlotOf(player, Items.DIRT, GraveBlockEntity.SIZE, player.containerMenu.slots.size()),
                            0, ClickType.QUICK_MOVE);
                }),
                step("take the bread out", 10, player -> {
                    check("nothing can be put into a grave", countIn(player, Items.DIRT, 0, GraveBlockEntity.SIZE) == 0,
                            "dirt in the grave");
                    click(player, menuSlotOf(player, Items.BREAD, 0, GraveBlockEntity.SIZE), 0, ClickType.QUICK_MOVE);
                }),
                step("check the bread", 5, player -> {
                    check("items can be taken out of a grave", player.getInventory().countItem(Items.BREAD) == 5,
                            describe(player));
                    player.closeContainer();
                }),

                step("clear a flat spot for beds", 10, player -> {
                    command("fill ~-4 ~-1 ~-4 ~4 ~-1 ~4 minecraft:smooth_stone");
                    command("fill ~-4 ~ ~-4 ~4 ~3 ~4 minecraft:air");
                    command("tp @s ~ ~ ~ 0 0");
                    command("give @s minecraft:red_bed 2");
                }),
                await("the beds arrive", 100, player -> player.getInventory().countItem(Items.RED_BED) == 2),
                step("place the first bed", 10, player -> {
                    firstBed = player.blockPosition().east(2);
                    secondBed = player.blockPosition().west(2);
                    placeBed(player, firstBed);
                }),
                step("try a second bed", 10, player -> {
                    check("the first bed can be placed", isBed(firstBed), "no bed at " + firstBed.toShortString());
                    placeBed(player, secondBed);
                }),
                step("pick up the first bed", 10, player -> {
                    check("a second bed is refused while the first stands",
                            !isBed(secondBed) && chatContains("already have a bed"), "bed at second spot: " + isBed(secondBed));
                    command(String.format("setblock %d %d %d minecraft:air", firstBed.getX(), firstBed.getY(), firstBed.getZ()));
                }),
                step("place the second bed again", 10, player -> placeBed(player, secondBed)),
                step("check the second bed", 0, player -> {
                    check("once the first bed is gone, a new one can be placed", isBed(secondBed),
                            "no bed at " + secondBed.toShortString());
                    screenshot("bed");
                }),

                // SURV-007: 16 minutes of day and 8 of night mean 0.677 and 1.146 game ticks per real tick (Hourglass).
                step("make it morning", 40, player -> command("time set 1000")),
                step("start the day clock", CLOCK_TICKS, player -> clockStart = gameTime()),
                step("check the day speed", 0, player -> checkClock("daytime runs at 16 minutes a day", 0.677)),
                step("make it night", 40, player -> command("time set 14000")),
                step("start the night clock", CLOCK_TICKS, player -> clockStart = gameTime()),
                step("check the night speed", 0, player -> checkClock("nighttime runs at 8 minutes a night", 1.146)),

                // SURV-005: sleeping through the night, sped up by Hourglass, heals on waking up.
                step("get hurt", 10, player -> {
                    command("effect clear @s");
                    // With natural regeneration on, a full food bar refills the hearts in those few seconds by itself.
                    command("gamerule naturalRegeneration false");
                    command("damage @s 10");
                }),
                await("the damage arrives", 100, player -> player.getHealth() <= 10.0F),
                step("lie down in the bed", 5, player -> Minecraft.getInstance().gameMode.useItemOn(player,
                        InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(secondBed), Direction.UP, secondBed, false))),
                await("the player falls asleep", 100, LocalPlayer::isSleeping),
                step("check sleeping", 0, player -> {
                    check("beds work at night", player.isSleeping(), "not asleep; health " + player.getHealth());
                    screenshot("sleeping");
                }),
                await("morning comes", 600, player -> !player.isSleeping()),
                step("check waking up", 20, player -> {
                    long time = gameTime() % 24000L;
                    check("the night races by and the player wakes up in the morning", !player.isSleeping() && (time < 12000L || time >= 23000L),
                            "time " + time + ", asleep " + player.isSleeping());
                }),
                step("check the healing", 0, player -> {
                    check("sleeping through the night fully heals", player.getHealth() == player.getMaxHealth(),
                            "health " + player.getHealth());
                    command("gamerule naturalRegeneration true");
                    command("effect give @s minecraft:resistance infinite 4 true");
                }),

                // SURV-003: having slept there, the player comes back at the bed after dying.
                step("die after sleeping", 10, player -> command("kill @s")),
                step("make sure the player is dead again", 30, player -> {
                    if (!(Minecraft.getInstance().screen instanceof DeathScreen)) {
                        command("kill @s");
                    }
                }),
                await("the death screen again", 100, player -> Minecraft.getInstance().screen instanceof DeathScreen),
                step("respawn at the bed", 40, player -> {
                    player.respawn();
                    Minecraft.getInstance().setScreen(null);
                }),
                step("check the respawn", 0, player -> {
                    LocalPlayer respawned = Minecraft.getInstance().player;
                    check("a player who dies respawns at their bed", respawned.blockPosition().closerThan(secondBed, RESPAWN_NEAR_BED),
                            "at " + respawned.blockPosition().toShortString() + ", bed at " + secondBed.toShortString());
                    command("effect give @s minecraft:resistance infinite 4 true");
                }));
    }

    private static long gameTime() {
        return Minecraft.getInstance().level.getDayTime();
    }

    private static void checkClock(String name, double expectedSpeed) {
        double speed = (double) (gameTime() - clockStart) / CLOCK_TICKS;
        log(String.format("%s: measured %.3f game ticks per tick", name, speed));
        check(name, Math.abs(speed - expectedSpeed) < CLOCK_TOLERANCE, String.format("measured %.3f, expected %.3f", speed, expectedSpeed));
    }

    private static BlockPos findGrave(LocalPlayer player) {
        for (BlockPos pos : BlockPos.betweenClosed(deathPos.offset(-3, -2, -3), deathPos.offset(3, 8, 3))) {
            if (player.level().getBlockEntity(pos) instanceof GraveBlockEntity) {
                return pos.immutable();
            }
        }
        throw new IllegalStateException("no grave near " + deathPos.toShortString());
    }

    private static int menuSlotOf(LocalPlayer player, Item item, int from, int to) {
        for (int i = from; i < to; i++) {
            if (player.containerMenu.getSlot(i).getItem().is(item)) {
                return i;
            }
        }
        throw new IllegalStateException("no " + item + " in menu slots " + from + "-" + (to - 1));
    }

    private static int countIn(LocalPlayer player, Item item, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            ItemStack stack = player.containerMenu.getSlot(i).getItem();
            count += stack.is(item) ? stack.getCount() : 0;
        }
        return count;
    }

    /** Holds a bed and right-clicks the top of the floor block below the spot, like a player placing it. */
    private static void placeBed(LocalPlayer player, BlockPos spot) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.RED_BED)) {
                player.getInventory().selected = i;
                break;
            }
        }
        BlockPos floor = spot.below();
        Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false));
    }

    private static boolean isBed(BlockPos pos) {
        return Minecraft.getInstance().level.getBlockState(pos).getBlock() instanceof BedBlock;
    }
}
