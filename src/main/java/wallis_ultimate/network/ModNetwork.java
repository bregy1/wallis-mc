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
        CHANNEL.messageBuilder(TeammateDownedPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TeammateDownedPacket::encode)
                .decoder(TeammateDownedPacket::decode)
                .consumerMainThread(TeammateDownedPacket::handle)
                .add();
        CHANNEL.messageBuilder(ReviveProgressPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ReviveProgressPacket::encode)
                .decoder(ReviveProgressPacket::decode)
                .consumerMainThread(ReviveProgressPacket::handle)
                .add();
        CHANNEL.messageBuilder(FirePistolPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FirePistolPacket::encode)
                .decoder(FirePistolPacket::decode)
                .consumerMainThread(FirePistolPacket::handle)
                .add();
        CHANNEL.messageBuilder(GiveUpPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(GiveUpPacket::encode)
                .decoder(GiveUpPacket::decode)
                .consumerMainThread(GiveUpPacket::handle)
                .add();
        CHANNEL.messageBuilder(ReviveHoldPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ReviveHoldPacket::encode)
                .decoder(ReviveHoldPacket::decode)
                .consumerMainThread(ReviveHoldPacket::handle)
                .add();
        CHANNEL.messageBuilder(TravelMenuPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TravelMenuPacket::encode)
                .decoder(TravelMenuPacket::decode)
                .consumerMainThread(TravelMenuPacket::handle)
                .add();
        CHANNEL.messageBuilder(TravelRequestPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TravelRequestPacket::encode)
                .decoder(TravelRequestPacket::decode)
                .consumerMainThread(TravelRequestPacket::handle)
                .add();
        CHANNEL.messageBuilder(ConversationPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ConversationPacket::encode)
                .decoder(ConversationPacket::decode)
                .consumerMainThread(ConversationPacket::handle)
                .add();
        CHANNEL.messageBuilder(ConversationActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ConversationActionPacket::encode)
                .decoder(ConversationActionPacket::decode)
                .consumerMainThread(ConversationActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(CaptionPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CaptionPacket::encode)
                .decoder(CaptionPacket::decode)
                .consumerMainThread(CaptionPacket::handle)
                .add();
    }
}
