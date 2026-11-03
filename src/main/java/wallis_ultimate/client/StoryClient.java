package wallis_ultimate.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wallis_ultimate.WallisUltimate;

/** STORY-002: the clean-language option also sits behind the Config button in the mods list, for players who want it before the first swear. */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StoryClient {
    private StoryClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new OptionsScreen(parent)));
    }

    /** Runs after the game has loaded the language: that replaces our wrapper. */
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> CleanLanguage.apply());
    }

    private static final class OptionsScreen extends Screen {
        private static final int BUTTON_WIDTH = 200;
        private static final int BUTTON_HEIGHT = 20;
        private static final int TEXT_COLOR = 0xFFFFFF;
        private final Screen parent;

        OptionsScreen(Screen parent) {
            super(Component.translatable("story." + WallisUltimate.MOD_ID + ".conversation.options"));
            this.parent = parent;
        }

        private static Component label() {
            return Component.translatable("story." + WallisUltimate.MOD_ID + ".conversation." + (CleanLanguage.isOn() ? "clean_on" : "clean_off"));
        }

        @Override
        protected void init() {
            int x = (width - BUTTON_WIDTH) / 2;
            addRenderableWidget(Button.builder(label(), button -> {
                CleanLanguage.toggle();
                button.setMessage(label());
            }).bounds(x, height / 2 - BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(x, height / 2 + BUTTON_HEIGHT, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            graphics.drawCenteredString(font, title, width / 2, height / 2 - 2 * BUTTON_HEIGHT, TEXT_COLOR);
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            minecraft.setScreen(parent);
        }
    }
}
