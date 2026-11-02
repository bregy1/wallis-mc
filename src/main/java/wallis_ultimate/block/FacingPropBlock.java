package wallis_ultimate.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** WORLD-013: a prop that turns to face the player placing it. The shape is given as a box against the block's north side. */
public class FacingPropBlock extends HorizontalDirectionalBlock {
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    public FacingPropBlock(BlockBehaviour.Properties properties, double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        shapes.put(Direction.NORTH, Block.box(fromX, fromY, fromZ, toX, toY, toZ));
        shapes.put(Direction.SOUTH, Block.box(16.0 - toX, fromY, 16.0 - toZ, 16.0 - fromX, toY, 16.0 - fromZ));
        shapes.put(Direction.WEST, Block.box(fromZ, fromY, 16.0 - toX, toZ, toY, 16.0 - fromX));
        shapes.put(Direction.EAST, Block.box(16.0 - toZ, fromY, fromX, 16.0 - fromZ, toY, toX));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state.getValue(FACING), Shapes.block());
    }
}
