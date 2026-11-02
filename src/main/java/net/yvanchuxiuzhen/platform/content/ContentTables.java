package net.yvanchuxiuzhen.platform.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/**
 * 一个系统或内容族的内容表加载器：读取 {@code content/<单位>/blocks.json} 与 {@code items.json}，
 * 解码后登记到本单位的 DeferredRegister，并把带创造栏的物品交给 {@link ContentTabs}。
 */
public final class ContentTables {
    private final String unit;
    private final DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(ModId.VALUE);
    private final DeferredRegister.Items items = DeferredRegister.createItems(ModId.VALUE);

    public ContentTables(String unit) {
        this.unit = unit;
    }

    public void register(IEventBus modBus) {
        Map<String, DeferredBlock<Block>> registered = new HashMap<>();
        for (BlockRow row : ContentFiles.read("content/" + unit + "/blocks.json", BlockRow.CODEC)) {
            registered.put(row.id(), blocks.register(row.id(), () -> ContentBlocks.create(row)));
        }
        for (ItemRow row : ContentFiles.read("content/" + unit + "/items.json", ItemRow.CODEC)) {
            registerItem(row, registered);
        }
        blocks.register(modBus);
        items.register(modBus);
    }

    private void registerItem(ItemRow row, Map<String, DeferredBlock<Block>> registered) {
        List<Component> tooltip = tooltip(row);
        DeferredItem<? extends Item> holder = switch (row.factory()) {
            case "item" -> items.register(row.id(), () -> new ContentItem(properties(row), tooltip));
            case "block_item" -> {
                DeferredBlock<Block> block = registered.get(row.block().orElseThrow());
                if (block == null) {
                    throw new IllegalStateException("方块物品 " + row.id() + " 的方块 " + row.block().get() + " 不在单位 " + unit + " 的方块表中");
                }
                yield row.doubleHigh().orElse(false)
                        ? items.register(row.id(), () -> new ContentDoubleBlockItem(block.get(), properties(row), tooltip))
                        : items.register(row.id(), () -> new ContentBlockItem(block.get(), properties(row), tooltip));
            }
            default -> throw new IllegalStateException("未知物品工厂 " + row.factory() + "：" + row.id());
        };
        row.tab().ifPresent(tab -> ContentTabs.add(tab, row.order(), holder));
    }

    private static List<Component> tooltip(ItemRow row) {
        int count = row.tooltip().map(List::size).orElse(0);
        return IntStream.rangeClosed(1, count)
                .<Component>mapToObj(number -> Component.translatable("tooltip." + ModId.VALUE + ".item." + row.id() + "." + number))
                .toList();
    }

    private static Item.Properties properties(ItemRow row) {
        Item.Properties properties = new Item.Properties();
        row.stack().ifPresent(properties::stacksTo);
        row.rarity().ifPresent(rarity -> properties.rarity(Rarity.valueOf(rarity)));
        if (row.fireResistant().orElse(false)) {
            properties.fireResistant();
        }
        row.durability().ifPresent(properties::durability);
        row.food().ifPresent(food -> properties.food(new FoodProperties(food.nutrition(),
                FoodConstants.saturationByModifier(food.nutrition(), food.saturationMod()), food.alwaysEat().orElse(false),
                food.useTicks().map(ticks -> ticks / 20.0F).orElse(1.6F), Optional.empty(), List.of())));
        return properties;
    }
}
