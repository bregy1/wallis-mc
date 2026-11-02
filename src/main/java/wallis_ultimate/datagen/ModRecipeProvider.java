package wallis_ultimate.datagen;

import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
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
        // GEAR-006: copper tools, shaped like vanilla's.
        copperTool(consumer, ModItems.COPPER_PICKAXE.get(), "CCC", " S ", " S ");
        copperTool(consumer, ModItems.COPPER_AXE.get(), "CC", "CS", " S");
        copperTool(consumer, ModItems.COPPER_SHOVEL.get(), "C", "S", "S");
        copperTool(consumer, ModItems.COPPER_HOE.get(), "CC", " S", " S");
        // GEAR-007: junk weapons. Flint stands in for nails and tines, since iron is locked at first.
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.NAIL_BAT.get())
                .define('F', Items.FLINT).define('P', ItemTags.PLANKS).define('S', Items.STICK)
                .pattern("F").pattern("P").pattern("S")
                .unlockedBy("has_flint", has(Items.FLINT)).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PITCHFORK.get())
                .define('F', Items.FLINT).define('S', Items.STICK)
                .pattern("FFF").pattern(" S ").pattern(" S ")
                .unlockedBy("has_flint", has(Items.FLINT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.HEAVY_SHOVEL.get())
                .requires(Items.STONE_SHOVEL).requires(Items.COBBLESTONE, 2).requires(Items.STRING)
                .unlockedBy("has_stone_shovel", has(Items.STONE_SHOVEL)).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.FRYING_PAN.get())
                .define('I', Items.IRON_INGOT).define('S', Items.STICK)
                .pattern("II").pattern("II").pattern(" S")
                .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.MOONSHINE_MOLOTOV.get())
                .requires(Items.GLASS_BOTTLE).requires(Items.SUGAR).requires(Items.PAPER)
                .unlockedBy("has_bottle", has(Items.GLASS_BOTTLE)).save(consumer);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.PIPE_BOMB.get())
                .requires(Items.IRON_INGOT).requires(Items.GUNPOWDER).requires(Items.STRING)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER)).save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SLINGSHOT.get())
                .define('S', Items.STICK).define('T', Items.STRING)
                .pattern("STS").pattern(" S ").pattern(" S ")
                .unlockedBy("has_string", has(Items.STRING)).save(consumer);
    }

    private static void copperTool(Consumer<FinishedRecipe> consumer, Item result, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .define('C', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_copper", has(Items.COPPER_INGOT));
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.save(consumer);
    }
}
