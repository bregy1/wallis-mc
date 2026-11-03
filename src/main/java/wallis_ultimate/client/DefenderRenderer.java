package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.Defender;

/** SURV-011: the four kinds of people holding safe spots, chosen by the defender's UUID, and the parachute they land with. */
public class DefenderRenderer extends HumanoidMobRenderer<Defender, DefenderModel> {
    public static final ModelLayerLocation PARACHUTE_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "defender"), "parachute");
    private static final ResourceLocation PARACHUTE_TEXTURE = ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/parachute.png");
    private static final ResourceLocation[] LOOKS = {texture("trucker"), texture("militia"), texture("techie"), texture("ranger")};

    public DefenderRenderer(EntityRendererProvider.Context context) {
        super(context, new DefenderModel(context.bakeLayer(DefenderModel.LAYER)), 0.5F);
        addLayer(new ParachuteLayer(this, context.bakeLayer(PARACHUTE_LAYER)));
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/defender/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(Defender defender) {
        return LOOKS[defender.look()];
    }

    /** A wide canopy above the head and four cords down to the shoulders. Model space: y grows downward. */
    public static LayerDefinition createParachuteLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("canopy", CubeListBuilder.create().texOffs(0, 0).addBox(-20.0F, -42.0F, -20.0F, 40.0F, 3.0F, 40.0F)
                .texOffs(0, 44).addBox(-12.0F, -45.0F, -12.0F, 24.0F, 3.0F, 24.0F), PartPose.ZERO);
        float[][] cords = {{16.0F, 16.0F}, {-16.0F, 16.0F}, {16.0F, -16.0F}, {-16.0F, -16.0F}};
        for (int i = 0; i < cords.length; i++) {
            float x = cords[i][0];
            float z = cords[i][1];
            root.addOrReplaceChild("cord" + i, CubeListBuilder.create().texOffs(0, 72).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 44.0F, 0.5F),
                    PartPose.offsetAndRotation(x, -39.0F, z, z > 0 ? -0.33F : 0.33F, 0.0F, x > 0 ? 0.33F : -0.33F));
        }
        return LayerDefinition.create(mesh, 160, 80);
    }

    private static final class ParachuteLayer extends RenderLayer<Defender, DefenderModel> {
        private final ModelPart parachute;

        ParachuteLayer(LivingEntityRenderer<Defender, DefenderModel> renderer, ModelPart parachute) {
            super(renderer);
            this.parachute = parachute;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, Defender defender, float limbSwing, float limbSwingAmount,
                float partialTick, float age, float headYaw, float headPitch) {
            if (defender.isParachuting()) {
                parachute.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(PARACHUTE_TEXTURE)), light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
