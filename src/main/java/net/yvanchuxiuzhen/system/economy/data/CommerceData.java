package net.yvanchuxiuzhen.system.economy.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.CommerceSession;

/** 玩家商品会话、商铺货币标识、物品市值与拍卖令牌/拍品组件；仅经济系统写入，服务端保存并同步。 */
public final class CommerceData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<AttachmentType<CommerceSession>> SESSION = TYPES.register("commerce_session", () -> AttachmentType
            .builder(() -> CommerceSession.EMPTY).serialize(CommerceSession.CODEC).copyOnDeath()
            .sync((holder, player) -> holder == player, CommerceSession.STREAM_CODEC).build());
    public static final Supplier<DataComponentType<String>> CURRENCY = COMPONENTS.registerComponentType("currency",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    public static final Supplier<DataComponentType<Double>> MARKET_VALUE = COMPONENTS.registerComponentType("market_value",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));
    public static final Supplier<DataComponentType<AuctionTokenData>> AUCTION_TOKEN = COMPONENTS.registerComponentType("auction_token",
            builder -> builder.persistent(AuctionTokenData.CODEC).networkSynchronized(AuctionTokenData.STREAM_CODEC));
    public static final Supplier<DataComponentType<AuctionLots>> AUCTION_LOTS = COMPONENTS.registerComponentType("auction_lots",
            builder -> builder.persistent(AuctionLots.CODEC).networkSynchronized(AuctionLots.STREAM_CODEC));

    private CommerceData() { }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        COMPONENTS.register(modBus);
    }
}
