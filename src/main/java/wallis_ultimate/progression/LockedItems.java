package wallis_ultimate.progression;

import net.bananemdnsa.historystages.api.lock.ItemLocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * PROG-002: where locked items may sit. History Stages decides what is locked (per team, from the stage files in
 * pack/config) and blocks using, placing and crafting. This class adds the slot rules: picked-up locked items go only
 * into the 27 main inventory slots, and a locked item in the main hand, offhand or an armor slot moves back into the
 * main inventory (or drops at the player's feet if that is full). Creative and spectator players are exempt (Q-130).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LockedItems {
    private static final int FIRST_MAIN_SLOT = Inventory.getSelectionSize();

    private LockedItems() {
    }

    public static boolean isExempt(Player player) {
        return player.isCreative() || player.isSpectator();
    }

    public static boolean isLocked(ItemStack stack, Player player) {
        return !stack.isEmpty() && ItemLocks.isGated(stack, player.getUUID());
    }

    /**
     * Replaces vanilla's pickup for locked items, which would fill the hotbar first. Mirrors the end of
     * {@code ItemEntity.playerTouch} (pickup event, animation, stats) for the part that fits.
     */
    @SubscribeEvent
    public static void onPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemEntity entity = event.getItem();
        ItemStack stack = entity.getItem();
        if (player.level().isClientSide() || isExempt(player) || !isLocked(stack, player)) {
            return;
        }
        event.setCanceled(true);
        if (!mayPickUp(entity, player)) {
            return;
        }
        Item item = stack.getItem();
        ItemStack before = stack.copy();
        int moved = insertIntoMainInventory(player.getInventory(), stack);
        if (moved == 0) {
            return;
        }
        ForgeEventFactory.firePlayerItemPickupEvent(player, entity, before.copyWithCount(moved));
        player.take(entity, moved);
        if (stack.isEmpty()) {
            entity.discard();
            stack.setCount(moved);
        }
        player.awardStat(Stats.ITEM_PICKED_UP.get(item), moved);
        player.onItemPickup(entity);
    }

    /** Vanilla only lets the item's target pick it up, if it has one. 1.20.1 has no getter for it. */
    private static boolean mayPickUp(ItemEntity entity, Player player) {
        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        return !tag.hasUUID("Owner") || tag.getUUID("Owner").equals(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            enforceSlots(event.player);
        }
    }

    /** Moves locked items out of the main hand, offhand and armor slots. Public for GameTests (mock players don't tick). */
    public static void enforceSlots(Player player) {
        if (isExempt(player)) {
            return;
        }
        Inventory inventory = player.getInventory();
        if (isLocked(inventory.getSelected(), player)) {
            ItemStack held = inventory.getSelected();
            inventory.setItem(inventory.selected, ItemStack.EMPTY);
            putBack(player, held);
        }
        if (isLocked(inventory.offhand.get(0), player)) {
            ItemStack offhand = inventory.offhand.get(0);
            inventory.offhand.set(0, ItemStack.EMPTY);
            putBack(player, offhand);
        }
        for (int slot = 0; slot < inventory.armor.size(); slot++) {
            ItemStack worn = inventory.armor.get(slot);
            if (isLocked(worn, player)) {
                inventory.armor.set(slot, ItemStack.EMPTY);
                putBack(player, worn);
            }
        }
    }

    private static void putBack(Player player, ItemStack stack) {
        insertIntoMainInventory(player.getInventory(), stack);
        if (!stack.isEmpty()) {
            ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack, 0.0, 0.0, 0.0);
            drop.setDefaultPickUpDelay();
            player.level().addFreshEntity(drop);
        }
        player.getInventory().setChanged();
    }

    /**
     * Moves as much of the stack as fits into the 27 main slots (not the hotbar), topping up matching stacks first
     * like vanilla does. Shrinks the stack and returns how many items moved.
     */
    public static int insertIntoMainInventory(Inventory inventory, ItemStack stack) {
        int start = stack.getCount();
        for (int slot = FIRST_MAIN_SLOT; slot < Inventory.INVENTORY_SIZE && !stack.isEmpty(); slot++) {
            ItemStack there = inventory.items.get(slot);
            if (!there.isEmpty() && ItemStack.isSameItemSameTags(there, stack) && there.isStackable()) {
                int room = Math.min(there.getMaxStackSize(), inventory.getMaxStackSize()) - there.getCount();
                int moved = Math.min(room, stack.getCount());
                if (moved > 0) {
                    there.grow(moved);
                    there.setPopTime(Inventory.POP_TIME_DURATION);
                    stack.shrink(moved);
                }
            }
        }
        for (int slot = FIRST_MAIN_SLOT; slot < Inventory.INVENTORY_SIZE && !stack.isEmpty(); slot++) {
            if (inventory.items.get(slot).isEmpty()) {
                ItemStack placed = stack.split(Math.min(stack.getMaxStackSize(), inventory.getMaxStackSize()));
                placed.setPopTime(Inventory.POP_TIME_DURATION);
                inventory.items.set(slot, placed);
            }
        }
        if (stack.getCount() != start) {
            inventory.setChanged();
        }
        return start - stack.getCount();
    }
}
