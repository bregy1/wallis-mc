package wallis_ultimate.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.ConversationPacket;

/**
 * STORY-002: a conversation for the player who has something to do in it: the replies if they were picked to answer, or Next to
 * hurry a line along. The rest of the world keeps running behind it. A corner button switches swear words to symbols. Placeholder
 * look; the game's own buttons on a dark panel.
 */
public class ConversationScreen extends Screen {
    private static final String PREFIX = "story." + WallisUltimate.MOD_ID + ".conversation.";
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 2;
    private static final int NEXT_WIDTH = 60;
    private static final int OPTION_WIDTH = 120;
    private static final int OPTION_MARGIN = 4;

    public ConversationScreen() {
        super(Component.translatable(PREFIX + "title"));
    }

    /** The conversation moved on: new text, new buttons. */
    void refresh() {
        rebuildWidgets();
    }

    private List<String> replies(ConversationPacket conversation) {
        return conversation.youChoose() ? conversation.replyKeys() : List.of();
    }

    private int extraHeight(ConversationPacket conversation) {
        int count = replies(conversation).size();
        return count > 0 ? count * (BUTTON_HEIGHT + BUTTON_GAP) : BUTTON_HEIGHT + BUTTON_GAP;
    }

    @Override
    protected void init() {
        ConversationPacket conversation = ClientConversation.current();
        addRenderableWidget(Button.builder(cleanLanguageLabel(), button -> {
            CleanLanguage.toggle();
            button.setMessage(cleanLanguageLabel());
        }).bounds(width - OPTION_WIDTH - OPTION_MARGIN, OPTION_MARGIN, OPTION_WIDTH, BUTTON_HEIGHT).build());
        if (conversation == null) {
            return;
        }
        Component text = Component.translatable(conversation.textKey());
        int panelWidth = Math.min(StoryHud.PANEL_WIDTH, width - 2 * StoryHud.PADDING);
        int left = (width - panelWidth) / 2 + StoryHud.PADDING;
        int buttonWidth = panelWidth - 2 * StoryHud.PADDING;
        int extra = extraHeight(conversation);
        boolean hint = !conversation.youChoose() && !conversation.replyKeys().isEmpty();
        int y = height - StoryHud.BOTTOM_MARGIN - StoryHud.PADDING - extra + BUTTON_GAP;
        List<String> replies = replies(conversation);
        for (int i = 0; i < replies.size(); i++) {
            int index = i;
            addRenderableWidget(Button.builder(Component.translatable(replies.get(i)), button -> ClientConversation.reply(index))
                    .bounds(left, y, buttonWidth, BUTTON_HEIGHT).build());
            y += BUTTON_HEIGHT + BUTTON_GAP;
        }
        if (replies.isEmpty() && conversation.youLead() && conversation.replyKeys().isEmpty()) {
            addRenderableWidget(Button.builder(Component.translatable(PREFIX + "next"), button -> ClientConversation.next())
                    .bounds(left + buttonWidth - NEXT_WIDTH, y, NEXT_WIDTH, BUTTON_HEIGHT).build());
        }
    }

    private static Component cleanLanguageLabel() {
        return Component.translatable(PREFIX + (CleanLanguage.isOn() ? "clean_on" : "clean_off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ConversationPacket conversation = ClientConversation.current();
        if (conversation != null) {
            Component hint = !conversation.youChoose() && !conversation.replyKeys().isEmpty()
                    ? Component.translatable(PREFIX + "choosing", conversation.chooserName()) : null;
            StoryHud.drawPanel(graphics, font, width, height, Component.translatable(conversation.speakerKey()), conversation.speakerColor(),
                    Component.translatable(conversation.textKey()), hint, extraHeight(conversation));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, StoryHud.ABOVE_CHAT);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.pose().popPose();
    }

    /** Esc: out of the talk. It goes on for the others; alone, the NPC can pick it up again from the start. */
    @Override
    public void onClose() {
        ClientConversation.leave();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
