package wallis_ultimate.boss;

import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

/**
 * ENEMY-011: /wallis_boss hog_daddy start, permission level 2. The way the story starts the fight: the last quest of
 * Chapter 1 runs it as a command reward, as the player who claims it, so that reward needs FTB Quests' "elevate perms".
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BossCommands {
    private static final int PERMISSION = 2;

    private BossCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wallis_boss").requires(source -> source.hasPermission(PERMISSION))
                .then(Commands.literal("hog_daddy")
                        .then(Commands.literal("start")
                                .executes(context -> HogDaddyFight.start(context.getSource().getPlayerOrException()) ? 1 : 0))));
    }
}
