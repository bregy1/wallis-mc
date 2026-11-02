package wallis_ultimate.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModMenus;

@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModScreens {
    private ModScreens() {
    }

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        // The grave looks like a vanilla six-row chest (SURV-004).
        event.enqueueWork(() -> MenuScreens.register(ModMenus.GRAVE.get(), ContainerScreen::new));
    }
}
