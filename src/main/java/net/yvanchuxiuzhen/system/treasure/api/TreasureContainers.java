package net.yvanchuxiuzhen.system.treasure.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 奖励与书籍生产者通过藏宝系统读写可消费的宝箱/书架物品配置组件。 */
public final class TreasureContainers {
    private TreasureContainers() {
    }

    public static TreasureContainerConfig config(ItemStack stack) {
        return stack.getOrDefault(TreasureData.CONTAINER.get(), TreasureContainerConfig.EMPTY);
    }

    public static void setConfig(ItemStack stack, TreasureContainerConfig config) {
        if (config == null || !config.isPresent() && config.level() == 0 && config.minimum() == 0 && config.maximum() == 0
                && config.subtype().isEmpty()) {
            stack.remove(TreasureData.CONTAINER.get());
        } else {
            stack.set(TreasureData.CONTAINER.get(), config);
        }
    }

    public static ItemStack create(Item carrier, String category, String subtype, double level, double minimum, double maximum) {
        ItemStack stack = new ItemStack(carrier);
        stack.set(TreasureData.CONTAINER.get(), new TreasureContainerConfig(category, subtype, level, minimum, maximum));
        return stack;
    }
}
