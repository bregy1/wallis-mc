package wallis_ultimate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/** STORY-014: the fiction disclaimer as small gray text at the top of the title screen, above the logo (Q-145). */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TitleDisclaimer {
    private static final Component TEXT = Component.translatable("menu." + WallisUltimate.MOD_ID + ".disclaimer");
    private static final float SCALE = 0.75F;
    private static final int COLOR = 0xA0A0A0;
    private static final int TOP = 3;

    private TitleDisclaimer() {
    }

    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int width = (int) ((screen.width - 20) / SCALE);
        graphics.pose().pushPose();
        graphics.pose().scale(SCALE, SCALE, 1.0F);
        int y = (int) (TOP / SCALE);
        for (FormattedCharSequence line : mc.font.split(TEXT, width)) {
            graphics.drawCenteredString(mc.font, line, (int) (screen.width / 2 / SCALE), y, COLOR);
            y += mc.font.lineHeight + 1;
        }
        graphics.pose().popPose();
    }
}
