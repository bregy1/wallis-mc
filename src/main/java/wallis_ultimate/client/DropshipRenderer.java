package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.entity.Dropship;

/** SURV-011: three times the size of its model, so it looks like the troop carrier it is from the ground. */
public class DropshipRenderer extends EntityRenderer<Dropship> {
    private static final float SCALE = 3.0F;

    private final DropshipModel model;

    public DropshipRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new DropshipModel(context.bakeLayer(DropshipModel.LAYER));
    }

    @Override
    public void render(Dropship dropship, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        pose.scale(-SCALE, -SCALE, SCALE);
        model.setupAnim(dropship, 0.0F, 0.0F, dropship.tickCount + partialTick, 0.0F, 0.0F);
        model.renderToBuffer(pose, buffers.getBuffer(model.renderType(getTextureLocation(dropship))), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
        super.render(dropship, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Dropship dropship) {
        return DropshipModel.texture();
    }
}
