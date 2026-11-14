package net.yvanchuxiuzhen.system.dungeon.data;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源体质载体中的副本字段独立保存；所有写入只在副本服务端发生。 */
public final class DungeonData {
    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModId.VALUE);
    public static final Supplier<AttachmentType<CompoundTag>> PLAYER = TYPES.register("dungeon_player", () -> AttachmentType
            .builder(() -> new CompoundTag()).serialize(CompoundTag.CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<ItemStack>> PAINTING_TRANSIT = TYPES.register("dungeon_painting_transit", () -> AttachmentType
            .builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC).copyOnDeath().build());

    private DungeonData() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
