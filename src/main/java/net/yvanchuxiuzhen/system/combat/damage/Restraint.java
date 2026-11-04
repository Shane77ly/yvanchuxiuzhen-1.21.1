package net.yvanchuxiuzhen.system.combat.damage;

import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory.Family;

/**
 * 五行相克对攻击量倍率的调整。受击方带实体类型标签 {@code <五行>entity}（金水木火土）时，
 * 被攻击五行所克 +0.3，克攻击五行 −0.3，生攻击五行 +0.2，为攻击五行所生 −0.2；多个标签同时命中时按这个顺序取第一个。
 * 相克：金克木、木克土、土克水、水克火、火克金；相生：金生水、水生木、木生火、火生土、土生金。
 */
final class Restraint {
    private Restraint() {
    }

    /** 每个攻击五行依次对应：被克、克我、生我、我所生。 */
    private static Family[] order(Family attack) {
        return switch (attack) {
            case GOLD -> new Family[] {Family.TREE, Family.FIRE, Family.SOIL, Family.WATER};
            case WATER -> new Family[] {Family.FIRE, Family.SOIL, Family.GOLD, Family.TREE};
            case TREE -> new Family[] {Family.SOIL, Family.GOLD, Family.WATER, Family.FIRE};
            case FIRE -> new Family[] {Family.GOLD, Family.WATER, Family.TREE, Family.SOIL};
            case SOIL -> new Family[] {Family.WATER, Family.TREE, Family.FIRE, Family.GOLD};
        };
    }

    private static final double[] ADJUSTMENT = {0.3, -0.3, 0.2, -0.2};

    static double adjustment(Family attack, Entity target) {
        Family[] families = order(attack);
        for (int i = 0; i < families.length; i++) {
            if (target.getType().is(tag(families[i]))) {
                return ADJUSTMENT[i];
            }
        }
        return 0;
    }

    private static TagKey<EntityType<?>> tag(Family family) {
        String name = family.name().toLowerCase(Locale.ROOT) + "entity";
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }
}
