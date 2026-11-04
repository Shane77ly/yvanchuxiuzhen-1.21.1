package net.yvanchuxiuzhen.system.combat.effect;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.xiulian.api.BloodStone;

/**
 * 战斗中的状态效果：流血、逃跑与幻音。僵直（{@code effect_033}）的每刻粒子随 particle 迁移，目前只是标记。
 */
final class BattleEffects {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private BattleEffects() {
    }

    static void register(ContentTables tables) {
        tables.effectBehavior("effect_004", EffectSupport.withStartSound("effect_004yinxiao", new Bleed()));
        tables.effectBehavior("effect_034", new Flee());
        tables.effectBehavior("effect_032", new Illusion());
    }

    private static final List<String> ILLUSION_SOUNDS = List.of("entity.blaze.shoot", "entity.zombified_piglin.angry", "entity.firework_rocket.shoot",
            "entity.zombie.hurt", "entity.enderman.ambient", "entity.zombie.step", "entity.zombie.ambient", "entity.ender_dragon.ambient",
            "entity.zombified_piglin.ambient", "entity.skeleton.shoot", "entity.wither_skeleton.step", "entity.skeleton.step", "entity.wither.shoot",
            "entity.wither.ambient", "entity.creeper.primed", "entity.tnt.primed", "entity.arrow.hit", "entity.arrow.hit_player", "entity.arrow.shoot",
            "entity.squid.squirt");

    /**
     * 流血：亡灵（1.21.1 以实体类型标签 {@code minecraft:undead} 判断）直接移除，受到瞬间治疗时移除。每刻计数，到达周期（20 刻；有见血封喉时 20 − (其等级 + 1) 刻）扣 (等级 + 1) 点生命，
     * 并给脚下的吸血石加同量的充血。血滴、血烟粒子（{@code xvedi}、{@code smokered}）随 particle 迁移。
     */
    private static final class Bleed implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (entity.getType().is(EntityTypeTags.UNDEAD)) {
                StatusEffects.remove(entity, "effect_004");
            }
            if (!EffectSupport.eligible(entity)) {
                return true;
            }
            if (entity.hasEffect(MobEffects.HEAL)) {
                StatusEffects.remove(entity, "effect_004");
            }
            double count = entity.getPersistentData().getDouble("effect004tick") + 1;
            entity.getPersistentData().putDouble("effect004tick", count);
            double period = StatusEffects.has(entity, "duyao_18") ? 20 - (StatusEffects.amplifier(entity, "duyao_18") + 1) : 20;
            if (count >= period) {
                entity.setHealth((float) (entity.getHealth() - (amplifier + 1)));
                entity.getPersistentData().putDouble("effect004tick", 0);
                BloodStone.addBlood(entity.level(), BlockPos.containing(entity.getX(), entity.getY() - 0.5, entity.getZ()), amplifier + 1);
            }
            return true;
        }
    }

    /**
     * 幻音：每 20 刻在身边水平 ±5 格内的随机位置播放 20 种原版敌对声音之一（中性类别，音量与音调均为 1）。
     * 来源的过程在客户端实体上还会再本地播放一次，目标版本只在服务端广播。
     */
    private static final class Illusion implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (EffectSupport.every(entity, "effect032", 20)) {
                String sound = ILLUSION_SOUNDS.get(Mth.nextInt(entity.getRandom(), 1, 20) - 1);
                BlockPos at = BlockPos.containing(entity.getX() + Mth.nextInt(entity.getRandom(), -5, 5), entity.getY(),
                        entity.getZ() + Mth.nextInt(entity.getRandom(), -5, 5));
                entity.level().playSound(null, at, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace(sound)), SoundSource.NEUTRAL, 1, 1);
            }
            return true;
        }
    }

    /** 逃跑：妖兽被连续击打到 8 层（等级 7）时，向视线反方向 8 格处跑开，加速与沉默各 1 秒，然后移除。 */
    private static final class Flee implements EffectBehavior {
        @Override
        public void started(LivingEntity entity, int amplifier) {
            if (amplifier >= 7 && entity.getType().is(YAOSHOU)) {
                if (entity instanceof Mob mob) {
                    Vec3 eye = entity.getEyePosition(1f);
                    BlockPos behind = entity.level().clip(new ClipContext(eye, eye.add(entity.getViewVector(1f).scale(-8)),
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
                    mob.getNavigation().moveTo(behind.getX(), entity.getY(), behind.getZ(), 1);
                }
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20, 1));
                entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_002"), 20, 1));
                StatusEffects.remove(entity, "effect_034");
            }
        }
    }
}
