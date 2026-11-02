package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.combat.DodgeRoll;

/** Client → server: "I pressed the roll key". The server decides whether the roll happens (CMBT-002). */
public record RollRequestPacket() {
    public static void encode(RollRequestPacket packet, FriendlyByteBuf buffer) {
    }

    public static RollRequestPacket decode(FriendlyByteBuf buffer) {
        return new RollRequestPacket();
    }

    public static void handle(RollRequestPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            DodgeRoll.tryRoll(player);
        }
        context.get().setPacketHandled(true);
    }
}
