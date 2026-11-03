package wallis_ultimate.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.Dropship;

/**
 * SURV-011: a tilt-rotor VTOL from before the fall: a long fuselage with a nose and a tail fin, short wings, an engine
 * pod with a spinning rotor at each wing tip, and an open ramp at the back. Placeholder by Claude. The nose points to
 * negative z, like every Minecraft model.
 */
public class DropshipModel extends EntityModel<Dropship> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "dropship"), "main");
    private static final float ROTOR_SPEED = 0.9F;

    private final ModelPart root;
    private final ModelPart leftRotor;
    private final ModelPart rightRotor;

    public DropshipModel(ModelPart root) {
        this.root = root;
        leftRotor = root.getChild("left_rotor");
        rightRotor = root.getChild("right_rotor");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("fuselage", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -8.0F, -22.0F, 18.0F, 14.0F, 44.0F), PartPose.ZERO);
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 60).addBox(-7.0F, -5.0F, -32.0F, 14.0F, 10.0F, 10.0F), PartPose.ZERO);
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(50, 60).addBox(-4.0F, -8.0F, 22.0F, 8.0F, 6.0F, 16.0F), PartPose.ZERO);
        root.addOrReplaceChild("fin", CubeListBuilder.create().texOffs(100, 60).addBox(-1.0F, -20.0F, 28.0F, 2.0F, 12.0F, 10.0F), PartPose.ZERO);
        root.addOrReplaceChild("wings", CubeListBuilder.create().texOffs(0, 84).addBox(-44.0F, -10.0F, -6.0F, 88.0F, 2.0F, 12.0F), PartPose.ZERO);
        root.addOrReplaceChild("left_pod", CubeListBuilder.create().texOffs(130, 0).addBox(-4.0F, -14.0F, -8.0F, 8.0F, 8.0F, 16.0F), PartPose.offset(44.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_pod", CubeListBuilder.create().texOffs(130, 0).addBox(-4.0F, -14.0F, -8.0F, 8.0F, 8.0F, 16.0F), PartPose.offset(-44.0F, 0.0F, 0.0F));
        CubeListBuilder rotor = CubeListBuilder.create().texOffs(0, 100).addBox(-20.0F, -0.5F, -2.0F, 40.0F, 1.0F, 4.0F)
                .texOffs(0, 106).addBox(-2.0F, -0.5F, -20.0F, 4.0F, 1.0F, 40.0F);
        root.addOrReplaceChild("left_rotor", rotor, PartPose.offset(44.0F, -15.0F, 0.0F));
        root.addOrReplaceChild("right_rotor", rotor, PartPose.offset(-44.0F, -15.0F, 0.0F));
        return LayerDefinition.create(mesh, 192, 150);
    }

    @Override
    public void setupAnim(Dropship dropship, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        leftRotor.yRot = age * ROTOR_SPEED;
        rightRotor.yRot = -age * ROTOR_SPEED;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {
        root.render(pose, buffer, light, overlay, red, green, blue, alpha);
    }

    public static ResourceLocation texture() {
        return ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "textures/entity/dropship.png");
    }
}
