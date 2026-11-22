package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.RenjianEnteredEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 人间道入场对玩家体质和背包的影响，与全服固定场景标记相互独立。 */
final class RenjianArrival {
    private RenjianArrival() {
    }

    static void onEntered(RenjianEnteredEvent event) {
        ServerPlayer player = event.player();
        if (Cultivation.tizhiBoolean(player, "shengxianling")) {
            Delayed.run(80, () -> {
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.removeEffect(MobEffects.LEVITATION);
            });
            Cultivation.setTizhiBoolean(player, "shengxianling", false);
        }
        if (!Cultivation.tizhiBoolean(player, "chuanjiegujing")) {
            give(player, "fabao_013");
            give(player, "qiankundai_06");
            Cultivation.setTizhiBoolean(player, "chuanjiegujing", true);
        }
    }

    private static void give(ServerPlayer player, String path) {
        ItemHandlerHelper.giveItemToPlayer(player,
                new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path))));
    }
}
