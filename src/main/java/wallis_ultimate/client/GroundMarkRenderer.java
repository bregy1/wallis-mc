package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.GroundMark;

/**
 * ENEMY-011: draws the red marks flat on the ground, always fully lit, so they read at night and with particles turned
 * off. A faint shape shows where the attack will land; a stronger one fills it while it's coming, along the line for a
 * charge, outward from the middle for a landing.
 */
public class GroundMarkRenderer extends EntityRenderer<GroundMark> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/ground_mark.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int RED = 255;
    private static final int GREEN = 30;
    private static final int BLUE = 25;
    private static final int FAINT_ALPHA = 110;
    private static final int STRONG_ALPHA = 210;
    private static final int SEGMENTS = 32;
    /** One repeat of the texture per this many blocks. */
    private static final float TILE = 2.0F;

    public GroundMarkRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(GroundMark mark, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        float fill = Mth.clamp((mark.tickCount + partialTick) / Math.max(mark.fillTicks(), 1), 0.0F, 1.0F);
        pose.pushPose();
        if (mark.isLine()) {
            pose.mulPose(Axis.YP.rotationDegrees(-mark.getYRot()));
            line(pose, buffer, mark.width(), mark.length(), 1.0F, FAINT_ALPHA);
            line(pose, buffer, mark.width() * 0.7F, mark.length(), fill, STRONG_ALPHA);
        } else {
            annulus(pose, buffer, 0.0F, mark.length(), FAINT_ALPHA);
            annulus(pose, buffer, 0.0F, mark.length() * fill, STRONG_ALPHA);
        }
        pose.popPose();
        super.render(mark, yaw, partialTick, pose, buffers, light);
    }

    /** Along +z, which the rotation above turns into the direction of the yaw. */
    private static void line(PoseStack pose, VertexConsumer buffer, float width, float length, float fill, int alpha) {
        PoseStack.Pose last = pose.last();
        float half = width / 2.0F;
        float end = length * fill;
        float v = end / TILE;
        quad(last.pose(), last.normal(), buffer, -half, 0.0F, 0.0F, 0.0F, half, 0.0F, 1.0F, 0.0F, half, end, 1.0F, v, -half, end, 0.0F, v, alpha);
    }

    private static void annulus(PoseStack pose, VertexConsumer buffer, float inner, float outer, int alpha) {
        PoseStack.Pose last = pose.last();
        for (int i = 0; i < SEGMENTS; i++) {
            float from = Mth.TWO_PI * i / SEGMENTS;
            float to = Mth.TWO_PI * (i + 1) / SEGMENTS;
            float cosFrom = Mth.cos(from);
            float sinFrom = Mth.sin(from);
            float cosTo = Mth.cos(to);
            float sinTo = Mth.sin(to);
            quad(last.pose(), last.normal(), buffer, cosFrom * inner, sinFrom * inner, 0.0F, 0.0F, cosFrom * outer, sinFrom * outer, 1.0F, 0.0F,
                    cosTo * outer, sinTo * outer, 1.0F, 1.0F, cosTo * inner, sinTo * inner, 0.0F, 1.0F, alpha);
        }
    }

    private static void quad(Matrix4f pose, Matrix3f normal, VertexConsumer buffer, float x1, float z1, float u1, float v1, float x2, float z2, float u2, float v2,
            float x3, float z3, float u3, float v3, float x4, float z4, float u4, float v4, int alpha) {
        vertex(pose, normal, buffer, x1, z1, u1, v1, alpha);
        vertex(pose, normal, buffer, x2, z2, u2, v2, alpha);
        vertex(pose, normal, buffer, x3, z3, u3, v3, alpha);
        vertex(pose, normal, buffer, x4, z4, u4, v4, alpha);
    }

    private static void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer buffer, float x, float z, float u, float v, int alpha) {
        buffer.vertex(pose, x, 0.0F, z).color(RED, GREEN, BLUE, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GroundMark mark) {
        return TEXTURE;
    }
}
