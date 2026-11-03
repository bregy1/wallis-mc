package wallis_ultimate.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModItems;
import wallis_ultimate.story.StoryQuests;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
            CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        // CMBT-003: a trinket fits the Curios slots whose tags list it.
        tag(curiosSlot("charm")).add(ModItems.GRANNYS_RABBIT_FOOT.get());
        tag(curiosSlot("head")).add(ModItems.JOGGING_HEADBAND.get());
        tag(curiosSlot("belt")).add(ModItems.WORN_SPURS.get(), ModItems.SPARE_CLIP.get());
        tag(curiosSlot("necklace")).add(ModItems.LUCKY_HORSESHOE.get());
        tag(curiosSlot("hands")).add(ModItems.BRASS_KNUCKLES.get());
        tag(curiosSlot("body")).add(ModItems.THICK_FLANNEL.get());
        // GEAR-006: copper tools count as tools for vanilla and other mods.
        tag(ItemTags.PICKAXES).add(ModItems.COPPER_PICKAXE.get());
        tag(ItemTags.AXES).add(ModItems.COPPER_AXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.COPPER_SHOVEL.get());
        tag(ItemTags.HOES).add(ModItems.COPPER_HOE.get());
        // Chapter 1's "Hot Meal" quest: anything off a furnace or a campfire.
        tag(StoryQuests.COOKED_FOOD).add(Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_RABBIT,
                Items.COOKED_COD, Items.COOKED_SALMON, Items.BAKED_POTATO, Items.DRIED_KELP);
    }

    private static TagKey<Item> curiosSlot(String slot) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", slot));
    }
}
