package wallis_ultimate.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.client.ClientDownedState;

/** Server → client: a teammate's downed timer; 0 means they're up again or dead (SURV-001). */
public record TeammateDownedPacket(UUID playerId, String name, int ticksLeft) {
    public static void encode(TeammateDownedPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeUtf(packet.name, 16);
        buffer.writeVarInt(packet.ticksLeft);
    }

    public static TeammateDownedPacket decode(FriendlyByteBuf buffer) {
        return new TeammateDownedPacket(buffer.readUUID(), buffer.readUtf(16), buffer.readVarInt());
    }

    public static void handle(TeammateDownedPacket packet, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientDownedState.updateTeammate(packet.playerId, packet.name, packet.ticksLeft));
        context.get().setPacketHandled(true);
    }
}
