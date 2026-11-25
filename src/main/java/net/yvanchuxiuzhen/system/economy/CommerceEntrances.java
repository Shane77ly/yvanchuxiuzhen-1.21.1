package net.yvanchuxiuzhen.system.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.economy.api.EconomyDialogueRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;

/** 天地银行全局右键入口，先设置本人商品会话，再请求银行模板对话。 */
public final class CommerceEntrances {
    private CommerceEntrances() { }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String block = BuiltInRegistries.BLOCK.getKey(event.getLevel().getBlockState(event.getPos()).getBlock()).toString();
        if (!block.equals("yvanchuxiuzhen:tiandiyinhang")) return;
        EconomyMenus.setShopCatalog(player, EconomyCatalogs.catalog("bank"));
        NeoForge.EVENT_BUS.post(new EconomyDialogueRequestEvent(player, "bank_block_dialogue"));
    }
}
