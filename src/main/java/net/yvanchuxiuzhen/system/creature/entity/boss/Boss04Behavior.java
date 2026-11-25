package net.yvanchuxiuzhen.system.creature.entity.boss;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 04号首领的实体 AI 状态、目标选择和十五种技能阶段。 */
final class Boss04Behavior {
    private static final String[] CLOSE_SKILLS = {"抓取", "铁山靠", "撼地锤", "快拳连打", "蓄力重拳", "正蹬", "力量提升", "二连蓄力重拳", "地脉拳", "冲击拳"};
    private static final String[] FAR_SKILLS = {"跃空槌", "冲拳", "飞跃", "拳炮", "二连拳炮", "赶路"};

    private Boss04Behavior() { }

    static void tick(Boss04Entity boss) {
        if (boss.level().isClientSide || !boss.isAlive()) return;
        CompoundTag state = boss.aiState();
        if (!state.getString("gongjizhe").equals(state.getString("linshigongjizhe"))) state.putString("gongjizhe", state.getString("linshigongjizhe"));
        Entity target = find(boss, state.getString("gongjizhe"));
        if (target != null) {
            String skill = state.getString("jineng");
            if (skill.isEmpty()) selectSkill(boss, target, state);
            else executeSkill(boss, target, skill, state);
        }
    }

    static void hurt(Boss04Entity boss, Entity attacker) {
        if (attacker == null || boss.level().isClientSide) return;
        CompoundTag state = boss.aiState();
        if (boss.isAlive()) {
            if (!StatusEffects.has(boss, "skilleffect_08")) {
                boss.setAnimation("empty");
                if (!state.getString("jineng").isEmpty()) {
                    state.putString("jineng", "");
                    state.putString("jinengjinxing", "");
                    state.putDouble("entitytick", 0);
                    state.putDouble("entitytick2", 0);
                } else if (boss.getRandom().nextFloat() < .3F) {
                    state.putString("jineng", "后跳");
                    state.putString("jinengjinxing", "");
                    state.putDouble("entitytick", 0);
                    state.putDouble("entitytick2", 0);
                }
                Vec3 center = new Vec3(boss.getX(), boss.getY() + 1, boss.getZ());
                for (Entity entity : nearby(boss, center, 1.5)) if (!Relations.isFriendly(boss, entity)) entity.stopRiding();
                boss.setAnimation("animation.ycxzboss_04.attacked");
            }
            state.putString("linshigongjizhe", attacker.getStringUUID());
        }
    }

    private static void selectSkill(Boss04Entity boss, Entity target, CompoundTag state) {
        state.putDouble("entitytick", state.getDouble("entitytick") + 1);
        if (state.getString("yvdingjineng").isEmpty()) {
            double distance = boss.distanceTo(target);
            boss.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
            int roll = boss.getRandom().nextInt(1, 101);
            if (distance <= 5) {
                if (roll >= 90) setPending(state, CLOSE_SKILLS[0], 20);
                else if (roll >= 80) setPending(state, CLOSE_SKILLS[1], 5);
                else if (roll >= 70) setPending(state, CLOSE_SKILLS[2], 40);
                else if (roll >= 60) setPending(state, CLOSE_SKILLS[3], 5);
                else if (roll >= 50) setPending(state, CLOSE_SKILLS[4], 20);
                else if (roll >= 40) setPending(state, CLOSE_SKILLS[5], 7);
                else if (roll >= 30) setPending(state, CLOSE_SKILLS[6], 80);
                else if (roll >= 20) setPending(state, CLOSE_SKILLS[7], 40);
                else if (roll >= 10) setPending(state, CLOSE_SKILLS[8], 10);
                else setPending(state, CLOSE_SKILLS[9], 10);
            } else {
                if (roll >= 70) setPending(state, FAR_SKILLS[0], 5);
                else if (roll >= 50) setPending(state, FAR_SKILLS[1], 5);
                else if (roll >= 25) setPending(state, FAR_SKILLS[2], 5);
                else if (roll >= 15) setPending(state, FAR_SKILLS[3], 20);
                else if (roll >= 10) setPending(state, FAR_SKILLS[4], 40);
                else setPending(state, FAR_SKILLS[5], 10);
            }
        } else if (state.getDouble("entitytick") >= state.getDouble("yvdingjinengjishi")) {
            state.putString("jineng", state.getString("yvdingjineng"));
            state.putString("yvdingjineng", "");
            state.putDouble("entitytick", 0);
        }
    }

    private static void setPending(CompoundTag state, String skill, int wait) {
        state.putString("yvdingjineng", skill);
        state.putDouble("yvdingjinengjishi", wait);
    }

