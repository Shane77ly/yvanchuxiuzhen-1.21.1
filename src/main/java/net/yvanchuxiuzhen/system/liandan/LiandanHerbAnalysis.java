package net.yvanchuxiuzhen.system.liandan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbs.Effect;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbs.Herb;
import net.yvanchuxiuzhen.system.liandan.data.HerbAnalysis;
import net.yvanchuxiuzhen.system.liandan.data.HerbAnalysis.HerbProgress;
import net.yvanchuxiuzhen.system.liandan.data.HerbAnalysis.RevealedEffect;
import net.yvanchuxiuzhen.system.liandan.data.LiandanData;

/** 百草鉴按玩家缓存选择药草，依次消费背包与样本并保存已揭示的原始药性说明。 */
public final class LiandanHerbAnalysis {
    public static final TagKey<Item> HERBS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "caoyao"));

    private LiandanHerbAnalysis() {
    }

    public static HerbAnalysis analysis(Player player) {
        return player.getData(LiandanData.HERB_ANALYSIS);
    }

    /** 空槽或非药草不改变最近一次显示；目录只更新当前药草的解析上限。 */
    public static void refresh(ServerPlayer player, AbstractContainerMenu menu) {
        ItemStack sample = menu.getSlot(0).getItem();
        if (!sample.is(HERBS)) return;
        HerbAnalysis current = analysis(player);
        String itemId = BuiltInRegistries.ITEM.getKey(sample.getItem()).toString();
        Herb definition = LiandanHerbs.definition(itemId);
        HerbProgress entry = current.entries().getOrDefault(itemId, HerbProgress.EMPTY);
        int maximum = definition != null ? definition.effects().size() : entry.maximum();
        int progress = entry.progress();
        if (maximum != entry.maximum()) {
            entry = new HerbProgress(progress, maximum, entry.revealed());
            current = current.withEntry(itemId, entry);
        }
        String name = sample.getDisplayName().getString();
        String progressText = "解析进度：" + progress + "/" + maximum;
        String needText;
        List<RevealedEffect> display = new ArrayList<>(Collections.nCopies(10, RevealedEffect.EMPTY));
        if (progress >= 1) {
            needText = progress < maximum ? nextCost(progress, name) : "此草药已完全解析。";
            for (int ordinal = 1; ordinal <= progress; ordinal++) {
                RevealedEffect revealed = ordinal <= entry.revealed().size()
                        ? entry.revealed().get(ordinal - 1) : RevealedEffect.EMPTY;
                display.set(ordinal - 1, new RevealedEffect(
                        Component.translatable(translationKey(revealed.name())).getString(), revealed.description()));
            }
        } else {
            needText = nextCost(progress, name);
        }
        player.setData(LiandanData.HERB_ANALYSIS, current.withSelection(name, itemId, progressText, needText, display));
    }

    /** 切换样本后的首次操作只刷新；辨识每次消耗四加四倍当前进度的同类物品。 */
    public static void identify(ServerPlayer player, AbstractContainerMenu menu) {
        ItemStack sample = menu.getSlot(0).getItem();
        if (!sample.is(HERBS)) return;
        HerbAnalysis current = analysis(player);
        String itemId = BuiltInRegistries.ITEM.getKey(sample.getItem()).toString();
        if (current.selectedName().isEmpty() || !current.selectedId().equals(itemId)) {
            refresh(player, menu);
            return;
        }
        int progress = current.progress(itemId);
        if (progress >= current.maximum(itemId)) {
            player.displayClientMessage(Component.literal("此草药的所有药效已全部解析完成。"), false);
            return;
        }
        Item item = sample.getItem();
        int inventoryCount = inventoryCount(player, item);
        int needed = 4 + progress * 4;
        if (inventoryCount + sample.getCount() >= needed) {
            if (inventoryCount >= needed) {
                player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(item), needed, player.inventoryMenu.getCraftSlots());
            } else {
                player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(item), inventoryCount, player.inventoryMenu.getCraftSlots());
                menu.getSlot(0).remove(needed - inventoryCount);
                menu.broadcastChanges();
            }
            int next = progress + 1;
            Herb definition = LiandanHerbs.definition(itemId);
            HerbProgress existing = current.entries().getOrDefault(itemId, HerbProgress.EMPTY);
            int maximum = definition != null ? definition.effects().size() : existing.maximum();
            List<RevealedEffect> revealed = new ArrayList<>(existing.revealed());
            if (definition != null) {
                Effect effect = definition.effects().get(next - 1);
                RevealedEffect entry = new RevealedEffect(effect.name(), description(effect));
                while (revealed.size() < next) revealed.add(RevealedEffect.EMPTY);
                revealed.set(next - 1, entry);
            }
            current = current.withEntry(itemId, new HerbProgress(next, maximum, revealed));
        } else {
            player.displayClientMessage(Component.literal("§4您的背包里没有足够的" + current.selectedName() + "，无法解析草药。"), false);
        }
        player.setData(LiandanData.HERB_ANALYSIS, current);
        refresh(player, menu);
    }

    /** 管理命令按运行目录解锁全部药草，不清除已缓存的界面选择和显示文本。 */
    public static void unlockAll(ServerPlayer player) {
        HerbAnalysis current = analysis(player);
        Map<String, HerbProgress> updated = new LinkedHashMap<>(current.entries());
        for (Herb herb : LiandanHerbs.all()) {
            List<RevealedEffect> revealed = new ArrayList<>(herb.effects().size());
            for (Effect effect : herb.effects()) {
                revealed.add(new RevealedEffect(effect.name(), description(effect)));
            }
            updated.put(herb.itemId(), new HerbProgress(herb.effects().size(), herb.effects().size(), revealed));
        }
        player.setData(LiandanData.HERB_ANALYSIS, current.withEntries(updated));
    }

    public static String translationKey(String effectName) {
        return effectName.contains(":") ? "effect." + effectName.replace(':', '.') : "effect.minecraft." + effectName;
    }

    private static int inventoryCount(ServerPlayer player, Item item) {
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        int count = 0;
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.is(item)) count += stack.getCount();
            }
        }
        return count;
    }

    private static String nextCost(int progress, String name) {
        return "进一步解析此草药，需要消耗" + (4 + progress * 4) + "个" + name;
    }

    private static String description(Effect effect) {
        return "药效" + effect.levelOp() + effect.level() + "点；持续时间" + effect.timeOp() + effect.time() + "刻";
    }
}
