package net.yvanchuxiuzhen.system.storage.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;

/** 材料仓随玩家持久化、死亡复制，只向持有者同步；袋06不持有这份库存。 */
public final class StorageData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<MaterialWarehouse>> WAREHOUSE = ATTACHMENTS.register("material_warehouse", () -> AttachmentType
            .builder(() -> MaterialWarehouse.EMPTY).serialize(MaterialWarehouse.CODEC).copyOnDeath()
            .sync((holder, player) -> holder == player, ByteBufCodecs.fromCodec(MaterialWarehouse.CODEC)).build());
    /** 来源 guijishiqi 随玩家保存，跨菜单累计筛选比对的二十刻周期。 */
    public static final Supplier<AttachmentType<Integer>> FILTER_TICKS = ATTACHMENTS.register("material_filter_ticks", () -> AttachmentType
            .builder(() -> 0).serialize(Codec.INT).copyOnDeath().build());

    private StorageData() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
