package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源体质载体中的副本字段及画卷/门票物品组件；所有写入只在副本服务端发生。 */
public final class DungeonData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModId.VALUE);
    public static final Supplier<AttachmentType<CompoundTag>> PLAYER = TYPES.register("dungeon_player", () -> AttachmentType
            .builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<ItemStack>> PAINTING_TRANSIT = TYPES.register("dungeon_painting_transit", () -> AttachmentType
            .builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).copyOnDeath().build());
    public static final Supplier<DataComponentType<PaintingState>> PAINTING_STATE = COMPONENTS.registerComponentType("painting_state",
            builder -> builder.persistent(PaintingState.CODEC).networkSynchronized(PaintingState.STREAM_CODEC));
    public static final Supplier<DataComponentType<String>> TICKET_ID = COMPONENTS.registerComponentType("dungeon_ticket",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private DungeonData() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        COMPONENTS.register(modBus);
    }
}
