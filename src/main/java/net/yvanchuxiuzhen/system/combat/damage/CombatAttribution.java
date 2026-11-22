package net.yvanchuxiuzhen.system.combat.damage;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.yvanchuxiuzhen.system.combat.api.CombatAttributable;

/**
 * 战斗死亡结算的统一攻击者归因：先读伤害来源的致因与直接实体，再沿投射物主人、驯服主人和技能实体施放者追溯到玩家。
 * 只有最终解析为服务端玩家时才进入玩家击杀收益；普通生物、无主技能实体和无来源伤害不会冒领玩家收益。
 */
public final class CombatAttribution {
    private CombatAttribution() {
    }

    /** 从原版或模组伤害来源解析实际玩家攻击者。 */
    public static ServerPlayer player(DamageSource source) {
        Set<Entity> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        ServerPlayer player = resolve(source.getEntity(), visited);
        return player != null ? player : resolve(source.getDirectEntity(), visited);
    }

    private static ServerPlayer resolve(Entity entity, Set<Entity> visited) {
        if (entity == null || !visited.add(entity)) {
            return null;
        }
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof CombatAttributable attributable) {
            ServerPlayer player = resolve(attributable.combatAttacker(), visited);
            if (player != null) {
                return player;
            }
        }
        if (entity instanceof Projectile projectile) {
            ServerPlayer player = resolve(projectile.getOwner(), visited);
            if (player != null) {
                return player;
            }
        }
        if (entity instanceof TamableAnimal tamable) {
            return resolve(tamable.getOwner(), visited);
        }
        return null;
    }
}
