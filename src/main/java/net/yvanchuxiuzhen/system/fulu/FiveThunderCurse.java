package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 惊庭五雷符的命中标记和五轮落雷；阶段、计数与施放者身份随受击实体保存。 */
public final class FiveThunderCurse implements EffectBehavior {
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulutexiao_41");

    /** 来源只比较实体team字段；玩家档案团队不代替本体字段，重施也不清既有阶段。 */
    public static void hit(ServerLevel level, Entity projectile, Entity target, double grade) {
        if (Relations.entityTeam(target).equals(Relations.entityTeam(projectile))) {
            return;
        }
        level.sendParticles(Particles.type("cailiao_229particle"), target.getX(), target.getY() + 1, target.getZ(), 10, 0, 0, 0, 1);
        CompoundTag data = target.getPersistentData();
        data.putString("belongToMasterUUID", projectile.getPersistentData().getString("belongToMasterUUID"));
        data.putString("belongToMasterteam", Relations.entityTeam(projectile));
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(effect(), 2400, (int) (grade - 1)));
        }
    }

    @Override
    public boolean tick(LivingEntity entity, int amplifier) {
        ServerLevel level = (ServerLevel) entity.level();
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("fulu41jindu");
        double tick = data.getDouble("fulu41tick") + 1;
        data.putDouble("fulu41tick", tick);
        int interval = switch (phase) {
            case "二雷", "四雷" -> 5;
            case "三雷" -> 20;
            case "五雷" -> 40;
            default -> 10;
        };
        if (tick < interval) {
            return true;
        }
        double count = data.getDouble("fulu41num");
        double grade = amplifier + 1;
        switch (phase) {
            case "五雷" -> strike(level, entity, randomPosition(level, entity), grade * 20, 2, "金色");
            case "四雷" -> strike(level, entity, randomPosition(level, entity), grade * 8, 2, "蓝色");
            case "三雷" -> {
                double offset = 10 - count * 2;
                strike(level, entity, entity.position().add(offset, 0, offset), grade * 10, 3, "绿色");
                strike(level, entity, entity.position().add(offset, 0, -offset), grade * 10, 3, "绿色");
                strike(level, entity, entity.position().add(-offset, 0, -offset), grade * 10, 3, "绿色");
                strike(level, entity, entity.position().add(-offset, 0, offset), grade * 10, 3, "绿色");
            }
            case "二雷" -> strike(level, entity, randomPosition(level, entity), grade * 10, 2, "红色");
            default -> strike(level, entity, entity.position(), grade * 4, 2, "白色");
        }
        count++;
        data.putDouble("fulu41num", count);
        data.putDouble("fulu41tick", 0);
        int total = switch (phase) {
            case "二雷" -> 16;
            case "三雷" -> 6;
            case "四雷" -> 24;
            case "五雷" -> 9;
            default -> 10;
        };
        if (count < total) {
            return true;
        }
        data.putDouble("fulu41num", 0);
        data.putString("fulu41jindu", switch (phase) {
            case "二雷" -> "三雷";
            case "三雷" -> "四雷";
            case "四雷" -> "五雷";
            case "五雷" -> "";
            default -> "二雷";
        });
        return !phase.equals("五雷");
    }

    private static Vec3 randomPosition(ServerLevel level, LivingEntity entity) {
        return entity.position().add(Mth.nextInt(level.random, -6, 6), 0, Mth.nextInt(level.random, -6, 6));
    }

    private static void strike(ServerLevel level, LivingEntity entity, Vec3 at, double damage, int diameter, String color) {
        CompoundTag data = entity.getPersistentData();
        Lightning.command(level, at, "lightninglanding " + damage + " " + data.getString("belongToMasterUUID")
                + " 1 " + data.getString("belongToMasterteam") + " " + diameter + " " + color + " 独立");
    }

    private static Holder<MobEffect> effect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(EFFECT).orElseThrow();
    }
}
