package com.anthonyahellman.odmgear.client.model;

import com.anthonyahellman.odmgear.OdmGearMod;
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
import net.minecraft.world.entity.LivingEntity;

public final class OdmHarnessModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(OdmGearMod.MOD_ID, "odm_harness"), "main");

    public OdmHarnessModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0F), 0.0F);
        PartDefinition body = mesh.getRoot().getChild("body");

        body.addOrReplaceChild("waist_belt",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-5.5F, 9.0F, -2.6F, 11.0F, 2.0F, 5.2F, new CubeDeformation(0.15F)),
                PartPose.ZERO);
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(16, 0)
                        .addBox(-4.5F, 4.0F, 2.0F, 9.0F, 6.0F, 1.8F),
                PartPose.ZERO);

        addHipAssembly(body, "left", -1.0F);
        addHipAssembly(body, "right", 1.0F);

        body.addOrReplaceChild("center_valve",
                CubeListBuilder.create().texOffs(48, 30)
                        .addBox(-1.5F, 7.0F, 3.6F, 3.0F, 3.0F, 2.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addHipAssembly(PartDefinition body, String side, float direction) {
        float hipX = 5.5F * direction;
        float outerX = direction < 0.0F ? -9.5F : 5.5F;
        float canisterX = direction < 0.0F ? -6.8F : 2.2F;
        float bladeX = direction < 0.0F ? -10.5F : 7.5F;

        PartDefinition assembly = body.addOrReplaceChild(side + "_assembly",
                CubeListBuilder.create(), PartPose.ZERO);
        assembly.addOrReplaceChild("gearbox",
                CubeListBuilder.create().texOffs(direction < 0.0F ? 0 : 18, 12)
                        .addBox(outerX, 6.0F, -1.5F, 4.0F, 5.0F, 4.5F),
                PartPose.ZERO);
        assembly.addOrReplaceChild("canister",
                CubeListBuilder.create().texOffs(direction < 0.0F ? 0 : 20, 24)
                        .addBox(canisterX, 0.0F, 2.8F, 4.6F, 8.0F, 4.0F),
                PartPose.ZERO);
        assembly.addOrReplaceChild("blade_housing",
                CubeListBuilder.create().texOffs(direction < 0.0F ? 40 : 52, 0)
                        .addBox(bladeX, 1.0F, -2.0F, 3.0F, 8.0F, 3.0F),
                PartPose.ZERO);
        assembly.addOrReplaceChild("grapple_port",
                CubeListBuilder.create().texOffs(direction < 0.0F ? 52 : 58, 38)
                        .addBox(hipX + 3.5F * direction, 8.0F, -2.5F, 1.0F, 1.5F, 1.5F),
                PartPose.ZERO);
    }
}
