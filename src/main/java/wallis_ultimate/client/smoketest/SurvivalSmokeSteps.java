package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.chatContains;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.click;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.describe;
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
 * SURV-004 and SURV-005 in a real client: a death leaves a take-only grave and says where it is (Q-122); a second
 * bed can't be placed while your first one stands (Q-144).
 */
final class SurvivalSmokeSteps {
    private static BlockPos deathPos;
    private static BlockPos firstBed;
    private static BlockPos secondBed;

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
                step("open the grave", 15, player -> {
                    BlockPos grave = findGrave(player);
                    Minecraft.getInstance().gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(grave), Direction.UP, grave, false));
                }),
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
                }));
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
