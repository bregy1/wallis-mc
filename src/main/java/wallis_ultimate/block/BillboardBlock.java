package wallis_ultimate.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * WORLD-013: one piece of a billboard, three wide and two high. TILE says which: 0 to 2 is the top row from the viewer's
 * left, 3 to 5 the bottom row. Use WreckBuilds.billboard to place all six.
 */
public class BillboardBlock extends FacingPropBlock {
    public static final int COLUMNS = 3;
    public static final int ROWS = 2;
    public static final IntegerProperty TILE = IntegerProperty.create("tile", 0, COLUMNS * ROWS - 1);

    public BillboardBlock(BlockBehaviour.Properties properties) {
        super(properties, 0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
        registerDefaultState(defaultBlockState().setValue(TILE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TILE);
    }
}
