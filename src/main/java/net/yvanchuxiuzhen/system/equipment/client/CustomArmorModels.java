package net.yvanchuxiuzhen.system.equipment.client;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.CustomArmorItem;

/** 1.18.2 来源 Blockbench 服装护甲模型层注册与按部位裁剪的 HumanoidModel 缓存。 */
final class CustomArmorModels {
    private static final CubeDeformation NONE = CubeDeformation.NONE;
    private static final Map<CustomArmorItem.Layer, ModelLayerLocation> LAYER_LOCATIONS = buildLayerLocations();
    private static final float[][] CLOTHES_08_BRIM_PANELS = {
            {0, 24, -7.0F, 0.0F, 9.0F, -0.3491F},
            {14, 24, 0.0F, 0.0F, 9.0F, 0.3491F},
            {0, 16, -7.0F, 9.0F, 0.0F, -1.2217F},
            {14, 16, 0.0F, 9.0F, 0.0F, -1.9199F},
            {0, 8, -7.0F, -9.0F, 0.0F, 1.9199F},
            {14, 8, 0.0F, -9.0F, 0.0F, 1.2217F},
            {14, 0, 0.0F, 0.0F, -9.0F, -0.3491F},
            {0, 0, -7.0F, 0.0F, -9.0F, 0.3491F}
    };
    private static final float[][] CLOTHES_08_HAT_PETALS = {
            {-1.1345F, 0.3927F, 0.0F},
            {-1.1345F, 0.7854F, 0.0F},
            {-1.1345F, 1.1781F, 0.0F},
            {0.0F, 1.5708F, 1.1345F},
            {2.0071F, -0.7854F, -3.1416F},
            {2.0071F, -0.3927F, -3.1416F},
            {2.0071F, 0.0F, 3.1416F},
            {2.0071F, 0.7854F, -3.1416F},
            {2.0071F, 0.3927F, -3.1416F},
            {2.0071F, 1.1781F, 3.1416F},
            {2.0071F, -1.1781F, 3.1416F},
            {-1.1345F, -1.1781F, 0.0F},
            {0.0F, -1.5708F, -1.1345F},
            {-1.1345F, -0.7854F, 0.0F},
            {-1.1345F, -0.3927F, 0.0F},
            {-1.1345F, 0.0F, 0.0F}
    };

    private static EntityModelSet cachedModelSet;
    private static Map<CustomArmorItem.Layer, Map<EquipmentSlot, HumanoidModel<LivingEntity>>> cachedSlotModels;