    private static void executeSkill(Boss04Entity boss, Entity target, String skill, CompoundTag state) {
        switch (skill) {
            case "抓取" -> skill01(boss, state);
            case "铁山靠" -> skill02(boss, state);
            case "撼地锤" -> skill03(boss, state);
            case "快拳连打" -> skill04(boss, state);
            case "蓄力重拳", "地脉拳", "冲击拳" -> skill05(boss, state);
            case "正蹬" -> skill06(boss, state);
            case "力量提升" -> skill07(boss, state);
            case "二连蓄力重拳" -> skill08(boss, state);
            case "跃空槌" -> skill09(boss, target, state);
            case "冲拳" -> skill10(boss, state);
            case "飞跃" -> skill11(boss, target, state);
            case "拳炮" -> skill12(boss, target, state);
            case "二连拳炮" -> skill13(boss, target, state);
            case "赶路" -> skill14(boss, target, state);
            case "后跳" -> skill15(boss, state);
            default -> { }
        }
    }

    private static void skill01(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state);
        String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { boss.setAnimation("animation.ycxzboss_04.skill03"); state.putString("jinengjinxing", "抓"); }
        else if (tick > 8 && phase.equals("抓")) {
            for (Entity victim : forwardTargets(boss, 2, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) victim.startRiding(boss);
            state.putString("jinengjinxing", "举");
        } else if (tick >= 12 && phase.equals("举")) {
            if (!boss.isVehicle()) abortGrab(boss, state); else state.putString("jinengjinxing", "打一");
        } else {
            String[] phases = {"打一", "打二", "打三", "打四", "打五", "打六", "打七", "打八"};
            int[] times = {40, 56, 72, 98, 103, 108, 112, 117};
            for (int i = 0; i < times.length; i++) if (tick >= times[i] && phase.equals(phases[i])) {
                for (Entity victim : forwardTargets(boss, 1, 1.5)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                    particle(victim, ParticleTypes.CRIT, 20);
                    damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK));
                }
                if (!boss.isVehicle()) abortGrab(boss, state);
                else state.putString("jinengjinxing", i == 7 ? "结束" : phases[i + 1]);
                break;
            }
            if (tick >= 122 && phase.equals("结束")) {
                state.putString("jineng", ""); state.putString("jinengjinxing", ""); state.putDouble("entitytick", 0);
                if (boss.isVehicle()) for (Entity victim : nearby(boss, new Vec3(boss.getX(), boss.getY() + 1, boss.getZ()), 1.5)) if (!Relations.isFriendly(boss, victim)) victim.stopRiding();
            }
        }
    }

    private static void skill02(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { boss.setAnimation("animation.ycxzboss_04.skill02"); state.putString("jinengjinxing", "打一"); return; }
        String[] phases = {"打一", "打二", "打三", "打四"};
        int[] times = {12, 20, 28, 36};
        for (int i = 0; i < times.length; i++) if (tick >= times[i] && phase.equals(phases[i])) {
            for (Entity victim : forwardTargets(boss, 3, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                particle(boss, ParticleTypes.CRIT, 20); damage(boss, victim, Math.round(EntityAttributes.stat(boss, Stat.ATTACK) * .3));
                if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3((victim.getX() - boss.getX()) * .5, .5, (victim.getZ() - boss.getZ()) * .5));
            }
            state.putString("jinengjinxing", i == 3 ? "结束" : phases[i + 1]); break;
        }
        if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill03(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) {
            StatusEffects.add(boss, "skilleffect_08", 40, 0);
            Skills.showEntityDamageArea(boss, boss.position(), "animation.shanghaifanwei.shiliuge1", 20);
            boss.setAnimation("animation.ycxzboss_04.skill01"); state.putString("jinengjinxing", "撼地"); return;
        }
        if (tick >= 20 && phase.equals("撼地")) {
            ring(boss, 6, "minecraft:large_smoke", 60, .5);
            particle(boss, boss.position(), Particles.type("bigbangdeeporange"), 1, 0);
            for (Entity victim : nearby(boss, boss.position(), 6)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                particle(victim, Particles.type("smallexplosiondeeporange"), 1);
                SpellDamage.deal(victim, DamageCategory.SOIL, boss, false, 2, 1.5);
                if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3(0, 1, 0));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill04(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 50, 0); boss.setAnimation("animation.ycxzboss_04.skill05"); state.putString("jinengjinxing", "连打"); return; }
        if (phase.equals("连打") && tick > 4) {
            if (tick >= 48) state.putString("jinengjinxing", "结束");
            else if (state.getDouble("entitytick2") + 1 >= 3) {
                particle(boss, forwardParticlePosition(boss, 3), ParticleTypes.EXPLOSION, 1, 0);
                for (Entity victim : forwardTargets(boss, 3, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) damage(boss, victim, Math.round(EntityAttributes.stat(boss, Stat.ATTACK) * .2));
                state.putDouble("entitytick2", 0);
            } else state.putDouble("entitytick2", state.getDouble("entitytick2") + 1);
        } else if (tick >= 69 && phase.equals("结束")) finish(state, true);
    }

    private static void skill05(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing"); String skill = state.getString("jineng");
        if (phase.isEmpty()) {
            StatusEffects.add(boss, "skilleffect_08", 10, 0); boss.setAnimation("animation.ycxzboss_04.skill06"); state.putString("jinengjinxing", "打");
            String particle = skill.equals("蓄力重拳") ? "whirlwindlightgray" : skill.equals("地脉拳") ? "whirlwinddeeporange" : "whirlwindgolden";
            particle(boss, forwardParticlePosition(boss, 2), Particles.type(particle), 1, 0); return;
        }
        if (tick > 20 && phase.equals("打")) {
            for (Entity victim : forwardTargets(boss, 3, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                if (skill.equals("蓄力重拳")) damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 1.5);
                else if (skill.equals("地脉拳")) { particle(victim, Particles.type("smallexplosiondeeporange"), 1); SpellDamage.deal(victim, DamageCategory.SOIL, boss, false, 10, 1.5); }
                else if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3(victim.getX() - boss.getX(), .5, victim.getZ() - boss.getZ()));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill06(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { boss.setAnimation("animation.ycxzboss_04.skill07"); state.putString("jinengjinxing", "打"); return; }
        if (tick > 20 && phase.equals("打")) {
            for (Entity victim : forwardTargets(boss, 3, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3(victim.getX() - boss.getX(), .5, victim.getZ() - boss.getZ()));
                damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * .8);
                if (victim instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill07(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { boss.setAnimation("animation.ycxzboss_04.skill08"); state.putString("jinengjinxing", "buff"); return; }
        if (tick > 20 && phase.equals("buff")) {
            ((ServerLevel) boss.level()).sendParticles(Particles.type("striporange"), boss.getX(), boss.getY(), boss.getZ(), 10, .7, 0, .7, 0);
            for (int i = 0; i < 8; i++) {
                int delay = 2 + i * 2;
                SkillDelays.after(delay, () -> {
                    if (boss.level() instanceof ServerLevel level) {
                        level.sendParticles(Particles.type("stargolden"), boss.getX(), boss.getY() + boss.getBbHeight() / 2, boss.getZ(), 2, .7, 2, .7, 0);
                        level.sendParticles(Particles.type("striporange"), boss.getX(), boss.getY(), boss.getZ(), 10, .7, 0, .7, 0);
                    }
                });
            }
            boss.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
            boss.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 6));
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill08(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 10, 0); boss.setAnimation("animation.ycxzboss_04.skill10"); state.putString("jinengjinxing", "打一"); return; }
        if (tick > 20 && phase.equals("打一")) {
            for (Entity victim : forwardTargets(boss, 3, 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 1.5);
            state.putString("jinengjinxing", "打二");
        } else if (tick > 20 && phase.equals("打二")) {
            for (Entity victim : forwardTargets(boss, 3, 2)) {
                if (victim != boss && !Relations.isFriendly(boss, victim)) damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 2);
                if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3((victim.getX() - boss.getX()) * .5, .2, (victim.getZ() - boss.getZ()) * .5));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill09(Boss04Entity boss, Entity target, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 40, 0); boss.setAnimation("animation.ycxzboss_04.skill01"); state.putString("jinengjinxing", "蓄力"); return; }
        if (tick >= 12 && phase.equals("蓄力")) { state.putString("jinengjinxing", "跳跃"); boss.setDeltaMovement(new Vec3(0, 1.5, 0)); }
        else if (tick >= 16 && phase.equals("跳跃")) {
            state.putString("jinengjinxing", "瞄准计算"); state.putDouble("xzengliang", (target.getX() - boss.getX()) / 4); state.putDouble("yzengliang", (target.getY() - boss.getY()) / 2); state.putDouble("zzengliang", (target.getZ() - boss.getZ()) / 4);
        } else if (tick >= 16 && phase.equals("瞄准计算")) {
            boss.setDeltaMovement(new Vec3(state.getDouble("xzengliang"), state.getDouble("yzengliang"), state.getDouble("zzengliang")));
            if (tick >= 20) state.putString("jinengjinxing", "砸落");
        } else if (tick > 20 && phase.equals("砸落")) {
            ring(boss, 2, "minecraft:large_smoke", 60, .5);
            for (Entity victim : forwardTargets(boss, 3, 5)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 1.5);
                if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3(0, 1, 0));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill10(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 40, 0); boss.setAnimation("animation.ycxzboss_04.skill06"); state.putString("jinengjinxing", "冲刺"); return; }
        if (tick > 20 && phase.equals("冲刺")) {
            boss.setDeltaMovement(new Vec3(boss.getLookAngle().x * .4, 0, boss.getLookAngle().z * .4));
            int next = (int) state.getDouble("entitytick2") + 1;
            state.putDouble("entitytick2", next);
            if (tick >= 38) state.putString("jinengjinxing", "结束");
            else if (next >= 3) {
                for (Entity victim : nearby(boss, new Vec3(boss.getX(), boss.getY() + boss.getBbHeight() / 2, boss.getZ()), 2)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                    damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 1.5);
                    if (!hasSkillGuard(victim)) victim.setDeltaMovement(new Vec3((victim.getX() - boss.getX()) * .3, .2, (victim.getZ() - boss.getZ()) * .3));
                }
                state.putDouble("entitytick2", 0);
            }
        } else if (tick >= 41 && phase.equals("结束")) finish(state, true);
    }

    private static void skill11(Boss04Entity boss, Entity target, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 40, 0); boss.setAnimation("animation.ycxzboss_04.skill04"); state.putString("jinengjinxing", "蓄力"); return; }
        if (tick >= 16 && phase.equals("蓄力")) { state.putString("jinengjinxing", "跳跃"); boss.setDeltaMovement(new Vec3(0, 1.5, 0)); }
        else if (tick >= 24 && phase.equals("跳跃")) {
            state.putString("jinengjinxing", "瞄准计算"); state.putDouble("xzengliang", (target.getX() - boss.getX()) / 4); state.putDouble("yzengliang", (target.getY() - boss.getY()) / 4); state.putDouble("zzengliang", (target.getZ() - boss.getZ()) / 4);
        } else if (tick >= 24 && phase.equals("瞄准计算")) {
            boss.setDeltaMovement(new Vec3(state.getDouble("xzengliang"), state.getDouble("yzengliang"), state.getDouble("zzengliang")));
            if (tick >= 28) state.putString("jinengjinxing", "砸落");
        } else if (tick > 28 && phase.equals("砸落")) {
            ring(boss, 2, "minecraft:large_smoke", 60, .5);
            for (Entity victim : forwardTargets(boss, 3, 3)) if (victim != boss && !Relations.isFriendly(boss, victim)) {
                damage(boss, victim, EntityAttributes.stat(boss, Stat.ATTACK) * 1.5);
                if (victim instanceof LivingEntity living) StatusEffects.add(living, "effect_001", 40, 0);
                if (victim instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
            }
            state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill12(Boss04Entity boss, Entity target, CompoundTag state) { shoot(boss, target, state, false); }
    private static void skill13(Boss04Entity boss, Entity target, CompoundTag state) { shoot(boss, target, state, true); }

    private static void shoot(Boss04Entity boss, Entity target, CompoundTag state, boolean twice) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) {
            boss.setAnimation(twice ? "animation.ycxzboss_04.skill10" : "animation.ycxzboss_04.skill06");
            state.putString("jinengjinxing", "打"); particle(boss, forwardParticlePosition(boss, 3), Particles.type("photospherered"), 1, 0); return;
        }
        if (tick > (phase.equals("打二") ? 28 : 20) && (phase.equals("打") || phase.equals("打二"))) {
            particle(boss, forwardParticlePosition(boss, 3), Particles.type("disappearlightred"), 20, 1);
            if (!state.getString("gongjizhe").isEmpty()) boss.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
            if (boss.level() instanceof ServerLevel level) SkillSummons.shootBossFireball(level, boss);
            if (twice && phase.equals("打")) state.putString("jinengjinxing", "打二");
            else state.putString("jinengjinxing", "结束");
        } else if (tick >= 41 && phase.equals("结束")) finish(state, false);
    }

    private static void skill14(Boss04Entity boss, Entity target, CompoundTag state) {
        int tick = increment(state); double distance = boss.distanceTo(target); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { moveNear(boss, target); state.putString("jinengjinxing", "移动"); }
        else if (tick > 40 || distance < 3) finish(state, true);
        else if (tick < 40 && phase.equals("移动")) {
            int next = (int) state.getDouble("entitytick2") + 1;
            if (next >= 5) { moveNear(boss, target); state.putDouble("entitytick2", 0); }
            else state.putDouble("entitytick2", next);
        }
    }

    private static void skill15(Boss04Entity boss, CompoundTag state) {
        int tick = increment(state); String phase = state.getString("jinengjinxing");
        if (phase.isEmpty()) { StatusEffects.add(boss, "skilleffect_08", 40, 0); boss.setAnimation("animation.ycxzboss_04.skill09"); state.putString("jinengjinxing", "起跳"); }
        else if (tick > 12 && phase.equals("起跳")) { boss.setDeltaMovement(new Vec3(-boss.getLookAngle().x, .5, -boss.getLookAngle().z)); state.putString("jinengjinxing", "结束"); }
        else if (tick >= 21 && phase.equals("结束")) finish(state, false);
    }

    private static int increment(CompoundTag state) {
        int tick = (int) state.getDouble("entitytick") + 1;
        state.putDouble("entitytick", tick);
        return tick;
    }

    private static void abortGrab(Boss04Entity boss, CompoundTag state) {
        boss.setAnimation("empty");
        finish(state, false);
        state.putBoolean("kaiguan", true);
    }

    private static void finish(CompoundTag state, boolean resetSecondary) {
        state.putString("jineng", ""); state.putString("jinengjinxing", ""); state.putDouble("entitytick", 0);
        if (resetSecondary) state.putDouble("entitytick2", 0);
    }

    private static List<Entity> nearby(Entity boss, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        return boss.level().getEntitiesOfClass(Entity.class, box, entity -> true).stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static List<Entity> forwardTargets(Boss04Entity boss, double reach, double radius) {
        return nearby(boss, forwardBlock(boss, reach), radius);
    }

    private static Vec3 forwardBlock(Boss04Entity boss, double reach) {
        BlockPos block = boss.level().clip(new ClipContext(boss.getEyePosition(1f), boss.getEyePosition(1f).add(boss.getViewVector(1f).scale(reach)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
        return new Vec3(block.getX(), block.getY(), block.getZ());
    }

    private static Vec3 forwardParticlePosition(Boss04Entity boss, double reach) {
        Vec3 block = forwardBlock(boss, reach);
        return new Vec3(block.x, boss.getY() + boss.getBbHeight() / 2, block.z);
    }

    private static void damage(Entity owner, Entity victim, double amount) {
        victim.hurt(owner.damageSources().source(DamageTypes.PLAYER_ATTACK, owner), (float) amount);
    }

    private static void particle(Entity target, ParticleOptions type, int count) {
        if (target.level() instanceof ServerLevel level) level.sendParticles(type, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), count, 0, 0, 0, count == 20 ? .5 : 0);
    }

    private static void particle(Boss04Entity boss, Vec3 position, ParticleOptions type, int count, double speed) {
        if (boss.level() instanceof ServerLevel level) level.sendParticles(type, position.x, position.y, position.z, count, 0, 0, 0, speed);
    }

    private static void ring(Boss04Entity boss, double radius, String particle, int count, double speed) {
        if (!(boss.level() instanceof ServerLevel level)) return;
        double angle = 0;
        for (int i = 0; i < count; i++) {
            double x = boss.getX() + radius * Math.cos(angle), z = boss.getZ() + radius * Math.sin(angle);
            double vx = speed * Math.cos(angle), vz = speed * Math.sin(angle);
            level.getServer().getCommands().performPrefixedCommand(boss.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    "particle " + particle + " " + x + " " + (boss.getY() + boss.getBbHeight() * .5) + " " + z + " " + vx + " 0 " + vz + " " + speed + " 0");
            angle += 360.0 / count;
        }
    }

    private static boolean hasSkillGuard(Entity entity) {
        return entity instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08");
    }

    private static void moveNear(Boss04Entity boss, Entity target) {
        boss.getNavigation().moveTo(target.getX() + Mth.nextInt(boss.getRandom(), -2, 2), target.getY(), target.getZ() + Mth.nextInt(boss.getRandom(), -2, 2), 1.5);
    }

    private static Entity find(Boss04Entity boss, String uuid) {
        if (!(boss.level() instanceof ServerLevel level) || uuid.isEmpty()) return null;
        try { return level.getEntity(UUID.fromString(uuid)); }
        // 来源 UUID 解析失败时把目标视为空，技能流程保留原状态。
        catch (IllegalArgumentException exception) { return null; }
    }
}
