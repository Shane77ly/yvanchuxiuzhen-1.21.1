package net.yvanchuxiuzhen.system.guchong.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

    /** 实体回收保留来源明确复制的十个字段，两个计时器属于当前实体实例。 */
    public static ItemStack fromState(CompoundTag source) {
        return item(new GuchongTrackingData(
                source.getBoolean("zhuizhong"),
                source.getString("belongToMasterUUID"),
                source.getDouble("zuobiaoX"),
                source.getDouble("zuobiaoY"),
                source.getDouble("zuobiaoZ"),
                source.getDouble("homeblockX"),
                source.getDouble("homeblockY"),
                source.getDouble("homeblockZ"),
                source.getBoolean("guchongxvnfu"),
                source.getBoolean("kanjia")));
    }

    private static ItemStack item(GuchongTrackingData data) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong_05item")));
        stack.set(GuchongData.TRACKING.get(), data);
        return stack;
    }
}
