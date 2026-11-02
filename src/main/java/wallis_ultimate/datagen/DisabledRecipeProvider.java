package wallis_ultimate.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

/**
 * Switches vanilla recipes off by overriding them with a file whose Forge condition is always false, so the
 * recipe is skipped when recipes load.
 */
public class DisabledRecipeProvider implements DataProvider {
    /** SURV-005: beds can't be crafted. Dyeing a bed you already have still works (the dye_*_bed recipes). */
    private static final List<ResourceLocation> DISABLED = Arrays.stream(DyeColor.values())
            .map(color -> ResourceLocation.withDefaultNamespace(color.getName() + "_bed"))
            .toList();

    private final PackOutput.PathProvider recipes;

    public DisabledRecipeProvider(PackOutput output) {
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        JsonObject never = new JsonObject();
        never.addProperty("type", "forge:false");
        JsonArray conditions = new JsonArray();
        conditions.add(never);
        JsonObject json = new JsonObject();
        json.add("conditions", conditions);
        return CompletableFuture.allOf(DISABLED.stream()
                .map(id -> DataProvider.saveStable(cache, json, recipes.json(id)))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Disabled vanilla recipes";
    }
}
