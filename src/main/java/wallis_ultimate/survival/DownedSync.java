package wallis_ultimate.survival;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import wallis_ultimate.network.DownedStatePacket;
import wallis_ultimate.network.ModNetwork;

/** Tells a downed player's client about its own downed state (timer, pistol ammo) — SURV-001. */
public final class DownedSync {
    private DownedSync() {
    }

    public static void sendState(Player player) {
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new DownedStatePacket(Downed.ticksLeft(player), Downed.ammo(player)));
        }
    }
}
