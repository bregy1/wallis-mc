package wallis_ultimate.gametest;

import java.util.Arrays;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;

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
}
