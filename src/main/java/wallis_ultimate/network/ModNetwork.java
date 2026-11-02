package wallis_ultimate.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import wallis_ultimate.WallisUltimate;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(RollRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RollRequestPacket::encode)
                .decoder(RollRequestPacket::decode)
                .consumerMainThread(RollRequestPacket::handle)
                .add();
        CHANNEL.messageBuilder(DownedStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DownedStatePacket::encode)
                .decoder(DownedStatePacket::decode)
                .consumerMainThread(DownedStatePacket::handle)
                .add();
    }
}
