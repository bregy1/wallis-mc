package wallis_ultimate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.FirePistolPacket;
import wallis_ultimate.network.GiveUpPacket;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.ReviveHoldPacket;

/**
 * Client input for SURV-001/002. While downed: attack fires the pistol (one shot per click), the give-up key
 * gives up, no sprinting. Holding use on a downed teammate reports a revive hold every tick.
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class DownedClient {
    private static boolean holdingRevive;

    private DownedClient() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            // START runs before vanilla handles key presses, so attack clicks never reach vanilla while downed.
            ClientDownedState.tick(mc.player);
            if (ClientDownedState.isDowned()) {
                mc.player.setSprinting(false);
                while (mc.options.keyAttack.consumeClick()) {
                    ModNetwork.CHANNEL.sendToServer(new FirePistolPacket());
                }
            }
            updateReviveHold(mc);
        } else {
            while (ClientKeys.GIVE_UP.consumeClick()) {
                if (ClientDownedState.isDowned()) {
                    ModNetwork.CHANNEL.sendToServer(new GiveUpPacket());
                }
            }
        }
    }

    private static Player downedTeammateUnderCrosshair(Minecraft mc) {
        return mc.crosshairPickEntity instanceof Player target && ClientDownedState.isTeammateDowned(target.getUUID()) ? target : null;
    }

    private static void updateReviveHold(Minecraft mc) {
        Player target = mc.screen == null && mc.options.keyUse.isDown() && !ClientDownedState.isDowned()
                ? downedTeammateUnderCrosshair(mc) : null;
        if (target != null) {
            ModNetwork.CHANNEL.sendToServer(new ReviveHoldPacket(target.getId()));
        } else if (holdingRevive) {
            ModNetwork.CHANNEL.sendToServer(new ReviveHoldPacket(ReviveHoldPacket.RELEASED));
        }
        holdingRevive = target != null;
    }

    /** No vanilla attacking or item use while downed or reviving, and none when using a downed teammate. */
    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.isPickBlock()) {
            return;
        }
        boolean busy = ClientDownedState.isDowned() || ClientDownedState.reviveProgress() > 0;
        if (busy || event.isUseItem() && downedTeammateUnderCrosshair(mc) != null) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDownedState.reset();
        holdingRevive = false;
    }
}
