package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 天道工具与展示家具共享的锁定状态，依附被操作方块实体保存。 */
public final class BlockDisplayLock {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final Supplier<AttachmentType<Boolean>> LOCKED = TYPES.register("block_display_lock", () ->
            AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());

    private BlockDisplayLock() { }

    public static void register(IEventBus modBus) { TYPES.register(modBus); }
    public static boolean locked(BlockEntity target) { return target.getData(LOCKED); }
    public static void lock(BlockEntity target) { target.setData(LOCKED, true); }

}
