package net.yvanchuxiuzhen.system.guchong.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;

/** 荧光蛊坐标物品的唯一生产与查询入口，悬赏只提交主人与目的地。 */
public final class GuchongTracking {
    private GuchongTracking() {
    }

    public static GuchongTrackingData get(ItemStack stack) {
        return stack.getOrDefault(GuchongData.TRACKING.get(), GuchongTrackingData.EMPTY);
    }

    public static ItemStack createBound(ServerPlayer owner, BlockPos destination) {
        return item(new GuchongTrackingData(true, owner.getStringUUID(),
                destination.getX(), destination.getY(), destination.getZ(), 0, 0, 0, false, false));
    }

    /** 实体回收保留物品组件的十个追踪字段，两个计时器属于当前实体实例。 */
    public static ItemStack fromState(GuchongTrackingData data) {
        return item(data == null ? GuchongTrackingData.EMPTY : data);
    }

    private static ItemStack item(GuchongTrackingData data) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong_05item")));
        stack.set(GuchongData.TRACKING.get(), data);
        return stack;
    }
}
