package net.yvanchuxiuzhen.system.particle.api;

import net.minecraft.core.particles.SimpleParticleType;
import net.yvanchuxiuzhen.system.particle.ParticleSetup;

/** 各系统以目标 ID 查询粒子类型；服务端广播与客户端本地生成仍使用原生入口。 */
public final class Particles {
    private Particles() {
    }

    public static SimpleParticleType type(String id) {
        return ParticleSetup.type(id);
    }
}
