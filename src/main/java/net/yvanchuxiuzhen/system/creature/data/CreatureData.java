package net.yvanchuxiuzhen.system.creature.data;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBagData;

/** 生物系统的强类型物品数据组件与实体附件。 */
public final class CreatureData {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<DataComponentType<SpiritBeastBagData>> SPIRIT_BEAST_BAG = COMPONENTS.registerComponentType(
            "spirit_beast_bag", builder -> builder.persistent(SpiritBeastBagData.CODEC)
                    .networkSynchronized(SpiritBeastBagData.STREAM_CODEC));
    public static final Supplier<DataComponentType<Double>> TAME_VALUE = COMPONENTS.registerComponentType(
            "tame_value", builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));
    public static final Supplier<AttachmentType<Boolean>> BOUNTY_ENTITY = ATTACHMENTS.register(
            "bounty_entity", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<Boolean>> DUNGEON_ENTITY = ATTACHMENTS.register(
            "dungeon_entity", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL).build());
    public static final Supplier<AttachmentType<CreatureState>> STATE = ATTACHMENTS.register(
            "state", () -> AttachmentType.builder(CreatureState::new).serialize(CreatureState.CODEC).build());
    public static final Supplier<AttachmentType<Double>> BLACK_MARSH_TICK = ATTACHMENTS.register(
            "black_marsh_tick", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());
    public static final Supplier<AttachmentType<Double>> THUNDERSTORM_TICK = ATTACHMENTS.register(
            "thunderstorm_tick", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());
    public static final Supplier<AttachmentType<Double>> BLIZZARD_TICK = ATTACHMENTS.register(
            "blizzard_tick", () -> AttachmentType.builder(() -> 0.0).serialize(Codec.DOUBLE).build());

    private CreatureData() {
    }

    public static CreatureState state(Entity entity) {
        return entity.getData(STATE);
    }

    public static boolean hasState(Entity entity) {
        return entity.hasData(STATE);
    }

    public static Optional<CreatureState> existingState(Entity entity) {
        return entity.getExistingData(STATE);
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
        ATTACHMENTS.register(modBus);
    }
}
