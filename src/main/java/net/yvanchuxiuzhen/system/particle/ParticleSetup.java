package net.yvanchuxiuzhen.system.particle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 粒子库的公共登记；只保存类型与只读定义，不接触客户端提供器。 */
public final class ParticleSetup {
    private static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, ModId.VALUE);
    private static final List<ParticleRow> ROWS = List.copyOf(ContentFiles.read("content/particle/particles.json", ParticleRow.CODEC));
    private static final Map<String, Supplier<SimpleParticleType>> TYPES = new HashMap<>();

    private ParticleSetup() {
    }

    public static void register(IEventBus modBus) {
        for (ParticleRow row : ROWS) {
            TYPES.put(row.id(), REGISTRY.register(row.id(), () -> new SimpleParticleType(row.alwaysShow())));
        }
        REGISTRY.register(modBus);
    }

    public static List<ParticleRow> definitions() {
        return ROWS;
    }

    public static SimpleParticleType type(String id) {
        return TYPES.get(id).get();
    }
}
