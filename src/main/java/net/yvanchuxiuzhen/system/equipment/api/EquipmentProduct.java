package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** 成品的品阶、说明与炼器原料记录；物品属性和原生附魔分别保存在对应组件中。 */
public record EquipmentProduct(String itemId, String name, String description, String type, String grade,
                               int level, int experience, int weight, Materials materials,
                               int enchantmentCount, List<Enchantment> enchantments) {
    public static final Codec<EquipmentProduct> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item_id").forGetter(EquipmentProduct::itemId),
            Codec.STRING.fieldOf("name").forGetter(EquipmentProduct::name),
            Codec.STRING.fieldOf("description").forGetter(EquipmentProduct::description),
            Codec.STRING.fieldOf("type").forGetter(EquipmentProduct::type),
            Codec.STRING.fieldOf("grade").forGetter(EquipmentProduct::grade),
            Codec.INT.fieldOf("level").forGetter(EquipmentProduct::level),
            Codec.INT.fieldOf("experience").forGetter(EquipmentProduct::experience),
            Codec.INT.fieldOf("weight").forGetter(EquipmentProduct::weight),
            Materials.CODEC.fieldOf("materials").forGetter(EquipmentProduct::materials),
            Codec.INT.fieldOf("enchantment_count").forGetter(EquipmentProduct::enchantmentCount),
            Enchantment.CODEC.listOf().fieldOf("enchantments").forGetter(EquipmentProduct::enchantments)
    ).apply(instance, EquipmentProduct::new));

    public EquipmentProduct {
        enchantments = List.copyOf(enchantments);
    }

    /** 来源先记录第 2 组，再记录第 1、0 组；保留组号和来源注册名。 */
    public record Enchantment(int slot, ResourceLocation id, int level) {
        public static final Codec<Enchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(Enchantment::slot),
                ResourceLocation.CODEC.fieldOf("id").forGetter(Enchantment::id),
                Codec.INT.fieldOf("level").forGetter(Enchantment::level)
        ).apply(instance, Enchantment::new));
    }

    /** 炼器原料量沿用来源的缩放与取整，三项倍率原料量仍为 double。 */
    public record Materials(int physicalAttack, int spellAttack, int soulAttack, int health, int power, int soul,
                            int armor, int physicalDefense, int spellDefense, int soulDefense,
                            double knockbackResistance, double attackSpeed, double moveSpeed) {
        public static final Codec<Materials> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("physical_attack").forGetter(Materials::physicalAttack),
                Codec.INT.fieldOf("spell_attack").forGetter(Materials::spellAttack),
                Codec.INT.fieldOf("soul_attack").forGetter(Materials::soulAttack),
                Codec.INT.fieldOf("health").forGetter(Materials::health),
                Codec.INT.fieldOf("power").forGetter(Materials::power),
                Codec.INT.fieldOf("soul").forGetter(Materials::soul),
                Codec.INT.fieldOf("armor").forGetter(Materials::armor),
                Codec.INT.fieldOf("physical_defense").forGetter(Materials::physicalDefense),
                Codec.INT.fieldOf("spell_defense").forGetter(Materials::spellDefense),
                Codec.INT.fieldOf("soul_defense").forGetter(Materials::soulDefense),
                Codec.DOUBLE.fieldOf("knockback_resistance").forGetter(Materials::knockbackResistance),
                Codec.DOUBLE.fieldOf("attack_speed").forGetter(Materials::attackSpeed),
                Codec.DOUBLE.fieldOf("move_speed").forGetter(Materials::moveSpeed)
        ).apply(instance, Materials::new));
    }
}
