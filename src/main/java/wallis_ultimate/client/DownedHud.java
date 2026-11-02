package wallis_ultimate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.survival.Downed;
import wallis_ultimate.survival.Revive;

/**
 * Placeholder HUD for SURV-001/002 (plain text, final look still to be designed): your own downed timer, pistol
 * ammo and give-up hint; your revive progress; and the downed teammates with their remaining time.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DownedHud {
    private static final String PREFIX = "hud." + WallisUltimate.MOD_ID + ".";
    private static final int RED = 0xFF5555;
    private static final int WHITE = 0xFFFFFF;
    private static final int GRAY = 0xAAAAAA;
    private static final int BAR_WIDTH = 80;

    private DownedHud() {
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("downed", DownedHud::render);
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        Font font = mc.font;
        int centerX = width / 2;
        if (ClientDownedState.isDowned()) {
            int y = height / 2 + 16;
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "downed.title"), centerX, y, RED);
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "downed.time_left", formatTime(ClientDownedState.ticksLeft())), centerX, y + 12, WHITE);
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "downed.ammo", ClientDownedState.ammo(), Downed.PISTOL_SHOTS,
                    mc.options.keyAttack.getTranslatedKeyMessage()), centerX, y + 24, WHITE);
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "downed.give_up", ClientKeys.GIVE_UP.getTranslatedKeyMessage()), centerX, y + 36, GRAY);
        }
        int progress = ClientDownedState.reviveProgress();
        if (progress > 0) {
            int y = height / 2 + 12;
            int left = centerX - BAR_WIDTH / 2;
            graphics.drawCenteredString(font, Component.translatable(PREFIX + "reviving"), centerX, y, WHITE);
            graphics.fill(left, y + 11, left + BAR_WIDTH, y + 15, 0x80000000);
            graphics.fill(left, y + 11, left + BAR_WIDTH * Math.min(progress, Revive.REVIVE_TICKS) / Revive.REVIVE_TICKS, y + 15, 0xFF55FF55);
        }
        int line = 0;
        for (ClientDownedState.Teammate teammate : ClientDownedState.teammates()) {
            graphics.drawString(font, Component.translatable(PREFIX + "teammate_down", teammate.name(), formatTime(teammate.ticksLeft())), 4, 4 + line * 10, RED);
            line++;
        }
    }

    private static String formatTime(int ticks) {
        int seconds = (ticks + 19) / 20;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }
}
