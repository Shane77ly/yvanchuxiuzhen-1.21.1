package net.yvanchuxiuzhen.system.material.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;

/** 材料系统供其他系统调用的公开入口。 */
public final class Materials {
    private Materials() {
    }

    public static MaterialDefinition definition(ItemStack stack) {
        return MaterialLibrary.definition(stack);
    }

    public static MaterialInstance instance(ItemStack stack) {
        return MaterialLibrary.instance(stack);
    }

    public static void initializeYihuo(ServerPlayer player) {
        MaterialLibrary.initializeYihuo(player);
    }
}
