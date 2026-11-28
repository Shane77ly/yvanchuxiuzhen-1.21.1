package net.yvanchuxiuzhen.system.plant;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 植物系统的实体附件；记录采藕临时计数。 */
public final class PlantData {
    private static final DeferredRegister<AttachmentType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<Double>> LOTUS_HARVEST = TYPES.register("lotus_harvest",
            () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());

    private PlantData() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
