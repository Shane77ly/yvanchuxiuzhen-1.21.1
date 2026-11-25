package net.yvanchuxiuzhen.system.creature.entity.boss;

import java.util.Comparator;
import java.util.List;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.fulu.api.NpcTalismans;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 首领01来源受击中断、目标反击与七类技能的逐刻阶段。 */
final class Boss01Behavior {
    private static final String FIREBALL = "火球术";
    private static final String FLAME_TORRENT = "灼焰湍流";
    private static final String TELEPORT = "传送";
    private static final String FLYING_THUNDER = "飞雷符";
    private static final String THUNDER_ARRAY = "控雷阵";
    private static final String HEAL = "吃丹药";
    private static final String THUNDER_ROAR = "惊蛰闻雷";

    private Boss01Behavior() { }

    static void hurt(Boss01Entity boss) {
        if (boss.level().isClientSide || !boss.isAlive()) return;
        CreatureState state = CreatureData.state(boss);
        if (!StatusEffects.has(boss, "skilleffect_08")) {
            if (!state.skill().isEmpty()) {
                boss.setAnimation("empty");
                state.setTick(0);
                state.setSkill("");
                state.setPhase("");
                state.setTick2(0);
                state.setAnimated(false);
            }
            boss.setAnimation("animation.ycxzboss_01.attacked");
        }
    }

    static void update(Boss01Entity boss) {
        if (boss.level().isClientSide || !boss.isAlive()) return;
        CreatureState state = CreatureData.state(boss);
        state.setTick(state.tick() + 1);
        LivingEntity target = boss.getTarget();
        if (target == null) {
            if (state.tick() >= 20) state.setTick(0);
            return;
        }

        state.setTick2(state.tick2() + 1);
        if (state.tick2() >= 60) {
            repelNearby(boss);
            state.setTick2(0);
        }
        if (state.skill().isEmpty()) {
            if (state.tick() >= 60) {
                int roll = boss.getRandom().nextInt(1, 121);
                state.setSkill(roll <= 20 ? FIREBALL : roll <= 40 ? FLAME_TORRENT :
                        roll <= 60 ? TELEPORT : roll <= 70 ? FLYING_THUNDER : roll <= 90 ? THUNDER_ARRAY : roll <= 100 ? HEAL : THUNDER_ROAR);
                state.setTick(0);
            }
            return;
        }

        switch (state.skill()) {
            case FIREBALL -> fireball(boss);
            case FLAME_TORRENT -> flameTorrent(boss, target);
            case TELEPORT -> teleport(boss, target);
            case FLYING_THUNDER -> flyingThunder(boss);
            case THUNDER_ARRAY -> thunderArray(boss);
            case HEAL -> heal(boss);
            case THUNDER_ROAR -> thunderRoar(boss);
            default -> { }
        }
    }

