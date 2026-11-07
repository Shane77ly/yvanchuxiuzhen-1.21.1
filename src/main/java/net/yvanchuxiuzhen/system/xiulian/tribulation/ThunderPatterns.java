package net.yvanchuxiuzhen.system.xiulian.tribulation;

import java.text.DecimalFormat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.Delayed;
import net.yvanchuxiuzhen.system.xiulian.XiulianSetup;
import net.yvanchuxiuzhen.system.xiulian.api.TribulationLightningEvent;

/** 七组天雷的前摇、放雷间隔、次数和实体生成（来源 Tianjie01、Leijie0101–0107）。延迟粒子在执行时读取云的位置。 */
final class ThunderPatterns {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");
    private static final DecimalFormat ATTACK = new DecimalFormat("");

    private ThunderPatterns() { }

    static String current(TribulationCloud cloud) {
        return cloud.getPersistentData().getString("thunder" + WHOLE.format(cloud.getPersistentData().getDouble("thundernum")));
    }

    static void tick(TribulationCloud cloud) {
        switch (current(cloud)) {
            case "雷1" -> flashes(cloud);
            case "雷2" -> skyBlue(cloud);
            case "雷3" -> rolling(cloud);
            case "雷4" -> fiveBolts(cloud);
            case "雷5" -> pillar(cloud);
            case "雷6" -> purple(cloud);
            case "雷7" -> dragon(cloud);
            default -> { }
        }
    }

    private static boolean ready(TribulationCloud cloud, int windup) {
        CompoundTag data = cloud.getPersistentData();
        if (data.getDouble("qianyao") <= windup) {
            increment(data, "qianyao");
            return false;
        }
        return true;
    }

    private static void announce(TribulationCloud cloud, String name) {
        ServerPlayer player = Tribulations.player(cloud);
        if (player != null) {
            player.displayClientMessage(Component.literal("第" + WHOLE.format(cloud.getPersistentData().getDouble("thundernum")) + "组天劫：" + name), false);
        }
    }

    private static boolean initialize(TribulationCloud cloud, String name, int count) {
        if (cloud.getPersistentData().getDouble("number") >= 1) return false;
        announce(cloud, name);
        cloud.getPersistentData().putDouble("number", count);
        return true;
    }

    private static double increment(CompoundTag data, String key) {
        double value = data.getDouble(key) + 1;
        data.putDouble(key, value);
        return value;
    }

    private static void advance(TribulationCloud cloud) {
        CompoundTag data = cloud.getPersistentData();
        increment(data, "thundernum");
        data.putDouble("qianyao", 0);
        data.putDouble("number", 0);
    }

    private static void used(TribulationCloud cloud) {
        CompoundTag data = cloud.getPersistentData();
        data.putDouble("number", data.getDouble("number") - 1);
        data.putDouble("leipitick", 0);
        if (data.getDouble("number") < 1) advance(cloud);
    }

    private static Vec3 ground(TribulationCloud cloud) { return cloud.position().add(0, -64, 0); }

    private static void strike(TribulationCloud cloud, Vec3 at, double attackFactor, String multiplier, int diameter, String color) {
        NeoForge.EVENT_BUS.post(new TribulationLightningEvent((ServerLevel) cloud.level(), at, "lightninglanding "
                + ATTACK.format(cloud.getPersistentData().getDouble("thunder_Att") * attackFactor) + " 无 " + multiplier + " 天劫 " + diameter + " " + color));
    }

    private static void particle(TribulationCloud cloud, String id, Vec3 at, int count, double spread) {
        Tribulations.particles(cloud, id, at, count, spread, 0, spread, 0);
    }

    private static void flashes(TribulationCloud cloud) {
        if (!ready(cloud, 60)) return;
        initialize(cloud, "瞬雷连闪", 9);
        if (increment(cloud.getPersistentData(), "leipitick") >= 15) {
            strike(cloud, ground(cloud), 1, "2", 4, "白色");
            particle(cloud, "thunderwhite", ground(cloud), 2, 2);
            particle(cloud, "thunderlightgray", ground(cloud), 2, 2);
            used(cloud);
        }
    }

    private static void skyBlue(TribulationCloud cloud) {
        if (!ready(cloud, 200)) return;
        CompoundTag data = cloud.getPersistentData();
        if (initialize(cloud, "青雷", 24)) data.putDouble("leipitick2", 0);
        if (increment(data, "leipitick") >= 4) {
            Vec3 at = ground(cloud).add(cloud.getRandom().nextInt(17) - 8, 0, cloud.getRandom().nextInt(17) - 8);
            strike(cloud, at, 0.8, "1", 4, "天蓝");
            particle(cloud, "thunderskyblue", at, 4, 2);
            used(cloud);
        }
        if (increment(data, "leipitick2") >= 30) {
            strike(cloud, ground(cloud), 1.2, "1", 4, "天蓝");
            particle(cloud, "thunderskyblue", ground(cloud), 4, 3);
            for (int delay = 5; delay <= 20; delay += 5) {
                Delayed.run(delay, () -> particle(cloud, "thunderskyblue", ground(cloud), 4, 3));
            }
            data.putDouble("leipitick2", 0);
        }
    }

