package wallis_ultimate.client;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.HogDaddy;

/**
 * Q-150, placeholder art: about 3 blocks tall, a pig's head with broken tusks, a huge gut, a meat hook for a hand and one
 * rubber boot. Worked out from the pose alone, like the regular zombie's model (no animation library), and the pose
 * follows what he's doing, which he tells the client ({@link HogDaddy#action()}).
 */
public class HogDaddyModel extends HierarchicalModel<HogDaddy> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "hog_daddy"), "main");
    private static final float WALK_PHASE = 0.5F;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HogDaddyModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body");
        head = body.getChild("head");
        rightArm = body.getChild("right_arm");
        leftArm = body.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    /** The texture is laid out to match by the tool that painted it: keep these offsets and sizes in step with it. */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13.0F, -16.0F, -12.0F, 26.0F, 18.0F, 20.0F) // the gut
                .texOffs(0, 38).addBox(-9.0F, -26.0F, -6.0F, 18.0F, 10.0F, 12.0F), // the chest
                PartPose.offset(0.0F, 10.0F, 0.0F));
        body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 60).addBox(-7.0F, -10.0F, -11.0F, 14.0F, 12.0F, 12.0F)
                .texOffs(52, 60).addBox(-4.0F, -5.0F, -16.0F, 8.0F, 6.0F, 5.0F) // the snout
                .texOffs(78, 60).addBox(4.0F, -7.0F, -15.0F, 2.0F, 6.0F, 2.0F) // the tusk that's left
                .texOffs(86, 60).addBox(-6.0F, -3.0F, -15.0F, 2.0F, 2.0F, 2.0F) // the broken one
                .texOffs(94, 60).addBox(7.0F, -12.0F, -7.0F, 3.0F, 5.0F, 1.0F)
                .texOffs(102, 60).addBox(-10.0F, -12.0F, -7.0F, 3.0F, 5.0F, 1.0F),
                PartPose.offset(0.0F, -26.0F, -3.0F));
        body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-5.0F, -2.0F, -3.0F, 6.0F, 20.0F, 6.0F)
                .texOffs(24, 84).addBox(-3.5F, 18.0F, -1.5F, 3.0F, 8.0F, 3.0F) // the hook's shaft
                .texOffs(24, 96).addBox(-3.5F, 26.0F, -6.0F, 3.0F, 2.0F, 7.0F)
                .texOffs(36, 84).addBox(-3.5F, 21.0F, -7.5F, 3.0F, 5.0F, 2.0F), // and its point
                PartPose.offset(-12.0F, -22.0F, 0.0F));
        body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(56, 84).addBox(-1.0F, -2.0F, -3.0F, 6.0F, 22.0F, 6.0F),
                PartPose.offset(12.0F, -22.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(94, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 14.0F, 8.0F)
                .texOffs(80, 84).addBox(-4.5F, 7.0F, -5.0F, 9.0F, 7.0F, 9.0F), // the rubber boot
                PartPose.offset(-6.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(94, 22).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 14.0F, 8.0F),
                PartPose.offset(6.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(HogDaddy boss, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        root().getAllParts().forEach(ModelPart::resetPose);
        float step = Mth.cos(limbSwing * WALK_PHASE) * limbSwingAmount;
        float waddle = Mth.sin(limbSwing * WALK_PHASE) * limbSwingAmount;
        float breath = Mth.sin(age * 0.08F);
        rightLeg.xRot = step * 1.1F;
        leftLeg.xRot = -step * 1.1F;
        body.zRot = waddle * 0.08F;
        body.xRot = 0.12F + breath * 0.012F;
        head.yRot = headYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD - 0.12F;
        rightArm.xRot = -step * 0.7F;
        leftArm.xRot = step * 0.7F;
        rightArm.zRot = 0.1F;
        leftArm.zRot = -0.1F + breath * 0.02F;
        // Dragging the hook behind him until it swings.
        float swing = attackTime > 0.0F ? Mth.sin(attackTime * Mth.PI) : 0.0F;
        rightArm.xRot -= swing * 2.4F;
        body.xRot += swing * 0.25F;
        switch (boss.action()) {
            case WIND_UP -> {
                // Snout down, pawing the ground with the boot.
                body.xRot = 0.5F;
                head.xRot = -0.3F;
                rightLeg.xRot = Mth.sin(age * 1.2F) * 0.6F;
                leftLeg.xRot = -0.1F;
                rightArm.xRot = 0.5F;
                leftArm.xRot = 0.5F;
            }
            case CHARGE -> {
                body.xRot = 0.75F;
                head.xRot = -0.55F;
                rightArm.xRot = 1.0F;
                leftArm.xRot = 1.0F;
                rightLeg.xRot = Mth.cos(age * 1.4F) * 1.1F;
                leftLeg.xRot = -Mth.cos(age * 1.4F) * 1.1F;
            }
            case STUNNED -> {
                // Bent double, swaying, his back up for all to hit.
                float sway = Mth.sin(age * 0.25F);
                body.xRot = 1.05F;
                body.zRot = sway * 0.08F;
                head.xRot = 0.2F;
                head.zRot = sway * 0.3F;
                rightArm.xRot = -0.9F;
                leftArm.xRot = -0.9F;
                rightLeg.xRot = -0.15F;
                leftLeg.xRot = 0.15F;
            }
            case LEAP, FLOP_AIR -> {
                body.xRot = 0.8F;
                rightArm.xRot = -2.6F;
                leftArm.xRot = -2.6F;
                rightArm.zRot = 0.5F;
                leftArm.zRot = -0.5F;
                rightLeg.xRot = 0.7F;
                leftLeg.xRot = 0.7F;
            }
            case FLOP_WIND_UP -> {
                body.xRot = -0.25F;
                rightArm.xRot = -2.2F;
                leftArm.xRot = -2.2F;
                head.xRot = -0.3F;
            }
            default -> {
            }
        }
    }
}
