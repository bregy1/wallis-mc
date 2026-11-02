package wallis_ultimate.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wallis_ultimate.WallisUltimate;

@Mod.EventBusSubscriber(modid = WallisUltimate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {
    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, files));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
        ModBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, event.getLookupProvider(), files));
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(output, event.getLookupProvider(), blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new DisabledRecipeProvider(output));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
    }
}
