package net.yvanchuxiuzhen.system.equipment.client;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 成品提示对应 Lore 的装备分支；Q17 移入统一贡献的生命与物攻在此显示。 */
final class EquipmentTooltip {
    private EquipmentTooltip() {
    }

    static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        boolean weapon = tagged(stack, "wuqi") || tagged(stack, "fabao");
        boolean armor = tagged(stack, "toukui") || tagged(stack, "xiongjia") || tagged(stack, "kuzi") || tagged(stack, "zhanxve");
        if (!weapon && !armor) return;
        Equipment.product(stack).filter(product -> !product.type().isEmpty()).ifPresent(product -> {
            List<Component> lines = event.getToolTip();
            lines.add(Component.literal("§f无主之物，可滴血认主"));
            lines.add(Component.literal("§6" + product.grade()));
            lines.add(Component.literal("§6" + product.type()));
            EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
            if (weapon) {
                positive(lines, "物理攻击力", stats.physicalAttack());
                positive(lines, "法术攻击力", stats.spellAttack());
                positive(lines, "精神攻击力", stats.soulAttack());
            } else {
                positive(lines, "血量上限", stats.health());
                positive(lines, "灵力上限", stats.power());
                positive(lines, "精力上限", stats.soul());
                positive(lines, "物理防御", stats.physicalDefense());
                positive(lines, "法术防御", stats.spellDefense());
                positive(lines, "精神防御", stats.soulDefense());
            }
            lines.add(Component.literal("§f重量：" + product.weight()));
            for (int start = 0; start < product.description().length(); start += 24) {
                lines.add(Component.literal("§f" + product.description().substring(start, Math.min(start + 24, product.description().length()))));
            }
        });
    }

    private static void positive(List<Component> lines, String name, int value) {
        if (value > 0) lines.add(Component.literal("§b" + name + "+" + value));
    }

    private static boolean tagged(ItemStack stack, String path) {
        return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }
}
