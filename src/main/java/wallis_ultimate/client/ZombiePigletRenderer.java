package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.ZombiePiglet;

/** ENEMY-011, placeholder art: vanilla's pig model, shrunk, with a rotten skin. */
public class ZombiePigletRenderer extends MobRenderer<ZombiePiglet, PigModel<ZombiePiglet>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/zombie_piglet.png");
    private static final float SCALE = 0.65F;

    public ZombiePigletRenderer(EntityRendererProvider.Context context) {
        super(context, new PigModel<>(context.bakeLayer(ModelLayers.PIG)), 0.35F);
    }

    @Override
    protected void scale(ZombiePiglet piglet, PoseStack pose, float partialTick) {
        pose.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(ZombiePiglet piglet) {
        return TEXTURE;
    }
}
