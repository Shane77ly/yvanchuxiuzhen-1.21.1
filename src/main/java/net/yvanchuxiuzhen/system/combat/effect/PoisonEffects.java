package net.yvanchuxiuzhen.system.combat.effect;

import java.util.List;
import java.util.function.IntUnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 毒药类状态效果（{@code duyao_01}–{@code duyao_20}）。共同结构：只作用于存活的生物和不在创造、旁观模式的玩家；
 * 到达周期后玩家按体质 {@code PoisonLoseEfficacyPercentage} 的概率免疫本周期（只有周期型的伤害类毒药检查），
 * 伤害按体质 {@code PoisonEffectPercentage} 放大，再直接扣减当前生命（不经伤害结算）。
 * 周期计数、累积层数存在实体持久化数据（玩家的累积层数存在体质物品，即 xiulian 的体质键）。
 * {@code duyao_21} 经伤害命令结算，随伤害结算迁移；{@code duyao_16}、{@code duyao_18} 是标记，等级由伤害结算读取。
 */
final class PoisonEffects {
    private PoisonEffects() {
    }

    static void register(ContentTables tables) {
        tables.effectBehavior("duyao_01", new Periodic("duyao01tick", amp -> 100) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                return 4 * (1 + amplifier);
            }
        });
        tables.effectBehavior("duyao_02", new Periodic("Duyao02tick", amp -> 200) {
            @Override
            void apply(LivingEntity entity, int amplifier) {
                if (entity instanceof ServerPlayer player) {
                    double soul = Cultivation.get(player, CultivationValue.SOUL) - scale(entity, 100 * (amplifier + 1));
                    Cultivation.set(player, CultivationValue.SOUL, Math.max(soul, 0));
                }
            }
        });
        tables.effectBehavior("duyao_03", new Instant("duyao_03") {
            @Override
            void apply(LivingEntity entity, int amplifier) {
                double damage = scale(entity, 3 * Math.pow(1 + amplifier, 2));
                if (damage != 0) {
                    hurt(entity, damage);
                }
            }
        });
        tables.effectBehavior("duyao_04", new Periodic("duyao04tick", amp -> 200 - (1 + amp) * 20) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                return 4;
            }
        });
        tables.effectBehavior("duyao_05", new Miasma());
        tables.effectBehavior("duyao_06", new Step());
        tables.effectBehavior("duyao_07", new DayDeath());
        tables.effectBehavior("duyao_08", new Periodic("Duyao08tick", amp -> 20) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                return 2 * (1 + amplifier);
            }
        });
        tables.effectBehavior("duyao_09", new MaxHealthDrain());
        tables.effectBehavior("duyao_10", new Instant("duyao_10") {
            @Override
            void apply(LivingEntity entity, int amplifier) {
                hurt(entity, scale(entity, 4 * (1 + amplifier)));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20));
                entity.addEffect(new MobEffectInstance(EffectSupport.holder("effect_006"), 20, 1));
            }
        });
        tables.effectBehavior("duyao_11", new Periodic("duyao11tick", amp -> 20) {
            @Override
            void apply(LivingEntity entity, int amplifier) {
                hurt(entity, scale(entity, 1 + amplifier));
                if (entity instanceof ServerPlayer player) {
                    double power = Cultivation.get(player, CultivationValue.POWER) - (1 + amplifier) * 10;
                    Cultivation.set(player, CultivationValue.POWER, Math.max(power, 0));
                }
            }
        });
        tables.effectBehavior("duyao_12", new ColdPoison());
        tables.effectBehavior("duyao_13", new Periodic("duyao13tick", amp -> 40) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                // 来源取的是 duyao_01 的等级，不是自己的等级（Q32）。
                return 4 * (1 + EffectSupport.amplifier(entity, "duyao_01"));
            }
        });
        tables.effectBehavior("duyao_14", new Periodic("duyao14tick", amp -> 1200) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                return 20 * (1 + amplifier);
            }
        });
        tables.effectBehavior("duyao_15", new Periodic("duyao15tick", amp -> 100) {
            @Override
            double damage(LivingEntity entity, int amplifier) {
                return entity.getMaxHealth() * 0.01 * (1 + amplifier);
            }
        });
        tables.effectBehavior("duyao_17", new Spore());
        tables.effectBehavior("duyao_19", new EffectBehavior() {
            @Override
            public void started(LivingEntity entity, int amplifier) {
                for (String effect : new String[] {"effect_001", "effect_003", "effect_005", "effect_006"}) {
                    entity.addEffect(new MobEffectInstance(EffectSupport.holder(effect), 1200, 2));
                }
            }
        });
        tables.effectBehavior("duyao_20", new Corpse());
    }

    /** 周期型毒药允许作用的对象：存活，玩家不在创造和旁观模式，或其他生物。 */
    static boolean eligible(LivingEntity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        if (entity instanceof ServerPlayer player) {
            return player.gameMode.getGameModeForPlayer() != GameType.CREATIVE && player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR;
        }
        return entity instanceof Mob;
    }

    /** 伤害按体质 {@code PoisonEffectPercentage} 放大（只对玩家，该项为 0 时不放大）。 */
    static double scale(LivingEntity entity, double damage) {
        if (entity instanceof Player player) {
            double percentage = Cultivation.tizhiDouble(player, "PoisonEffectPercentage");
            if (percentage != 0) {
                return damage * (percentage + 1);
            }
        }
        return damage;
    }

    /** 体质 {@code PoisonLoseEfficacyPercentage} 非零的玩家以该概率免疫本周期。 */
    static boolean resisted(LivingEntity entity) {
        if (entity instanceof Player player) {
            double percentage = Cultivation.tizhiDouble(player, "PoisonLoseEfficacyPercentage");
            return percentage != 0 && Math.random() < percentage;
        }
        return false;
    }

    /** 直接扣减当前生命，不经伤害结算。 */
    static void hurt(LivingEntity entity, double damage) {
        entity.setHealth((float) (entity.getHealth() - damage));
    }

    /** 周期型：计数到周期后结算一次，计数每刻增加，到达周期即清零。 */
    private abstract static class Periodic implements EffectBehavior {
        private final String counter;
        private final IntUnaryOperator period;

        Periodic(String counter, IntUnaryOperator period) {
            this.counter = counter;
            this.period = period;
        }

        double damage(LivingEntity entity, int amplifier) {
            return 0;
        }

        void apply(LivingEntity entity, int amplifier) {
            hurt(entity, scale(entity, damage(entity, amplifier)));
        }

        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity) && EffectSupport.every(entity, counter, period.applyAsInt(amplifier)) && !resisted(entity)) {
                apply(entity, amplifier);
            }
            return true;
        }
    }

    /** 瞬时型：第一刻结算，被体质免疫时保留效果，下一刻重新判定。 */
    private abstract static class Instant implements EffectBehavior {
        private final String effect;

        Instant(String effect) {
            this.effect = effect;
        }

        abstract void apply(LivingEntity entity, int amplifier);

        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity) && !resisted(entity)) {
                apply(entity, amplifier);
                EffectSupport.remove(entity, effect);
            }
            return true;
        }
    }

    /** 瘴气：每 40 刻累积层数（玩家 +0.1，首次 1），伤害为 层数 × (1 + 等级)，并清除一批增益效果。 */
    private static final class Miasma implements EffectBehavior {
        private static final List<Holder<MobEffect>> CLEARED = List.of(MobEffects.MOVEMENT_SPEED, MobEffects.DAMAGE_BOOST,
                MobEffects.HEAL, MobEffects.JUMP, MobEffects.REGENERATION, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING,
                MobEffects.HEALTH_BOOST, MobEffects.ABSORPTION, MobEffects.LUCK);

        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity) && EffectSupport.every(entity, "Duyao05tick", 40) && !resisted(entity)) {
                double level;
                if (entity instanceof Player player) {
                    double stored = Cultivation.tizhiDouble(player, "Duyao05level");
                    level = stored == 0 ? 1 : stored + 0.1;
                    Cultivation.setTizhiDouble((ServerPlayer) player, "Duyao05level", level);
                    hurt(entity, scale(entity, level * (1 + amplifier)));
                } else {
                    double stored = entity.getPersistentData().getDouble("Duyao05level");
                    level = stored == 0 ? 1 : stored + 0.1;
                    entity.getPersistentData().putDouble("Duyao05level", level);
                    hurt(entity, level * (1 + amplifier));
                }
                CLEARED.forEach(entity::removeEffect);
            }
            return true;
        }
    }

    /** 含笑半步颠：每 5 刻检查，站定后再移动一格（取整坐标变化）即受 100 点凋零伤害并爆炸；游泳、跳跃、骑乘、岩浆中清除记录。 */
    private static final class Step implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (!eligible(entity) || !EffectSupport.every(entity, "duyao06tick", 5)) {
                return true;
            }
            var data = entity.getPersistentData();
            long x = Math.round(entity.getX());
            long z = Math.round(entity.getZ());
            if (entity.isInLava() || entity.isInWaterOrBubble() || entity.isPassenger() || !entity.onGround()) {
                data.putDouble("duyao06weizhiX", 0);
                data.putDouble("duyao06weizhiZ", 0);
            } else if (data.getDouble("duyao06weizhiX") + data.getDouble("duyao06weizhiZ") != 0) {
                if (data.getDouble("duyao06weizhiX") != x || data.getDouble("duyao06weizhiZ") != z) {
                    entity.hurt(entity.damageSources().wither(), 100);
                    entity.level().playSound(null, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                            SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 1, 1);
                    if (entity.level() instanceof ServerLevel level) {
                        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 1, 0, 0, 0, 0);
                    }
                    data.putDouble("duyao06weizhiX", 0);
                    data.putDouble("duyao06weizhiZ", 0);
                } else {
                    data.putDouble("duyao06weizhiX", x);
                    data.putDouble("duyao06weizhiZ", z);
                }
            } else {
                data.putDouble("duyao06weizhiX", x);
                data.putDouble("duyao06weizhiZ", z);
            }
            return true;
        }
    }

    /** 一日丧命散：每 20 刻累积 1 层，满 1200 层（20 分钟）毙命；玩家的灵力每次被压到 1，生物则被沉默 2 秒。 */
    private static final class DayDeath implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (!eligible(entity) || !EffectSupport.every(entity, "duyao07tick", 20)) {
                return true;
            }
            if (entity instanceof ServerPlayer player) {
                Cultivation.set(player, CultivationValue.POWER, 1);
                double level = Cultivation.tizhiDouble(player, "duyao07level") + 1;
                if (level >= 1200) {
                    Cultivation.setTizhiDouble(player, "duyao07level", 0);
                    entity.kill();
                } else {
                    Cultivation.setTizhiDouble(player, "duyao07level", level);
                }
            } else {
                double level = entity.getPersistentData().getDouble("duyao07level") + 1;
                if (level >= 1200) {
                    entity.getPersistentData().putDouble("duyao07level", 0);
                    entity.kill();
                } else {
                    entity.getPersistentData().putDouble("duyao07level", level);
                }
                entity.addEffect(new MobEffectInstance(EffectSupport.holder("effect_002"), 40, 0, false, false));
            }
            return true;
        }
    }

    /** 番木鳖：每刻把当前生命压到 最大生命 − 10 × (1 + 等级)（体质放大目标值，至少 1）。 */
    private static final class MaxHealthDrain implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity)) {
                double target = Math.max(scale(entity, entity.getMaxHealth() - 10 * (1 + amplifier)), 1);
                if (entity.getHealth() > target) {
                    entity.setHealth((float) target);
                }
            }
            return true;
        }
    }

    /** 寒毒：每刻累积冷冻值，每 80 刻发抖 1 秒。 */
    private static final class ColdPoison implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity)) {
                entity.setTicksFrozen(entity.getTicksFrozen() + 1);
                if (EffectSupport.every(entity, "duyao12", 80)) {
                    entity.addEffect(new MobEffectInstance(EffectSupport.holder("effect_006"), 20, 1));
                }
            }
            return true;
        }
    }

    /** 孢子：每 100 刻随机获得缓慢、中毒、霉运、发光、虚弱、失明、隐身之一，持续 5 秒，等级同孢子。 */
    private static final class Spore implements EffectBehavior {
        private static final List<Holder<MobEffect>> POOL = List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.POISON,
                MobEffects.UNLUCK, MobEffects.GLOWING, MobEffects.WEAKNESS, MobEffects.BLINDNESS, MobEffects.INVISIBILITY);

        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (eligible(entity) && EffectSupport.every(entity, "duyao17tick", 100)) {
                entity.addEffect(new MobEffectInstance(POOL.get(Mth.nextInt(entity.getRandom(), 1, 7) - 1), 100, EffectSupport.amplifier(entity, "duyao_17")));
            }
            return true;
        }
    }

    /**
     * 尸毒：每次感染累积 1 层（开始时增加），每 80 刻受 等级 + 1 + 层数 点伤害；身上有瞬间治疗或生命恢复时层数清零。
     * 体质放大在来源中先于伤害赋值，被覆盖而不生效（Q32），这里保持。
     */
    private static final class Corpse implements EffectBehavior {
        @Override
        public void started(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                Cultivation.setTizhiDouble(player, "duyao20level", Cultivation.tizhiDouble(player, "duyao20level") + 1);
            } else {
                entity.getPersistentData().putDouble("duyao20level", entity.getPersistentData().getDouble("duyao20level") + 1);
            }
        }

        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (!eligible(entity) || !EffectSupport.every(entity, "duyao20tick", 80)) {
                return true;
            }
            if (entity.hasEffect(MobEffects.HEAL) || entity.hasEffect(MobEffects.REGENERATION)) {
                if (entity instanceof ServerPlayer player) {
                    Cultivation.setTizhiDouble(player, "duyao20level", 0);
                } else {
                    entity.getPersistentData().putDouble("duyao20level", 0);
                }
            }
            if (!resisted(entity)) {
                double level = entity instanceof Player player ? Cultivation.tizhiDouble(player, "duyao20level")
                        : entity.getPersistentData().getDouble("duyao20level");
                hurt(entity, amplifier + 1 + level);
            }
            return true;
        }
    }
}
