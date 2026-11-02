package wallis_ultimate.survival;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/** SURV-005: sleeping through the night fully heals (Q-123). Leaving the bed early gives nothing. */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class Beds {
    private static final long TICKS_PER_DAY = 24000L;
    private static final long NIGHTFALL = 12000L;

    private Beds() {
    }

    /**
     * Vanilla skips the night by setting the time to morning and then waking everyone with
     * (wakeImmediately = false, updateLevel = false). Getting out of bed yourself uses updateLevel = true.
     */
    public static boolean sleptThroughTheNight(boolean wakeImmediately, boolean updateLevel, long dayTime) {
        return !wakeImmediately && !updateLevel && dayTime % TICKS_PER_DAY < NIGHTFALL;
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()
                && sleptThroughTheNight(event.wakeImmediately(), event.updateLevel(), player.level().getDayTime())) {
            player.setHealth(player.getMaxHealth());
        }
    }
}
