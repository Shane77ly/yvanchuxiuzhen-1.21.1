package net.yvanchuxiuzhen.system.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlockEntity;
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenu;

/** 任务方块编辑页只更新本系统持有的模板 ID 与商店 ID 配置。 */
public final class QuestTriggerEditorRuntime {
    private QuestTriggerEditorRuntime() {
    }

    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (!(event.getEntity() instanceof ServerPlayer player) || player.gameMode.getGameModeForPlayer() != GameType.CREATIVE
                || !player.isShiftKeyDown()) return;
        if (!(player.level().getBlockEntity(event.getPos()) instanceof DisguisedQuestBlockEntity)) return;
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new QuestTriggerEditorMenu(id, inventory, event.getPos()),
                Component.literal("任务方块设置")), buffer -> buffer.writeBlockPos(event.getPos()));
    }

    public static void apply(ServerPlayer player, DisguisedQuestBlockEntity block, String state) {
        player.displayClientMessage(Component.literal("设置任务事件"), false);
        if (block == null) return;
        String[] entries = state.split(",");
        for (int row = 1; row <= 10; row++) {
            String questId = value(entries, "renwulist" + row + ":");
            if (!questId.isEmpty()) {
                if (value(entries, "gaojirenwu" + row + "-").equals("1")) block.setAdvancedQuest(row, questId);
                else block.setOrdinaryQuest(row, questId);
            }
            String information = value(entries, "shopinformation" + row + ":");
            String productId = value(entries, "productlist" + row + ":");
            if (!information.isEmpty()) {
                block.setShopId(row, productId.isEmpty() ? information : productId);
            } else if (!productId.isEmpty()) {
                block.setShopId(row, productId);
            }
        }
        if (value(entries, "lizi-").equals("1")) block.setShowParticles(true);
        block.setChanged();
        player.serverLevel().sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), 3);
    }

    private static String value(String[] entries, String key) {
        for (String entry : entries) {
            if (entry.contains(key)) return entry.replace(key, "");
        }
        return entries[0].replace(key, "");
    }
}
