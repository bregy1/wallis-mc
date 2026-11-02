package wallis_ultimate.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // WORLD-006: not even the wither or the ender dragon may break unminable blocks.
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.REINFORCED_CONCRETE.get(), ModBlocks.RIVETED_STEEL.get(), ModBlocks.ALIEN_ALLOY.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.REINFORCED_CONCRETE.get(), ModBlocks.RIVETED_STEEL.get(), ModBlocks.ALIEN_ALLOY.get());
    }
}
