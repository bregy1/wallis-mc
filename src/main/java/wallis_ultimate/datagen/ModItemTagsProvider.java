package wallis_ultimate.datagen;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModItems;

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
    }

    private static TagKey<Item> curiosSlot(String slot) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("curios", slot));
    }
}
