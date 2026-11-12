package net.yvanchuxiuzhen.system.liandan.client;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.data.HerbAnalysis;

/** 炼丹物品提示按来源分派顺序读取丹药实例或玩家药草辨识快照。 */
public final class MedicineTooltip {
    private static final TagKey<Item> MEDICINE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danyaoguan"));
    private static final TagKey<Item> HERBS = LiandanHerbAnalysis.HERBS;
    private static final List<TagKey<Item>> EQUIPMENT = List.of("wuqi", "fabao", "toukui", "xiongjia", "kuzi", "zhanxve")
            .stream().map(path -> TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))).toList();

    private MedicineTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getEntity() == null || EQUIPMENT.stream().anyMatch(stack::is)) {
            return;
        }
        if (!stack.is(MEDICINE) && !stack.is(HERBS)) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        List<Component> lines = event.getToolTip();
        if (stack.is(MEDICINE)) {
            if (data.getInt("danyaonum") < 1) return;
            lines.add(Component.literal("§b" + data.getString("danyaoshuoming")));
            lines.add(Component.literal("§f剩余丹药数量：" + data.getInt("danyaonum")));
            boolean treasure = data.getBoolean("danyaobaoxiang");
            for (int index = 0; !data.getString("danyaoxiaoguo" + index).isEmpty(); index++) {
                int rawLevel = data.getInt("danyaolevel" + index);
                if (rawLevel < (treasure ? 1 : 100)) continue;
                String effect = data.getString("danyaoxiaoguo" + index);
                String translation = effect.contains(":") ? "effect." + effect.replace(':', '.') : "effect.minecraft." + effect;
                int level = treasure ? rawLevel : rawLevel / 100;
                int seconds = treasure ? data.getInt("danyaotick" + index) : data.getInt("danyaotick" + index) / 20;
                lines.add(Component.literal(level + "级" + Component.translatable(translation).getString() + "：" + seconds + "秒"));
            }
            return;
        }

        HerbAnalysis analysis = LiandanHerbAnalysis.analysis(event.getEntity());
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int maximum = analysis.maximum(itemId);
        int progress = analysis.progress(itemId);
        lines.add(Component.literal("§6炼丹材料"));
        if (maximum == 0) {
            lines.add(Component.literal("§4还未开始解析"));
        } else if (progress < maximum) {
            lines.add(Component.literal("§b解析进度" + progress + "/" + maximum));
        }
        for (HerbAnalysis.RevealedEffect effect : analysis.revealed(itemId)) {
            lines.add(Component.literal("§b" + Component.translatable(LiandanHerbAnalysis.translationKey(effect.name())).getString()
                    + ":§8" + effect.description()));
        }
    }
}
