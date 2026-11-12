package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
        CompoundTag state = analysis(player).data();
        String itemId = BuiltInRegistries.ITEM.getKey(sample.getItem()).toString();
        if (!state.getString("txtname").isEmpty()) {
            state.putString("txtname", "");
            state.putString("txtjindu", "");
            state.putString("txtjiesuoneed", "");
            state.putString("txtitemid", "");
            for (int ordinal = 1; ordinal <= 10; ordinal++) {
                state.putString("txtyaoxiao" + ordinal, "");
                state.putString("txtyaoxiao" + ordinal + "jieshao", "");
            }
        }
        Herb definition = LiandanHerbs.definition(itemId);
        if (definition != null) state.putInt(itemId + "jindumax", definition.effects().size());
        String name = sample.getDisplayName().getString();
        int progress = state.getInt(itemId + "jindu");
        int maximum = state.getInt(itemId + "jindumax");
        state.putString("txtname", name);
        state.putString("txtitemid", itemId);
        state.putString("txtjindu", "解析进度：" + progress + "/" + maximum);
        if (progress >= 1) {
            state.putString("txtjiesuoneed", progress < maximum ? nextCost(progress, name) : "此草药已完全解析。");
            for (int ordinal = 1; ordinal <= progress; ordinal++) {
                String effect = state.getString(itemId + "yaoxiao" + ordinal);
                state.putString("txtyaoxiao" + ordinal, Component.translatable(translationKey(effect)).getString());
                state.putString("txtyaoxiao" + ordinal + "jieshao", state.getString(itemId + "yaoxiao" + ordinal + "txt"));
            }
        } else {
            state.putString("txtjiesuoneed", nextCost(progress, name));
        }
        player.setData(LiandanData.HERB_ANALYSIS, new HerbAnalysis(state));
    }

    /** 切换样本后的首次操作只刷新；辨识每次消耗四加四倍当前进度的同类物品。 */
    public static void identify(ServerPlayer player, AbstractContainerMenu menu) {
        ItemStack sample = menu.getSlot(0).getItem();
        if (!sample.is(HERBS)) return;
        HerbAnalysis analysis = analysis(player);
        String itemId = BuiltInRegistries.ITEM.getKey(sample.getItem()).toString();
        if (analysis.selectedName().isEmpty() || !analysis.selectedId().equals(itemId)) {
            refresh(player, menu);
            return;
        }
        int progress = analysis.progress(itemId);
        if (progress >= analysis.maximum(itemId)) {
            player.displayClientMessage(Component.literal("此草药的所有药效已全部解析完成。"), false);
            return;
        }
        Item item = sample.getItem();
        int inventoryCount = inventoryCount(player, item);
        int needed = 4 + progress * 4;
        CompoundTag state = analysis.data();
        if (inventoryCount + sample.getCount() >= needed) {
            if (inventoryCount >= needed) {
                player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(item), needed, player.inventoryMenu.getCraftSlots());
            } else {
                player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(item), inventoryCount, player.inventoryMenu.getCraftSlots());
                menu.getSlot(0).remove(needed - inventoryCount);
                menu.broadcastChanges();
            }
            int next = progress + 1;
            state.putInt(itemId + "jindu", next);
            Herb definition = LiandanHerbs.definition(itemId);
            if (definition != null) {
                state.putInt(itemId + "jindumax", definition.effects().size());
                Effect effect = definition.effects().get(next - 1);
                state.putString(itemId + "yaoxiao" + next, effect.name());
                state.putString(itemId + "yaoxiao" + next + "txt", description(effect));
            }
        } else {
            player.displayClientMessage(Component.literal("§4您的背包里没有足够的" + analysis.selectedName() + "，无法解析草药。"), false);
        }
        player.setData(LiandanData.HERB_ANALYSIS, new HerbAnalysis(state));
        refresh(player, menu);
    }

    /** 管理命令按运行目录解锁全部药草，不清除已缓存的界面选择和显示文本。 */
    public static void unlockAll(ServerPlayer player) {
        CompoundTag state = analysis(player).data();
        for (Herb herb : LiandanHerbs.all()) {
            state.putInt(herb.itemId() + "jindumax", herb.effects().size());
            state.putInt(herb.itemId() + "jindu", herb.effects().size());
            for (int index = 0; index < herb.effects().size(); index++) {
                Effect effect = herb.effects().get(index);
                state.putString(herb.itemId() + "yaoxiao" + (index + 1), effect.name());
                state.putString(herb.itemId() + "yaoxiao" + (index + 1) + "txt", description(effect));
            }
        }
        player.setData(LiandanData.HERB_ANALYSIS, new HerbAnalysis(state));
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
