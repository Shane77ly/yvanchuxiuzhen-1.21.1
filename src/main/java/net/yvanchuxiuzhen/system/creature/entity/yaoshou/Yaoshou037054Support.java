package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 本批编号妖兽共用的来源随机、目标查找、动作结束与受击记录。 */
final class Yaoshou037054Support {
    private Yaoshou037054Support() { }

    static int randomInt(int minimum, int maximum) {
        Random random = new Random();
        return minimum >= maximum ? minimum : random.nextInt(maximum - minimum + 1) + minimum;
    }

    static double randomDouble(double minimum, double maximum) {
        Random random = new Random();
        return minimum >= maximum ? minimum : random.nextDouble() * (maximum - minimum) + minimum;
    }

    static Entity target(LevelAccessor world, Entity entity) {
        if (!(world instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UUID.fromString(entity.getPersistentData().getString("gongjizhe")));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    static boolean isKind(Entity entity, String id) {
        return entity instanceof YaoshouEntity && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())
                .equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }

    static void finish(Entity entity) {
        YaoshouShared.finishSkill(entity);
    }

    static void escape(Entity entity) {
        YaoshouShared.escape(entity);
    }

    static void hurt(YaoshouEntity entity, DamageSource source, String animation) {
        Entity attacker = source.getEntity();
        if (!entity.isAlive() || attacker == null || Relations.isFriendly(entity, attacker)) return;
        CompoundTag data = entity.getPersistentData();
        data.putString("linshigongjizhe", attacker.getStringUUID());
        if (data.getBoolean("beidaduan")) {
            entity.setAnimation("empty");
            data.putBoolean("beidaduan", false);
            if (isKind(entity, "yaoshou_041")) entity.removeEffect(MobEffects.SLOW_FALLING);
        }
        if (data.getString("jineng").isEmpty() && !entity.level().isClientSide()) {
            entity.setAnimation(animation);
        }
    }
}
