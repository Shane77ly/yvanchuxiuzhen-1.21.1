package net.yvanchuxiuzhen.system.cooking.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 酒精数值由烹饪保存、同步给本人，并按来源在所有玩家克隆中保留。 */
public final class CookingAttachments {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<Double>> ALCOHOL = TYPES.register("alcohol", () -> AttachmentType
            .builder(() -> 0.0).serialize(Codec.DOUBLE).copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.DOUBLE).build());

    private CookingAttachments() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
