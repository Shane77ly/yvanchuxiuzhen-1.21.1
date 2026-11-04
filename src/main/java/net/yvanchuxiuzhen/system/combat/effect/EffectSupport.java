package net.yvanchuxiuzhen.system.combat.effect;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;

/** 状态效果行为共用的读取与表现操作。 */
final class EffectSupport {
    private EffectSupport() {
    }

    /** 在实体身体中部播放本 Mod 的声音事件（来源 {@code playSound(null, 位置, 事件, 类别, 1, 1)}）。 */
    static void playAtBody(LivingEntity entity, String sound, SoundSource source) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, sound));
        entity.level().playSound(null, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), event, source, 1, 1);
    }

    /** 持续伤害类效果作用的对象：存活，玩家不在创造和旁观模式，或其他生物（来源的 {@code keyi}）。 */
    static boolean eligible(LivingEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof ServerPlayer player) {
            GameType mode = player.gameMode.getGameModeForPlayer();
            return mode != GameType.CREATIVE && mode != GameType.SPECTATOR;
        }
        return entity instanceof Mob;
    }

    /** 每次调用计数加 1，到达阈值后清零并返回真（来源把计数存在实体持久化数据中）。 */
    static boolean every(LivingEntity entity, String key, int ticks) {
        double count = entity.getPersistentData().getDouble(key) + 1;
        if (count >= ticks) {
            entity.getPersistentData().putDouble(key, 0);
            return true;
        }
        entity.getPersistentData().putDouble(key, count);
        return false;
    }

    /** 效果开始时播放声音（来源在开始过程中只在服务端播放），其余行为交给内层。 */
    static EffectBehavior withStartSound(String sound, EffectBehavior inner) {
        return new EffectBehavior() {
            @Override
            public void started(LivingEntity entity, int amplifier) {
                playAtBody(entity, sound, SoundSource.HOSTILE);
                inner.started(entity, amplifier);
            }

            @Override
            public boolean tick(LivingEntity entity, int amplifier) {
                return inner.tick(entity, amplifier);
            }

            @Override
            public void clientTick(LivingEntity entity, int amplifier) {
                inner.clientTick(entity, amplifier);
            }
        };
    }
}
