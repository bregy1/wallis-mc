package wallis_ultimate.survival;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import wallis_ultimate.network.DownedStatePacket;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.ReviveProgressPacket;
import wallis_ultimate.network.TeammateDownedPacket;
import wallis_ultimate.team.Teams;

/**
 * Server → client updates for SURV-001/002: the downed player's own state (timer, pistol ammo), the downed
 * timer shown to teammates, and the reviver's progress. Mock players in GameTests have no connection and are skipped.
 */
public final class DownedSync {
    private DownedSync() {
    }

    private static ServerPlayer connected(Player player) {
        return player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null ? serverPlayer : null;
    }

    public static void sendState(Player player) {
        ServerPlayer serverPlayer = connected(player);
        if (serverPlayer == null) {
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                new DownedStatePacket(Downed.ticksLeft(player), Downed.ammo(player)));
        TeammateDownedPacket teammatePacket = new TeammateDownedPacket(player.getUUID(), player.getGameProfile().getName(), Downed.ticksLeft(player));
        for (ServerPlayer teammate : Teams.onlineTeammates(serverPlayer)) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> teammate), teammatePacket);
        }
    }

    /** Teammates hear an alert wherever they are when someone on their team goes down. */
    public static void alertTeammates(Player player) {
        ServerPlayer serverPlayer = connected(player);
        if (serverPlayer == null) {
            return;
        }
        for (ServerPlayer teammate : Teams.onlineTeammates(serverPlayer)) {
            teammate.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0F, 0.5F);
        }
    }

    public static void sendReviveProgress(Player reviver, int progress) {
        ServerPlayer serverPlayer = connected(reviver);
        if (serverPlayer != null) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ReviveProgressPacket(progress));
        }
    }
}
