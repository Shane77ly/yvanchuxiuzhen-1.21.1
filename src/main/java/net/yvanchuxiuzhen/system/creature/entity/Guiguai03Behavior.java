package net.yvanchuxiuzhen.system.creature.entity;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouShared;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 鬼怪03的来源技能选择、分阶段攻击与鬼火召唤；阶段保存在来源实体字段。 */
final class Guiguai03Behavior {
    private Guiguai03Behavior() { }

    static void initialize(Entity entity) {
        CreatureSourceStats.initialize((Mob) entity, 21, false, false,
                "强劲", "强劲", "贫弱", "贫弱", "贫弱", "贫弱", "强劲", "平庸", "勉强", "平庸", "贫弱", "贫弱", "贫弱");
    }

    static void hurt(Entity entity, Entity attacker) {
        if (entity.level().isClientSide || attacker == null || !entity.isAlive() || Relations.isFriendly(entity, attacker)) return;
        CompoundTag data = entity.getPersistentData();
        data.putString("linshigongjizhe", attacker.getStringUUID());
        if (data.getBoolean("beidaduan")) {
            animate(entity, "empty");
            data.putBoolean("beidaduan", false);
        }
        if (data.getString("jineng").isEmpty()) animate(entity, "animation.guiguai03.attacked");
    }

