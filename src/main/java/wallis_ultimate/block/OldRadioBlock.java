package wallis_ultimate.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import wallis_ultimate.story.StoryEvents;

/** STORY-002, Chapter 1's "Radio voices": the taped-together radio in the cabin. Right-click to listen. Placeholder art. */
public class OldRadioBlock extends FacingPropBlock {
    public OldRadioBlock(BlockBehaviour.Properties properties) {
        super(properties, 3.0, 0.0, 5.0, 13.0, 8.0, 11.0);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            StoryEvents.listenToRadio(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
