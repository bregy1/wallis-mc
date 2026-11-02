package wallis_ultimate.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.registry.ModTiers;

/** Checks for GEAR-006: tools for every ore, each tier needs the one before it. */
@GameTestHolder(WallisUltimate.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ToolGameTests {
    private static final String PLATFORM = "platform";

    private ToolGameTests() {
    }

    /** Copper ore takes a stone pickaxe, iron ore a copper one. */
    @GameTest(template = PLATFORM)
    public static void eachOreNeedsThePreviousTiersPickaxe(GameTestHelper helper) {
        BlockState copperOre = Blocks.COPPER_ORE.defaultBlockState();
        BlockState ironOre = Blocks.IRON_ORE.defaultBlockState();
        if (TierSortingRegistry.isCorrectTierForDrops(Tiers.WOOD, copperOre) || !TierSortingRegistry.isCorrectTierForDrops(Tiers.STONE, copperOre)) {
            helper.fail("copper ore should take at least a stone pickaxe");
        }
        if (TierSortingRegistry.isCorrectTierForDrops(Tiers.STONE, ironOre) || !TierSortingRegistry.isCorrectTierForDrops(ModTiers.COPPER, ironOre)
                || !TierSortingRegistry.isCorrectTierForDrops(Tiers.IRON, ironOre)) {
            helper.fail("iron ore should take at least a copper pickaxe");
        }
        if (!ModItems.COPPER_PICKAXE.get().isCorrectToolForDrops(new ItemStack(ModItems.COPPER_PICKAXE.get()), ironOre)) {
            helper.fail("the copper pickaxe can't mine iron ore");
        }
        helper.succeed();
    }

    /** Each tier digs faster and lasts longer than the one before. */
    @GameTest(template = PLATFORM)
    public static void copperSitsBetweenStoneAndIron(GameTestHelper helper) {
        BlockState stone = Blocks.STONE.defaultBlockState();
        float stoneSpeed = speed(Items.STONE_PICKAXE, stone);
        float copperSpeed = speed(ModItems.COPPER_PICKAXE.get(), stone);
        float ironSpeed = speed(Items.IRON_PICKAXE, stone);
        if (!(stoneSpeed < copperSpeed && copperSpeed < ironSpeed)) {
            helper.fail("speeds: stone " + stoneSpeed + ", copper " + copperSpeed + ", iron " + ironSpeed);
        }
        int copperUses = ModItems.COPPER_PICKAXE.get().getMaxDamage(new ItemStack(ModItems.COPPER_PICKAXE.get()));
        if (!(Tiers.STONE.getUses() < copperUses && copperUses < Tiers.IRON.getUses())) {
            helper.fail("copper lasts " + copperUses);
        }
        helper.succeed();
    }

    private static float speed(Item pickaxe, BlockState state) {
        return pickaxe.getDestroySpeed(new ItemStack(pickaxe), state);
    }
}
