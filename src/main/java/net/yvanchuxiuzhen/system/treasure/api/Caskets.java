package net.yvanchuxiuzhen.system.treasure.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 宝匣奖励的公开构造入口，配置指令随物品放置后交给宝匣执行。 */
public final class Caskets {
    private Caskets() {
    }

    public static ItemStack create(String command) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:baoxiaitem")));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("zhiling", command));
        return stack;
    }
}
