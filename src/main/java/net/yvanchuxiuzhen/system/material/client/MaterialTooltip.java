package net.yvanchuxiuzhen.system.material.client;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;

/** 炼器材料悬停提示：显示实例等级、品阶、千材录解析进度与已揭示条目。 */
public final class MaterialTooltip {
    private static final TagKey<Item> MATERIALS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao"));
    private static final String[] QUALITIES = {"次品", "下品", "中品", "上品", "极品"};

    private MaterialTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (event.getEntity() == null || !stack.is(MATERIALS)) return;
        MaterialInstance instance = Materials.instance(stack);
        int level = Math.max(1, instance.level());
        int quality = instance.quality();
        List<Component> lines = event.getToolTip();
        if (quality >= 5) {
            lines.add(Component.literal("§b" + level + "§6级炼器材料"));
        } else {
            String qualityName = quality >= 0 && quality < QUALITIES.length ? QUALITIES[quality] : QUALITIES[0];
            lines.add(Component.literal("§b" + level + "§6级炼器材料中的§b" + qualityName + "§6材料"));
        }
        MaterialAnalysis analysis = MaterialLibrary.analysis(event.getEntity());
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int maximum = analysis.maximum(itemId);
        int progress = analysis.progress(itemId);
        if (maximum == 0) {
            lines.add(Component.literal("§4还未开始解析"));
        } else if (progress < maximum) {
            lines.add(Component.literal("§b解析进度" + progress + "/" + maximum));
        }
        for (MaterialRevealedEntry entry : analysis.revealed(itemId)) {
            lines.add(Component.literal("§b" + Component.translatable(translationKey(entry)).getString()));
        }
    }

    /** 统一处理特殊标记、属性及附魔（含原初系统旧命名空间）翻译键。 */
    public static String translationKey(MaterialRevealedEntry entry) {
        if (entry.namespace().equals("teshu")) return "teshu.yvanchuxiuzhen." + entry.name();
        if (!entry.open()) return "attribute.yvanchuxiuzhen." + entry.name();
        String namespace = entry.namespace().equals("yvanchuxitong") ? ModId.VALUE : entry.namespace();
        return "enchantment." + namespace + "." + entry.name();
    }
}
