package net.yvanchuxiuzhen.system.quest.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.quest.data.BankDialogueData;

/** 单步银行对话的公开请求与只读会话查询；其他任务状态不由此入口维护。 */
public final class BankDialogueActions {
    private BankDialogueActions() {
    }

    public static BankDialogueSession snapshot(Player player) {
        return player.getData(BankDialogueData.SESSION);
    }

    public static void request(ServerPlayer player) {
        player.setData(BankDialogueData.SESSION, new BankDialogueSession(BankDialogueSession.SCRIPT, 0, true));
    }
}
