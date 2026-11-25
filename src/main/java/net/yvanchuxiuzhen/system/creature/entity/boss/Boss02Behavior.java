package net.yvanchuxiuzhen.system.creature.entity.boss;

import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 02号首领来源等级初始化、斧技状态机、范围伤害与旋转子实体召唤。 */
final class Boss02Behavior {
    private static final List<Stat> STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED);
    private static final List<Stat> COMBAT_STATS = List.of(
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF);
    private static final String[] RATINGS = {"绝佳", "强劲", "勉强", "勉强", "强劲", "强劲", "绝佳", "强劲", "平庸", "平庸", "平庸", "平庸", "强劲"};

    private Boss02Behavior() { }

    static void initialize(Boss02Entity entity) {
        if (!EntityAttributes.realmName(entity).isEmpty()) return;
        double level = EntityAttributes.level(entity) < 1 ? 20 : EntityAttributes.level(entity);
        double healthMax = Math.ceil(100 + rating(RATINGS[0], 14, 13, 12, 11, 10) * Math.pow(level / 4, 2));
        double healthRec = Math.ceil(1 + Math.pow(level / rating(RATINGS[1], 8.4, 8.8, 9.2, 9.6, 10), 2));
        double powerMax = Math.ceil(120 + rating(RATINGS[2], 28, 26, 24, 22, 20) * Math.pow(level / 3, 2));
        double powerRec = Math.ceil(Math.pow(level / rating(RATINGS[3], 6, 6.5, 7, 7.5, 8), 2));
        double soulMax = Math.ceil(120 + rating(RATINGS[4], 28, 26, 24, 22, 20) * Math.pow(level / 3, 2));
        double soulRec = Math.ceil(Math.pow(level / rating(RATINGS[5], 6, 6.5, 7, 7.5, 8), 2));
        double speed = rating(RATINGS[12], .28, .26, .24, .22, .2) + level * .01;
        EntityAttributes.update(entity, current -> {
            EntityStats updated = current.withLevel(level).withRealmName(levelName(level))
                    .withStat(Stat.HEALTH_MAX, healthMax)
                    .withStat(Stat.HEALTH_REC, healthRec)
                    .withStat(Stat.POWER_MAX, powerMax)
                    .withStat(Stat.POWER_REC, powerRec)
                    .withStat(Stat.SOUL_MAX, soulMax)
                    .withStat(Stat.SOUL_REC, soulRec)
                    .withStat(Stat.MOVE_SPEED, speed);
            for (int i = 0; i < STATS.size(); i++) {
                updated = updated.withRating(STATS.get(i), RATINGS[i]);
            }
            for (int i = 0; i < COMBAT_STATS.size(); i++) {
                double statValue = Math.ceil(2 + Math.pow(level / rating(RATINGS[6 + i], 3, 3.2, 3.4, 3.6, 3.8), 2));
                updated = updated.withStat(COMBAT_STATS.get(i), statValue);
            }
            return updated;
        });
        entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(EntityAttributes.stat(entity, Stat.ATTACK));
        entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        SkillDelays.after(20, () -> {
            double delayedMax = EntityAttributes.stat(entity, Stat.HEALTH_MAX);
            entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(delayedMax);
            entity.setHealth((float) delayedMax);
        });
        String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(entity, team);
        Relations.addFriendlyTeam(entity, team);
    }

    static void tick(Boss02Entity boss, EntityModelRow subEntityModel) {
        if (boss.level().isClientSide || !boss.isAlive()) return;
        CreatureState state = CreatureData.state(boss);
        state.setTick(state.tick() + 1);
        LivingEntity target = boss.getTarget();
        if (target == null) {
            if (state.tick() >= 20) state.setTick(0);
            return;
        }
        double distance = boss.distanceTo(target);
        String skill = state.skill();
        if (skill.isEmpty()) {
            if (distance >= 10) state.setSkill("前跳");
            else if (state.tick() >= 60) {
                boolean close = distance <= 5;
                int roll = boss.getRandom().nextInt(close ? 80 : 60) + 1;
                state.setSkill(close
                        ? roll <= 20 ? "横劈" : roll <= 40 ? "蓄力重劈" : roll <= 60 ? "八荒斩" : "定身斩"
                        : roll <= 20 ? "力劈华山" : roll <= 40 ? "飞斧" : "转轮斧");
                state.setTick(0);
            }
            return;
        }
        switch (skill) {
            case "横劈" -> sweep(boss);
            case "力劈华山" -> force(boss, target);
            case "飞斧" -> flyingAxe(boss, target, subEntityModel);
            case "蓄力重劈" -> chargedStrike(boss);
            case "前跳" -> leap(boss);
            case "转轮斧" -> wheel(boss, target);
            case "八荒斩" -> eightDirections(boss, subEntityModel);
            case "定身斩" -> bindingStrike(boss);
            default -> { }
        }
    }

    private static void sweep(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill01");
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(2.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            for (Entity hit : nearby(boss, new Vec3(at.getX(), at.getY(), at.getZ()), 2)) if (!Relations.isFriendly(boss, hit)) {
                damage(boss, hit, 1.3);
                if (hit instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08")) hit.setDeltaMovement((hit.getX() - boss.getX()) * .5, .4, (hit.getZ() - boss.getZ()) * .5);
            }
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 20, false, false));
            d.setAnimated(true);
        }
        if (tick(boss) >= 21) followUp80(boss);
    }

    private static void force(Boss02Entity boss, LivingEntity target) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill02");
            chargeEffects(boss, 40);
            d.setPhase("蓄力");
            d.setDirX(target.getX()); d.setDirY(target.getY()); d.setDirZ(target.getZ());
            d.setAnimated(true);
        }
        faceStored(boss);
        if (tick(boss) >= 4 && d.phase().equals("蓄力")) {
            boss.setDeltaMovement(boss.getLookAngle().x, 1, boss.getLookAngle().z);
            d.setPhase("跳跃");
        } else if (tick(boss) >= 20 && d.phase().equals("跳跃")) {
            boss.fallDistance = 0;
            boss.setDeltaMovement(0, -1, 0);
            d.setPhase("劈砍");
        } else if (tick(boss) >= 22 && d.phase().equals("劈砍")) {
            d.setPhase("收招");
            for (Entity hit : nearby(boss, boss.position(), 2)) if (!Relations.isFriendly(boss, hit)) {
                hit.hurt(boss.damageSources().source(DamageTypes.FALLING_STALACTITE, boss),
                        (float) (EntityAttributes.stat(boss, Stat.ATTACK) * 1.5));
                if (boss.level() instanceof ServerLevel server)
                    server.sendParticles(ParticleTypes.FLAME, hit.getX(), hit.getY() + 1, hit.getZ(), 5, .2, .5, .2, .1);
                if (hit instanceof LivingEntity living && boss.getRandom().nextDouble() < .4) StatusEffects.add(living, "effect_004", 600, 3);
                if (hit instanceof LivingEntity living && boss.getRandom().nextDouble() < .2) StatusEffects.add(living, "effect_007", 1200, 3);
            }
        } else if (tick(boss) >= 40 && d.phase().equals("收招")) {
            finish(boss);
        }
    }

    private static void flyingAxe(Boss02Entity boss, LivingEntity target, EntityModelRow subEntityModel) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill03");
            slow(boss, 60); d.setAnimated(true); d.setPhase("准备");
            boss.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        }
        if (tick(boss) >= 20 && d.phase().equals("飞掷")) {
            finish(boss);
            return;
        }
        if (tick(boss) >= 8 && d.phase().equals("准备")) {
            Boss0201Entity axe = new Boss0201Entity(CreatureSetup.<Boss0201Entity>entityType("ycxzboss_0201").get(), boss.level(), subEntityModel);
            BlockPos pos = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            axe.moveTo(pos.getX(), pos.getY(), pos.getZ(), boss.getYRot(), 0);
            axe.setYBodyRot(axe.getYRot());
            axe.setYHeadRot(axe.getYRot());
            axe.yRotO = axe.getYRot();
            axe.xRotO = axe.getXRot();
            axe.yBodyRotO = axe.getYRot();
            axe.yHeadRotO = axe.getYRot();
            axe.setAnimation("animation.ycxzboss_02texiao.xie");
            axe.configureSkill(EntityAttributes.stat(boss, Stat.ATTACK) * 1.2, boss.getStringUUID(), "飞斧", 0);
            Relations.shareEntityAllegiance(boss, axe);
            ((Mob) axe).finalizeSpawn((ServerLevel) boss.level(), boss.level().getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
            boss.level().addFreshEntity(axe);
            d.setPhase("飞掷");
        }
    }

    private static void chargedStrike(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill04");
            chargeEffects(boss, 40);
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(2.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            Skills.showEntityDamageArea(boss, new Vec3(at.getX(), boss.getY(), at.getZ()), "animation.shanghaifanwei.liuge1", 20);
            d.setDirX(targetX(boss)); d.setDirY(targetY(boss)); d.setDirZ(targetZ(boss));
            d.setAnimated(true); d.setPhase("蓄力");
        }
        faceStored(boss);
        if (tick(boss) >= 41 && d.phase().equals("劈砍")) {
            finish(boss);
            return;
        }
        if (tick(boss) >= 20 && d.phase().equals("蓄力")) {
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(2.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            Vec3 center = new Vec3(at.getX(), at.getY(), at.getZ());
            if (boss.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE, at.getX(), at.getY(), at.getZ(), 20, 0, 0, 0, 1);
                server.sendParticles(ParticleTypes.EXPLOSION, at.getX(), at.getY(), at.getZ(), 1, 0, 0, 0, 0);
            }
            for (Entity hit : nearby(boss, center, 2)) if (!Relations.isFriendly(boss, hit)) {
                damage(boss, hit, 2);
                if (!(hit instanceof LivingEntity living) || !StatusEffects.has(living, "skilleffect_08")) hit.setDeltaMovement((hit.getX() - boss.getX()) * .1, .2, (hit.getZ() - boss.getZ()) * .1);
            }
            d.setPhase("劈砍");
        }
    }

    private static void leap(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill05"); chargeEffects(boss, 8);
            boss.setDeltaMovement(boss.getLookAngle().x, .5, boss.getLookAngle().z); d.setAnimated(true);
        }
        if (tick(boss) >= 9) {
            d.setAnimated(false);
            int roll = boss.getRandom().nextInt(60) + 1;
            d.setSkill(roll <= 20 ? "力劈华山" : roll <= 40 ? "飞斧" : "八荒斩");
            d.setTick(0);
        }
    }

    private static void wheel(Boss02Entity boss, LivingEntity target) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) { boss.setAnimation("animation.ycxzboss_02.skill06"); chargeEffects(boss, 38); d.setAnimated(true); }
        if (tick(boss) < 38) {
            boss.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(1.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            for (Entity hit : nearby(boss, new Vec3(at.getX(), at.getY(), at.getZ()), 1.5)) {
                if (hit instanceof Arrow) {
                    hit.discard();
                    if (boss.level() instanceof ServerLevel server)
                        server.sendParticles(Particles.type("opticalprismlizi"), hit.getX(), hit.getY(), hit.getZ(), 5, 0, 0, 0, 0);
                }
                else if (!Relations.entityTeam(boss).equals(Relations.entityTeam(hit))) damage(boss, hit, .5);
            }
        } else {
            d.setAnimated(false); d.setTick(0);
            followUp80(boss);
        }
    }

    private static void eightDirections(Boss02Entity boss, EntityModelRow subEntityModel) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) {
            boss.setAnimation("animation.ycxzboss_02.skill07"); chargeEffects(boss, 40); d.setAnimated(true); d.setPhase("蓄力");
        }
        if (tick(boss) >= 60 || tick(boss) >= 40 && d.phase().equals("蓄力")) {
            finish(boss);
            return;
        }
        if (tick(boss) >= 20 && d.phase().equals("蓄力")) {
            for (int i = 0; i < 8; i++) {
                Boss0201Entity axe = new Boss0201Entity(CreatureSetup.<Boss0201Entity>entityType("ycxzboss_0201").get(), boss.level(), subEntityModel);
                axe.moveTo(boss.getX(), boss.getY(), boss.getZ(), i * 45, 0);
                axe.setAnimation("animation.ycxzboss_02texiao.idle");
                axe.configureSkill(EntityAttributes.stat(boss, Stat.ATTACK) * 1.2, boss.getStringUUID(), "八荒斩", i + 1);
                Relations.setTeam(axe, Relations.team(boss));
                ((Mob) axe).finalizeSpawn((ServerLevel) boss.level(), boss.level().getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                boss.level().addFreshEntity(axe);
            }
            d.setPhase("下劈");
        }
    }

    private static void bindingStrike(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss);
        if (!d.animated()) { boss.setAnimation("animation.ycxzboss_02.skill08"); chargeEffects(boss, 47); d.setAnimated(true); d.setPhase("蓄力"); }
        if (tick(boss) >= 48 && d.phase().equals("踢腿")) {
            followUp80(boss);
            return;
        }
        if (tick(boss) >= 13 && d.phase().equals("蓄力")) {
            d.setPhase("劈砍");
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(2.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            for (Entity hit : nearby(boss, new Vec3(at.getX(), at.getY(), at.getZ()), 2)) if (!Relations.entityTeam(boss).equals(Relations.entityTeam(hit))) {
                if (boss.level() instanceof ServerLevel server) {
                    server.sendParticles(Particles.type("fulutexiao_33"), hit.getX(), hit.getY() + 1, hit.getZ(), 3, .2, .2, .2, 0);
                    server.sendParticles(ParticleTypes.FLAME, hit.getX(), hit.getY() + 1, hit.getZ(), 5, .2, .5, .2, .1);
                }
                damage(boss, hit, 1.3); if (hit instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 20));
            }
            return;
        }
        if (tick(boss) >= 32 && d.phase().equals("劈砍")) {
            BlockPos at = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(2.5)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            if (boss.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.EXPLOSION, at.getX(), at.getY(), at.getZ(), 1, 0, 0, 0, 0);
            for (Entity hit : nearby(boss, new Vec3(at.getX(), at.getY(), at.getZ()), 2)) if (!Relations.entityTeam(boss).equals(Relations.entityTeam(hit))) {
                if (boss.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.FLAME, hit.getX(), hit.getY() + 1, hit.getZ(), 5, .2, .5, .2, .1);
                damage(boss, hit, 1); hit.setDeltaMovement((hit.getX() - boss.getX()), .3, (hit.getZ() - boss.getZ()));
            }
            d.setPhase("踢腿");
        }
    }

    static void tickAxe(Boss0201Entity axe) {
        if (axe.level().isClientSide) return;
        String skill = axe.skillName();
        Entity owner = null;
        if (axe.level() instanceof ServerLevel server) {
            try { owner = server.getEntity(UUID.fromString(axe.attackUuid())); }
            catch (IllegalArgumentException malformedOwner) {
                // 来源查找失败时保留无攻击者的伤害来源。
                owner = null;
            }
        }
        if (skill.equals("飞斧")) {
            axe.setDeltaMovement(axe.getLookAngle());
            hitNearby(axe, owner, 1);
            axe.setSkillTick(axe.skillTick() + 1);
            if (axe.skillTick() >= 20) {
                if (axe.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, axe.getX(), axe.getY(), axe.getZ(), 1, 0, 0, 0, 0);
                    server.sendParticles(ParticleTypes.SMOKE, axe.getX(), axe.getY(), axe.getZ(), 10, 0, 0, 0, 1);
                }
                for (Entity hit : nearby(axe, axe.position(), 2)) if (!Relations.isFriendly(axe, hit))
                    hit.hurt(axe.damageSources().source(DamageTypes.PLAYER_ATTACK, owner), (float) axe.attackDamage());
                axe.discard();
            }
        } else if (skill.equals("八荒斩")) {
            if (axe.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.SMOKE, axe.getX(), axe.getY(), axe.getZ(), 10, 0, 0, 0, 1);
            hitNearby(axe, owner, 1);
            int direction = axe.direction();
            Vec3 velocity = switch (direction) {
                case 2 -> new Vec3(.7, -.2, .7);
                case 3 -> new Vec3(0, -.2, 1);
                case 4 -> new Vec3(-.7, -.2, .7);
                case 5 -> new Vec3(-1, -.2, 0);
                case 6 -> new Vec3(-.7, -.2, -.7);
                case 7 -> new Vec3(0, -.2, -1);
                case 8 -> new Vec3(.7, -.2, -.7);
                default -> new Vec3(1, -.2, 0);
            };
            axe.setDeltaMovement(velocity);
            axe.setSkillTick(axe.skillTick() + 1);
            if (axe.skillTick() >= 20) {
                if (axe.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, axe.getX(), axe.getY(), axe.getZ(), 1, 0, 0, 0, 0);
                axe.discard();
            }
        }
    }

    private static void hitNearby(Boss0201Entity axe, Entity owner, double radius) {
        for (Entity hit : nearby(axe, axe.position(), radius)) {
            if (Relations.isFriendly(axe, hit)) continue;
            hit.hurt(axe.damageSources().source(DamageTypes.PLAYER_ATTACK, owner), (float) axe.attackDamage());
            BlockPos below = BlockPos.containing(axe.getX(), axe.getY() - 1, axe.getZ());
            axe.level().levelEvent(2001, BlockPos.containing(axe.position()), Block.getId(axe.level().getBlockState(below)));
        }
    }

    private static List<Entity> nearby(Entity owner, Vec3 center, double radius) {
        return owner.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius)).stream().sorted((a, b) -> Double.compare(a.distanceToSqr(center), b.distanceToSqr(center))).toList();
    }

    private static void damage(Entity boss, Entity hit, double multiplier) {
        hit.hurt(boss.damageSources().source(DamageTypes.PLAYER_ATTACK, boss), (float) (EntityAttributes.stat(boss, Stat.ATTACK) * multiplier));
    }

    private static void chargeEffects(Boss02Entity boss, int duration) {
        boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 20, false, false));
        boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false));
        StatusEffects.add(boss, "skilleffect_08", duration, 10);
    }

    private static void slow(Boss02Entity boss, int duration) { boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 20, false, false)); }
    private static double tick(Entity boss) { return CreatureData.state(boss).tick(); }

    static void finish(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss); d.setAnimated(false); d.setSkill(""); d.setPhase(""); d.setTick(0);
    }

    private static void followUp80(Boss02Entity boss) {
        int roll = boss.getRandom().nextInt(80) + 1;
        CreatureState data = CreatureData.state(boss);
        data.setSkill(roll <= 20 ? "力劈华山" : roll <= 40 ? "前跳" : roll <= 60 ? "飞斧" : "八荒斩");
        data.setPhase("");
        data.setAnimated(false);
        data.setTick(0);
    }

    private static void faceStored(Boss02Entity boss) {
        CreatureState d = CreatureData.state(boss); boss.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(d.dirX(), d.dirY(), d.dirZ()));
    }

    private static double targetX(Boss02Entity boss) { return boss.getTarget() == null ? boss.getX() : boss.getTarget().getX(); }
    private static double targetY(Boss02Entity boss) { return boss.getTarget() == null ? boss.getY() : boss.getTarget().getY(); }
    private static double targetZ(Boss02Entity boss) { return boss.getTarget() == null ? boss.getZ() : boss.getTarget().getZ(); }

    private static double rating(String rating, double excellent, double strong, double average, double weak, double poor) {
        return switch (rating) { case "绝佳" -> excellent; case "强劲" -> strong; case "平庸" -> average; case "勉强" -> weak; default -> poor; };
    }

    private static String levelName(double level) {
        if (level >= 50) return "化神圆满";
        int n = (int) level;
        String[] realms = {"练气", "筑基", "结丹", "元婴", "化神"};
        int realm = Mth.clamp((n - 1) / 10, 0, 4);
        int tier = (n - 1) % 10;
        String[] tiers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (realm == 0) return realms[realm] + (tier == 9 ? "圆满" : tiers[tier] + "层");
        String suffix = switch (realm) { case 1 -> "阶"; case 2 -> "转"; case 3 -> "重"; default -> "级"; };
        if (tier == 9) return realms[realm] + "圆满";
        return realms[realm] + tiers[tier] + suffix;
    }

}
