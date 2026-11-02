package wallis_ultimate.datagen;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import wallis_ultimate.registry.ModItems;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // SURV-010 (Q-119): 3 string or wool, in any mix, plus 1 paper.
        Ingredient cloth = Ingredient.fromValues(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.STRING)),
                new Ingredient.TagValue(ItemTags.WOOL)));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BANDAGE.get())
                .requires(cloth, 3)
                .requires(Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(consumer);
    }
}
