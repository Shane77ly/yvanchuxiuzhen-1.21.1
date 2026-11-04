package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 灵力伤害 {@code linglidamage} 与神识伤害 {@code jinglidamage} 的伤害类型（数据包定义，绕过护甲）。
 * 来源有来源者时用消息 ID {@code <名>.player}、没有时用 {@code <名>}，两种各是一个类型；
 * 来源语言文件里的死亡消息键缺一个点（{@code death.attacklinglidamage.player}），与消息 ID 对不上，保持来源（Q34）。
 */
public final class CombatDamageTypes {
    private CombatDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 伤害来源：神识类（音波、灵魂）用 {@code jinglidamage}，其余用 {@code linglidamage}；{@code attacker} 为空时无来源者。 */
    public static DamageSource source(Level level, boolean soul, Entity attacker) {
        String name = (soul ? "jinglidamage" : "linglidamage") + (attacker == null ? "" : "_player");
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key(name)), attacker);
    }
}
