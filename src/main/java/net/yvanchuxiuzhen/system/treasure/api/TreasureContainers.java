package net.yvanchuxiuzhen.system.treasure.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 奖励生产者通过藏宝系统构造可消费的宝箱物品参数。 */
public final class TreasureContainers {
    private TreasureContainers() {
    }

    public static ItemStack create(Item carrier, String category, String subtype, double level, double minimum, double maximum) {
        ItemStack stack = new ItemStack(carrier);
        CompoundTag data = new CompoundTag();
        data.putString("TreasureTypeBig", category);
        data.putString("TreasureTypeSmall", subtype);
        data.putDouble("TreasureLEVEL", level);
        data.putDouble("minimumvalue", minimum);
        data.putDouble("Maximumvalue", maximum);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }
}
