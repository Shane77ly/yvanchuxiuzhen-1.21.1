package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 由修炼系统构造出身奖励中的自有物品实例。 */
public final class BirthItems {
    private BirthItems() {
    }

    /** 保存侠客初始石盘的来源坐标副本；右键指引另外读取世界全局坐标。 */
    public static ItemStack ancientStoneDisc(Vec3 center) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_11")));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putDouble("mubiaoX", center.x());
            data.putDouble("mubiaoY", center.y());
            data.putDouble("mubiaoZ", center.z());
        });
        return stack;
    }
}
