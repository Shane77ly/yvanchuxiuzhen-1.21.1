package net.yvanchuxiuzhen.system.liandan.client;

import java.util.List;
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

/** 丹药提示读取容器实例，数量与药效单位按普通丹药、宝箱丹药分别解释。 */
public final class MedicineTooltip {
    private static final TagKey<Item> MEDICINE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danyaoguan"));
    private static final List<TagKey<Item>> EQUIPMENT = List.of("wuqi", "fabao", "toukui", "xiongjia", "kuzi", "zhanxve")
            .stream().map(path -> TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))).toList();

    private MedicineTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getEntity() == null || !stack.is(MEDICINE) || EQUIPMENT.stream().anyMatch(stack::is)) {
            return;
        }
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.getInt("danyaonum") < 1) {
            return;
        }
        List<Component> lines = event.getToolTip();
        lines.add(Component.literal("§b" + data.getString("danyaoshuoming")));
        lines.add(Component.literal("§f剩余丹药数量：" + data.getInt("danyaonum")));
        boolean treasure = data.getBoolean("danyaobaoxiang");
        for (int index = 0; !data.getString("danyaoxiaoguo" + index).isEmpty(); index++) {
            int rawLevel = data.getInt("danyaolevel" + index);
            if (rawLevel < (treasure ? 1 : 100)) {
                continue;
            }
            String effect = data.getString("danyaoxiaoguo" + index);
            String translation = effect.contains(":") ? "effect." + effect.replace(':', '.') : "effect.minecraft." + effect;
            int level = treasure ? rawLevel : rawLevel / 100;
            int seconds = treasure ? data.getInt("danyaotick" + index) : data.getInt("danyaotick" + index) / 20;
            lines.add(Component.literal(level + "级" + Component.translatable(translation).getString() + "：" + seconds + "秒"));
        }
    }
}
