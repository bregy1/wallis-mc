package wallis_ultimate.client;

import net.bananemdnsa.historystages.api.client.ClientLocks;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import wallis_ultimate.WallisUltimate;

/**
 * PROG-002: locked items are shown grayed out (Q-128). A gray tint over the item; History Stages draws its lock
 * icon on top. Drawn wherever item decorations are: inventories, the hotbar and JEI.
 */
public final class LockedItemTint implements IItemDecorator {
    /** The inventory slot gray, mostly opaque. */
    private static final int TINT = 0xB08B8B8B;
    /**
     * Above the item (rendered around z 150–170), below the stack count (200) and History Stages' lock icon (250).
     * Decorators render with depth testing on, so the icon stays visible whichever decorator runs first.
     */
    private static final int Z = 190;

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (!ClientLocks.isItemGated(stack)) {
            return false;
        }
        guiGraphics.fill(xOffset, yOffset, xOffset + 16, yOffset + 16, Z, TINT);
        return true;
    }

    @Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void register(RegisterItemDecorationsEvent event) {
            LockedItemTint tint = new LockedItemTint();
            ForgeRegistries.ITEMS.forEach(item -> event.register(item, tint));
        }
    }
}
