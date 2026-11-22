package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.yvanchuxiuzhen.system.economy.CommerceGoods;
import net.yvanchuxiuzhen.system.economy.CommerceTrading;
import net.yvanchuxiuzhen.system.economy.EconomyPrices;

/** 经济所有者的外部币值入口；通用命令和任务只负责调用与来源流程条件。 */
public final class EconomyCurrency {
    private EconomyCurrency() { }

    public static void giveCoins(ServerPlayer player, double value) { CommerceTrading.giveCoins(player, value); }

    public static void giveStones(ServerPlayer player, double value) { CommerceTrading.giveStones(player, value); }

    /** 寻踪虫补领优先消耗20文，其次消耗2贯，不合并两种币值。 */
    public static boolean payTrackingReissue(ServerPlayer player) {
        for (int tier = 1; tier <= 2; tier++) {
            Item coin = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "money_0" + tier));
            int need = tier == 1 ? 20 : 2;
            int count = player.getInventory().clearOrCountMatchingItems(stack -> stack.is(coin), 0, player.inventoryMenu.getCraftSlots());
            if (count >= need) {
                player.getInventory().clearOrCountMatchingItems(stack -> stack.is(coin), need, player.inventoryMenu.getCraftSlots());
                return true;
            }
        }
        return false;
    }

    /** 参数串保留目标名/类别/分支/数值/反馈开关，匹配玩家的来源顺序由通用命令承担。 */
    public static boolean command(CommandSourceStack source, String raw) {
        String[] arguments = raw.trim().split("\\s+");
        if (arguments.length < 3) return false;
        boolean deposit = arguments[1].equals("numerical") && arguments[2].equals("depositinbank");
        boolean price = arguments[1].equals("set") && EconomyPrices.KEYS.contains(arguments[2]);
        if (!deposit && !price) return false;
        double value = CommerceGoods.number(arguments.length > 3 ? arguments[3] : "");
        for (ServerPlayer player : source.getLevel().players()) {
            if (player.getDisplayName().getString().equals(arguments[0])) {
                if (deposit) BankAccounts.creditValue(player, value);
                else EconomyPrices.get(source.getLevel()).set(arguments[2], value);
                if (price && arguments.length > 4 && arguments[4].equals("true") && source.getEntity() instanceof ServerPlayer executor) {
                    executor.displayClientMessage(Component.literal("§6已重设" + priceName(arguments[2]) + value), false);
                }
            } else if (source.getEntity() instanceof ServerPlayer executor) {
                executor.displayClientMessage(Component.literal("§4未找到名为“" + arguments[0] + "”的玩家"), false);
            }
        }
        return true;
    }

    private static String priceName(String key) {
        return switch (key) {
            case "gongfajiage" -> "功法技能书价格";
            case "tuzhijiage" -> "图纸书价格";
            case "zhuangbeijiage" -> "武器、法宝、装备价格";
            case "danyaojiage" -> "丹药价格";
            case "fulujiage" -> "符箓价格";
            case "shipinjiage" -> "饰品价格";
            case "yaocaijiage" -> "药材价格";
            case "cailiaojiage" -> "炼器材料价格";
            default -> throw new IllegalArgumentException(key);
        };
    }
}
