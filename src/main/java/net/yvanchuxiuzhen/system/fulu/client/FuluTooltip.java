package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;

/** 符箓品阶提示优先采用制作时保存的名称；没有名称时按来源数值分支显示。 */
public final class FuluTooltip {
    private static final TagKey<Item> FULU = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulu"));

    private FuluTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getEntity() == null || !stack.is(FULU)) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String name = data.getString("fululeveltxt");
        Component grade;
        if (name.isEmpty()) {
            double value = data.getDouble("fululevel");
            int number = value == 4 ? 4 : value == 3 ? 3 : value == 2 ? 2 : 1;
            grade = Component.translatable(DrawingTableRuntime.knowledgeKey("tiandizhu", number));
        } else if (name.startsWith("txt.yvanchuxiuzhen.huafu_tiandizhu_")) {
            grade = Component.translatable(name);
        } else {
            grade = Component.literal(name);
        }
        event.getToolTip().add(grade.copy().withStyle(ChatFormatting.GOLD));
    }
}
