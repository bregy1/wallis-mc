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
        handheld(ModItems.COPPER_PICKAXE.getId().getPath());
        handheld(ModItems.COPPER_AXE.getId().getPath());
        handheld(ModItems.COPPER_SHOVEL.getId().getPath());
        handheld(ModItems.COPPER_HOE.getId().getPath());
        handheld(ModItems.NAIL_BAT.getId().getPath());
        handheld(ModItems.HEAVY_SHOVEL.getId().getPath());
        handheld(ModItems.PITCHFORK.getId().getPath());
        handheld(ModItems.FRYING_PAN.getId().getPath());
        basicItem(ModItems.MOONSHINE_MOLOTOV.get());
        basicItem(ModItems.PIPE_BOMB.get());
        handheld(ModItems.SLINGSHOT.getId().getPath());
    }

    /** Held like a tool, not like a flat item. */
    private void handheld(String name) {
        withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
    }
}
