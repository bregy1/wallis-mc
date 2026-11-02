package wallis_ultimate.registry;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import wallis_ultimate.WallisUltimate;

/** GEAR-006: tool tiers vanilla lacks. Starting values, tuned with balancing. */
public final class ModTiers {
    /** Blocks that need at least a copper tool to drop anything, like iron ore. */
    public static final TagKey<Block> NEEDS_COPPER_TOOL = BlockTags.create(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "needs_copper_tool"));

    /** Between stone (131 uses, speed 4, attack 1) and iron (250, 6, 2). */
    public static final Tier COPPER = TierSortingRegistry.registerTier(
            new ForgeTier(1, 180, 5.0F, 1.5F, 12, NEEDS_COPPER_TOOL, () -> Ingredient.of(Items.COPPER_INGOT)),
            ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "copper"), List.of(Tiers.STONE), List.of(Tiers.IRON));

    private ModTiers() {
    }
}
