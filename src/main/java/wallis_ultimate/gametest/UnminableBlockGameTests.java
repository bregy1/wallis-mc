package wallis_ultimate.gametest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModBlocks;
import wallis_ultimate.registry.ModItems;

/** Checks for WORLD-006: unminable blocks. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UnminableBlockGameTests {
    private static final String PLATFORM = "platform";

    private UnminableBlockGameTests() {
    }

    private static List<Block> blocks() {
        return List.of(ModBlocks.REINFORCED_CONCRETE.get(), ModBlocks.RIVETED_STEEL.get(), ModBlocks.ALIEN_ALLOY.get());
    }

    @GameTest(template = PLATFORM)
    public static void cannotBeMinedOrLooted(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        for (Block block : blocks()) {
            helper.setBlock(pos, block);
            BlockState state = helper.getBlockState(pos);
            String name = block.getName().getString();
            if (state.getDestroySpeed(helper.getLevel(), helper.absolutePos(pos)) >= 0) {
                helper.fail(name + " can be mined");
            }
            if (block.getExplosionResistance() < 3_600_000.0F) {
                helper.fail(name + " explosion resistance too low");
            }
            if (!block.getLootTable().equals(BuiltInLootTables.EMPTY)) {
                helper.fail(name + " has a loot table");
            }
            if (!state.is(BlockTags.WITHER_IMMUNE) || !state.is(BlockTags.DRAGON_IMMUNE)) {
                helper.fail(name + " is not wither/dragon immune");
            }
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "explosions")
    public static void survivesTnt(GameTestHelper helper) {
        List<Block> blocks = blocks();
        for (int i = 0; i < blocks.size(); i++) {
            helper.setBlock(new BlockPos(2 + i, 1, 3), blocks.get(i));
        }
        BlockPos blast = helper.absolutePos(new BlockPos(3, 2, 3));
        helper.getLevel().explode(null, blast.getX() + 0.5, blast.getY() + 0.5, blast.getZ() + 0.5, 4.0F, Level.ExplosionInteraction.TNT);
        for (int i = 0; i < blocks.size(); i++) {
            helper.assertBlockPresent(blocks.get(i), new BlockPos(2 + i, 1, 3));
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM)
    public static void noRecipeMakesThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<Item> items = List.of(ModItems.REINFORCED_CONCRETE.get(), ModItems.RIVETED_STEEL.get(), ModItems.ALIEN_ALLOY.get());
        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            if (items.contains(recipe.getResultItem(level.registryAccess()).getItem())) {
                helper.fail("recipe " + recipe.getId() + " makes an unminable block");
            }
        }
        helper.succeed();
    }
}
