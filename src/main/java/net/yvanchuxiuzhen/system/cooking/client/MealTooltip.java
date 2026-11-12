package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 餐食类别提示按来源四标签的互斥顺序追加金色中文文本。 */
public final class MealTooltip {
    private static final TagKey<Item> STAPLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhushi"));
    private static final TagKey<Item> DISH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "caipin"));
    private static final TagKey<Item> SOUP = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tangshui"));
    private static final TagKey<Item> TEA = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chadian"));

    private MealTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        ItemStack stack = event.getItemStack();
        if (stack.is(STAPLE)) {
            event.getToolTip().add(Component.literal("主食").withStyle(ChatFormatting.GOLD));
        } else if (stack.is(DISH)) {
            event.getToolTip().add(Component.literal("菜品").withStyle(ChatFormatting.GOLD));
        } else if (stack.is(SOUP)) {
            event.getToolTip().add(Component.literal("汤水").withStyle(ChatFormatting.GOLD));
        } else if (stack.is(TEA)) {
            event.getToolTip().add(Component.literal("茶点").withStyle(ChatFormatting.GOLD));
        }
    }
}
