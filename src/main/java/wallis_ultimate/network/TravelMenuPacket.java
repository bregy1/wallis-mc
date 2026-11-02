package wallis_ultimate.network;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.TravelScreen;

/** Server → client: the player used a signpost; here's where they are and where the team can go from there (WORLD-004). */
public record TravelMenuPacket(String here, List<String> known) {
    public static void encode(TravelMenuPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.here);
        buffer.writeCollection(packet.known, FriendlyByteBuf::writeUtf);
    }

    public static TravelMenuPacket decode(FriendlyByteBuf buffer) {
        return new TravelMenuPacket(buffer.readUtf(), buffer.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(TravelMenuPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TravelScreen.open(packet.here, packet.known));
        context.get().setPacketHandled(true);
    }
}
