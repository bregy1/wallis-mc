package wallis_ultimate.network;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.ClientConversation;

/**
 * Server → client: the line a conversation is on (STORY-002), or that it's over. Only lang keys travel, never text. The
 * replies go to everyone so they can see what's being chosen; only the picked member gets to click one.
 */
public record ConversationPacket(int session, boolean open, String speakerKey, int speakerColor, String textKey, List<String> replyKeys,
                                 String chooserName, boolean youChoose, boolean youLead, int lineTicks) {
    public static ConversationPacket closed(int session) {
        return new ConversationPacket(session, false, "", 0, "", List.of(), "", false, false, 0);
    }

    public static void encode(ConversationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.session);
        buffer.writeBoolean(packet.open);
        buffer.writeUtf(packet.speakerKey);
        buffer.writeInt(packet.speakerColor);
        buffer.writeUtf(packet.textKey);
        buffer.writeCollection(packet.replyKeys, FriendlyByteBuf::writeUtf);
        buffer.writeUtf(packet.chooserName);
        buffer.writeBoolean(packet.youChoose);
        buffer.writeBoolean(packet.youLead);
        buffer.writeVarInt(packet.lineTicks);
    }

    public static ConversationPacket decode(FriendlyByteBuf buffer) {
        return new ConversationPacket(buffer.readVarInt(), buffer.readBoolean(), buffer.readUtf(), buffer.readInt(), buffer.readUtf(),
                buffer.readList(FriendlyByteBuf::readUtf), buffer.readUtf(), buffer.readBoolean(), buffer.readBoolean(), buffer.readVarInt());
    }

    public static void handle(ConversationPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConversation.accept(packet));
        context.get().setPacketHandled(true);
    }
}
