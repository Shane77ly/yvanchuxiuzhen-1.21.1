package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/**
 * 境界提升的两组粒子：进阶文字非空用金色组，其余用蓝色组（来源 {@code Jingjietishengtexiao1/2}）。
 * 烟环立即发出，条光在第1至15个服务端结束刻中的奇数刻发出；延迟动作保留原维度并读取玩家当时位置。
 */
final class LevelUpEffects {
    private LevelUpEffects() {
    }

    static void emit(ServerPlayer player, boolean advancement) {
        ServerLevel level = player.serverLevel();
        SimpleParticleType smoke = Particles.type(advancement ? "smokegolden" : "smokeblue");
        SimpleParticleType primary = Particles.type(advancement ? "striporange" : "stripblue");
        SimpleParticleType secondary = Particles.type(advancement ? "stripgolden" : "striplightblue");
        double y = player.getY();
        for (int index = 0; index < 40; index++) {
            // 来源把每步9直接传入三角函数，保留弧度取值与count为0时的定向速度。
            double angle = index * 9.0;
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            level.sendParticles(smoke, player.getX() + 2 * x, y, player.getZ() + 2 * z, 0, 0.5 * x, 0, 0.5 * z, 0.5);
        }
        for (int delay = 1; delay <= 15; delay += 2) {
            Delayed.run(delay, () -> level.sendParticles(primary, player.getX(), player.getY(), player.getZ(), 10, 1, 0, 1, 0));
        }
        for (int delay = 1; delay <= 15; delay += 2) {
            Delayed.run(delay, () -> level.sendParticles(secondary, player.getX(), player.getY(), player.getZ(), 5, 0.6, 0, 0.6, 0));
        }
    }
}
