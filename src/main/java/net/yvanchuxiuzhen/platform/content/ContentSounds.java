package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 声音事件：{@code content/platform/sounds.json} 的每一行登记一个可变范围的声音事件（与来源的
 * {@code new SoundEvent(位置)} 一致）；声音文件、类别与字幕由 {@code src/main/sounds/platform.json} 声明。
 */
public final class ContentSounds {
    private record SoundRow(String id) {
        static final Codec<SoundRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(SoundRow::id)
        ).apply(instance, SoundRow::new));
    }

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ModId.VALUE);

    private ContentSounds() {
    }

    public static void register(IEventBus modBus) {
        for (SoundRow row : ContentFiles.read("content/platform/sounds.json", SoundRow.CODEC)) {
            SOUNDS.register(row.id(), () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, row.id())));
        }
        SOUNDS.register(modBus);
    }
}
