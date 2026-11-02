package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.survival.Travel;

/** Client → server: the player picked a safe spot on the signpost's list (WORLD-004). */
public record TravelRequestPacket(String spotId) {
    public static void encode(TravelRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.spotId);
    }

    public static TravelRequestPacket decode(FriendlyByteBuf buffer) {
        return new TravelRequestPacket(buffer.readUtf());
    }

    public static void handle(TravelRequestPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            Travel.travel(player, packet.spotId);
        }
        context.get().setPacketHandled(true);
    }
}
