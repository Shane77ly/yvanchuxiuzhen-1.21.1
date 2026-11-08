package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 器物实例的炼器数值；整数沿用来源 lianqizong 的取整结果，三项倍率作用于原版基础属性。 */
public record EquipmentStats(int health, int power, int soul, int physicalDefense, int spellDefense, int soulDefense,
                             double knockbackResistance, double attackSpeed, double moveSpeed,
                             int physicalAttack, int spellAttack, int soulAttack, int armor) {
    /** 防具生成时没有三项武器攻击数值。 */
    public EquipmentStats(int health, int power, int soul, int physicalDefense, int spellDefense, int soulDefense,
                          double knockbackResistance, double attackSpeed, double moveSpeed) {
        this(health, power, soul, physicalDefense, spellDefense, soulDefense, knockbackResistance, attackSpeed, moveSpeed, 0, 0, 0, 0);
    }
    public static final EquipmentStats EMPTY = new EquipmentStats(0, 0, 0, 0, 0, 0, 0, 0, 0);

    public EquipmentStats addSpellAttack(int amount) {
        return new EquipmentStats(health, power, soul, physicalDefense, spellDefense, soulDefense,
                knockbackResistance, attackSpeed, moveSpeed, physicalAttack, spellAttack + amount, soulAttack, armor);
    }

    public EquipmentStats addSoulAttack(int amount) {
        return new EquipmentStats(health, power, soul, physicalDefense, spellDefense, soulDefense,
                knockbackResistance, attackSpeed, moveSpeed, physicalAttack, spellAttack, soulAttack + amount, armor);
    }

    public static final Codec<EquipmentStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("health").forGetter(EquipmentStats::health),
            Codec.INT.fieldOf("power").forGetter(EquipmentStats::power),
            Codec.INT.fieldOf("soul").forGetter(EquipmentStats::soul),
            Codec.INT.fieldOf("physical_defense").forGetter(EquipmentStats::physicalDefense),
            Codec.INT.fieldOf("spell_defense").forGetter(EquipmentStats::spellDefense),
            Codec.INT.fieldOf("soul_defense").forGetter(EquipmentStats::soulDefense),
            Codec.DOUBLE.fieldOf("knockback_resistance").forGetter(EquipmentStats::knockbackResistance),
            Codec.DOUBLE.fieldOf("attack_speed").forGetter(EquipmentStats::attackSpeed),
            Codec.DOUBLE.fieldOf("move_speed").forGetter(EquipmentStats::moveSpeed),
            Codec.INT.optionalFieldOf("physical_attack", 0).forGetter(EquipmentStats::physicalAttack),
            Codec.INT.optionalFieldOf("spell_attack", 0).forGetter(EquipmentStats::spellAttack),
            Codec.INT.optionalFieldOf("soul_attack", 0).forGetter(EquipmentStats::soulAttack),
            Codec.INT.optionalFieldOf("armor", 0).forGetter(EquipmentStats::armor)
    ).apply(instance, EquipmentStats::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentStats> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
