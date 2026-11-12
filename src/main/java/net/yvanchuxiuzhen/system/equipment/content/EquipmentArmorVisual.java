package net.yvanchuxiuzhen.system.equipment.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 动画护甲的来源资源、默认循环和八个部位骨骼，由提取表按材质名称装配。 */
public record EquipmentArmorVisual(String id, String geometry, String animation, String texture, String idle, Bones bones) {
    public static final Codec<EquipmentArmorVisual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EquipmentArmorVisual::id),
            Codec.STRING.fieldOf("geometry").forGetter(EquipmentArmorVisual::geometry),
            Codec.STRING.fieldOf("animation").forGetter(EquipmentArmorVisual::animation),
            Codec.STRING.fieldOf("texture").forGetter(EquipmentArmorVisual::texture),
            Codec.STRING.fieldOf("idle").forGetter(EquipmentArmorVisual::idle),
            Bones.CODEC.fieldOf("bones").forGetter(EquipmentArmorVisual::bones)
    ).apply(instance, EquipmentArmorVisual::new));

    private static final Map<String, EquipmentArmorVisual> MODELS = ContentFiles
            .read("content/equipment/armor_models.json", CODEC).stream()
            .collect(Collectors.toUnmodifiableMap(EquipmentArmorVisual::id, Function.identity()));

    public static EquipmentArmorVisual byId(String id) {
        EquipmentArmorVisual visual = MODELS.get(id);
        if (visual == null) {
            throw new IllegalStateException("缺少动画护甲表现定义：" + id);
        }
        return visual;
    }

    /** 八个部位名称沿用各来源渲染器，包含自定义名称与 armor 前缀名称两组。 */
    public record Bones(String head, String body, String rightArm, String leftArm,
                        String rightLeg, String leftLeg, String rightBoot, String leftBoot) {
        private static final Codec<Bones> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("head").forGetter(Bones::head),
                Codec.STRING.fieldOf("body").forGetter(Bones::body),
                Codec.STRING.fieldOf("right_arm").forGetter(Bones::rightArm),
                Codec.STRING.fieldOf("left_arm").forGetter(Bones::leftArm),
                Codec.STRING.fieldOf("right_leg").forGetter(Bones::rightLeg),
                Codec.STRING.fieldOf("left_leg").forGetter(Bones::leftLeg),
                Codec.STRING.fieldOf("right_boot").forGetter(Bones::rightBoot),
                Codec.STRING.fieldOf("left_boot").forGetter(Bones::leftBoot)
        ).apply(instance, Bones::new));
    }
}
