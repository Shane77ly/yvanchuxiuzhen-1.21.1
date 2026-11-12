package net.yvanchuxiuzhen.system.equipment.client;

import java.util.Map;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.yvanchuxiuzhen.system.equipment.projectile.FabaoProjectile;

/** 五至八号法宝的原生模型几何，保留来源部件层级、姿态、尺寸与纹理坐标。 */
final class FabaoProjectileModels {
    static final Map<FabaoProjectile.Kind, String> MODELS = Map.of(
            FabaoProjectile.Kind.FABAO_005, "fabao005",
            FabaoProjectile.Kind.FABAO_006, "fabao006",
            FabaoProjectile.Kind.FABAO_007, "fabao007",
            FabaoProjectile.Kind.FABAO_008, "fabao008");

    private FabaoProjectileModels() {
    }

    static LayerDefinition layer(String model) {
        return switch (model) {
            case "fabao005" -> fabao005();
            case "fabao006" -> fabao006();
            case "fabao007" -> fabao007();
            case "fabao008" -> fabao008();
            default -> throw new IllegalArgumentException("未知法宝弹体模型：" + model);
        };
    }

    private static LayerDefinition fabao005() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("zong", CubeListBuilder.create().texOffs(8, 0).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                .addBox(-1.0F, -9.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.2F)).texOffs(12, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 16, 8);
    }

    private static LayerDefinition fabao006() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition zong = partdefinition.addOrReplaceChild("zong",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-2.0F, 4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-1.0F, 3.0F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-1.0F, 6.0F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition biao1 = zong.addOrReplaceChild("biao1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));
        biao1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        PartDefinition biao2 = zong.addOrReplaceChild("biao2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 1.5708F));
        biao2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        PartDefinition biao3 = zong.addOrReplaceChild("biao3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -3.1416F));
        biao3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        PartDefinition biao4 = zong.addOrReplaceChild("biao4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
        biao4.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 3).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        return LayerDefinition.create(meshdefinition, 8, 7);
    }

    private static LayerDefinition fabao007() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition zong = partdefinition.addOrReplaceChild("zong",
                CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-0.5F, -8.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        zong.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(1, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 2).addBox(0.0F, -2.0F, -2.5F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));
        zong.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 2).addBox(0.0F, -4.0F, -2.5F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        return LayerDefinition.create(meshdefinition, 10, 11);
    }

    private static LayerDefinition fabao008() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("zong",
                CubeListBuilder.create().texOffs(0, 5).addBox(-4.0F, -15.0F, 0.0F, 4.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(0.0F, -5.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-3.0F, -9.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-3.0F, -11.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-2.5F, -13.0F, -0.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-1.5F, -14.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 8, 14);
    }
}
