package net.yvanchuxiuzhen.system.creature.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouShared;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import com.mojang.util.UndashedUuid;

/** 鬼火来源初始化、目标选择、靠近与普攻阶段。 */
final class Guiguai02Behavior {
    private Guiguai02Behavior() { }

    static void initialize(Guiguai02Entity entity) {
        CreatureSourceStats.initialize(entity, 1, false, false,
                "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "强筋");
    }

    static void touch(Guiguai02Entity entity, Player player) {
        entity.getPersistentData().putString("gongjizhe", player.getStringUUID());
        entity.getPersistentData().putString("jineng", "普攻");
    }

    static void hurt(Guiguai02Entity entity, Entity attacker) {
        if (attacker == null || !entity.isAlive() || Relations.isFriendly(entity, attacker)) return;
        CompoundTag data = entity.getPersistentData();
        data.putString("linshigongjizhe", attacker.getStringUUID());
        if (data.getBoolean("beidaduan")) data.putBoolean("beidaduan", false);
    }

    static void tick(Guiguai02Entity entity) {
        if (!entity.isAlive()) return;
        CompoundTag data = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            var objective = entity.level().getScoreboard().getObjective("zhaohuanwu");
            if (objective != null && entity.level().getScoreboard().getOrCreatePlayerScore(entity, objective).get() == 1) {
                double elapsed = data.getDouble("xiaoshi") + 1;
                if (elapsed >= 100) {
                    data.putString("jineng", "普攻");
                    elapsed = 0;
                }
                data.putDouble("xiaoshi", elapsed);
            }
        }
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        Vec3 center = entity.position();
        List<Entity> nearby = entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), candidate -> true)
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList();
        for (Entity candidate : nearby) {
            if (!Relations.isFriendly(entity, candidate)) {
                data.putString("linshigongjizhe", candidate.getStringUUID());
                break;
            }
        }
        if (data.getString("jineng").isEmpty() && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe")))
            data.putString("gongjizhe", data.getString("linshigongjizhe"));

        Entity target = target(entity, data.getString("gongjizhe"));
        if (target == null) {
            if (data.getDouble("entitytick") >= 40) {
                entity.getNavigation().moveTo(entity.getX() + entity.getRandom().nextInt(7) - 3, entity.getY(),
                        entity.getZ() + entity.getRandom().nextInt(7) - 3, 1);
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
        if (entity.level().isClientSide()) return;
        if (data.getString("jineng").isEmpty()) {
            chooseSkill(entity, target);
            return;
        }
        switch (data.getString("jineng")) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> attack(entity);
            default -> { }
        }
    }

    private static Entity target(Guiguai02Entity entity, String text) {
        if (text.isEmpty() || !(entity.level() instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UndashedUuid.fromStringLenient(text));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void chooseSkill(Guiguai02Entity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getString("yvdingjineng").isEmpty()) {
            if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) {
                data.putString("jineng", data.getString("yvdingjineng"));
                data.putString("yvdingjineng", "");
                data.putDouble("yvdingjinengjishi", 0);
                data.putDouble("entitytick", 0);
                clearCandidates(data);
            }
            return;
        }
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
        double distance = entity.distanceTo(target) - entity.getBbWidth() / 2 - target.getBbWidth() / 2;
        data.putString("linshijineng1", "靠近");
        data.putDouble("jinengjishi1", 10);
        data.putDouble("jinengqvanzhong1", 20);
        int count = 1;
        int weight = 20;
        if (distance <= 2) {
            count = 2;
            weight += 100;
            data.putString("linshijineng2", "普攻");
            data.putDouble("jinengjishi2", 10);
            data.putDouble("jinengqvanzhong2", 100);
        }
        data.putDouble("jinengshuliang", count);
        int roll = entity.getRandom().nextInt(weight) + 1;
        String selected = roll <= 20 ? "靠近" : "普攻";
        data.putString("yvdingjineng", selected);
        data.putDouble("yvdingjinengjishi", 10);
    }

    private static void clearCandidates(CompoundTag data) {
        for (int i = 1; i <= 2; i++) {
            data.putString("linshijineng" + i, "");
            data.putDouble("jinengjishi" + i, 0);
            data.putDouble("jinengqvanzhong" + i, 0);
        }
        data.putDouble("jinengshuliang", 0);
    }

    private static void approach(Guiguai02Entity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.putString("jinengjinxing", "移动中");
        } else if (data.getString("jinengjinxing").equals("移动中") && data.getDouble("entitytick") >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void attack(Guiguai02Entity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getString("jinengjinxing").equals("准备")) {
            data.putString("jinengjinxing", "准备");
            Entity target = target(entity, data.getString("gongjizhe"));
            if (target != null) entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
            return;
        }
        if (data.getDouble("entitytick") < 1 || !(entity.level() instanceof ServerLevel level)) return;
        level.sendParticles(Particles.type("cailiao_238particle"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                entity.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(Particles.type("brokenskyblue"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                entity.getZ(), 5, 0, 0, 0, 0.5);
        Vec3 center = entity.position();
        List<Entity> nearby = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5), candidate -> true)
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList();
        for (Entity candidate : nearby) {
            if (candidate != entity && !Relations.isFriendly(entity, candidate)) {
                level.sendParticles(Particles.type("smallexplosionskyblue"), candidate.getX(), candidate.getY() + candidate.getBbHeight() / 2,
                        candidate.getZ(), 1, 0, 0, 0, 0);
                if (candidate instanceof LivingEntity living)
                    SpellDamage.command(level, living, "shanghai 火 " + entity.getStringUUID() + " false 1 3");
            }
        }
        entity.discard();
    }
}
