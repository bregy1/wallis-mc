package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.ClientDownedState;

/** Server → client: the receiving player's downed state. ticksLeft 0 means "not downed" (SURV-001). */
public record DownedStatePacket(int ticksLeft, int ammo) {
    public static void encode(DownedStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.ticksLeft);
        buffer.writeVarInt(packet.ammo);
    }

    public static DownedStatePacket decode(FriendlyByteBuf buffer) {
        return new DownedStatePacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(DownedStatePacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientDownedState.update(packet.ticksLeft, packet.ammo));
        context.get().setPacketHandled(true);
    }
}
