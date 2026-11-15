package net.yvanchuxiuzhen.system.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 答题完成时保留来源写入的通用物品归属字段，不改变所属系统的业务组件。 */
public final class BookOwnership {
    private BookOwnership() {
    }

    /** 按来源写入当前物品的答题归属，不要求物品是功法书或技能书。 */
    public static void bindQuizOwner(ServerPlayer player, ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.putString("belongToMasterUUID", player.getStringUUID());
            data.putString("belongToName", player.getDisplayName().getString());
        });
    }
}