    private static void repelNearby(Boss01Entity boss) {
        Vec3 center = boss.position();
        List<Entity> entities = boss.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2))
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        for (Entity entity : entities) {
            if (!Relations.isFriendly(boss, entity)) {
                boss.setDeltaMovement((boss.getX() - entity.getX()) * 0.4, 0.5, (boss.getZ() - entity.getZ()) * 0.4);
            }
        }
    }

    private static void fireball(Boss01Entity boss) {
        if (!animationStarted(boss, "animation.ycxzboss_01.skill01", 10)) return;
        if (tick(boss) >= 8) {
            // 46号弹体首刻读取fululevel46，非正值按1；眼部下方0.1格按视线以速度1无散布发射。
            NpcTalismans.shootBossFireball((ServerLevel) boss.level(), boss);
            finish(boss, false);
        }
    }

    private static void flameTorrent(Boss01Entity boss, LivingEntity target) {
        if (!animationStarted(boss, "animation.ycxzboss_01.skill02", 80)) return;
        if (tick(boss) >= 80) {
            finish(boss, false);
            return;
        }
        if (tick(boss) < 8) return;
        boss.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + 1, target.getZ()));
        ServerLevel level = (ServerLevel) boss.level();
        for (int i = 0; i < 5; i++) {
            Vec3 look = boss.getLookAngle();
            Vec3 direction = new Vec3(look.x * Mth.nextDouble(boss.getRandom(), 0.5, 1.5),
                    look.y * Mth.nextDouble(boss.getRandom(), 0.5, 1.5), look.z * Mth.nextDouble(boss.getRandom(), 0.5, 1.5));
            for (ServerPlayer player : level.players()) {
                level.sendParticles(player, ParticleTypes.FLAME, true, boss.getX(), boss.getY() + 1.5, boss.getZ(),
                        0, direction.x, direction.y, direction.z, 1);
            }
        }
        double distance = 1;
        for (int i = 0; i < 5; i++, distance += 2) {
            BlockPos hit = level.clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(distance)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            Vec3 center = Vec3.atLowerCornerOf(hit);
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1))
                    .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
            for (Entity entity : entities) {
                if (!Relations.isFriendly(boss, entity)) {
                    level.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 1, entity.getZ(), 5, .2, .5, .2, .1);
                    if (entity instanceof LivingEntity living) {
                        SpellDamage.command(level, living, "shanghai 火 " + boss.getStringUUID() + " false 2 1");
                    }
                }
            }
        }
    }

    private static void teleport(Boss01Entity boss, LivingEntity target) {
        if (!animationStarted(boss, "animation.ycxzboss_01.skill04", 60)) return;
        if (tick(boss) >= 60) {
            BlockPos destination = boss.level().clip(new ClipContext(target.getEyePosition(), target.getEyePosition().add(target.getViewVector(1).scale(-4)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, target)).getBlockPos();
            boss.teleportTo(destination.getX(), destination.getY(), destination.getZ());
            finish(boss, false);
        }
    }

    private static void flyingThunder(Boss01Entity boss) {
        if (!animationStarted(boss, "animation.ycxzboss_01.skill05", 40)) return;
        CreatureState state = CreatureData.state(boss);
        if (tick(boss) >= 40 && state.phase().equals("第五发")) {
            finish(boss, true);
        } else if (tick(boss) >= 32 && state.phase().equals("第四发")) {
            shoot(boss);
            state.setPhase("第五发");
        } else if (tick(boss) >= 23 && state.phase().equals("第三发")) {
            shoot(boss);
            state.setPhase("第四发");
        } else if (tick(boss) >= 19 && state.phase().equals("第二发")) {
            shoot(boss);
            state.setPhase("第三发");
        } else if (tick(boss) >= 13 && state.phase().equals("第一发")) {
            shoot(boss);
            state.setPhase("第二发");
        } else if (tick(boss) >= 6 && state.phase().isEmpty()) {
            shoot(boss);
            state.setPhase("第一发");
        }
    }

    private static void shoot(Boss01Entity boss) {
        // 45号弹体首刻读取fulu45level，非正值取mana_Att；本入口只发射，不额外写品阶。
        NpcTalismans.shootBossCannon((ServerLevel) boss.level(), boss);
    }

    private static void thunderArray(Boss01Entity boss) {
        CreatureState state = CreatureData.state(boss);
        if (!state.animated()) {
            boss.setAnimation("animation.ycxzboss_01.skill06");
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 20, false, false));
            boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0, false, false));
            boss.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 80, 10, false, false));
            state.setAnimated(true);
        }
        if (tick(boss) >= 80 && state.phase().equals("第三组")) {
            finish(boss, true);
        } else if (tick(boss) >= 48 && state.phase().equals("第二组")) {
            for (int i = 0; i < 14; i++) {
                Vec3 at = new Vec3(boss.getX() + boss.getRandom().nextInt(-8, 9), boss.getY(), boss.getZ() + boss.getRandom().nextInt(-8, 9));
                Skills.showDamageArea(boss, at, "animation.shanghaifanwei.idle", "控雷阵", 20, EntityAttributes.stat(boss, Stat.MANA_ATT) * 1.5);
            }
            state.setPhase("第三组");
        } else if (tick(boss) >= 32 && state.phase().equals("第一组")) {
            spawnRayAreas(boss, 1, 1.2);
            state.setPhase("第二组");
        } else if (tick(boss) >= 16 && state.phase().isEmpty()) {
            spawnRayAreas(boss, 1, 1);
            state.setPhase("第一组");
        }
    }

    private static void spawnRayAreas(Boss01Entity boss, double distance, double multiplier) {
        double ray = distance;
        for (int i = 0; i < 7; i++, ray += 2) {
            BlockPos hit = boss.level().clip(new ClipContext(boss.getEyePosition(), boss.getEyePosition().add(boss.getViewVector(1).scale(ray)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, boss)).getBlockPos();
            Skills.showDamageArea(boss, new Vec3(hit.getX(), boss.getY(), hit.getZ()), "animation.shanghaifanwei.idle", "控雷阵", 20,
                    EntityAttributes.stat(boss, Stat.MANA_ATT) * multiplier);
        }
    }

    private static void heal(Boss01Entity boss) {
        if (!animationStarted(boss, "animation.ycxzboss_01.skill07", 23)) return;
        if (tick(boss) >= 20) {
            ((ServerLevel) boss.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, boss.getX(), boss.getY() + 1, boss.getZ(), 10, .2, .5, .2, 0);
            boss.setHealth(boss.getHealth() + 40);
            finish(boss, false);
        }
    }

    private static void thunderRoar(Boss01Entity boss) {
        CreatureState state = CreatureData.state(boss);
        if (!state.animated()) {
            Skills.showDamageArea(boss, boss.position(), "animation.shanghaifanwei.bage2", "惊蛰闻雷", 40, 0);
            boss.setAnimation("animation.ycxzboss_01.skill08");
            boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, 56, 0, false, false));
            boss.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 56, 0, false, false));
            boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 56, 20, false, false));
            state.setAnimated(true);
        }
        if (tick(boss) < 45) return;
        ServerLevel level = (ServerLevel) boss.level();
        level.sendParticles(ParticleTypes.CLOUD, boss.getX(), boss.getY() + 1, boss.getZ(), 40, 0, 0, 0, 1);
        for (int[] offset : new int[][]{{0, 0}, {4, 4}, {4, -4}, {-4, -4}, {-4, 4}}) {
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            lightning.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(boss.getX() + offset[0], boss.getY(), boss.getZ() + offset[1])));
            lightning.setVisualOnly(true);
            level.addFreshEntity(lightning);
        }
        Vec3 center = boss.position();
        List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(4), entity -> !Relations.isFriendly(boss, entity))
                .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
        for (LivingEntity enemy : enemies) SpellDamage.command(level, enemy, "shanghai 火 " + boss.getStringUUID() + " false 10 1.2");
        finish(boss, false);
    }

    private static boolean animationStarted(Boss01Entity boss, String animation, int duration) {
        CreatureState state = CreatureData.state(boss);
        if (!state.animated()) {
            boss.setAnimation(animation);
            if (animation.equals("animation.ycxzboss_01.skill01") || animation.equals("animation.ycxzboss_01.skill07")) {
                boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 20, false, false));
            } else if (animation.equals("animation.ycxzboss_01.skill02") || animation.equals("animation.ycxzboss_01.skill05")
                    || animation.equals("animation.ycxzboss_01.skill06")) {
                boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 20, false, false));
                boss.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, false));
                boss.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), duration, 10, false, false));
            } else if (animation.equals("animation.ycxzboss_01.skill04")) {
                boss.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 20, false, false));
            }
            state.setAnimated(true);
        }
        return true;
    }

    private static double tick(Boss01Entity boss) {
        return CreatureData.state(boss).tick();
    }

    private static void finish(Boss01Entity boss, boolean clearPhase) {
        CreatureState state = CreatureData.state(boss);
        state.setAnimated(false);
        state.setSkill("");
        state.setTick(0);
        if (clearPhase) state.setPhase("");
    }
}
