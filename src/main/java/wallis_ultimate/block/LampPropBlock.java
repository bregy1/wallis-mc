package wallis_ultimate.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** WORLD-013: a street lamp or floodlight. Most of them in the world are dead; structures choose which ones still work. */
public class LampPropBlock extends FacingPropBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public LampPropBlock(BlockBehaviour.Properties properties, double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        super(properties, fromX, fromY, fromZ, toX, toY, toZ);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
}
