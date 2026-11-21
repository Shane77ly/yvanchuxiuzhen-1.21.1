package net.yvanchuxiuzhen.system.creature.entity;

import com.mojang.util.UndashedUuid;
import java.util.Random;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;

/** 小尸王与鬼火的同维度索敌、体表距离及独立随机抽取。 */
final class GuiguaiTargeting {
    private GuiguaiTargeting() { }

    static Entity target(LevelAccessor world, String id) {
        if (!(world instanceof ServerLevel level) || id.isEmpty()) return null;
        try {
            return level.getEntity(UndashedUuid.fromStringLenient(id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static double surfaceDistance(Entity from, Entity to) {
        return from.position().distanceTo(to.position()) - (from.getBbWidth() / 2 + to.getBbWidth() / 2);
    }

    static int random(int min, int max) {
        return min >= max ? min : new Random().nextInt(max - min + 1) + min;
    }
}
