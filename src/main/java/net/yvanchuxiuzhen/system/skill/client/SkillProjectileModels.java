package net.yvanchuxiuzhen.system.skill.client;

import java.util.Map;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** 三种冰弹与灵纹弹的原生模型几何，保留来源的层级、纹理坐标与尺寸。 */
final class SkillProjectileModels {
    static final Map<String, String> MODELS = Map.of(
            "projectile_skill_ice_01", "skillice01",
            "projectile_skill_ice_02", "skillice02",
            "projectile_skill_ice_03", "skillice03",
            "projectile_lingwentexiao", "lingwen");

    private SkillProjectileModels() {
    }

    static LayerDefinition layer(String model) {
        return switch (model) {
            case "skillice01" -> skillice01();
            case "skillice02" -> skillice02();
            case "skillice03" -> skillice03();
            case "lingwen" -> lingwen();
            default -> throw new IllegalArgumentException("未知弹体模型：" + model);
        };
    }

    private static LayerDefinition skillice01() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("zong", CubeListBuilder.create().texOffs(8, 7).addBox(-0.5F, -12.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 7).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 12, 13);
    }

    private static LayerDefinition skillice02() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition zong = partdefinition.addOrReplaceChild("zong",
                CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(8, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-1.5F, -12.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 9).addBox(-1.0F, -18.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(8, 10)
                        .addBox(-0.5F, -24.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition bian = zong.addOrReplaceChild("bian", CubeListBuilder.create(), PartPose.offset(0.0F, -18.0F, 0.0F));
        bian.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2618F, 0.0F, 0.0F));
        PartDefinition bian2 = zong.addOrReplaceChild("bian2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        bian2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2618F, 0.0F, 0.0F));
        PartDefinition bian3 = zong.addOrReplaceChild("bian3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        bian3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2618F, 0.0F, 0.0F));
        PartDefinition bian4 = zong.addOrReplaceChild("bian4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -18.0F, 0.0F, 0.0F, 1.5708F, 0.0F));
        bian4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, -0.2618F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 22, 25);
    }

    private static LayerDefinition skillice03() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition zong = partdefinition.addOrReplaceChild("zong", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition xvanzhuan = zong.addOrReplaceChild("xvanzhuan", CubeListBuilder.create().texOffs(28, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));
        PartDefinition yizu1 = xvanzhuan.addOrReplaceChild("yizu1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        yizu1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu1.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        PartDefinition yizu2 = xvanzhuan.addOrReplaceChild("yizu2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu2.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        PartDefinition yizu3 = xvanzhuan.addOrReplaceChild("yizu3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.0944F, 0.0F));
        yizu3.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu3.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        PartDefinition yizu4 = xvanzhuan.addOrReplaceChild("yizu4", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        yizu4.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu4.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        PartDefinition yizu5 = xvanzhuan.addOrReplaceChild("yizu5", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.0944F, 0.0F));
        yizu5.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu5.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        PartDefinition yizu6 = xvanzhuan.addOrReplaceChild("yizu6", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -22.0F, 2.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-2.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(1.0F, -1.5F, -7.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(4, 10).addBox(0.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -12.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(4, 10).addBox(-1.0F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -12.0F, 0.0F, -1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -9.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(0, 6).addBox(-2.0F, -1.5F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -9.0F, 0.0F, -1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(6, 0).addBox(0.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, 0.0F, -7.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(6, 0).addBox(-2.0F, -1.5F, -5.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.0F, -7.0F, 0.0F, -1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(6, 12).addBox(-1.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -18.0F, 0.0F, -1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(3, 9).addBox(-1.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -16.0F, 0.0F, -1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(6, 12).addBox(0.0F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -18.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(3, 9).addBox(0.0F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -16.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, -14.0F, 0.0F, 1.0472F, 0.0F));
        yizu6.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(0, 6).addBox(-1.0F, -0.5F, -8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -1.0F, -14.0F, 0.0F, -1.0472F, 0.0F));
        return LayerDefinition.create(meshdefinition, 44, 22);
    }

    private static LayerDefinition lingwen() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("zong", CubeListBuilder.create().texOffs(-8, 0).addBox(-4.0F, -0.5F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 8);
    }
}
