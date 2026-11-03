package wallis_ultimate.story;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.progression.Tiers;
import wallis_ultimate.team.Teams;

/**
 * /wallis_story, permission level 2: what the quest file's rewards run, plus a way in for testing. /wallis_tier shows the
 * team's tier to anyone, since all members see the same one (PROG-006).
 */
@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StoryCommands {
    private StoryCommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wallis_story").requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("talk").then(Commands.argument("id", StringArgumentType.word()).executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    String id = StringArgumentType.getString(context, "id");
                    if (!ChapterOneTalks.exists(id)) {
                        context.getSource().sendFailure(Component.literal("No such talk: " + id));
                        return 0;
                    }
                    return Conversations.start(player, ChapterOneTalks.talk(id)) ? 1 : 0;
                })))
                .then(Commands.literal("opening").executes(context -> OpeningCutscene.start(context.getSource().getPlayerOrException()) ? 1 : 0))
                .then(Commands.literal("flag").then(Commands.argument("flag", StringArgumentType.word()).executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    StoryState.get(player.server).setFlag(Teams.teamKey(player), StringArgumentType.getString(context, "flag"));
                    return 1;
                }))));
        dispatcher.register(Commands.literal("wallis_tier").executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            Tiers.Tier tier = Tiers.current(player);
            if (tier == null) {
                return 0;
            }
            context.getSource().sendSuccess(() -> Component.translatable(Tiers.MESSAGE_CURRENT, Component.translatable(tier.nameKey()), tier.order()), false);
            return tier.order();
        }));
    }
}
