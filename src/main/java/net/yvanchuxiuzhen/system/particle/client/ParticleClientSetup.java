package net.yvanchuxiuzhen.system.particle.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.yvanchuxiuzhen.system.particle.ParticleRow;
import net.yvanchuxiuzhen.system.particle.ParticleSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 客户端粒子提供器登记；SpriteSet 随原生粒子引擎的资源重载更新。 */
public final class ParticleClientSetup {
    private ParticleClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ParticleClientSetup::providers);
    }

    private static void providers(RegisterParticleProvidersEvent event) {
        for (ParticleRow row : ParticleSetup.definitions()) {
            event.registerSpriteSet(Particles.type(row.id()), sprites -> (type, level, x, y, z, vx, vy, vz) ->
                    new LibraryParticle(level, x, y, z, vx, vy, vz, row, sprites));
        }
    }
}
