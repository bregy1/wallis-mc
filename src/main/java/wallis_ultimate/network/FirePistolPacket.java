package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.survival.Downed;

/** Client → server: a downed player clicked attack. The server fires the downed pistol if allowed (SURV-001). */
public record FirePistolPacket() {
    public static void encode(FirePistolPacket packet, FriendlyByteBuf buffer) {
    }

    public static FirePistolPacket decode(FriendlyByteBuf buffer) {
        return new FirePistolPacket();
    }

    public static void handle(FirePistolPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            Downed.firePistol(player);
        }
        context.get().setPacketHandled(true);
    }
}
