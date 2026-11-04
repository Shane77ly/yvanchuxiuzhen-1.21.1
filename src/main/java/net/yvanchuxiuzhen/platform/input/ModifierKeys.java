package net.yvanchuxiuzhen.platform.input;

import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 玩家的 Ctrl 与 Alt 按住状态（来源玩家变量 {@code lctrl}、{@code lalt}，由 Ctrl、Alt 键的按下与松开过程写入，技能、阵法、符箓、
 * 世界、装备、蛊虫读取）。客户端在状态变化时发给服务端，服务端保存；状态只存在于本次登录，不入存档、不复制。
 */
public final class ModifierKeys {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final Supplier<AttachmentType<State>> STATE = ATTACHMENTS.register("modifier_keys", () -> AttachmentType.builder(() -> new State(false, false)).build());

    private ModifierKeys() {
    }

    /** 按住状态。 */
    public record State(boolean ctrl, boolean alt) {
    }

    /** 客户端 → 服务端：当前 Ctrl 与 Alt 是否按住。 */
    public record StatePayload(boolean ctrl, boolean alt) implements CustomPacketPayload {
        public static final Type<StatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modifier_keys"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StatePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, StatePayload::ctrl, ByteBufCodecs.BOOL, StatePayload::alt, StatePayload::new);

        @Override
        public Type<StatePayload> type() {
            return TYPE;
        }

        static void handle(StatePayload payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                update(player, payload.ctrl(), payload.alt());
            }
        }
    }

    /** 服务端记下玩家的按住状态（收到客户端通知时调用）。 */
    public static void update(ServerPlayer player, boolean ctrl, boolean alt) {
        player.setData(STATE, new State(ctrl, alt));
    }

    public static boolean ctrl(Player player) {
        return player.getData(STATE).ctrl();
    }

    public static boolean alt(Player player) {
        return player.getData(STATE).alt();
    }

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) ->
                event.registrar("1").playToServer(StatePayload.TYPE, StatePayload.STREAM_CODEC, StatePayload::handle));
    }
}
