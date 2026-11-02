package wallis_ultimate.gametest;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.bananemdnsa.historystages.api.lock.ItemLocks;
import net.bananemdnsa.historystages.api.stage.StageStates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.progression.LockedItems;
import wallis_ultimate.progression.TeamUnlocks;
import wallis_ultimate.registry.ModItems;

/**
 * Checks for PROG-002 (item locks) and PROG-006 (joining a team). Needs the stage files from pack/config, which
 * Gradle copies into run/config before the server starts.
 */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LockGameTests {
    private static final String PLATFORM = "platform";
    private static final String IRON = "iron";

    private LockGameTests() {
    }

    /** A mock player with a fresh UUID, so no stage is unlocked for it. Not added to the level, so it isn't ticked. */
    private static Player mockPlayer(GameTestHelper helper, String name, boolean creative) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        Player player = new Player(helper.getLevel(), pos, 0.0F, new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return creative;
            }
        };
        player.moveTo(Vec3.atBottomCenterOf(pos));
        return player;
    }

    private static ItemEntity dropNear(GameTestHelper helper, Player player, ItemStack stack) {
        ItemEntity entity = new ItemEntity(helper.getLevel(), player.getX(), player.getY(), player.getZ(), stack, 0.0, 0.0, 0.0);
        entity.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static void fillMainInventory(Player player) {
        for (int slot = Inventory.getSelectionSize(); slot < Inventory.INVENTORY_SIZE; slot++) {
            player.getInventory().items.set(slot, new ItemStack(Items.DIRT, 64));
        }
    }

    private static int countInHotbar(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            ItemStack stack = player.getInventory().items.get(slot);
            count += stack.is(item) ? stack.getCount() : 0;
        }
        return count;
    }

    private static int countInMainInventory(Player player, Item item) {
        int count = 0;
        for (int slot = Inventory.getSelectionSize(); slot < Inventory.INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().items.get(slot);
            count += stack.is(item) ? stack.getCount() : 0;
        }
        return count;
    }

    @GameTest(template = PLATFORM)
    public static void stageFilesLockEverythingButTheAllowList(GameTestHelper helper) {
        UUID nobody = UUID.randomUUID();
        List<ItemStack> unlocked = List.of(new ItemStack(Items.DIRT), new ItemStack(Items.OAK_PLANKS),
                new ItemStack(Items.STONE_PICKAXE), new ItemStack(Items.BOW), new ItemStack(Items.BREAD),
                new ItemStack(Items.WHITE_BED), new ItemStack(ModItems.BANDAGE.get()), new ItemStack(Items.LEATHER_CHESTPLATE),
                new ItemStack(Items.SPRUCE_BOAT), new ItemStack(Items.FISHING_ROD), new ItemStack(Items.STONE_BUTTON),
                new ItemStack(Items.OAK_PRESSURE_PLATE));
        for (ItemStack stack : unlocked) {
            if (ItemLocks.isGated(stack, nobody)) {
                helper.fail(stack.getItem() + " should be unlocked from the start");
            }
        }
        List<ItemStack> locked = List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_PICKAXE),
                new ItemStack(Items.DIAMOND), new ItemStack(Items.ENCHANTING_TABLE), new ItemStack(Items.REDSTONE));
        for (ItemStack stack : locked) {
            if (!ItemLocks.isGated(stack, nobody)) {
                helper.fail(stack.getItem() + " should be locked (are the stage files in run/config/historystages?)");
            }
        }
        if (!ItemLocks.isActionLocked(new ItemStack(Items.IRON_PICKAXE), "recipe", nobody)) {
            helper.fail("crafting a locked item isn't blocked");
        }
        if (ItemLocks.isActionLocked(new ItemStack(Items.IRON_INGOT), "pickup", nobody)) {
            helper.fail("picking up a locked item is blocked");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void lockedPickupsSkipTheHotbar(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_pickup", false);
        player.getInventory().items.set(Inventory.getSelectionSize() + 3, new ItemStack(Items.IRON_INGOT, 60));
        ItemEntity entity = dropNear(helper, player, new ItemStack(Items.IRON_INGOT, 10));
        entity.playerTouch(player);
        if (countInHotbar(player, Items.IRON_INGOT) != 0) {
            helper.fail("locked item went into the hotbar");
        }
        if (countInMainInventory(player, Items.IRON_INGOT) != 70) {
            helper.fail("main inventory has " + countInMainInventory(player, Items.IRON_INGOT) + " iron ingots, expected 70");
        }
        if (!entity.isRemoved()) {
            helper.fail("the item is still on the ground");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void lockedPickupsStayOnTheGroundWhenTheMainInventoryIsFull(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_full", false);
        fillMainInventory(player);
        ItemEntity entity = dropNear(helper, player, new ItemStack(Items.IRON_INGOT, 5));
        entity.playerTouch(player);
        if (countInHotbar(player, Items.IRON_INGOT) != 0) {
            helper.fail("locked item went into the hotbar");
        }
        if (entity.isRemoved() || entity.getItem().getCount() != 5) {
            helper.fail("the item didn't stay on the ground");
        }
        entity.discard();
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void unlockedPickupsFillTheHotbarAsUsual(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_dirt", false);
        ItemEntity entity = dropNear(helper, player, new ItemStack(Items.DIRT, 5));
        entity.playerTouch(player);
        if (!player.getInventory().items.get(0).is(Items.DIRT)) {
            helper.fail("an unlocked item didn't go into the first hotbar slot");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void lockedItemsLeaveTheHandsAndArmorSlots(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_hands", false);
        Inventory inventory = player.getInventory();
        inventory.selected = 4;
        inventory.items.set(4, new ItemStack(Items.IRON_INGOT, 3));
        inventory.offhand.set(0, new ItemStack(Items.IRON_NUGGET, 2));
        inventory.armor.set(3, new ItemStack(Items.IRON_HELMET));
        inventory.armor.set(0, new ItemStack(Items.LEATHER_BOOTS));
        LockedItems.enforceSlots(player);
        if (!inventory.getSelected().isEmpty() || !inventory.offhand.get(0).isEmpty() || !inventory.armor.get(3).isEmpty()) {
            helper.fail("a locked item stayed in the main hand, offhand or helmet slot");
        }
        if (countInMainInventory(player, Items.IRON_INGOT) != 3 || countInMainInventory(player, Items.IRON_NUGGET) != 2
                || countInMainInventory(player, Items.IRON_HELMET) != 1) {
            helper.fail("locked items didn't move into the main inventory");
        }
        if (!inventory.armor.get(0).is(Items.LEATHER_BOOTS)) {
            helper.fail("unlocked boots were taken off");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void lockedItemInHandDropsWhenTheMainInventoryIsFull(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_drop", false);
        fillMainInventory(player);
        player.getInventory().items.set(0, new ItemStack(Items.IRON_SWORD));
        LockedItems.enforceSlots(player);
        if (!player.getInventory().getSelected().isEmpty()) {
            helper.fail("the locked sword stayed in the hand");
        }
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(2.0),
                entity -> entity.getItem().is(Items.IRON_SWORD));
        if (drops.size() != 1) {
            helper.fail("expected the sword on the ground at the player's feet, found " + drops.size());
        }
        drops.forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void creativePlayersAreExempt(GameTestHelper helper) {
        Player player = mockPlayer(helper, "lk_creative", true);
        player.getInventory().items.set(0, new ItemStack(Items.IRON_SWORD));
        LockedItems.enforceSlots(player);
        if (!player.getInventory().getSelected().is(Items.IRON_SWORD)) {
            helper.fail("a creative player's locked item was moved");
        }
        ItemEntity entity = dropNear(helper, player, new ItemStack(Items.IRON_INGOT, 5));
        entity.playerTouch(player);
        if (countInHotbar(player, Items.IRON_INGOT) != 5) {
            helper.fail("a creative player's pickup didn't use the hotbar");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void unlockingAStageLetsYouHoldItsItems(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Player player = mockPlayer(helper, "lk_unlocked", false);
        if (!StageStates.unlockIndividual(IRON, player.getUUID(), server)) {
            helper.fail("couldn't unlock the iron stage");
        }
        player.getInventory().items.set(0, new ItemStack(Items.IRON_SWORD));
        LockedItems.enforceSlots(player);
        boolean kept = player.getInventory().getSelected().is(Items.IRON_SWORD);
        StageStates.relockIndividual(IRON, player.getUUID(), server);
        if (!kept) {
            helper.fail("an unlocked sword was taken out of the hand");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void joinersLoseUnlocksTheTeamDoesNotHave(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        UUID joiner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        ItemStack ingot = new ItemStack(Items.IRON_INGOT);

        StageStates.unlockIndividual(IRON, joiner, server);
        if (!TeamUnlocks.removeUnlocksTheTeamLacks(joiner, Set.of(), server).isEmpty() || ItemLocks.isGated(ingot, joiner)) {
            helper.fail("a team without other members took the creator's unlocks");
        }
        List<String> removed = TeamUnlocks.removeUnlocksTheTeamLacks(joiner, Set.of(member), server);
        if (!removed.equals(List.of(IRON)) || !ItemLocks.isGated(ingot, joiner)) {
            helper.fail("the joiner kept an unlock the team doesn't have (removed: " + removed + ")");
        }

        StageStates.unlockIndividual(IRON, joiner, server);
        StageStates.unlockIndividual(IRON, member, server);
        removed = TeamUnlocks.removeUnlocksTheTeamLacks(joiner, Set.of(member), server);
        boolean kept = removed.isEmpty() && !ItemLocks.isGated(ingot, joiner);
        StageStates.relockIndividual(IRON, joiner, server);
        StageStates.relockIndividual(IRON, member, server);
        if (!kept) {
            helper.fail("the joiner lost an unlock the team has");
        }
        helper.succeed();
    }
}
