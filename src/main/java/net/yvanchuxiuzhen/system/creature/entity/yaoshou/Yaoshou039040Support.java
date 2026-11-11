package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import com.mojang.util.UndashedUuid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

/** 039与040共用的来源攻击者UUID解析。 */
final class Yaoshou039040Support {
    private Yaoshou039040Support() { }

    static Entity target(LevelAccessor world, String uuid) {
        if (!(world instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UndashedUuid.fromStringLenient(uuid));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
