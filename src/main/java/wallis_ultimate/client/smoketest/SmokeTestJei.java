package wallis_ultimate.client.smoketest;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import wallis_ultimate.WallisUltimate;

/**
 * Gives the client smoke test a handle on JEI, so it can open JEI's recipe view. Holds nothing outside smoke-test
 * runs. Only touch this class when JEI is loaded.
 */
@JeiPlugin
public final class SmokeTestJei implements IModPlugin {
    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "smoke_test");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (ClientSmokeTest.ENABLED) {
            runtime = jeiRuntime;
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    /** Opens JEI's recipe view for recipes that make the stack. False if JEI isn't running. */
    static boolean showRecipesFor(ItemStack stack) {
        if (runtime == null) {
            return false;
        }
        runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory()
                .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack));
        return true;
    }
}
