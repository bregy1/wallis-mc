package wallis_ultimate.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** WORLD-013: a thin rod along one axis: a girder, a lamp post, a neon tube. */
public class PillarPropBlock extends RotatedPillarBlock {
    private final VoxelShape alongX;
    private final VoxelShape alongY;
    private final VoxelShape alongZ;

    /** The radius is in sixteenths of a block, from the rod's center. */
    public PillarPropBlock(BlockBehaviour.Properties properties, double radius) {
        super(properties);
        double from = 8.0 - radius;
        double to = 8.0 + radius;
        alongX = Block.box(0.0, from, from, 16.0, to, to);
        alongY = Block.box(from, 0.0, from, to, 16.0, to);
        alongZ = Block.box(from, from, 0.0, to, to, 16.0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(AXIS);
        return axis == Direction.Axis.X ? alongX : axis == Direction.Axis.Y ? alongY : alongZ;
    }
}
