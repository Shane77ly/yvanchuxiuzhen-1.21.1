package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.particle.Particle;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 从真实粒子引擎验证同名灵气的重力差异、动画粒子到期，并留存三种原生批次的画面。 */
final class ParticleLibraryCheck extends SystemCheck {
    private final Consumer<String> shot;
    private final List<Particle> shown = new ArrayList<>();

    ParticleLibraryCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("粒子库", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
            });
            return true;
        }
        if (step == 1) {
            double x = minecraft.player.getX();
            double y = minecraft.player.getY() + 2;
            double z = minecraft.player.getZ() + 8;
            Particle still = create("lingqi", x, y, z);
            Particle rising = create("lingqi_xitong", x, y, z);
            still.tick();
            rising.tick();
            expectTrue("两种灵气经真实提供器分别保持高度与受负重力上浮", Math.abs(still.getBoundingBox().minY - y) < .000001
                    && rising.getBoundingBox().minY > y + .001);
            still.remove();
            rising.remove();
            Particle burst = create("bigbangred", x, y, z);
            for (int tick = 0; tick < 13; tick++) burst.tick();
            expectTrue("十二刻爆炸粒子按原生寿命在第十三次推进移除", !burst.isAlive());
            String[] samples = {"bigbangred", "smokelightgray", "flash_ice", "lingqi", "yaoshou_033skill_01", "xvedi"};
            for (int index = 0; index < samples.length; index++) {
                shown.add(create(samples[index], x + (index - 2.5) * 1.5, y, z));
            }
            return true;
        }
        if (local >= 2) {
            shot.accept("particle-library.png");
            shown.forEach(Particle::remove);
            finish();
            return true;
        }
        return false;
    }

    private Particle create(String id, double x, double y, double z) {
        return minecraft.particleEngine.createParticle(Particles.type(id), x, y, z, 0, 0, 0);
    }
}
