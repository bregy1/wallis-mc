package wallis_ultimate.worldgen;

import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DistrictBorder {
    /** WORLD-002: each district is 2,000 x 2,000 blocks. */
    public static final double DISTRICT_SIZE = 2000;

    private DistrictBorder() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        WorldBorder border = event.getServer().overworld().getWorldBorder();
        // Only an untouched border (vanilla default) gets resized, so later changes by admins stick.
        if (border.getSize() == WorldBorder.MAX_SIZE) {
            border.setCenter(0, 0);
            border.setSize(DISTRICT_SIZE);
        }
    }
}
