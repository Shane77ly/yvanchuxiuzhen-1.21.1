package net.yvanchuxiuzhen.system.particle.api;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.particle.ParticleSetup;

/** 各系统以目标 ID 查询粒子类型；服务端广播与客户端本地生成仍使用原生入口。 */
public final class Particles {
    private Particles() {
    }

    public static SimpleParticleType type(String id) {
        return ParticleSetup.type(id);
    }

    /** 原命令中的绝对坐标与方向由 double 拼成十进制；Brigadier 不接受指数形式。count=0 表示一颗定向粒子。 */
    public static void directed(ServerLevel level, SimpleParticleType type, double x, double y, double z,
            double dx, double dy, double dz, double speed) {
        if (decimal(x) && decimal(y) && decimal(z) && decimal(dx) && decimal(dy) && decimal(dz)) {
            level.sendParticles(type, x, y, z, 0, dx, dy, dz, speed);
        }
    }

    /** 云雾以实体半高为相对原点，只有方向经过命令数值解析；远距包由原版按观察者筛选。 */
    public static void distantCloud(ServerLevel level, Entity entity, double dx, double dy, double dz) {
        if (!decimal(dx) || !decimal(dy) || !decimal(dz)) return;
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, ParticleTypes.CLOUD, true, entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(), 0, dx, dy, dz, 1);
        }
    }

    static boolean decimal(double value) {
        double magnitude = Math.abs(value);
        return magnitude == 0 || magnitude >= 0.001 && magnitude < 1e7;
    }
}
