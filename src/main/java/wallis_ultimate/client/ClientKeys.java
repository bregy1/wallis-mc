package wallis_ultimate.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.network.ModNetwork;
import wallis_ultimate.network.RollRequestPacket;

/**
 * Client-only key bindings. CMBT-002: the roll key defaults to T (the pack moves chat to Enter).
 * SURV-001: the give-up key (used in {@link DownedClient}) defaults to G.
 */
public final class ClientKeys {
    public static final String CATEGORY = "key.categories." + WallisUltimate.MOD_ID;
    public static final KeyMapping ROLL = new KeyMapping("key." + WallisUltimate.MOD_ID + ".roll", InputConstants.KEY_T, CATEGORY);
    public static final KeyMapping GIVE_UP = new KeyMapping("key." + WallisUltimate.MOD_ID + ".give_up", InputConstants.KEY_G, CATEGORY);

    private ClientKeys() {
    }

    @Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ROLL);
            event.register(GIVE_UP);
        }
    }

    @Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class Handler {
        private Handler() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                while (ROLL.consumeClick()) {
                    ModNetwork.CHANNEL.sendToServer(new RollRequestPacket());
                }
            }
        }
    }
}
