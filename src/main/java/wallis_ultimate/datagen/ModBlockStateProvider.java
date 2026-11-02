package wallis_ultimate.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper files) {
        super(output, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        cubeWithItem(ModBlocks.REINFORCED_CONCRETE);
        cubeWithItem(ModBlocks.RIVETED_STEEL);
        cubeWithItem(ModBlocks.ALIEN_ALLOY);
    }

    private void cubeWithItem(RegistryObject<Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }
}
