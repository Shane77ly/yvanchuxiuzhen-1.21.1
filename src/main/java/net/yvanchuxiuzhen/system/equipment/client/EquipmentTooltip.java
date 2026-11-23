package net.yvanchuxiuzhen.system.equipment.client;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.BenmingData;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentProperties;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentStats;
import net.yvanchuxiuzhen.system.equipment.api.FabaoFireData;
import net.yvanchuxiuzhen.system.equipment.content.Fabao010Item;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 装备成品提示读取炼器实例，饰品提示读取根字段中的品质、类型与恢复和上限数值。 */
final class EquipmentTooltip {
    private static final List<String> SPECIAL_TRAITS = List.of(
            "fenglei", "bahuang", "kongbao", "liedi", "zaijie", "sanmei", "xiuluo", "tianqing", "zhiyang",
            "youluo", "ziwei", "yejin", "bihua", "yvanmo", "youming", "huian", "zhiyin", "xingyvn");

    private EquipmentTooltip() {
    }

    static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (tagged(stack, "jewelry")) {
            jewelry(event.getToolTip(), Equipment.properties(stack));
            return;
        }
        boolean weapon = tagged(stack, "wuqi") || tagged(stack, "fabao");
        boolean armor = tagged(stack, "toukui") || tagged(stack, "xiongjia") || tagged(stack, "kuzi") || tagged(stack, "zhanxve");
        if (!weapon && !armor) return;
        Equipment.product(stack).filter(product -> !product.type().isEmpty()).ifPresent(product -> {
            List<Component> lines = event.getToolTip();
            String owner = Equipment.ownerId(stack);
            if (owner.isEmpty()) {
                lines.add(Component.translatable("tooltip.yvanchuxiuzhen.bond.unowned").withStyle(ChatFormatting.WHITE));
            } else if (event.getEntity() != null) {
                if (owner.equals(event.getEntity().getStringUUID())) {
                    BenmingData bond = stack.getOrDefault(EquipmentData.BENMING.get(), BenmingData.EMPTY);
                    lines.add(Component.translatable("tooltip.yvanchuxiuzhen.bond.owned").withStyle(ChatFormatting.GREEN));
                    lines.add(Component.translatable("tooltip.yvanchuxiuzhen.bond.progress", bond.level(),
                            Mth.floor(bond.experience()), Mth.floor(bond.experienceMax())).withStyle(ChatFormatting.WHITE));
                } else {
                    lines.add(Component.translatable("tooltip.yvanchuxiuzhen.bond.foreign").withStyle(ChatFormatting.DARK_RED));
                }
            }
            lines.add(Component.literal("§6" + product.grade()));
            lines.add(Component.literal("§6" + product.type()));
            EquipmentStats stats = stack.getOrDefault(EquipmentData.STATS.get(), EquipmentStats.EMPTY);
            CompoundTag refinement = stack.getOrDefault(EquipmentData.REFINEMENT.get(), CustomData.EMPTY).copyTag();
            if (weapon) {
                positive(lines, "物理攻击力", stats.physicalAttack());
                positive(lines, "法术攻击力", stats.spellAttack());
                positive(lines, "精神攻击力", stats.soulAttack());
                for (String trait : SPECIAL_TRAITS) {
                    if (refinement.getBoolean(trait)) {
                        lines.add(Component.literal("§b" + Component.translatable("teshu.yvanchuxiuzhen." + trait).getString()));
                    }
                }
            } else {
                positive(lines, "血量上限", stats.health());
                positive(lines, "血量恢复", stats.recovery().health());
                positive(lines, "灵力上限", stats.power());
                positive(lines, "灵力恢复", stats.recovery().power());
                positive(lines, "精力上限", stats.soul());
                positive(lines, "精力恢复", stats.recovery().soul());
                positive(lines, "物理防御", stats.physicalDefense());
                positive(lines, "法术防御", stats.spellDefense());
                positive(lines, "精神防御", stats.soulDefense());
            }
            lines.add(Component.literal("§f重量：" + product.weight()));
            String description = product.description();
            HolderLookup.Provider registries = event.getContext().registries();
            if (registries != null && refinement.contains("fabaoshuoming_component", Tag.TAG_STRING)) {
                description = Component.Serializer.fromJson(refinement.getString("fabaoshuoming_component"), registries).getString();
            }
            for (int start = 0; start < description.length(); start += 24) {
                lines.add(Component.literal("§f" + description.substring(start, Math.min(start + 24, description.length()))));
            }
        });
        if (event.getEntity() != null && stack.getItem() instanceof Fabao010Item && tagged(stack, "fabao")) {
            storedFire(event.getToolTip(), stack.getOrDefault(EquipmentData.FABAO_FIRE.get(), FabaoFireData.EMPTY));
        }
    }

    private static void storedFire(List<Component> lines, FabaoFireData fire) {
        DecimalFormat whole = new DecimalFormat("0");
        for (int slot = 1; slot <= 10; slot++) {
            double count = fire.get(slot);
            if (count >= 1) {
                Component amount = Component.literal(whole.format(count)).withStyle(ChatFormatting.WHITE);
                lines.add((slot == 1 ? Component.translatable("tooltip.yvanchuxiuzhen.gourd.ordinary", amount)
                        : Component.translatable("tooltip.yvanchuxiuzhen.gourd.graded", slot - 1, amount)).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    private static void jewelry(List<Component> lines, EquipmentProperties properties) {
        if (properties.text("fabaotype").isEmpty()) return;
        lines.add(Component.literal("§6" + properties.text("item_level")));
        lines.add(Component.literal("§6" + properties.text("fabaotype")));
        nonzero(lines, "血量恢复", properties.number("health_Rec"));
        nonzero(lines, "灵力上限", properties.number("power_Max"));
        nonzero(lines, "灵力恢复", properties.number("power_Rec"));
        nonzero(lines, "精力上限", properties.number("soul_Max"));
        nonzero(lines, "精力恢复", properties.number("soul_Rec"));
    }

    private static void nonzero(List<Component> lines, String name, double value) {
        if (value != 0) lines.add(Component.literal("§b" + name + "+" + new DecimalFormat("0").format(value)));
    }

    private static void positive(List<Component> lines, String name, int value) {
        if (value > 0) lines.add(Component.literal("§b" + name + "+" + value));
    }

    private static boolean tagged(ItemStack stack, String path) {
        return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)));
    }
}
