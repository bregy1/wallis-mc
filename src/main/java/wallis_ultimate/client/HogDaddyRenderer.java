package wallis_ultimate.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.HogDaddy;

/** ENEMY-011: redder and wild-eyed once he's enraged, so everyone can see the fight has changed. */
public class HogDaddyRenderer extends MobRenderer<HogDaddy, HogDaddyModel> {
    private static final ResourceLocation TEXTURE = texture("hog_daddy");
    private static final ResourceLocation ENRAGED = texture("hog_daddy_enraged");

    public HogDaddyRenderer(EntityRendererProvider.Context context) {
        super(context, new HogDaddyModel(context.bakeLayer(HogDaddyModel.LAYER)), 1.2F);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/hog_daddy/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(HogDaddy boss) {
        return boss.phase() == HogDaddy.Phase.ENRAGED ? ENRAGED : TEXTURE;
    }
}
