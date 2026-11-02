package wallis_ultimate.gametest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.TestReporter;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import wallis_ultimate.WallisUltimate;

/**
 * Logs every GameTest result, so in-world runs (/test runall over RCON) are readable from the server log.
 * Vanilla's reporter only logs failures. Active only when GameTests are enabled (dev runs).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TestResultLogger implements TestReporter {
    private TestResultLogger() {
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (ForgeGameTestHooks.isGametestEnabled()) {
            GlobalTestReporter.replaceWith(new TestResultLogger());
        }
    }

    @Override
    public void onTestFailed(GameTestInfo info) {
        WallisUltimate.LOGGER.error("[GAMETEST] FAILED {}: {}", info.getTestName(), info.getError().getMessage());
    }

    @Override
    public void onTestSuccess(GameTestInfo info) {
        WallisUltimate.LOGGER.info("[GAMETEST] PASSED {} ({} ms)", info.getTestName(), info.getRunTime());
    }
}
