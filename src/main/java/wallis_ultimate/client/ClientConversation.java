package wallis_ultimate.client;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import wallis_ultimate.network.CaptionPacket;
import wallis_ultimate.network.ConversationActionPacket;
import wallis_ultimate.network.ConversationPacket;
import wallis_ultimate.network.ModNetwork;

/**
 * The client's view of the team's conversation (STORY-002). Whoever has something to do gets the screen: the member picked to
 * answer, and the one who started the talk, who can hurry a line along. Everyone else stays free to move and reads along on the
 * HUD, so a teammate in a fight isn't pulled into a screen.
 */
public final class ClientConversation {
    private static final int NONE = -1;
    @Nullable
    private static ConversationPacket current;
    /** The talk this player walked out of, which shouldn't pull them back in. */
    private static int leftSession = NONE;
    @Nullable
    private static CaptionPacket caption;
    private static int captionTicksLeft;

    private ClientConversation() {
    }

    @Nullable
    public static ConversationPacket current() {
        return current;
    }

    @Nullable
    public static CaptionPacket caption() {
        return caption;
    }

    public static void accept(ConversationPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (!packet.open()) {
            if (current != null && current.session() == packet.session()) {
                current = null;
                if (mc.screen instanceof ConversationScreen) {
                    mc.setScreen(null);
                }
            }
            return;
        }
        current = packet;
        if (packet.session() != leftSession) {
            leftSession = NONE;
        }
        if (!hasSomethingToDo() && mc.screen instanceof ConversationScreen) {
            mc.setScreen(null);
        } else if (mc.screen instanceof ConversationScreen screen) {
            screen.refresh();
        } else {
            openScreenIfNeeded();
        }
    }

    private static boolean hasSomethingToDo() {
        return current != null && (current.youChoose() || (current.youLead() && current.replyKeys().isEmpty()));
    }

    /** Also from the tick: someone with their inventory open gets the screen as soon as they close it. */
    private static void openScreenIfNeeded() {
        Minecraft mc = Minecraft.getInstance();
        if (hasSomethingToDo() && mc.screen == null && current.session() != leftSession) {
            mc.setScreen(new ConversationScreen());
        }
    }

    /** A click on a reply: the server decides whether it's this player's turn. */
    public static void reply(int index) {
        send(ConversationActionPacket.Action.REPLY, index);
    }

    public static void next() {
        send(ConversationActionPacket.Action.NEXT, 0);
    }

    /** The screen was closed with Esc. */
    public static void leave() {
        if (current != null) {
            leftSession = current.session();
        }
        send(ConversationActionPacket.Action.LEAVE, 0);
    }

    private static void send(ConversationActionPacket.Action action, int index) {
        if (current != null) {
            ModNetwork.CHANNEL.sendToServer(new ConversationActionPacket(current.session(), action, index));
        }
    }

    public static List<String> replies() {
        return current == null ? List.of() : current.replyKeys();
    }

    public static void showCaption(CaptionPacket packet) {
        caption = packet;
        captionTicksLeft = packet.ticks();
    }

    public static void tick() {
        if (captionTicksLeft > 0 && --captionTicksLeft == 0) {
            caption = null;
        }
        if (Minecraft.getInstance().level == null) {
            current = null;
            caption = null;
            return;
        }
        openScreenIfNeeded();
    }
}
