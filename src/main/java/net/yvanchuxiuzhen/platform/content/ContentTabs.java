package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 创造栏：自定义栏由 {@code content/platform/tabs.json} 登记，物品按来源注册顺序排列；
 * 物品行的 tab 为原版栏（带命名空间的 ID）时，在对应栏的内容事件中追加。
 */
public final class ContentTabs {
    private record TabRow(String id, String icon) {
        static final Codec<TabRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(TabRow::id),
                Codec.STRING.fieldOf("icon").forGetter(TabRow::icon)
        ).apply(instance, TabRow::new));
    }

    private record Entry(int order, Supplier<? extends ItemLike> item) {
    }

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModId.VALUE);
    private static final Map<String, List<Entry>> ENTRIES = new HashMap<>();

    private ContentTabs() {
    }

    static void add(String tab, int order, Supplier<? extends ItemLike> item) {
        ENTRIES.computeIfAbsent(tab, key -> new ArrayList<>()).add(new Entry(order, item));
    }

    public static void register(IEventBus modBus) {
        for (TabRow row : ContentFiles.read("content/platform/tabs.json", TabRow.CODEC)) {
            String key = ModId.VALUE + ":" + row.id();
            TABS.register(row.id(), () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ModId.VALUE + "." + row.id()))
                    .icon(() -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(row.icon()))))
                    .displayItems((parameters, output) -> sorted(key).forEach(entry -> output.accept(entry.item().get())))
                    .build());
        }
        TABS.register(modBus);
        modBus.addListener(ContentTabs::addToVanillaTabs);
    }

    private static void addToVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        sorted(event.getTabKey().location().toString()).forEach(entry -> event.accept(entry.item().get()));
    }

    private static List<Entry> sorted(String tab) {
        return ENTRIES.getOrDefault(tab, List.of()).stream().sorted(Comparator.comparingInt(Entry::order)).toList();
    }
}
