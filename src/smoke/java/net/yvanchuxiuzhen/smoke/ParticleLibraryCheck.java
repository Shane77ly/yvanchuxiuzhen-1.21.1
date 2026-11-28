package net.yvanchuxiuzhen.smoke;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 从真实粒子引擎验证同名灵气的重力差异、动画粒子到期，并留存三种原生批次的画面。 */
final class ParticleLibraryCheck extends SystemCheck {
    private final Consumer<String> shot;
    private final List<Particle> shown = new ArrayList<>();
    private final List<ClientboundLevelParticlesPacket> packets = new CopyOnWriteArrayList<>();

    ParticleLibraryCheck(List<String> report, String levelId, Consumer<String> shot) {
        super("粒子库", report, levelId);
        this.shot = shot;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            minecraft.getConnection().getConnection().channel().pipeline().addBefore("packet_handler", "directed_particle_probe", new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                    if (message instanceof ClientboundLevelParticlesPacket packet && packet.getParticle() == ParticleTypes.CLOUD
                            && packet.getCount() == 0 && packet.getZDist() == -.375f) packets.add(packet);
                    super.channelRead(context, message);
                }
            });
            onServer(player -> {
                Arena.level(player);
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), 0, 0);
                var level = player.serverLevel();
                double x = player.getBlockX() + .5, y = player.getBlockY() + .5, z = player.getBlockZ() + .5;
                Particles.directed(level, ParticleTypes.CLOUD, x, y, z, .25, .5, -.375, .75);
                Particles.directed(level, ParticleTypes.CLOUD, x + 64, y, z, .25, .5, -.375, .75);
                Particles.directed(level, ParticleTypes.CLOUD, x, y, z, 1e-5, .5, -.375, .75);
                Particles.distantCloud(level, new ArmorStand(level, x + 64, y, z), .25, .5, -.375);
            });
            return true;
        }
        if (step == 1) {
            if (packets.size() < 2 && local < 80) return false;
            minecraft.getConnection().getConnection().channel().pipeline().remove("directed_particle_probe");
            expect("定向云雾仅近距普通和远距强制两个包", 2, packets.size());
            if (packets.size() == 2) {
                var near = packets.get(0);
                var far = packets.get(1);
                expectTrue("远距包使用强制标志并位于发出实体处", !near.isOverrideLimiter() && far.isOverrideLimiter()
                        && far.getX() - near.getX() == 64 && far.getY() > near.getY() && near.getZ() == far.getZ());
                expectTrue("零计数粒子的方向和速度按原生包传递", near.getXDist() == .25f && near.getYDist() == .5f
                        && far.getXDist() == .25f && far.getYDist() == .5f && near.getMaxSpeed() == .75f && far.getMaxSpeed() == 1);
            }
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
