package net.yvanchuxiuzhen.system.combat.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 阵法、热油、剧情与火炮弹写入目标的待消费法术攻击记录（替代 {@code ManaAttackDamage}/{@code SoulAttackDamage} 持久键）。 */
public record PendingSpellAttack(
        boolean soul,
        double amount,
        String attackerUuid,
        String damageType,
        double multiplier,
        boolean independent
) {
    public static final PendingSpellAttack EMPTY = new PendingSpellAttack(false, 0, "", "", 0, false);

    public static final Codec<PendingSpellAttack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("soul", false).forGetter(PendingSpellAttack::soul),
            Codec.DOUBLE.optionalFieldOf("amount", 0.0).forGetter(PendingSpellAttack::amount),
            Codec.STRING.optionalFieldOf("attacker_uuid", "").forGetter(PendingSpellAttack::attackerUuid),
            Codec.STRING.optionalFieldOf("damage_type", "").forGetter(PendingSpellAttack::damageType),
            Codec.DOUBLE.optionalFieldOf("multiplier", 0.0).forGetter(PendingSpellAttack::multiplier),
            Codec.BOOL.optionalFieldOf("independent", false).forGetter(PendingSpellAttack::independent)
    ).apply(instance, PendingSpellAttack::new));
}
