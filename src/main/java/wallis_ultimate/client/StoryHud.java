package wallis_ultimate.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.CaptionPacket;
import wallis_ultimate.network.ConversationPacket;

/**
 * Story text on the HUD: captions in cutscenes and over the radio, and the conversation for team members who aren't the one
 * answering. Last in line, so it's drawn over a cutscene's letterbox bars. Placeholder look: a dark panel and plain text.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StoryHud {
    static final int PANEL_WIDTH = 340;
    static final int PANEL_COLOR = 0xB0101010;
    static final int TEXT_COLOR = 0xFFFFFF;
    static final int HINT_COLOR = 0xA0A0A0;
    static final int PADDING = 6;
    static final int BOTTOM_MARGIN = 28;
    static final int LINE_HEIGHT = 10;
    /** Chat is drawn at a height of its own, which would put it through the panel. */
    static final float ABOVE_CHAT = 200.0F;

    private StoryHud() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientConversation.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        ConversationPacket conversation = ClientConversation.current();
        if (conversation != null && !(mc.screen instanceof ConversationScreen)) {
            Component hint = conversation.replyKeys().isEmpty() ? null
                    : Component.translatable("story." + WallisUltimate.MOD_ID + ".conversation.choosing", conversation.chooserName());
            drawPanel(graphics, mc.font, width, height, Component.translatable(conversation.speakerKey()), conversation.speakerColor(),
                    Component.translatable(conversation.textKey()), hint, 0);
            return;
        }
        CaptionPacket caption = ClientConversation.caption();
        if (caption != null) {
            drawPanel(graphics, mc.font, width, height, Component.translatable(caption.speakerKey()), caption.speakerColor(),
                    Component.translatable(caption.textKey()), null, 0);
        }
    }

    /**
     * The panel all story text sits in, at the bottom of the screen. Returns the y of its top. {@code extraHeight} leaves room
     * under the text for a screen's buttons.
     */
    static int drawPanel(GuiGraphics graphics, Font font, int screenWidth, int screenHeight, Component speaker, int speakerColor, Component text,
                         Component hint, int extraHeight) {
        int width = Math.min(PANEL_WIDTH, screenWidth - 2 * PADDING);
        int textWidth = width - 2 * PADDING;
        List<FormattedCharSequence> lines = font.split(text, textWidth);
        int height = PADDING + LINE_HEIGHT + 2 + lines.size() * LINE_HEIGHT + (hint == null ? 0 : LINE_HEIGHT + 2) + extraHeight + PADDING;
        int left = (screenWidth - width) / 2;
        int top = screenHeight - BOTTOM_MARGIN - height;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, ABOVE_CHAT);
        graphics.fill(left, top, left + width, top + height, PANEL_COLOR);
        int y = top + PADDING;
        graphics.drawString(font, speaker, left + PADDING, y, speakerColor);
        y += LINE_HEIGHT + 2;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, left + PADDING, y, TEXT_COLOR);
            y += LINE_HEIGHT;
        }
        if (hint != null) {
            graphics.drawString(font, hint, left + PADDING, y + 2, HINT_COLOR);
        }
        graphics.pose().popPose();
        return top;
    }

    /** How tall the panel's text part is, for placing a screen's buttons below it. */
    static int textHeight(Font font, int screenWidth, Component text, boolean hasHint) {
        int width = Math.min(PANEL_WIDTH, screenWidth - 2 * PADDING) - 2 * PADDING;
        return PADDING + LINE_HEIGHT + 2 + font.split(text, width).size() * LINE_HEIGHT + (hasHint ? LINE_HEIGHT + 2 : 0);
    }
}
