package net.yvanchuxiuzhen.system.guchong.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;

/** 荧光蛊坐标物品的唯一生产入口，悬赏只提交主人与目的地。 */
public final class GuchongTracking {
    private GuchongTracking() {
    }

    public static ItemStack createBound(ServerPlayer owner, BlockPos destination) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("zhuizhong", true);
        data.putString("belongToMasterUUID", owner.getStringUUID());
        data.putInt("zuobiaoX", destination.getX());
        data.putInt("zuobiaoY", destination.getY());
        data.putInt("zuobiaoZ", destination.getZ());
        return item(data);
    }

    /** 实体回收保留来源明确复制的十个字段，两个计时器属于当前实体实例。 */
    public static ItemStack fromState(CompoundTag source) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("zhuizhong", source.getBoolean("zhuizhong"));
        data.putString("belongToMasterUUID", source.getString("belongToMasterUUID"));
        for (String key : new String[]{"zuobiaoX", "zuobiaoY", "zuobiaoZ", "homeblockX", "homeblockY", "homeblockZ"}) {
            data.putDouble(key, source.getDouble(key));
        }
        data.putBoolean("guchongxvnfu", source.getBoolean("guchongxvnfu"));
        data.putBoolean("kanjia", source.getBoolean("kanjia"));
        return item(data);
    }

    private static ItemStack item(CompoundTag data) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchong_05item")));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }
}
