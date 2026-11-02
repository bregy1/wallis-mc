package wallis_ultimate.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;

/** ENEMY-012, Q-151: varied rural victims. Which one a zombie is follows from its UUID (RegularZombie.look). */
public class RegularZombieRenderer extends HumanoidMobRenderer<RegularZombie, RegularZombieModel> {
    private static final ResourceLocation[] LOOKS = {texture("farmer"), texture("hunter"), texture("flannel"), texture("worker"), texture("nightgown")};

    public RegularZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new RegularZombieModel(context.bakeLayer(RegularZombieModel.LAYER)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), context.getModelManager()));
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/zombie/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(RegularZombie zombie) {
        return LOOKS[zombie.look()];
    }
}
