package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import wallis_ultimate.entity.MudRing;

/** ENEMY-011: a low wall of mud racing outward, with a red edge on the ground so it reads as dangerous. */
public class MudRingRenderer extends EntityRenderer<MudRing> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/ground_mark.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int SEGMENTS = 48;
    private static final float WAVE_HEIGHT = 0.5F;

    public MudRingRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(MudRing ring, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        float radius = (ring.tickCount + partialTick) * MudRing.GROWTH_PER_TICK;
        float inner = Math.max(radius - MudRing.HALF_WIDTH, 0.0F);
        float outer = radius + MudRing.HALF_WIDTH;
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        Matrix3f normal = last.normal();
        for (int i = 0; i < SEGMENTS; i++) {
            float from = Mth.TWO_PI * i / SEGMENTS;
            float to = Mth.TWO_PI * (i + 1) / SEGMENTS;
            float cosFrom = Mth.cos(from);
            float sinFrom = Mth.sin(from);
            float cosTo = Mth.cos(to);
            float sinTo = Mth.sin(to);
            // On the ground: red.
            vertex(matrix, normal, buffer, cosFrom * inner, 0.05F, sinFrom * inner, 0.0F, 0.0F, 240, 40, 30, 190);
            vertex(matrix, normal, buffer, cosFrom * outer, 0.05F, sinFrom * outer, 1.0F, 0.0F, 240, 40, 30, 190);
            vertex(matrix, normal, buffer, cosTo * outer, 0.05F, sinTo * outer, 1.0F, 1.0F, 240, 40, 30, 190);
            vertex(matrix, normal, buffer, cosTo * inner, 0.05F, sinTo * inner, 0.0F, 1.0F, 240, 40, 30, 190);
            // Standing on it: the wave of mud.
            vertex(matrix, normal, buffer, cosFrom * radius, 0.0F, sinFrom * radius, 0.0F, 0.0F, 95, 62, 38, 235);
            vertex(matrix, normal, buffer, cosFrom * radius, WAVE_HEIGHT, sinFrom * radius, 1.0F, 0.0F, 95, 62, 38, 235);
            vertex(matrix, normal, buffer, cosTo * radius, WAVE_HEIGHT, sinTo * radius, 1.0F, 1.0F, 95, 62, 38, 235);
            vertex(matrix, normal, buffer, cosTo * radius, 0.0F, sinTo * radius, 0.0F, 1.0F, 95, 62, 38, 235);
        }
        super.render(ring, yaw, partialTick, pose, buffers, light);
    }

    private static void vertex(Matrix4f pose, Matrix3f normal, VertexConsumer buffer, float x, float y, float z, float u, float v, int red, int green, int blue, int alpha) {
        buffer.vertex(pose, x, y, z).color(red, green, blue, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MudRing ring) {
        return TEXTURE;
    }
}
