package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.survival.Revive;

/**
 * Client → server, every tick while the use key is held on a downed teammate (SURV-002). A negative target id
 * means "released". The server checks team, range and state itself.
 */
public record ReviveHoldPacket(int targetId) {
    public static final int RELEASED = -1;

    public static void encode(ReviveHoldPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.targetId + 1);
    }

    public static ReviveHoldPacket decode(FriendlyByteBuf buffer) {
        return new ReviveHoldPacket(buffer.readVarInt() - 1);
    }

    public static void handle(ReviveHoldPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer reviver = context.get().getSender();
        if (reviver != null) {
            if (packet.targetId >= 0 && reviver.level().getEntity(packet.targetId) instanceof Player target) {
                Revive.hold(reviver, target);
            } else {
                Revive.cancel(reviver);
            }
        }
        context.get().setPacketHandled(true);
    }
}
