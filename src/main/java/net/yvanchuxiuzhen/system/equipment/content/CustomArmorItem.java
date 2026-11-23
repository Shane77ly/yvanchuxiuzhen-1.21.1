package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentArmorItem;

/** 使用原版 Blockbench 人形层定义与实体贴图的自定义服装护甲；仅对指定服装系列覆盖模型与贴图路径。 */
public final class CustomArmorItem extends ContentArmorItem {
    private final ModelSpec modelSpec;

    public CustomArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties,
                           ModelSpec modelSpec, List<Component> tooltip) {
        super(material, type, properties, tooltip);
        this.modelSpec = modelSpec;
    }

    public ModelSpec modelSpec() {
        return modelSpec;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer, boolean innerModel) {
        return modelSpec.texture();
    }

    public static ModelSpec findSpec(String itemId, String materialId) {
        if ("clothes_06_helmet".equals(itemId)) {
            return ModelSpec.CLOTHES_06;
        }
        return BY_MATERIAL.get(materialId);
    }

    /** 1.18.2 来源中存在独立的 Blockbench 护甲模型定义。 */
    public enum Layer {
        CLOTHES_0601("clothes_0601", false),
        CLOTHES_08("clothes_08", false),
        CLOTHES_09("clothes_09", false),
        CLOTHES_10("clothes_10", false),
        CLOTHES_12("clothes_12", false),
        CLOTHES_16("clothes_16", true),
        CLOTHES_18("clothes_18", true);

        private final String id;
        private final boolean armoredNames;

        Layer(String id, boolean armoredNames) {
            this.id = id;
            this.armoredNames = armoredNames;
        }

        public String id() {
            return id;
        }

        public boolean armoredNames() {
            return armoredNames;
        }
    }

    /** 各自定义服装对应的人形护甲模型层与单张实体贴图。 */
    public enum ModelSpec {
        CLOTHES_06(Layer.CLOTHES_0601, "textures/models/armor/_clothes06_layer_1.png"),
        CLOTHES_08(Layer.CLOTHES_08, "textures/entities/clothes08.png"),
        CLOTHES_09(Layer.CLOTHES_09, "textures/entities/clothes09.png"),
        CLOTHES_10(Layer.CLOTHES_10, "textures/entities/clothes10.png"),
        CLOTHES_11(Layer.CLOTHES_10, "textures/entities/clothes11.png"),
        CLOTHES_12(Layer.CLOTHES_12, "textures/entities/clothes12.png"),
        CLOTHES_18(Layer.CLOTHES_16, "textures/entities/clothes18.png"),
        CLOTHES_20(Layer.CLOTHES_18, "textures/entities/clothes20.png");

        private final Layer layer;
        private final ResourceLocation texture;

        ModelSpec(Layer layer, String texturePath) {
            this.layer = layer;
            this.texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, texturePath);
        }

        public Layer layer() {
            return layer;
        }

        public ResourceLocation texture() {
            return texture;
        }
    }

    private static final Map<String, ModelSpec> BY_MATERIAL = Map.of(
            "clothes_08", ModelSpec.CLOTHES_08,
            "clothes_09", ModelSpec.CLOTHES_09,
            "clothes_10", ModelSpec.CLOTHES_10,
            "clothes_11", ModelSpec.CLOTHES_11,
            "clothes_12", ModelSpec.CLOTHES_12,
            "clothes_18", ModelSpec.CLOTHES_18,
            "clothes_20", ModelSpec.CLOTHES_20
    );
}
