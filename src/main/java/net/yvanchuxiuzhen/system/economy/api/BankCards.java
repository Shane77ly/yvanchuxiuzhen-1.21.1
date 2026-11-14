package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.economy.data.BankData;

/** 通宝玉的物品实例归属由经济系统维护；银行登录只查询此处的归属。 */
public final class BankCards {
    private BankCards() {
    }

    public static BankCard read(ItemStack stack) {
        return stack.getOrDefault(BankData.CARD.get(), BankCard.EMPTY);
    }

    public static boolean ownedBy(ItemStack stack, ServerPlayer player) {
        return read(stack).masterUUID().equals(player.getStringUUID());
    }

    public static void bind(ServerPlayer player, ItemStack stack) {
        BankCard card = read(stack);
        if (card.masterUUID().isEmpty()) {
            stack.set(BankData.CARD.get(), card.withMaster(player.getStringUUID()));
            message(player, "bound");
        } else {
            message(player, "already_bound");
        }
    }

    public static void message(ServerPlayer player, String suffix, Object... arguments) {
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.bank." + suffix, arguments), false);
    }
}
