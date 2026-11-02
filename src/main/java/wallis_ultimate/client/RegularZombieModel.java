package wallis_ultimate.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import wallis_ultimate.WallisUltimate;
import wallis_ultimate.entity.RegularZombie;

/**
 * ENEMY-012: vanilla's body plan, so it stays in Minecraft's style, but hunched and lurching: it drags one leg, tilts
 * its head, reaches with its arms when it has seen you, and throws its whole body into the attack. Skins use the player
 * layout, so left and right limbs can be torn up differently. Worked out from the pose alone, so it costs nothing extra
 * per zombie (PACK-003) and needs no animation library.
 */
public class RegularZombieModel extends HumanoidModel<RegularZombie> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(WallisUltimate.MOD_ID, "zombie"), "main");
    private static final float HUNCH = 0.38F;
    /** Rotating the body at the neck swings the hips back; everything moves forward to stay centered over the feet. */
    private static final float HIPS_BACK = 12.0F * Mth.sin(HUNCH);
    private static final float BODY_FORWARD = HIPS_BACK / 2.0F;
    private static final float WALK_PHASE = 0.6662F;
    private static final float DRAGGED_LEG = 0.45F;
    private static final float REACH = -1.65F;

    public RegularZombieModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(RegularZombie zombie, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
        super.setupAnim(zombie, limbSwing, limbSwingAmount, age, headYaw, headPitch);
        int look = zombie.look();
        boolean deadArm = look % 2 == 0;
        // Which side it leans to and drags stays the same for each zombie.
        float side = look % 3 == 0 ? -1.0F : 1.0F;
        float walking = Math.min(limbSwingAmount, 1.0F);
        float step = Mth.sin(limbSwing * WALK_PHASE);
        float lunge = attackTime > 0.0F ? Mth.sin(attackTime * Mth.PI) : 0.0F;
        float breath = Mth.sin(age * 0.06F);

        body.xRot = HUNCH + lunge * 0.5F;
        body.yRot = step * 0.18F * walking + lunge * 0.25F * side;
        body.zRot = side * 0.07F + step * 0.13F * walking;
        body.y = 12.0F - 12.0F * Mth.cos(HUNCH);
        body.z = -BODY_FORWARD - lunge * 2.0F;

        head.y = body.y;
        head.z = body.z - 0.5F;
        head.xRot = headPitch * Mth.DEG_TO_RAD - HUNCH * 0.6F + breath * 0.04F;
        head.zRot = side * 0.22F + body.zRot + Mth.sin(age * 0.11F) * 0.03F;
        hat.copyFrom(head);

        float legLift = Mth.cos(limbSwing * WALK_PHASE) * 1.3F * limbSwingAmount;
        (side > 0.0F ? rightLeg : leftLeg).xRot = legLift * DRAGGED_LEG + 0.1F * walking;
        (side > 0.0F ? leftLeg : rightLeg).xRot = -legLift;
        rightLeg.xRot -= lunge * 0.55F;
        leftLeg.xRot += lunge * 0.35F;
        rightLeg.z = HIPS_BACK - BODY_FORWARD;
        leftLeg.z = HIPS_BACK - BODY_FORWARD;

        float shoulderY = body.y + 2.0F * Mth.cos(HUNCH);
        float shoulderZ = body.z + 2.0F * Mth.sin(HUNCH) - lunge * 1.0F;
        rightArm.y = shoulderY + 0.6F;
        leftArm.y = shoulderY + 0.6F;
        rightArm.z = shoulderZ;
        leftArm.z = shoulderZ;
        rightArm.yRot = 0.0F;
        leftArm.yRot = 0.0F;
        boolean reaching = zombie.isAggressive() || lunge > 0.0F;
        float dangle = -HUNCH * 0.8F - step * 0.3F * walking;
        rightArm.xRot = reaching ? REACH + Mth.cos(age * 0.31F) * 0.1F - lunge * 0.5F : dangle;
        leftArm.xRot = reaching && !deadArm ? REACH * 0.85F + Mth.cos(age * 0.23F) * 0.14F - lunge * 0.5F : -HUNCH * 0.8F + step * 0.3F * walking;
        rightArm.zRot = 0.08F + breath * 0.03F;
        leftArm.zRot = -0.08F - breath * 0.03F;
    }
}
