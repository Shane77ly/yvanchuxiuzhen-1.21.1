package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianData;

/** 由修炼系统构造出身奖励中的自有物品实例。 */
public final class BirthItems {
    private BirthItems() {
    }

    /** 保存侠客初始石盘的来源坐标副本；右键指引另外读取世界全局坐标。 */
    public static ItemStack ancientStoneDisc(Vec3 center) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_11")));
        setSpiritDisc(stack, new SpiritDiscData("", center.x(), center.y(), center.z()));
        return stack;
    }

    public static SpiritDiscData spiritDisc(ItemStack stack) {
        return stack.getOrDefault(XiulianData.SPIRIT_DISC.get(), SpiritDiscData.EMPTY);
    }

    public static void setSpiritDisc(ItemStack stack, SpiritDiscData data) {
        stack.set(XiulianData.SPIRIT_DISC.get(), data);
    }

    public static double supplyCount(ItemStack stack) {
        return stack.getOrDefault(XiulianData.SUPPLY_COUNT.get(), 0.0);
    }

    public static void setSupplyCount(ItemStack stack, double count) {
        stack.set(XiulianData.SUPPLY_COUNT.get(), count);
    }
}
