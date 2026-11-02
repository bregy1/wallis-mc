package wallis_ultimate.gametest;

import java.util.Arrays;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.survival.Beds;

/** Checks for SURV-005: beds are rare. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedGameTests {
    private static final String PLATFORM = "platform";

    private BedGameTests() {
    }

    /** No recipe makes a new bed. Recoloring a bed you already have (bed + dye) is still allowed. */
    @GameTest(template = PLATFORM)
    public static void bedsCantBeCrafted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            boolean makesBed = recipe.getResultItem(level.registryAccess()).getItem() instanceof BedItem;
            boolean usesBed = recipe.getIngredients().stream()
                    .anyMatch(ingredient -> Arrays.stream(ingredient.getItems()).anyMatch(stack -> stack.getItem() instanceof BedItem));
            if (makesBed && !usesBed) {
                helper.fail("recipe " + recipe.getId() + " makes a new bed");
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void onlySleepingThroughTheNightHeals(GameTestHelper helper) {
        if (!Beds.sleptThroughTheNight(false, false, 1000L) || !Beds.sleptThroughTheNight(false, false, 24000L * 3 + 50L)) {
            helper.fail("waking in the morning doesn't count");
        }
        if (Beds.sleptThroughTheNight(false, true, 1000L)) {
            helper.fail("getting out of bed counts");
        }
        if (Beds.sleptThroughTheNight(true, false, 1000L)) {
            helper.fail("being thrown out of bed counts");
        }
        if (Beds.sleptThroughTheNight(false, false, 18000L)) {
            helper.fail("waking at midnight counts");
        }

        Player player = helper.makeMockPlayer();
        player.setHealth(5.0F);
        MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, false, true));
        if (player.getHealth() != 5.0F) {
            helper.fail("getting out of bed healed");
        }
        MinecraftForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, false, false));
        float expected = Beds.sleptThroughTheNight(false, false, helper.getLevel().getDayTime()) ? player.getMaxHealth() : 5.0F;
        if (player.getHealth() != expected) {
            helper.fail("health after waking: " + player.getHealth() + ", expected " + expected);
        }
        helper.succeed();
    }
}
