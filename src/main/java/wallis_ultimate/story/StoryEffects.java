package wallis_ultimate.story;

import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import wallis_ultimate.WallisUltimate;

/** What lines and replies in a talk can do. */
public final class StoryEffects {
    public static final String LIST_NAME_KEY = "story." + WallisUltimate.MOD_ID + ".item.lurleens_list";

    private StoryEffects() {
    }

    /** Remembers something for the whole team (PROG-006). Quests and later talks read it. */
    public static Talk.Effect flag(String flag) {
        return (session, actor) -> StoryState.get(actor.server).setFlag(session.team(), flag);
    }

    /** FTB Quests' quest book, as far as the players know Lurleen's scrawled list. Everyone who's there gets their own. */
    public static Talk.Effect lurleensList() {
        return (session, actor) -> {
            for (UUID id : session.participants()) {
                ServerPlayer player = actor.server.getPlayerList().getPlayer(id);
                // Someone who walked out of the talk and started over already has theirs.
                if (player != null && player.getInventory().countItem(FTBQuestsItems.BOOK.get()) == 0) {
                    ItemHandlerHelper.giveItemToPlayer(player, list());
                }
            }
        };
    }

    public static ItemStack list() {
        ItemStack book = new ItemStack(FTBQuestsItems.BOOK.get());
        book.setHoverName(Component.translatable(LIST_NAME_KEY));
        return book;
    }

    /** Runs a command as the player, with permission level 2 and without any output: it works or it quietly doesn't. */
    public static Talk.Effect command(String command) {
        return (session, actor) -> runSilently(actor, command);
    }

    public static void runSilently(ServerPlayer player, String command) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(2).withSuppressedOutput(), command);
    }
}
