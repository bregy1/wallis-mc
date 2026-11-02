package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.ClientDownedState;

/** Server → client: the receiving player's revive progress in ticks; 0 means "not reviving" (SURV-002). */
public record ReviveProgressPacket(int progress) {
    public static void encode(ReviveProgressPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.progress);
    }

    public static ReviveProgressPacket decode(FriendlyByteBuf buffer) {
        return new ReviveProgressPacket(buffer.readVarInt());
    }

    public static void handle(ReviveProgressPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientDownedState.setReviveProgress(packet.progress));
        context.get().setPacketHandled(true);
    }
}