    static void tick(LevelAccessor world, Entity entity) {
        if (entity.level().isClientSide || !entity.isAlive()) return;
        CompoundTag data = entity.getPersistentData();
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty()) data.putString("gongjizhe", data.getString("linshigongjizhe"));
        Entity target = target(world, data.getString("gongjizhe"));
        if (target == null) {
            if (data.getDouble("entitytick") >= 40) {
                ((Mob) entity).getNavigation().moveTo(entity.getX() + random(entity, -3, 3), entity.getY(), entity.getZ() + random(entity, -3, 3), 1);
                data.putDouble("entitytick", 0);
            }
            return;
        }
        if (!target.isAlive()) {
            data.putString("gongjizhe", "");
            data.putString("linshigongjizhe", "");
            YaoshouShared.finishSkill(entity);
            return;
        }
        if (data.getString("jineng").isEmpty()) {
            if (data.getString("yvdingjineng").isEmpty()) {
                choose(entity, target);
            } else if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) {
                data.putString("jineng", data.getString("yvdingjineng"));
                data.putString("yvdingjineng", "");
                data.putDouble("yvdingjinengjishi", 0);
                data.putDouble("entitytick", 0);
                for (int i = 1; i <= data.getDouble("jinengshuliang"); i++) {
                    data.putString("linshijineng" + i, "");
                    data.putDouble("jinengjishi" + i, 0);
                    data.putDouble("jinengqvanzhong" + i, 0);
                }
                data.putDouble("jinengshuliang", 0);
            }
            return;
        }
        switch (data.getString("jineng")) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> basic(world, entity, target);
            case "冲刺" -> dash(entity, target);
            case "瞬身" -> teleport(world, entity, target);
            case "召唤鬼火" -> combo(world, entity, target, true);
            case "三连击" -> combo(world, entity, target, false);
            case "力劈" -> charged(world, entity, target, false);
            case "熊抱" -> charged(world, entity, target, true);
            case "横扫" -> sweep(world, entity, target);
            case "爆气" -> burst(world, entity);
            default -> { }
        }
    }

    private static void choose(Entity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        face(entity, target);
        double distance = entity.distanceTo(target) - (entity.getBbWidth() + target.getBbWidth()) / 2;
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!has(entity, "effect_002") && !has(entity, "effect_033")) {
                if (distance > 3) {
                    candidate(data, ++count, "靠近", 5, 30);
                    candidate(data, ++count, "冲刺", 5, 30);
                    candidate(data, ++count, "瞬身", 5, 30);
                    candidate(data, ++count, "召唤鬼火", 5, 30);
                } else {
                    candidate(data, ++count, "普攻", 10, 20);
                    candidate(data, ++count, "三连击", 5, 30);
                    candidate(data, ++count, "力劈", 5, 30);
                    candidate(data, ++count, "熊抱", 5, 30);
                    candidate(data, ++count, "横扫", 5, 30);
                    int amplifier = has(entity, "effect_034") ? ((LivingEntity) entity).getEffect(StatusEffects.holder("effect_034")).getAmplifier() : 0;
                    candidate(data, ++count, "爆气", 10, 10 + 10 * amplifier);
                }
            }
            if (!has(entity, "effect_033") && distance < 3) candidate(data, ++count, "普攻", 10, 20);
        }
        int total = 0;
        for (int i = 1; i <= count; i++) total += (int) data.getDouble("jinengqvanzhong" + i);
        int selected = random(entity, 1, total);
        int weight = 0;
        for (int i = 1; i <= count; i++) {
            weight += (int) data.getDouble("jinengqvanzhong" + i);
            if (weight >= selected) {
                data.putString("yvdingjineng", data.getString("linshijineng" + i));
                data.putDouble("yvdingjinengjishi", data.getDouble("jinengjishi" + i));
                break;
            }
        }
        data.putDouble("jinengshuliang", count);
    }

    private static void candidate(CompoundTag data, int index, String skill, int delay, int weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    private static void approach(Entity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (phase(entity).isEmpty()) {
            face(entity, target);
            ((Mob) entity).getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.putString("jinengjinxing", "移动中");
        } else if (phase(entity).equals("移动中") && tick(entity) >= 40) YaoshouShared.finishSkill(entity);
    }

    private static void basic(LevelAccessor world, Entity entity, Entity target) {
        if (phase(entity).isEmpty()) {
            entity.getPersistentData().putString("jinengjinxing", "准备");
            animate(entity, "animation.guiguai03.attack");
            face(entity, target);
        } else if (phase(entity).equals("准备") && tick(entity) >= 4) {
            Vec3 center = ahead(entity, 1);
            for (Entity hit : enemies(world, entity, center, new AABB(center, center).inflate(1.5))) {
                crit(world, hit, 5, .5);
                double amount = entity.getPersistentData().getDouble("attack_Num");
                if (((LivingEntity) entity).hasEffect(MobEffects.WEAKNESS)) amount -= (((LivingEntity) entity).getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (((LivingEntity) entity).hasEffect(MobEffects.DAMAGE_BOOST)) amount += (((LivingEntity) entity).getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                hit.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) Math.max(.1, amount));
            }
            entity.getPersistentData().putString("jinengjinxing", "攻击");
        } else if (phase(entity).equals("攻击") && tick(entity) >= 9) YaoshouShared.finishSkill(entity);
    }

    private static void dash(Entity entity, Entity target) {
        if (phase(entity).isEmpty()) {
            face(entity, target);
            entity.setDeltaMovement(entity.getLookAngle().x, .4, entity.getLookAngle().z);
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            animate(entity, "animation.guiguai03.skill01");
        } else if (phase(entity).equals("蓄力") && tick(entity) >= 10) {
            face(entity, target);
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void teleport(LevelAccessor world, Entity entity, Entity target) {
        if (phase(entity).isEmpty()) {
            face(entity, target);
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, false, false));
            particles(world, entity, "brokenblack", 20, .5);
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if (phase(entity).equals("蓄力") && tick(entity) >= 10) {
            double x = 0;
            double z = 0;
            for (int i = 0; i < 6; i++) {
                x = target.getX() + random(entity, -2, 2);
                z = target.getZ() + random(entity, -2, 2);
                if (world.isEmptyBlock(BlockPos.containing(x, target.getY(), z))) break;
            }
            entity.teleportTo(x, target.getY(), z);
            entity.getPersistentData().putString("jinengjinxing", "结束");
            particles(world, entity, "starblack", 20, .5);
        } else if (phase(entity).equals("结束") && tick(entity) >= 12) {
            face(entity, target);
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void charged(LevelAccessor world, Entity entity, Entity target, boolean hug) {
        if (phase(entity).isEmpty()) {
            if (hug) ((LivingEntity) entity).addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 20, 10, false, false));
            face(entity, target);
            Skills.showDamageArea(entity, ahead(entity, hug ? 1.5 : 2), "animation.shanghaifanwei.sige1", "", 20, 0);
            entity.getPersistentData().putString("jinengjinxing", hug ? "准备" : "序");
        } else if (phase(entity).equals(hug ? "准备" : "序") && tick(entity) >= (hug ? 6 : 4)) {
            animate(entity, hug ? "animation.guiguai03.skill04" : "animation.guiguai03.skill03");
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
        } else if (phase(entity).equals("蓄力") && tick(entity) >= 20) {
            if (!hug) face(entity, target);
            Vec3 center = ahead(entity, hug ? 1.5 : 2);
            for (Entity hit : enemies(world, entity, center, new AABB(center, center).inflate(hug ? 2 : 2.5))) {
                crit(world, hit, 20, 1);
                damage(entity, hit, hug ? .8 : 1.5);
                if (hug) {
                    if (hit instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
                } else if (!has(hit, "skilleffect_08")) hit.setDeltaMovement((hit.getX() - entity.getX()) * 1.3, .5, (hit.getZ() - entity.getZ()) * 1.3);
            }
            entity.getPersistentData().putString("jinengjinxing", "结束");
        } else if (phase(entity).equals("结束") && tick(entity) >= (hug ? 28 : 26)) YaoshouShared.finishSkill(entity);
    }

    private static void combo(LevelAccessor world, Entity entity, Entity target, boolean summon) {
        if (phase(entity).isEmpty()) {
            face(entity, target);
            entity.getPersistentData().putString("jinengjinxing", "第一击");
            animate(entity, "animation.guiguai03.skill02");
            return;
        }
        int strike = switch (phase(entity)) {
            case "第一击" -> tick(entity) >= 10 ? 1 : 0;
            case "第二击" -> tick(entity) >= 17 ? 2 : 0;
            case "第三击" -> tick(entity) >= 24 ? 3 : 0;
            default -> 0;
        };
        if (strike > 0) {
            face(entity, target);
            if (summon) summon(world, entity);
            else {
                Vec3 center = ahead(entity, 1.5);
                if (world instanceof ServerLevel level) level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 5, 1, 0, 1, 0);
                for (Entity hit : enemies(world, entity, center, new AABB(center, center).inflate(3, 2, 3))) {
                    crit(world, hit, 20, 1);
                    damage(entity, hit, strike == 1 ? .7 : strike == 2 ? 1 : 1.5);
                    bleed(hit, 100, 1);
                }
            }
            entity.getPersistentData().putString("jinengjinxing", strike == 1 ? "第二击" : strike == 2 ? "第三击" : "结束");
        } else if (phase(entity).equals("结束") && tick(entity) >= 36) YaoshouShared.finishSkill(entity);
    }

    private static void summon(LevelAccessor world, Entity entity) {
        if (!(world instanceof ServerLevel level)) return;
        for (int i = 0; i < 3; i++) {
            Vec3 at = ahead(entity, 1.5).add(random(entity, -1, 1), random(entity, -1, 1), random(entity, -1, 1));
            level.sendParticles(Particles.type("smokeskyblue"), at.x, at.y, at.z, 3, 0, 0, 0, 0);
            Guiguai02Entity ghost = CreatureSetup.<Guiguai02Entity>entityType("guiguai_02").get().create(level);
            ghost.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360, 0);
            Scoreboard scoreboard = level.getScoreboard();
            Objective objective = scoreboard.getObjective("zhaohuanwu");
            if (objective == null) objective = scoreboard.addObjective("zhaohuanwu", ObjectiveCriteria.DUMMY, Component.literal("zhaohuanwu"), ObjectiveCriteria.RenderType.INTEGER, false, null);
            scoreboard.getOrCreatePlayerScore(ghost, objective).set(1);
            ghost.setDeltaMovement(entity.getLookAngle());
            Relations.shareEntityAllegiance(entity, ghost);
            ghost.getPersistentData().putString("gongjizhe", entity.getPersistentData().getString("gongjizhe"));
            ghost.finalizeSpawn(level, level.getCurrentDifficultyAt(ghost.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(ghost);
        }
    }

    private static void sweep(LevelAccessor world, Entity entity, Entity target) {
        if (phase(entity).isEmpty()) {
            face(entity, target);
            entity.getPersistentData().putString("jinengjinxing", "蓄力");
            animate(entity, "animation.guiguai03.skill05");
        } else if ((phase(entity).equals("蓄力") && tick(entity) >= 12) || (phase(entity).equals("续") && tick(entity) >= 17)) {
            boolean first = phase(entity).equals("蓄力");
            Vec3 center = ahead(entity, 1.5);
            if (world instanceof ServerLevel level) level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 8, 2, 0, 2, 0);
            for (Entity hit : enemies(world, entity, center, new AABB(center, center).inflate(4, 2, 4))) {
                crit(world, hit, 20, 1);
                damage(entity, hit, 1.5);
                bleed(hit, 600, 0);
            }
            if (first) entity.setDeltaMovement(entity.getLookAngle());
            entity.getPersistentData().putString("jinengjinxing", first ? "续" : "结束");
        } else if (phase(entity).equals("结束") && tick(entity) >= 20) YaoshouShared.finishSkill(entity);
    }

    private static void burst(LevelAccessor world, Entity entity) {
        Skills.showWindBurst(entity);
        if (world instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 40, 0, 0, 0, 1);
            level.playSound(null, entity.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "tongyong_baoqi")), SoundSource.NEUTRAL, 1, 1);
        }
        Vec3 center = entity.position();
        for (Entity hit : enemies(world, entity, center, new AABB(center, center).inflate(4.5))) {
            if (hit.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "noentity")))) {
                if (world instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("brokenlightgray"), hit.getX(), hit.getY(), hit.getZ(), 16, 0, 0, 0, 1);
                    hit.discard();
                }
            } else if (!has(hit, "skilleffect_08")) hit.setDeltaMovement(hit.getX() - entity.getX(), .5, hit.getZ() - entity.getZ());
        }
        if (world instanceof ServerLevel level) {
            for (int i = 0; i < 20; i++) {
                double angle = i * 18;
                level.sendParticles(ParticleTypes.CLOUD, entity.getX() + entity.getBbWidth() * Math.cos(angle), entity.getY() + entity.getBbHeight() * .5,
                        entity.getZ() + entity.getBbWidth() * Math.sin(angle), 0, .5 * Math.cos(angle), 0, .5 * Math.sin(angle), .5);
            }
        }
        YaoshouShared.finishSkill(entity);
    }

    private static void bleed(Entity hit, int duration, int increment) {
        if (!(hit instanceof LivingEntity living)) return;
        MobEffectInstance previous = living.getEffect(StatusEffects.holder("effect_004"));
        living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_004"), duration + (previous == null ? 0 : previous.getDuration()), previous == null ? 0 : previous.getAmplifier() + increment));
    }

    private static List<Entity> enemies(LevelAccessor world, Entity entity, Vec3 center, AABB box) {
        return world.getEntitiesOfClass(Entity.class, box, candidate -> !Relations.isFriendly(entity, candidate)).stream()
                .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList();
    }

    private static Vec3 ahead(Entity entity, double distance) {
        BlockPos pos = entity.level().clip(new ClipContext(entity.getEyePosition(1), entity.getEyePosition(1).add(entity.getViewVector(1).scale(distance + entity.getBbWidth() / 2)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    private static void damage(Entity entity, Entity hit, double multiplier) {
        hit.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (entity.getPersistentData().getDouble("attack_Num") * multiplier));
    }

    private static void crit(LevelAccessor world, Entity hit, int count, double speed) {
        if (world instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, hit.getX(), hit.getY() + hit.getBbHeight() / 2, hit.getZ(), count, 0, 0, 0, speed);
    }

    private static void particles(LevelAccessor world, Entity entity, String id, int count, double speed) {
        if (world instanceof ServerLevel level) level.sendParticles(Particles.type(id), entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), count, 0, 0, 0, speed);
    }

    private static Entity target(LevelAccessor world, String id) {
        if (!(world instanceof ServerLevel level) || id.isEmpty()) return null;
        try {
            return level.getEntity(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void face(Entity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
    }

    private static boolean has(Entity entity, String effect) {
        return entity instanceof LivingEntity living && living.hasEffect(StatusEffects.holder(effect));
    }

    private static int random(Entity entity, int min, int max) {
        return Mth.nextInt(entity.level().getRandom(), min, max);
    }

    private static double tick(Entity entity) {
        return entity.getPersistentData().getDouble("entitytick");
    }

    private static String phase(Entity entity) {
        return entity.getPersistentData().getString("jinengjinxing");
    }

    private static void animate(Entity entity, String animation) {
        if (!entity.level().isClientSide) ((Guiguai03Entity) entity).setAnimation(animation);
    }
}
