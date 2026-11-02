package wallis_ultimate.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import wallis_ultimate.survival.Downed;

/** Client → server: a downed player pressed the give-up key (SURV-001). */
public record GiveUpPacket() {
    public static void encode(GiveUpPacket packet, FriendlyByteBuf buffer) {
    }

    public static GiveUpPacket decode(FriendlyByteBuf buffer) {
        return new GiveUpPacket();
    }

    public static void handle(GiveUpPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            Downed.giveUp(player);
        }
        context.get().setPacketHandled(true);
    }
}
