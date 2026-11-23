package net.yvanchuxiuzhen.system.cooking.client;

import java.text.DecimalFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 餐食类别与酒类年份提示按来源标签追加中文文本。 */
public final class MealTooltip {
    private static final TagKey<Item> WINE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jiu"));
    private static final TagKey<Item> STAPLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhushi"));
    private static final TagKey<Item> DISH = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "caipin"));
    private static final TagKey<Item> SOUP = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tangshui"));
    private static final TagKey<Item> TEA = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chadian"));
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private MealTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        ItemStack stack = event.getItemStack();
        if (stack.is(WINE)) {
            double age = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("agenum");
            event.getToolTip().add(Component.literal("年份：" + WHOLE.format(age)));
        }
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
