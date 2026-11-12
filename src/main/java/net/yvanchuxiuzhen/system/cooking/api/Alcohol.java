package net.yvanchuxiuzhen.system.cooking.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.cooking.data.CookingAttachments;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 烹饪的酒精查询、饮用加减、每秒代谢和观想炼化操作。 */
public final class Alcohol {
    private Alcohol() {
    }

    public static double amount(Player player) {
        return player.getData(CookingAttachments.ALCOHOL);
    }

    public static void add(ServerPlayer player, double delta) {
        player.setData(CookingAttachments.ALCOHOL, amount(player) + delta);
    }

    /** 在来源每秒段的突破处理后、修炼冷却前调用；公式允许负值或正增长。 */
    public static void metabolize(ServerPlayer player) {
        if (amount(player) >= 1) {
            add(player, -1 + Cultivation.tizhiDouble(player, "AlcoholDigestion")
                    + 100 * Cultivation.tizhiDouble(player, "AlcoholDigestionPercentage"));
        }
    }

    /** 来源观想只在当前酒精至少1时清零、移除反胃并提示。 */
    public static void refine(ServerPlayer player) {
        if (amount(player) >= 1) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.cooking.wine.refined"), false);
            player.setData(CookingAttachments.ALCOHOL, 0.0);
            player.removeEffect(MobEffects.CONFUSION);
        }
    }
}
