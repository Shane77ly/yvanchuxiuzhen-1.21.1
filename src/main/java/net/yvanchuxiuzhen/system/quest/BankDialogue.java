package net.yvanchuxiuzhen.system.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.economy.api.BankDialogueRequestEvent;
import net.yvanchuxiuzhen.system.economy.api.BankMenus;
import net.yvanchuxiuzhen.system.quest.api.BankDialogueActions;
import net.yvanchuxiuzhen.system.quest.api.BankDialogueSession;
import net.yvanchuxiuzhen.system.quest.data.BankDialogueData;

/** 本人高级玉启动的来源单步对话，服务端END刻清启动标记与脚本后打开登录页。 */
public final class BankDialogue {
    private BankDialogue() {
    }

    public static void begin(BankDialogueRequestEvent event) {
        BankDialogueActions.request(event.player());
    }

    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BankDialogueSession session = BankDialogueActions.snapshot(player);
        if (!session.begin()) return;
        player.setData(BankDialogueData.SESSION, new BankDialogueSession(session.script(), 1, false));
        player.setData(BankDialogueData.SESSION, BankDialogueSession.EMPTY);
        BankMenus.openLogin(player, BlockPos.containing(player.getX(), player.getY() + 1, player.getZ()));
    }

    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) player.setData(BankDialogueData.SESSION, BankDialogueSession.EMPTY);
    }
}