    private static void prepare(TribulationCloud cloud, TribulationEntity child, Vec3 at) {
        child.moveTo(at.x, at.y, at.z, cloud.getRandom().nextFloat() * 360, 0);
        Relations.copyAllegiance(cloud, child);
        child.getPersistentData().putDouble("thunder_Att", cloud.getPersistentData().getDouble("thunder_Att"));
    }

    private static void rolling(TribulationCloud cloud) {
        if (!ready(cloud, 100)) return;
        initialize(cloud, "地滚雷", 16);
        if (increment(cloud.getPersistentData(), "leipitick") >= 10) {
            ServerLevel level = (ServerLevel) cloud.level();
            RollingThunder child = XiulianSetup.<RollingThunder>entityType("tianjie_03").get().create(level);
            Vec3 center = ground(cloud);
            prepare(cloud, child, center.add(cloud.getRandom().nextInt(21) - 10, 12, cloud.getRandom().nextInt(21) - 10));
            child.getPersistentData().putDouble("mubiaoX", center.x);
            child.getPersistentData().putDouble("mubiaoY", center.y);
            child.getPersistentData().putDouble("mubiaoZ", center.z);
            child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(child);
            used(cloud);
        }
    }

    private static void fiveBolts(TribulationCloud cloud) {
        if (!ready(cloud, 200)) return;
        initialize(cloud, "五雷轰顶", 5);
        if (increment(cloud.getPersistentData(), "leipitick") >= 40) {
            strike(cloud, ground(cloud), 1, "3", 4, "金色");
            particle(cloud, "thundergolden", ground(cloud), 2, 1);
            for (int delay = 1; delay <= 16; delay += 5) {
                Delayed.run(delay, () -> particle(cloud, "thundergolden", ground(cloud), 2, 1));
            }
            for (int dx : new int[] {2, -2}) {
                for (int dz : new int[] {2, -2}) {
                    strike(cloud, ground(cloud).add(dx, 0, dz), 1, "2", 2, "金色");
                    particle(cloud, "thundergolden", ground(cloud).add(dx, 0, dz), 2, 1);
                    Delayed.run(5, () -> particle(cloud, "thundergolden", ground(cloud).add(dx, 0, dz), 2, 1));
                }
            }
            used(cloud);
        }
    }

    private static void pillar(TribulationCloud cloud) {
        if (!ready(cloud, 200)) return;
        if (cloud.getPersistentData().getDouble("number") < 1) {
            announce(cloud, "九霄雷柱");
            ServerLevel level = (ServerLevel) cloud.level();
            ThunderPillar child = XiulianSetup.<ThunderPillar>entityType("tianjie_05").get().create(level);
            prepare(cloud, child, ground(cloud));
            child.getPersistentData().putString("playerUUID", cloud.getPersistentData().getString("playerUUID"));
            child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(child);
            cloud.getPersistentData().putDouble("number", 9);
        }
        if (increment(cloud.getPersistentData(), "leipitick") >= 320) {
            advance(cloud);
            cloud.getPersistentData().putDouble("leipitick", 0);
        }
    }

    private static void purple(TribulationCloud cloud) {
        if (!ready(cloud, 160)) return;
        initialize(cloud, "紫金神雷", 9);
        if (increment(cloud.getPersistentData(), "leipitick") >= 20) {
            strike(cloud, ground(cloud), 1, "1", 4, "紫色");
            purpleParticles(cloud);
            for (int delay = 1; delay <= 16; delay += 5) Delayed.run(delay, () -> purpleParticles(cloud));
            used(cloud);
        }
    }

    private static void purpleParticles(TribulationCloud cloud) {
        particle(cloud, "thunderpurple", ground(cloud), 2, 2);
        particle(cloud, "thundergolden", ground(cloud), 2, 2);
    }

    private static void dragon(TribulationCloud cloud) {
        CompoundTag data = cloud.getPersistentData();
        double windup = increment(data, "qianyao");
        if (windup >= 200 && !data.getBoolean("jieduan")) {
            announce(cloud, "化灵雷龙");
            ServerLevel level = (ServerLevel) cloud.level();
            ThunderDragon child = XiulianSetup.<ThunderDragon>entityType("tianjie_07").get().create(level);
            prepare(cloud, child, cloud.position().add(0, -36, 0));
            child.getPersistentData().putString("playerUUID", data.getString("playerUUID"));
            child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(child);
            data.putBoolean("jieduan", true);
            data.putDouble("qianyao", 0);
        } else if (windup >= 100 && data.getBoolean("jieduan")) {
            data.putBoolean("jieduan", false);
            advance(cloud);
        }
    }
}
