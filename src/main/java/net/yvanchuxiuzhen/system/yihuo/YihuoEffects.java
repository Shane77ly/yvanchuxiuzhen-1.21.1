package net.yvanchuxiuzhen.system.yihuo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.yihuo.data.YihuoBurnState;
import net.yvanchuxiuzhen.system.yihuo.data.YihuoData;

/** 阳火炙烤与阴火灼烧：独立持久计时、每二十刻伤害及阳火触发的阴阳爆燃。 */
public final class YihuoEffects {
    private static final List<LivingEntity> RESET_INVULNERABILITY = new ArrayList<>();

    private YihuoEffects() {
    }

    public static void register(ContentTables tables) {
        tables.effectBehavior("effect_023", new Burning(true));
        tables.effectBehavior("effect_024", new Burning(false));
        NeoForge.EVENT_BUS.addListener(YihuoEffects::endServerTick);
    }

    /** 来源延时一刻的任务在 ServerTick.END 执行，阳火普通伤害之后清除受击无敌时间。 */
    private static void endServerTick(ServerTickEvent.Post event) {
        for (LivingEntity entity : RESET_INVULNERABILITY) entity.invulnerableTime = 0;
        RESET_INVULNERABILITY.clear();
    }

    static void resetInvulnerabilityNextTick(LivingEntity entity) {
        RESET_INVULNERABILITY.add(entity);
    }

    private record Burning(boolean yang) implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (!eligible(entity)) return true;
            ServerLevel level = (ServerLevel) entity.level();
            int particle = (yang ? 221 : 231) + (amplifier >= 0 && amplifier < 8 ? amplifier : 8);
            level.sendParticles(Particles.type("cailiao_" + particle + "particle"), entity.getX(), entity.getY() + .7, entity.getZ(), 2, .8, .8, .8, .1);
            YihuoBurnState state = entity.getData(YihuoData.BURN_STATE);
            double elapsed = (yang ? state.yangTick() : state.yinTick()) + 1;
            if (yang) state.setYangTick(elapsed);
            else state.setYinTick(elapsed);
            if (elapsed >= 20) {
                String attacker = entity instanceof Player player ? Cultivation.tizhiString(player, "fireAttackUUID")
                        : state.fireAttackUuid();
                if (yang && StatusEffects.has(entity, "effect_024")) {
                    explode(level, entity, attacker);
                } else {
                    SpellDamage.dealSaved(level, entity, DamageCategory.FIRE, attacker, true, 8 * (amplifier + 1), 1);
                    if (yang) RESET_INVULNERABILITY.add(entity);
                }
                if (yang) state.setYangTick(0);
                else state.setYinTick(0);
            }
            return true;
        }

        @Override
        public void clientTick(LivingEntity entity, int amplifier) {
            if (!yang || !eligible(entity) || !StatusEffects.has(entity, "effect_024")) {
                return;
            }
            YihuoBurnState state = entity.getData(YihuoData.BURN_STATE);
            double elapsed = state.yangClientTick() + 1;
            state.setYangClientTick(elapsed);
            if (elapsed >= 20) {
                entity.level().playLocalSound(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1, 1, false);
                state.setYangClientTick(0);
            }
        }
    }

    private static boolean eligible(LivingEntity entity) {
        if (!entity.isAlive()) return false;
        if (entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
        return entity instanceof Mob;
    }

    /** 来源范围只处理 Mob，未调用敌友判定；角度按原数值直接传入三角函数。 */
    private static void explode(ServerLevel level, LivingEntity entity, String attacker) {
        for (Mob target : level.getEntitiesOfClass(Mob.class, new AABB(entity.position(), entity.position()).inflate(2)).stream()
                .sorted(Comparator.comparingDouble(entity::distanceToSqr)).toList()) {
            int damage = 10 * (StatusEffects.amplifier(entity, "effect_023") + StatusEffects.amplifier(entity, "effect_024") + 2);
            SpellDamage.dealSaved(level, target, DamageCategory.FIRE, attacker, false, damage, 1);
        }
        double y = entity.getY() + entity.getBbHeight() * .5;
        level.playSound(null, BlockPos.containing(entity.getX(), y, entity.getZ()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1, 1);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, entity.getX(), y, entity.getZ(), 1, .5, .5, .5, 0);
        for (int index = 0; index < 40; index++) {
            double angle = index * 9.0;
            level.sendParticles(ParticleTypes.FLAME, entity.getX() + .5 * Math.cos(angle), y, entity.getZ() + .5 * Math.sin(angle),
                    0, Math.cos(angle), 0, Math.sin(angle), 1);
        }
        StatusEffects.remove(entity, "effect_023");
        StatusEffects.remove(entity, "effect_024");
    }
}
