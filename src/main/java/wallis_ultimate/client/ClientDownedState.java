package wallis_ultimate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Pose;

/** Client-side copy of the local player's downed state, set from DownedStatePacket (SURV-001). */
public final class ClientDownedState {
    private static int ticksLeft;
    private static int ammo;

    private ClientDownedState() {
    }

    public static void update(int newTicksLeft, int newAmmo) {
        ticksLeft = newTicksLeft;
        ammo = newAmmo;
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setForcedPose(isDowned() ? Pose.SWIMMING : null);
        }
    }

    public static boolean isDowned() {
        return ticksLeft > 0;
    }

    public static int ticksLeft() {
        return ticksLeft;
    }

    public static int ammo() {
        return ammo;
    }
}
