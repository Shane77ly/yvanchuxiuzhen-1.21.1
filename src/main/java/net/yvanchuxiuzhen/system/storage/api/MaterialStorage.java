package net.yvanchuxiuzhen.system.storage.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Grade;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Group;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Preferences;
import net.yvanchuxiuzhen.system.storage.data.StorageData;

/** 材料仓唯一写入口；存入只保留材料身份、等级、品阶，外部得到不可变快照。 */
public final class MaterialStorage {
    /** 炼器同一槽位依次写入的桶栈、剩余需求和跨图纸位置沿用的退出标记。 */
    public record RefiningWithdrawal(List<ItemStack> replacements, int remaining, boolean stopped) {
        public RefiningWithdrawal {
            replacements = List.copyOf(replacements);
        }
    }

    private MaterialStorage() {
    }

    public static MaterialWarehouse snapshot(Player player) {
        return player.getData(StorageData.WAREHOUSE);
    }

    public static boolean accepts(ItemStack stack) {
        return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao")))
                && !stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi")))
                && !stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "huoyan")))
                && Materials.instance(stack).level() <= 41;
    }

    /** 消费已由输入槽校验的整栈；来源只把等级0改为1，不调整其他等级或品阶。 */
    public static void deposit(ServerPlayer player, ItemStack stack) {
        MaterialWarehouse warehouse = snapshot(player);
        if (stack.isEmpty()) {
            refreshPageLimit(player, warehouse);
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        MaterialInstance instance = Materials.instance(stack);
        int level = instance.level() == 0 ? 1 : instance.level();
        Group group = warehouse.group(id);
        if (group == null) group = new Group(id, 0, 0, List.of(), List.of());
        boolean newLevel = !group.activeLevels().contains(level);
        List<Integer> levels = new ArrayList<>(group.activeLevels());
        if (newLevel) levels.add(level);
        int count = stack.getCount();
        Group next = new Group(id, group.total() + count, group.levelCount() + (newLevel ? 1 : 0), levels,
                amounts(group, level, instance.quality(), group.count(level, instance.quality()) + count));
        List<Group> groups = replace(warehouse.groups(), group, next);
        int styles = warehouse.styles() + (newLevel ? 1 : 0);
        save(player, new MaterialWarehouse(groups, warehouse.total() + count, styles, pageMax(styles), warehouse.preferences()));
        stack.setCount(0);
    }

    /** 品质按钮每次只取一件；交付背包或掉落由当前菜单完成。 */
    public static ItemStack withdraw(ServerPlayer player, String id, int level, int quality) {
        MaterialWarehouse warehouse = snapshot(player);
        Group group = warehouse.group(id);
        if (group == null || group.count(level, quality) <= 0) {
            refreshPageLimit(player, warehouse);
            return ItemStack.EMPTY;
        }
        Group next = changed(group, level, quality, 1, true, true);
        int styles = warehouse.styles() - (next.total() <= 0 || !next.activeLevels().contains(level) ? 1 : 0);
        save(player, new MaterialWarehouse(replace(warehouse.groups(), group, next.total() <= 0 ? null : next),
                warehouse.total() - 1, styles, pageMax(styles), warehouse.preferences()));
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));
        Materials.setInstance(result, level, quality);
        return result;
    }

    /** 辨识专用：等级1至40、品阶0至4，返回未满足的需求；不代替炼器装料的相反顺序。 */
    public static int consumeForAnalysis(ServerPlayer player, String id, int required) {
        MaterialWarehouse warehouse = snapshot(player);
        if (!warehouse.preferences().spending()) return required;
        Group group = warehouse.group(id);
        if (group == null) return required;
        int remaining = required;
        int styles = warehouse.styles();
        for (int level = 1; level <= 40 && remaining > 0; level++) {
            if (!group.activeLevels().contains(level)) continue;
            for (int quality = 0; quality <= 4 && remaining > 0; quality++) {
                int count = group.count(level, quality);
                if (count <= 0) continue;
                int used = Math.min(count, remaining);
                // 来源部分扣减与清空桶分支不同：只有清空桶时才处理等级开关和组合数。
                Group next = changed(group, level, quality, used, count <= remaining, false);
                if (count <= remaining && (next.total() <= 0 || !next.activeLevels().contains(level))) styles--;
                group = next;
                remaining -= used;
                if (group.total() <= 0) break;
            }
            if (group.total() <= 0) break;
        }
        save(player, new MaterialWarehouse(replace(warehouse.groups(), warehouse.group(id), group.total() <= 0 ? null : group),
                warehouse.total() - (required - remaining), styles, warehouse.pageMax(), warehouse.preferences()));
        return remaining;
    }

    /** 炼器按41至1级、4至0品阶扣仓；每个桶返回一次炉槽覆盖，不合并已扣出的栈。 */
    public static RefiningWithdrawal consumeForRefining(ServerPlayer player, String id, int required, boolean stopped) {
        MaterialWarehouse warehouse = snapshot(player);
        if (!warehouse.preferences().spending()) return new RefiningWithdrawal(List.of(), required, stopped);
        Group original = warehouse.group(id);
        if (original == null) return new RefiningWithdrawal(List.of(), required, stopped);
        Group group = original;
        List<ItemStack> replacements = new ArrayList<>();
        int remaining = required;
        int styles = warehouse.styles();
        boolean removed = false;
        if (remaining > 0) {
            for (int level = 41; level >= 1; level--) {
                if (group.activeLevels().contains(level)) {
                    for (int quality = 4; quality >= 0; quality--) {
                        int count = group.count(level, quality);
                        if (count >= 1) {
                            int used = Math.min(count, remaining);
                            ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id)));
                            Materials.setInstance(result, level, quality);
                            result.setCount(used);
                            replacements.add(result);
                            boolean emptied = count <= remaining;
                            Group next = changed(group, level, quality, used, emptied, false);
                            if (emptied) {
                                if (next.total() <= 0) {
                                    styles--;
                                    removed = true;
                                    stopped = true;
                                } else if (!next.activeLevels().contains(level)) {
                                    styles--;
                                }
                            }
                            group = next;
                            remaining -= used;
                        }
                        // 来源在循环体末检查沿用标记；true时仍先处理41级的品阶4桶。
                        if (remaining <= 0 || stopped) break;
                    }
                }
                if (remaining <= 0 || stopped) break;
            }
        }
        save(player, new MaterialWarehouse(replace(warehouse.groups(), original, removed ? null : group),
                warehouse.total() - (required - remaining), styles, warehouse.pageMax(), warehouse.preferences()));
        return new RefiningWithdrawal(replacements, remaining, stopped);
    }

    public static void preferences(ServerPlayer player, Preferences preferences) {
        MaterialWarehouse warehouse = snapshot(player);
        save(player, new MaterialWarehouse(warehouse.groups(), warehouse.total(), warehouse.styles(), warehouse.pageMax(), preferences));
    }

    private static Group changed(Group group, int level, int quality, int used, boolean clearLevel, boolean decrementLevelCount) {
        List<Grade> amounts = amounts(group, level, quality, group.count(level, quality) - used);
        Group next = new Group(group.itemId(), group.total() - used, group.levelCount(), group.activeLevels(), amounts);
        if (clearLevel && next.total() > 0 && next.count(level) <= 0) {
            List<Integer> levels = new ArrayList<>(next.activeLevels());
            levels.remove(Integer.valueOf(level));
            next = new Group(next.itemId(), next.total(), next.levelCount() - (decrementLevelCount ? 1 : 0), levels, amounts);
        }
        return next;
    }

    private static List<Grade> amounts(Group group, int level, int quality, int count) {
        List<Grade> amounts = new ArrayList<>(group.amounts());
        amounts.removeIf(amount -> amount.level() == level && amount.quality() == quality);
        amounts.add(new Grade(level, quality, count));
        return amounts;
    }

    private static List<Group> replace(List<Group> previous, Group group, Group next) {
        List<Group> groups = new ArrayList<>(previous);
        int index = groups.indexOf(group);
        if (index >= 0) {
            if (next == null) groups.remove(index);
            else groups.set(index, next);
        } else if (next != null) groups.add(next);
        return groups;
    }

    private static int pageMax(int styles) {
        return Math.max(1, (int) Math.ceil(styles / 45.0));
    }

    private static void refreshPageLimit(ServerPlayer player, MaterialWarehouse warehouse) {
        save(player, new MaterialWarehouse(warehouse.groups(), warehouse.total(), warehouse.styles(), pageMax(warehouse.styles()), warehouse.preferences()));
    }

    private static void save(ServerPlayer player, MaterialWarehouse warehouse) {
        player.setData(StorageData.WAREHOUSE, warehouse);
    }
}
