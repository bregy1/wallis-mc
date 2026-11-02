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
        // WORLD-013: the props that are flat sheets or cut-outs show their texture as the icon.
        blockIcon(ModItems.BARBED_WIRE.getId().getPath(), "barbed_wire");
        blockIcon(ModItems.GREEN_TARP.getId().getPath(), "green_tarp");
        blockIcon(ModItems.BLUE_TARP.getId().getPath(), "blue_tarp");
        blockIcon(ModItems.GREEN_TARP_SLOPE.getId().getPath(), "green_tarp");
        blockIcon(ModItems.BLUE_TARP_SLOPE.getId().getPath(), "blue_tarp");
        blockIcon(ModItems.WRECK_WINDOW.getId().getPath(), "wreck_window");
        blockIcon(ModItems.ANTLERS.getId().getPath(), "antlers");
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
    private void blockIcon(String name, String texture) {
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + texture));
    }

    private void handheld(String name) {
        withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
    }
}
