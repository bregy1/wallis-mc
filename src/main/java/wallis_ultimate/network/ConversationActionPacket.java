package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.story.Conversations;

/** Client → server: a reply was clicked, a line was hurried along, or the screen was closed. The server decides if it counts. */
public record ConversationActionPacket(int session, Action action, int reply) {
    public enum Action {
        REPLY, NEXT, LEAVE
    }

    public static void encode(ConversationActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.session);
        buffer.writeEnum(packet.action);
        buffer.writeVarInt(packet.reply);
    }

    public static ConversationActionPacket decode(FriendlyByteBuf buffer) {
        return new ConversationActionPacket(buffer.readVarInt(), buffer.readEnum(Action.class), buffer.readVarInt());
    }

    public static void handle(ConversationActionPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            switch (packet.action) {
                case REPLY -> Conversations.answer(player, packet.session, packet.reply);
                case NEXT -> Conversations.hurry(player, packet.session);
                case LEAVE -> Conversations.leave(player, packet.session);
            }
        }
        context.get().setPacketHandled(true);
    }
}
