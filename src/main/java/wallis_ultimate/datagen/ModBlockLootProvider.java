package wallis_ultimate.datagen;

import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModKit;

public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // WORLD-013: props drop themselves; the locks decide what players can do with them.
        dropSelf(ModBlocks.SANDBAGS.get());
        add(ModBlocks.SANDBAG_SLAB.get(), createSlabItemTable(ModBlocks.SANDBAG_SLAB.get()));
        dropSelf(ModBlocks.BARBED_WIRE.get());
        dropSelf(ModBlocks.OIL_DRUM.get());
        dropSelf(ModBlocks.GREEN_TARP.get());
        dropSelf(ModBlocks.BLUE_TARP.get());
        dropSelf(ModBlocks.GREEN_TARP_SLOPE.get());
        dropSelf(ModBlocks.BLUE_TARP_SLOPE.get());
        dropSelf(ModBlocks.SUPPLY_CRATE.get());
        dropSelf(ModBlocks.WRECK_BODY.get());
        add(ModBlocks.WRECK_PANEL.get(), createSlabItemTable(ModBlocks.WRECK_PANEL.get()));
        dropSelf(ModBlocks.WRECK_WINDOW.get());
        dropSelf(ModBlocks.WRECK_WHEEL.get());
        dropSelf(ModBlocks.BEER_CANS.get());
        dropSelf(ModBlocks.ANTLERS.get());
        ModKit.BLOCKS.getEntries().forEach(entry -> {
            Block block = entry.get();
            if (block instanceof SlabBlock) {
                add(block, createSlabItemTable(block));
            } else {
                dropSelf(block);
            }
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.concat(ModBlocks.BLOCKS.getEntries().stream(), ModKit.BLOCKS.getEntries().stream()).map(RegistryObject::get).map(block -> (Block) block).toList();
    }
}
