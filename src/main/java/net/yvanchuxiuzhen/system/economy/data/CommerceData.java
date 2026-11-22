package net.yvanchuxiuzhen.system.economy.data;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 玩家商品会话与价格显示；仅经济系统写入，服务端保存并向本人同步。 */
public final class CommerceData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<CompoundTag>> SESSION = TYPES.register("commerce_session", () -> AttachmentType
            .builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.COMPOUND_TAG).build());

    private CommerceData() { }

    public static void register(IEventBus modBus) { TYPES.register(modBus); }
}