    private static final IClientItemExtensions CLIENT_EXTENSIONS = new IClientItemExtensions() {
        @Override
        public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                      EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
            if (!(itemStack.getItem() instanceof CustomArmorItem armor)) {
                return original;
            }
            Map<EquipmentSlot, HumanoidModel<LivingEntity>> bySlot = modelsFor(armor.modelSpec().layer());
            HumanoidModel<LivingEntity> custom = bySlot.get(equipmentSlot);
            return custom != null ? custom : original;
        }
    };

    private CustomArmorModels() {
    }

    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (CustomArmorItem.Layer layer : CustomArmorItem.Layer.values()) {
            event.registerLayerDefinition(LAYER_LOCATIONS.get(layer), () -> createLayer(layer));
        }
    }

    static IClientItemExtensions extensions() {
        return CLIENT_EXTENSIONS;
    }

    private static Map<CustomArmorItem.Layer, ModelLayerLocation> buildLayerLocations() {
        Map<CustomArmorItem.Layer, ModelLayerLocation> map = new EnumMap<>(CustomArmorItem.Layer.class);
        for (CustomArmorItem.Layer layer : CustomArmorItem.Layer.values()) {
            map.put(layer, new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "custom_armor_" + layer.id()), "main"));
        }
        return map;
    }

    private static Map<EquipmentSlot, HumanoidModel<LivingEntity>> modelsFor(CustomArmorItem.Layer layer) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        if (cachedModelSet != modelSet || cachedSlotModels == null) {
            Map<CustomArmorItem.Layer, Map<EquipmentSlot, HumanoidModel<LivingEntity>>> built = new EnumMap<>(CustomArmorItem.Layer.class);
            for (CustomArmorItem.Layer entry : CustomArmorItem.Layer.values()) {
                built.put(entry, buildSlotModels(entry, modelSet.bakeLayer(LAYER_LOCATIONS.get(entry))));
            }
            cachedModelSet = modelSet;
            cachedSlotModels = built;
        }
        return cachedSlotModels.get(layer);
    }

    private static Map<EquipmentSlot, HumanoidModel<LivingEntity>> buildSlotModels(CustomArmorItem.Layer layer, ModelPart root) {
        boolean armored = layer.armoredNames();
        Map<EquipmentSlot, HumanoidModel<LivingEntity>> slots = new EnumMap<>(EquipmentSlot.class);
        slots.put(EquipmentSlot.HEAD, wrapParts(Map.of("head", root.getChild(armored ? "armorHead" : "head"))));
        if (layer == CustomArmorItem.Layer.CLOTHES_0601) {
            return slots;
        }
        slots.put(EquipmentSlot.CHEST, wrapParts(Map.of(
                "body", root.getChild(armored ? "armorBody" : "body"),
                "left_arm", root.getChild(armored ? "armorLeftArm" : "leftarm"),
                "right_arm", root.getChild(armored ? "armorRightArm" : "rightarm")
        )));
        slots.put(EquipmentSlot.LEGS, wrapParts(Map.of(
                "left_leg", root.getChild(armored ? "armorLeftLeg" : "leftleg"),
                "right_leg", root.getChild(armored ? "armorRightLeg" : "rightleg")
        )));
        slots.put(EquipmentSlot.FEET, wrapParts(Map.of(
                "left_leg", root.getChild(armored ? "armorLeftBoot" : "leftboot"),
                "right_leg", root.getChild(armored ? "armorRightBoot" : "rightboot")
        )));
        return slots;
    }

    private static HumanoidModel<LivingEntity> wrapParts(Map<String, ModelPart> active) {
        return new HumanoidModel<>(new ModelPart(List.of(), Map.of(
                "head", active.getOrDefault("head", emptyPart()),
                "hat", emptyPart(),
                "body", active.getOrDefault("body", emptyPart()),
                "right_arm", active.getOrDefault("right_arm", emptyPart()),
                "left_arm", active.getOrDefault("left_arm", emptyPart()),
                "right_leg", active.getOrDefault("right_leg", emptyPart()),
                "left_leg", active.getOrDefault("left_leg", emptyPart())
        )));
    }

    private static ModelPart emptyPart() {
        return new ModelPart(List.of(), Map.of());
    }

    private static LayerDefinition createLayer(CustomArmorItem.Layer layer) {
        return switch (layer) {
            case CLOTHES_0601 -> createClothes0601();
            case CLOTHES_08 -> createClothes08();
            case CLOTHES_09 -> createClothes09();
            case CLOTHES_10 -> createClothes10();
            case CLOTHES_12 -> createClothes12();
            case CLOTHES_16 -> createClothes16();
            case CLOTHES_18 -> createClothes18();
        };
    }

    private static LayerDefinition createClothes0601() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F))
                        .texOffs(24, 0).addBox(-4.0F, -13.5F, 0.0F, 4.0F, 4.0F, 4.0F, NONE)
                        .texOffs(24, 0).mirror().addBox(0.0F, -13.5F, 0.0F, 4.0F, 4.0F, 4.0F, NONE).mirror(false)
                        .texOffs(0, 0).addBox(-4.0F, -9.5F, -4.0F, 8.0F, 1.0F, 8.0F, NONE),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(6.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(6.0F, 1.0F, -1.0F, 3.0F, 3.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(3.0F, 0.0F, -1.0F, 3.0F, 3.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(0.0F, 0.0F, -1.0F, 3.0F, 2.0F, 1.0F, NONE),
                PartPose.offsetAndRotation(4.0F, -8.0F, 4.0F, 0.0F, -0.1745F, 0.1745F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-7.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(-3.0F, 0.0F, -1.0F, 3.0F, 2.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(-6.0F, 0.0F, -1.0F, 3.0F, 3.0F, 1.0F, NONE)
                        .texOffs(32, 0).addBox(-9.0F, 1.0F, -1.0F, 3.0F, 3.0F, 1.0F, NONE),
                PartPose.offsetAndRotation(-4.0F, -8.0F, 4.0F, 0.0F, 0.1745F, -0.1745F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition createClothes08() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.3F))
                        .texOffs(0, 32).addBox(-2.0F, 0.3F, -4.3F, 4.0F, 2.0F, 0.0F, NONE),
                PartPose.ZERO);
        for (int i = 0; i < CLOTHES_08_BRIM_PANELS.length; i++) {
            float[] p = CLOTHES_08_BRIM_PANELS[i];
            head.addOrReplaceChild("cube_r" + (i + 1),
                    CubeListBuilder.create().texOffs((int) p[0], (int) p[1]).addBox(p[2], -4.0F, 0.0F, 7.0F, 8.0F, 0.0F, NONE),
                    PartPose.offsetAndRotation(p[3], -4.0F, p[4], 0.0F, p[5], 0.0F));
        }
        PartDefinition maozi = head.addOrReplaceChild("maozi", CubeListBuilder.create(), PartPose.offset(0.0F, -10.0F, -9.0F));
        for (int i = 0; i < CLOTHES_08_HAT_PETALS.length; i++) {
            float[] r = CLOTHES_08_HAT_PETALS[i];
            maozi.addOrReplaceChild("cube_r" + (i + 9),
                    CubeListBuilder.create().texOffs(28, 0).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 14.0F, 1.0F, NONE),
                    PartPose.offsetAndRotation(0.0F, -2.0F, 9.0F, r[0], r[1], r[2]));
        }
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
                        .texOffs(40, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
                PartPose.ZERO);
        addArmPair(root, false, 24, 48, 0.3F, 0.3F);
        addLegPair(root, "rightleg", "leftleg", 28, 15, 0.4F);
        addLegPair(root, "rightboot", "leftboot", 44, 15, 0.6F);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition createClothes09() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addLegPair(root, "rightboot", "leftboot", 32, 48, 0.55F);
        root.addOrReplaceChild("leftleg", CubeListBuilder.create()
                        .texOffs(0, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)).mirror(false)
                        .texOffs(16, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("rightleg", CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
                        .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create()
                        .texOffs(0, 32).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                        .texOffs(16, 32).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
                        .texOffs(16, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F))
                        .texOffs(24, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
                        .texOffs(42, 32).addBox(-1.0F, 8.3F, -3.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -10.0F, -2.0F, 4.0F, 2.0F, 4.0F, NONE), PartPose.ZERO);
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(12, 6).addBox(0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -12.0F, 0.5F, 0.0F, 0.0F, 1.5708F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                        .texOffs(0, 6).addBox(-1.5F, -4.0F, 0.0F, 3.0F, 4.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -10.0F, -1.5F, -0.3491F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static LayerDefinition createClothes10() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addClothes10Or12TorsoLimbs(root);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 32).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 20.0F, 0.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -5.0F, 4.0F, 0.0873F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -1.0F, 0.0F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, -1.0472F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -2.5F, -2.5F, 8.0F, 6.0F, 6.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -11.0F, 0.5F, -0.9599F, 0.0F, 0.0F));
        head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -3.5F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, -1.0F, -0.1745F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 60, 52);
    }

    private static LayerDefinition createClothes12() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        addClothes10Or12TorsoLimbs(root);
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition maozi = head.addOrReplaceChild("maozi", CubeListBuilder.create()
                        .texOffs(16, 4).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(-0.1F))
                        .texOffs(0, 7).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 1.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.3491F, 0.0F, 0.0F));
        maozi.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.0F, 9.0F, 4.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(4.0F, -1.0F, 0.0F, 0.5236F, -1.5708F, 0.0F));
        maozi.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.0F, 9.0F, 4.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(-4.0F, -1.0F, 0.0F, 0.5236F, 1.5708F, 0.0F));
        maozi.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -1.0F, 9.0F, 4.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, 4.0F, -0.5236F, 0.0F, 0.0F));
        maozi.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.0F, 9.0F, 4.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.5236F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 56, 48);
    }

    private static void addClothes10Or12TorsoLimbs(PartDefinition root) {
        addLegPair(root, "rightboot", "leftboot", 0, 16, 0.55F);
        addLegPair(root, "rightleg", "leftleg", 18, 32, 0.4F);
        addArmPair(root, false, 40, 16, 0.4F, 0.4F);
        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
    }

    private static LayerDefinition createClothes16() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("armorHead", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.2F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.7F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("armorBody", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)), PartPose.ZERO);
        body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(29, 0).addBox(0.0F, 0.0F, 0.5F, 2.0F, 4.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(3.0F, 6.0F, -4.0F, 0.0873F, 0.0F, -0.2618F));
        body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(27, 1).addBox(0.0F, 4.0F, -0.25F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(4.0F, -1.0F, -3.5F, 0.0F, 0.0F, 0.1745F));
        body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, 0.0F, 0.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(4.0F, -1.0F, -3.5F, -0.0436F, 0.0F, 0.1745F));
        body.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                        .texOffs(26, 0).addBox(-0.5F, 0.0F, -2.0F, 2.0F, 3.0F, 3.0F, NONE)
                        .texOffs(24, 0).addBox(1.5F, 0.0F, -2.0F, 4.0F, 3.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(6.0F, -2.0F, 2.0F, 1.5708F, 1.3963F, 1.5708F));
        body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(24, 1).addBox(0.0F, 0.0F, -2.0F, 5.0F, 3.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(3.0F, -3.0F, 5.0F, 0.3491F, 0.5236F, 0.2618F));
        body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(27, 0).addBox(-2.0F, 0.0F, 0.5F, 2.0F, 4.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(-3.0F, 6.0F, -4.0F, 0.0873F, 0.0F, 0.2618F));
        body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(27, 1).addBox(-2.0F, 4.0F, -0.25F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(-4.0F, -1.0F, -3.5F, 0.0F, 0.0F, -0.1745F));
        body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(30, 0).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(-4.0F, -1.0F, -3.5F, -0.0436F, 0.0F, -0.1745F));
        body.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                        .texOffs(26, 0).addBox(-5.5F, 0.0F, -2.0F, 2.0F, 3.0F, 3.0F, NONE)
                        .texOffs(24, 0).addBox(-3.5F, 0.0F, -2.0F, 4.0F, 3.0F, 3.0F, NONE),
                PartPose.offsetAndRotation(-6.0F, -2.0F, 2.0F, 1.5708F, -1.3963F, -1.5708F));
        body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(24, 1).addBox(-5.0F, 0.0F, -2.0F, 5.0F, 3.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(-3.0F, -3.0F, 5.0F, 0.3491F, -0.5236F, -0.2618F));
        body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 4.0F, 2.0F, NONE),
                PartPose.offsetAndRotation(0.0F, -3.0F, 5.0F, 0.2182F, 0.0F, 0.0F));

        root.addOrReplaceChild("armorRightArm", CubeListBuilder.create()
                        .texOffs(24, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F))
                        .texOffs(24, 0).addBox(-3.0F, 5.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.7F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("armorLeftArm", CubeListBuilder.create()
                        .texOffs(24, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                        .texOffs(24, 0).addBox(-1.0F, 5.5F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.7F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        addLegPair(root, "armorRightLeg", "armorLeftLeg", 0, 32, 0.45F);
        addLegPair(root, "armorRightBoot", "armorLeftBoot", 16, 32, 0.6F);
        return LayerDefinition.create(mesh, 40, 48);
    }

    private static LayerDefinition createClothes18() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("armorHead", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
        root.addOrReplaceChild("armorBody", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.51F)), PartPose.ZERO);
        addArmPair(root, true, 24, 16, 0.5F, 0.5F);
        addLegPair(root, "armorRightLeg", "armorLeftLeg", 0, 32, 0.5F);
        addLegPair(root, "armorRightBoot", "armorLeftBoot", 16, 32, 0.6F);
        return LayerDefinition.create(mesh, 40, 48);
    }

    private static void addArmPair(PartDefinition root, boolean armored, int u, int v, float rightDeform, float leftDeform) {
        root.addOrReplaceChild(armored ? "armorRightArm" : "rightarm", CubeListBuilder.create()
                        .texOffs(u, v).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(rightDeform)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild(armored ? "armorLeftArm" : "leftarm", CubeListBuilder.create()
                        .texOffs(u, v).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(leftDeform)).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F));
    }

    private static void addLegPair(PartDefinition root, String rightName, String leftName, int u, int v, float deform) {
        root.addOrReplaceChild(rightName, CubeListBuilder.create()
                        .texOffs(u, v).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(deform)),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild(leftName, CubeListBuilder.create()
                        .texOffs(u, v).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(deform)).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F));
    }
}
