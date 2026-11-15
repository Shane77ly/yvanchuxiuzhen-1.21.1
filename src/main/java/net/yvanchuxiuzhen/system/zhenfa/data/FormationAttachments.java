package net.yvanchuxiuzhen.system.zhenfa.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 阳二写在目标方块实体上的罡气，由阵法保存；来源没有消费该计数的路径。 */
public final class FormationAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<Integer>> GANGQI = TYPES.register("formation_gangqi", () ->
            AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    private FormationAttachments() { }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
