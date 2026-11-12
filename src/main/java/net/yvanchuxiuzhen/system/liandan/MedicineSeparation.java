package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyMenu;

/** 分丹台只搬移三个槽位的丹药实例，数量、容器转换和来源分支顺序在服务端结算。 */
public final class MedicineSeparation {
    private MedicineSeparation() {
    }

    public static void split(ServerPlayer player, AbstractContainerMenu menu) {
        ItemStack source = menu.getSlot(2).getItem();
        if (source.isEmpty()) {
            tell(player, "请先把需要分开的丹药放到最上面。");
            return;
        }
        CompoundTag data = data(source);
        if (data.getString("danyaoname").isEmpty()) {
            tell(player, "最上面的槽位内的物品不是丹药。");
            return;
        }
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        boolean leftBottle = left.is(AlchemyMenu.BOTTLES);
        boolean rightBottle = right.is(AlchemyMenu.BOTTLES);
        if (!leftBottle && !rightBottle) {
            tell(player, "需要至少在下面两个槽位中的一个里，放置一个盛放丹药的容器。");
            return;
        }
        // 来源把有效接收瓶复制成同一临时物品后比较 Item；不比较丹名或药效是否一致。
        boolean both = leftBottle && rightBottle
                || (!leftBottle || data(left).getString("danyaoname").isEmpty())
                && (!rightBottle || data(right).getString("danyaoname").isEmpty());
        int count = data.getInt("danyaonum");
        if (both) {
            if (count < 1) {
                tell(player, "需要拆分的丹药瓶内没有丹药！");
                return;
            }
            // Math.ceil 的实参先做整数除法，因此奇数时右侧多一颗。
            int half = count / 2;
            fill(menu.getSlot(0), source, half);
            fill(menu.getSlot(1), source, count - half);
        } else {
            // 来源左侧判定对有效瓶和空临时副本均成立，右侧独占分支不可达。
            fill(menu.getSlot(0), source, count);
        }
        empty(menu.getSlot(2));
    }

    public static void merge(ServerPlayer player, AbstractContainerMenu menu) {
        ItemStack target = menu.getSlot(2).getItem();
        if (!target.is(AlchemyMenu.BOTTLES)) {
            tell(player, "需要在最下面放置一个可以盛放丹药的容器。");
            return;
        }
        if (!data(target).getString("danyaoname").isEmpty()) {
            tell(player, "下面槽位内的容器里已有其它种类的丹药，无法作为上面两种丹药合并的容器。");
            return;
        }
        ItemStack left = menu.getSlot(0).getItem();
        ItemStack right = menu.getSlot(1).getItem();
        CompoundTag leftData = data(left);
        CompoundTag rightData = data(right);
        boolean leftPresent = !left.isEmpty() && !leftData.getString("danyaoname").isEmpty();
        boolean rightPresent = !right.isEmpty() && !rightData.getString("danyaoname").isEmpty();
        if (!leftPresent) tell(player, "请在上面左边的槽位内放置需要合并的丹药。");
        if (!rightPresent) tell(player, "请在上面右边的槽位内放置需要合并的丹药。");
        if (!leftPresent || !rightPresent) return;

        if (leftData.getString("danyaoname").equals(rightData.getString("danyaoname"))) {
            for (int index = 0; !leftData.getString("danyaoxiaoguo" + index).isEmpty()
                    || !rightData.getString("danyaoxiaoguo" + index).isEmpty(); index++) {
                if (!leftData.getString("danyaoxiaoguo" + index).equals(rightData.getString("danyaoxiaoguo" + index))
                        || leftData.getInt("danyaolevel" + index) != rightData.getInt("danyaolevel" + index)
                        || leftData.getInt("danyaotick" + index) != rightData.getInt("danyaotick" + index)) {
                    tell(player, "上面两个槽位内的丹药必须是相同的才可以合并。");
                    return;
                }
            }
        } else {
            // 来源名称不同时只提示，没有设置阻止合并的标记。
            tell(player, "上面两个槽位内的丹药必须是相同的才可以合并。");
        }
        fill(menu.getSlot(2), left, leftData.getInt("danyaonum") + rightData.getInt("danyaonum"));
        empty(menu.getSlot(0));
        empty(menu.getSlot(1));
    }

    /** 接收容器保留自身 Item 与数量，以来源全部实例组件替换原有组件；空槽不能成为物品。 */
    private static void fill(Slot slot, ItemStack source, int count) {
        ItemStack target = slot.getItem();
        if (target.isEmpty()) return;
        if (target.is(item("04"))) target = new ItemStack(item("0401"));
        else if (target.is(item("05"))) target = new ItemStack(item("0501"));
        ItemStack result = source.transmuteCopy(target.getItem(), target.getCount());
        CompoundTag data = data(source);
        data.putInt("danyaonum", count);
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        slot.set(result);
    }

    /** 来源只为六种有药容器返回新空瓶，其他输入在转移后清空整个槽位。 */
    private static void empty(Slot slot) {
        ItemStack stack = slot.getItem();
        ItemStack replacement = ItemStack.EMPTY;
        for (String variant : new String[]{"01", "02", "03", "0401", "0501", "06"}) {
            if (stack.is(item(variant))) {
                replacement = LiandanMedicine.emptyContainer(stack.getItem());
                break;
            }
        }
        slot.set(replacement);
    }

    private static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static Item item(String variant) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "itemdanyaohulu_" + variant));
    }

    private static void tell(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal("§4" + message), false);
    }
}
