package net.yvanchuxiuzhen.system.treasure.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 宝匣奖励的公开构造与查询入口，配置指令随物品放置后交给宝匣执行。 */
public final class Caskets {
    private Caskets() {
    }

    public static String command(ItemStack stack) {
        return stack.getOrDefault(TreasureData.CASKET_COMMAND.get(), CasketCommand.EMPTY).command();
    }

    public static ItemStack create(String command) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:baoxiaitem")));
        stack.set(TreasureData.CASKET_COMMAND.get(), new CasketCommand(command));
        return stack;
    }
}
