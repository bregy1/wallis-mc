package wallis_ultimate.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.registry.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, WallisUltimate.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.BANDAGE.get());
        basicItem(ModItems.GRANNYS_RABBIT_FOOT.get());
        basicItem(ModItems.JOGGING_HEADBAND.get());
        basicItem(ModItems.WORN_SPURS.get());
        basicItem(ModItems.LUCKY_HORSESHOE.get());
        basicItem(ModItems.SPARE_CLIP.get());
        basicItem(ModItems.BRASS_KNUCKLES.get());
        basicItem(ModItems.THICK_FLANNEL.get());
        withExistingParent(ModItems.ZOMBIE_SPAWN_EGG.getId().getPath(), mcLoc("item/template_spawn_egg"));
    }
}
