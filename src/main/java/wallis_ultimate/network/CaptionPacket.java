package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.ClientConversation;

/** Server → client: one line shown on the HUD for a while, in a cutscene (STORY-010) or over the radio (STORY-003). */
public record CaptionPacket(String speakerKey, int speakerColor, String textKey, int ticks) {
    public static void encode(CaptionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.speakerKey);
        buffer.writeInt(packet.speakerColor);
        buffer.writeUtf(packet.textKey);
        buffer.writeVarInt(packet.ticks);
    }

    public static CaptionPacket decode(FriendlyByteBuf buffer) {
        return new CaptionPacket(buffer.readUtf(), buffer.readInt(), buffer.readUtf(), buffer.readVarInt());
    }

    public static void handle(CaptionPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientConversation.showCaption(packet));
        context.get().setPacketHandled(true);
    }
}
