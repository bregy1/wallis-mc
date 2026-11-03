package wallis_ultimate.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModKit;
import wallis_ultimate.registry.ModTiers;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // WORLD-006: not even the wither or the ender dragon may break unminable blocks.
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.REINFORCED_CONCRETE.get(), ModBlocks.RIVETED_STEEL.get(), ModBlocks.ALIEN_ALLOY.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.REINFORCED_CONCRETE.get(), ModBlocks.RIVETED_STEEL.get(), ModBlocks.ALIEN_ALLOY.get());
        // SURV-004: graves are just as unbreakable, so nobody but the owner gets at the items.
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.GRAVE.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.GRAVE.get());
        tag(BlockTags.WITHER_IMMUNE).add(ModBlocks.TRAVEL_SIGNPOST.get());
        tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.TRAVEL_SIGNPOST.get());
        // GEAR-006: iron needs at least a copper pickaxe, one tier above what copper ore needs (a stone one).
        tag(ModTiers.NEEDS_COPPER_TOOL).add(Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK);
        // WORLD-013: which tool mines a prop fastest; none of them needs one to drop.
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.SANDBAGS.get(), ModBlocks.SANDBAG_SLAB.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.BARBED_WIRE.get(), ModBlocks.OIL_DRUM.get(), ModBlocks.WRECK_BODY.get(), ModBlocks.WRECK_PANEL.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.SUPPLY_CRATE.get(), ModBlocks.ANTLERS.get());
        tag(BlockTags.SLABS).add(ModBlocks.SANDBAG_SLAB.get(), ModBlocks.WRECK_PANEL.get());
        ModKit.pickaxeBlocks().forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModKit.axeBlocks().forEach(block -> tag(BlockTags.MINEABLE_WITH_AXE).add(block.get()));
        ModKit.shovelBlocks().forEach(block -> tag(BlockTags.MINEABLE_WITH_SHOVEL).add(block.get()));
        tag(BlockTags.SLABS).add(ModKit.CRACKED_CONCRETE_SLAB.get(), ModKit.ASPHALT_SLAB.get(), ModKit.EV_PANEL.get());
        tag(BlockTags.STAIRS).add(ModKit.CRACKED_CONCRETE_STAIRS.get());
        // Walls and panes only connect to what they know.
        tag(BlockTags.WALLS).add(ModKit.CRACKED_CONCRETE_WALL.get());
    }
}
