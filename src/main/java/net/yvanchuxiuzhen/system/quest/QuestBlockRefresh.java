package net.yvanchuxiuzhen.system.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;

/** 全局方块右键在来源手别筛选后先修补任务方块配置，随后由原生方块交互消费。 */
public final class QuestBlockRefresh {
    private static final BlockPos MATERIAL_SHOP = new BlockPos(-103, 64, -263);
    private static final BlockPos CLOTHING_SHOP = new BlockPos(-51, 64, -320);

    private QuestBlockRefresh() {
    }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        if (!pos.equals(MATERIAL_SHOP) && !pos.equals(CLOTHING_SHOP)) return;
        if (!(level.getBlockEntity(pos) instanceof DisguisedQuestBlockEntity block)) return;
        if (pos.equals(MATERIAL_SHOP)) {
            if (!block.refreshDue()) return;
            block.setRefreshDue(false);
            block.setProductList(1, EconomyCatalogs.materialShop(1));
        } else {
            if (!block.shopInformation(1).equals(QuestBlockRefreshData.OLD_CLOTHING_INFORMATION)) return;
            block.setShopInformation(1, QuestBlockRefreshData.CLOTHING_INFORMATION);
            block.setProductList(1, QuestBlockRefreshData.CLOTHING_PRODUCTS);
        }
        level.sendBlockUpdated(pos, block.getBlockState(), block.getBlockState(), 3);
    }
}
