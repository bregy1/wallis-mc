package wallis_ultimate.client.smoketest;

import static wallis_ultimate.client.smoketest.ClientSmokeTest.await;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.check;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.click;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.command;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.count;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.craftingMenuSlot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.describe;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.find;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.inventoryMenuSlot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.log;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.screenshot;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.showTooltip;
import static wallis_ultimate.client.smoketest.ClientSmokeTest.step;

import java.util.List;
import net.bananemdnsa.historystages.api.client.ClientLocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

/**
 * PROG-002 in a real client: locked pickups, locked items leaving the hands and armor slots, the locked look and
 * tooltip, crafting and the recipe book, unlocking and relocking the Iron stage, and creative being exempt.
 */
final class LockSmokeSteps {
    private static final Item LOCKED = Items.IRON_INGOT;
    private static final Item UNLOCKED = Items.OAK_LOG;
    /** The hotbar slot the steps hold items in. */
    private static final int HAND = 4;
    private static final int HOTBAR_END = 9;
    private static final int MAIN_END = 36;
    private static final int RESULT_SLOT = 0;

    private static BlockPos table;

    private LockSmokeSteps() {
    }

    static List<ClientSmokeTest.Step> steps() {
        return List.of(
                step("empty the inventory", 10, player -> {
                    command("clear");
                    command("give @s minecraft:stick 2");
                }),
                await("the sticks arrive", 100, player -> player.getInventory().countItem(Items.STICK) == 2),
                step("pick up a locked item", 20, player ->
                        command("summon item ~ ~ ~ {Item:{id:\"minecraft:iron_ingot\",Count:16b}}")),
                await("the iron is picked up", 100, player -> player.getInventory().countItem(LOCKED) == 16),
                step("check the locked pickup", 0, player -> check("locked pickups skip the hotbar",
                        count(player, LOCKED, 0, HOTBAR_END) == 0 && count(player, LOCKED, HOTBAR_END, MAIN_END) == 16,
                        describe(player))),
                step("pick up an unlocked item", 20, player ->
                        command("summon item ~ ~ ~ {Item:{id:\"minecraft:oak_log\",Count:4b}}")),
                await("the logs are picked up", 100, player -> player.getInventory().countItem(UNLOCKED) == 4),
                step("check the unlocked pickup", 0, player -> check("unlocked pickups go to the hotbar",
                        count(player, UNLOCKED, 0, HOTBAR_END) == 4, describe(player))),

                step("hold a locked item", 10, player -> hold(player, LOCKED)),
                step("check the main hand", 0, player -> check("a locked item in the main hand moves back",
                        !player.getMainHandItem().is(LOCKED) && count(player, LOCKED, HOTBAR_END, MAIN_END) == 16,
                        describe(player))),
                step("move a locked item into the offhand (F key)", 10, player -> click(player,
                        inventoryMenuSlot(find(player, LOCKED, HOTBAR_END, MAIN_END)), 40, ClickType.SWAP)),
                // History Stages refuses it and puts it into the first free slot, which can be in the hotbar.
                step("check the offhand", 0, player -> check("a locked item doesn't stay in the offhand",
                        !player.getOffhandItem().is(LOCKED) && count(player, LOCKED, 0, MAIN_END) == 16,
                        describe(player))),
                step("get an iron helmet", 10, player -> command("give @s minecraft:iron_helmet")),
                await("the helmet arrives", 100, player -> player.getInventory().countItem(Items.IRON_HELMET) == 1),
                step("shift-click the helmet onto the head", 10, player -> click(player,
                        inventoryMenuSlot(find(player, Items.IRON_HELMET, 0, MAIN_END)), 0, ClickType.QUICK_MOVE)),
                step("check the armor slot", 0, player -> check("a locked helmet doesn't stay on the head",
                        player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                                && count(player, Items.IRON_HELMET, 0, MAIN_END) == 1,
                        "head " + player.getItemBySlot(EquipmentSlot.HEAD) + ", " + describe(player))),

                step("check the locked look", 0, LockSmokeSteps::checkLockedLook),
                step("open the inventory", 15, player -> {
                    Minecraft.getInstance().setScreen(new InventoryScreen(player));
                    showTooltip(new ItemStack(LOCKED));
                }),
                step("screenshot the inventory", 0, player -> screenshot("inventory-locked")),
                step("close the inventory", 5, player -> {
                    showTooltip(ItemStack.EMPTY);
                    player.closeContainer();
                }),
                step("check the recipe book", 0, LockSmokeSteps::checkRecipeBook),

                step("place a crafting table", 5, player -> {
                    table = player.blockPosition().relative(player.getDirection(), 2);
                    command("setblock " + table.getX() + " " + table.getY() + " " + table.getZ()
                            + " minecraft:crafting_table");
                }),
                step("open the crafting table", 15, player -> Minecraft.getInstance().gameMode.useItemOn(player,
                        InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(table), Direction.UP, table, false))),
                step("craft planks (control)", 10, player -> {
                    check("the crafting table opens", Minecraft.getInstance().screen instanceof CraftingScreen,
                            "screen " + Minecraft.getInstance().screen);
                    placeOneEach(player, UNLOCKED, 1);
                }),
                step("check the planks", 0, player -> check("unlocked recipes craft (oak log to planks)",
                        player.containerMenu.getSlot(RESULT_SLOT).getItem().is(Items.OAK_PLANKS),
                        "result " + player.containerMenu.getSlot(RESULT_SLOT).getItem())),
                step("clear the grid", 5, player -> click(player, 1, 0, ClickType.QUICK_MOVE)),
                step("try an iron pickaxe", 10, player -> {
                    placeOneEach(player, LOCKED, 1, 2, 3);
                    placeOneEach(player, Items.STICK, 5, 8);
                }),
                step("take the iron pickaxe", 10, player -> {
                    log("crafting grid: " + grid(player) + ", result " + player.containerMenu.getSlot(RESULT_SLOT).getItem());
                    screenshot("crafting-locked");
                    click(player, RESULT_SLOT, 0, ClickType.QUICK_MOVE);
                }),
                step("check the iron pickaxe", 10, player -> {
                    check("locked recipes can't be crafted (iron pickaxe)",
                            count(player, Items.IRON_PICKAXE, 0, MAIN_END) == 0, describe(player));
                    player.closeContainer();
                }),
                step("open JEI's recipes for the iron pickaxe", 20, player -> check("JEI opens the recipe view",
                        ModList.get().isLoaded("jei") && SmokeTestJei.showRecipesFor(new ItemStack(Items.IRON_PICKAXE)),
                        "JEI not running")),
                step("screenshot JEI", 0, player -> {
                    log("JEI screen: " + Minecraft.getInstance().screen);
                    screenshot("jei-iron-pickaxe");
                }),
                step("close JEI", 5, player -> Minecraft.getInstance().setScreen(null)),

                step("unlock the Iron stage", 20, player -> command("history individual unlock @s iron")),
                await("the client learns about the unlock", 100, player -> !ClientLocks.isItemGated(new ItemStack(LOCKED))),
                step("hold an unlocked item", 10, player -> {
                    check("unlocking the Iron stage unlocks iron on the client",
                            !ClientLocks.isItemGated(new ItemStack(LOCKED)), "still locked");
                    hold(player, LOCKED);
                }),
                step("check holding it", 0, player -> check("an unlocked item stays in the hand",
                        player.getMainHandItem().is(LOCKED), describe(player))),
                step("open the inventory", 15, player -> Minecraft.getInstance().setScreen(new InventoryScreen(player))),
                step("screenshot the inventory", 0, player -> screenshot("inventory-unlocked")),
                step("close the inventory", 5, LocalPlayer::closeContainer),
                step("lock the Iron stage again", 20, player -> command("history individual lock @s iron")),
                await("iron is locked again and leaves the hand", 100, player -> ClientLocks.isItemGated(new ItemStack(LOCKED)) && !player.getMainHandItem().is(LOCKED)),
                step("check relocking", 0, player -> check("relocking moves iron out of the hand",
                        !player.getMainHandItem().is(LOCKED) && ClientLocks.isItemGated(new ItemStack(LOCKED)),
                        describe(player))),

                step("switch to creative", 10, player -> command("gamemode creative")),
                await("creative mode arrives", 100, LocalPlayer::isCreative),
                step("hold a locked item in creative", 10, player -> hold(player, LOCKED)),
                step("check creative", 0, player -> check("creative players can hold locked items",
                        player.getMainHandItem().is(LOCKED), describe(player))),
                step("switch back to survival", 10, player -> command("gamemode survival")),
                await("survival mode arrives and the item leaves the hand", 100, player -> !player.isCreative() && !player.getMainHandItem().is(LOCKED)),
                step("check survival again", 0, player -> check("back in survival, the locked item leaves the hand",
                        !player.getMainHandItem().is(LOCKED), describe(player))));
    }

    /** Selects the hand slot and swaps the item into it from wherever it is, like pressing 5 over it. */
    private static void hold(LocalPlayer player, Item item) {
        player.getInventory().selected = HAND;
        int from = -1;
        for (int i = 0; i < MAIN_END && from < 0; i++) {
            if (i != HAND && player.getInventory().items.get(i).is(item)) {
                from = i;
            }
        }
        if (from < 0) {
            throw new IllegalStateException("no " + item + " outside the hand: " + describe(player));
        }
        click(player, inventoryMenuSlot(from), HAND, ClickType.SWAP);
    }

    /** In an open crafting table: picks up the item's stack, right-clicks one into each grid slot, puts the rest back. */
    private static void placeOneEach(LocalPlayer player, Item item, int... gridSlots) {
        int from = craftingMenuSlot(find(player, item, 0, MAIN_END));
        click(player, from, 0, ClickType.PICKUP);
        for (int gridSlot : gridSlots) {
            click(player, gridSlot, 1, ClickType.PICKUP);
        }
        click(player, from, 0, ClickType.PICKUP);
    }

    private static String grid(LocalPlayer player) {
        StringBuilder text = new StringBuilder();
        for (int slot = 1; slot <= 9; slot++) {
            ItemStack stack = player.containerMenu.getSlot(slot).getItem();
            text.append(stack.isEmpty() ? "-" : stack.getItem().toString()).append(slot < 9 ? " " : "");
        }
        return text.toString();
    }

    private static void checkLockedLook(LocalPlayer player) {
        check("the client sees iron as locked and oak logs as unlocked",
                ClientLocks.isItemGated(new ItemStack(LOCKED)) && !ClientLocks.isItemGated(new ItemStack(UNLOCKED)),
                "iron " + ClientLocks.isItemGated(new ItemStack(LOCKED)) + ", oak log "
                        + ClientLocks.isItemGated(new ItemStack(UNLOCKED)));
        List<String> lines = new ItemStack(LOCKED).getTooltipLines(player, TooltipFlag.NORMAL).stream()
                .map(ClientSmokeTest::renderedText).toList();
        log("locked tooltip: " + String.join(" | ", lines));
        // Line 0 is the item's own name ("Iron Ingot"); the stage's name ("Iron") has to come after it.
        check("a locked item's tooltip names the stage that unlocks it",
                lines.stream().skip(1).anyMatch(line -> line.contains("Iron")), String.join(" | ", lines));
    }

    private static void checkRecipeBook(LocalPlayer player) {
        RegistryAccess access = player.level().registryAccess();
        boolean ironPickaxe = false;
        boolean woodenPickaxe = false;
        for (RecipeCollection collection : player.getRecipeBook().getCollections()) {
            for (Recipe<?> recipe : collection.getRecipes()) {
                ItemStack result = recipe.getResultItem(access);
                ironPickaxe |= result.is(Items.IRON_PICKAXE);
                woodenPickaxe |= result.is(Items.WOODEN_PICKAXE);
            }
        }
        check("the recipe book hides locked recipes and keeps unlocked ones", !ironPickaxe && woodenPickaxe,
                "iron pickaxe listed " + ironPickaxe + ", wooden pickaxe listed " + woodenPickaxe);
    }
}
