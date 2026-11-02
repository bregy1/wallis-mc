package wallis_ultimate.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import wallis_ultimate.survival.SafeZones;
import wallis_ultimate.survival.Travel;
import wallis_ultimate.worldgen.SafeSpot;

/** WORLD-004: a hand-painted signpost in every safe spot. Right-click it to pick where to go. Only structures have them. */
public class TravelSignpostBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);

    public TravelSignpostBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            SafeSpot here = SafeZones.spotAt(level, pos.getX(), pos.getZ());
            if (here != null) {
                Travel.openMenu(serverPlayer, here);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
