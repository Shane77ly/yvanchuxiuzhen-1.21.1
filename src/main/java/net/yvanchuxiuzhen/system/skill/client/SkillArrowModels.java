package net.yvanchuxiuzhen.system.skill.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** 十种技能箭的原生模型层，保留 1.18.2 版本 ModelSkillArrow0101–1001 的几何、UV 与部件姿态。 */
final class SkillArrowModels {
    private SkillArrowModels() {
    }

    static LayerDefinition layer(int kind) {
        return switch (kind) {
            case 1 -> arrow01();
            case 2 -> arrow02();
            case 3 -> arrow03();
            case 4 -> arrow04();
            case 5 -> arrow05();
            case 6 -> arrow06();
            case 7 -> arrow07();
            case 8 -> arrow08();
            case 9 -> arrow09();
            case 10 -> arrow10();
            default -> throw new IllegalArgumentException("技能箭类型：" + kind);
        };
    }

    private static LayerDefinition arrow01() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, -5.0F, -1.5F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(12, 3).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-0.5F, -15.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 4).addBox(-0.5F, -16.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow02() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(6, 1).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(4, 0)
                        .addBox(-1.5F, -13.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 1).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(8, 2)
                        .addBox(-0.5F, -16.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.2F, -16.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(0.2F, -16.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.2F, -16.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.2F, -16.0F, -4.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow03() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(12, 3).addBox(-0.5F, -13.2F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 5)
                .addBox(-1.0F, -17.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(6, 5).addBox(-0.5F, -19.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -3.5F, -2.5F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, -0.1745F, 0.0F));
        PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 4).addBox(0.0F, -3.5F, -2.5F, 0.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.5F, 0.0F, 0.0F, 0.1745F, 0.0F));
        PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, -14.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow04() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(8, 4).addBox(-1.0F, -11.5F, -1.0F, 2.0F, 10.0F, 2.0F, new CubeDeformation(-0.5F)).texOffs(0, 0).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)).texOffs(0, 8)
                .addBox(-0.5F, -15.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 10).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow05() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, -6)
                .addBox(0.0F, -15.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 12).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, -6).addBox(0.0F, -15.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        PartDefinition bian1 = bone.addOrReplaceChild("bian1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
        PartDefinition cube_r2 = bian1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r3 = bian1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r4 = bian1.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition cube_r5 = bian1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6109F, 0.0F, 0.0F));
        PartDefinition bian2 = bone.addOrReplaceChild("bian2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, -2.3562F, 0.0F));
        PartDefinition cube_r6 = bian2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r7 = bian2.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r8 = bian2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition cube_r9 = bian2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6109F, 0.0F, 0.0F));
        PartDefinition bian3 = bone.addOrReplaceChild("bian3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 2.3562F, 0.0F));
        PartDefinition cube_r10 = bian3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r11 = bian3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r12 = bian3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition cube_r13 = bian3.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6109F, 0.0F, 0.0F));
        PartDefinition bian4 = bone.addOrReplaceChild("bian4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -16.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        PartDefinition cube_r14 = bian4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r15 = bian4.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 14.0F, 0.0F, 0.7854F, 0.0F, 0.0F));
        PartDefinition cube_r16 = bian4.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));
        PartDefinition cube_r17 = bian4.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.6109F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow06() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(6, 12).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(6, 12)
                        .addBox(-1.0F, -6.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(-0.1F)).texOffs(4, 11).addBox(-1.5F, -16.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(4, -5)
                        .addBox(0.0F, -6.0F, -2.5F, 0.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(1, 6).addBox(-2.5F, -3.0F, -2.5F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, -5).addBox(0.0F, -6.0F, -2.5F, 0.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow07() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(8, 8).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 8).addBox(-1.5F, 10.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(4, 11)
                        .addBox(-0.5F, 1.0F, 1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 11).addBox(-0.5F, 1.0F, -2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0596F, -0.4856F, 0.7007F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow08() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(12, 3).addBox(-0.5F, -12.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 12).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 9)
                        .addBox(-1.0F, -12.2F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(-0.2F)).texOffs(0, 6).addBox(-0.5F, -16.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, -3).addBox(0.0F, -5.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -3).addBox(0.0F, -5.0F, -1.5F, 0.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow09() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(4, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(12, 6).addBox(-0.5F, -10.7F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 8)
                        .addBox(-1.0F, -12.2F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)).texOffs(0, 3).addBox(-1.5F, -13.0F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-1.5F, -14.9F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -15.0F, 0.0F, 0.0F, 0.0F, 0.4363F));
        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -15.0F, 0.0F, 0.0F, 0.0F, -0.4363F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    private static LayerDefinition arrow10() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 6).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(4, 10)
                        .addBox(-0.5F, -16.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, -2).addBox(0.0F, -5.0F, -1.5F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(-3, 13)
                        .addBox(-1.5F, -2.0F, -1.5F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, -2).addBox(0.0F, -5.0F, -1.5F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}
