package wallis_ultimate.block;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import wallis_ultimate.registry.ModMenus;

/**
 * SURV-004: the grave's screen, a six-row chest you can only take items out of (Q-122). Its own menu type, so the
 * client builds the same take-only slots and nothing flickers back.
 */
public class GraveMenu extends ChestMenu {
    private static final int ROWS = 6;

    /** Client side: the contents arrive from the server. */
    public GraveMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(GraveBlockEntity.SIZE));
    }

    public GraveMenu(int containerId, Inventory playerInventory, Container grave) {
        super(ModMenus.GRAVE.get(), containerId, playerInventory, grave, ROWS);
        // ChestMenu adds the grave's slots first; swap them for take-only ones at the same place.
        for (int i = 0; i < GraveBlockEntity.SIZE; i++) {
            Slot chestSlot = slots.get(i);
            Slot takeOnly = new Slot(grave, chestSlot.getContainerSlot(), chestSlot.x, chestSlot.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            };
            takeOnly.index = i;
            slots.set(i, takeOnly);
        }
    }
}
