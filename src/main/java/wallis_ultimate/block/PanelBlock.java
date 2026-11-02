package wallis_ultimate.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * WORLD-013: a thin sheet against one side of its block: tarps and wreck windows. FACING is the side it sits on. It
 * needs nothing to hang from, so structures can stretch tarps across open air.
 */
public class PanelBlock extends DirectionalBlock {
    private static final double THICKNESS = 1.0;
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        SHAPES.put(Direction.DOWN, Block.box(0.0, 0.0, 0.0, 16.0, THICKNESS, 16.0));
        SHAPES.put(Direction.UP, Block.box(0.0, 16.0 - THICKNESS, 0.0, 16.0, 16.0, 16.0));
        SHAPES.put(Direction.NORTH, Block.box(0.0, 0.0, 0.0, 16.0, 16.0, THICKNESS));
        SHAPES.put(Direction.SOUTH, Block.box(0.0, 0.0, 16.0 - THICKNESS, 16.0, 16.0, 16.0));
        SHAPES.put(Direction.WEST, Block.box(0.0, 0.0, 0.0, THICKNESS, 16.0, 16.0));
        SHAPES.put(Direction.EAST, Block.box(16.0 - THICKNESS, 0.0, 0.0, 16.0, 16.0, 16.0));
    }

    public PanelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
