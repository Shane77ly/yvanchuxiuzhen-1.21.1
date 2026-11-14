package net.yvanchuxiuzhen.system.quest.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 对话与任务目录的公开入口；调用方提交来源脚本，客户端只读当前投影。 */
public final class DialogueActions {
    private DialogueActions() {
    }

    public static void request(ServerPlayer player, String script) {
        CompoundTag session = snapshot(player);
        session.putString("renwulist", script);
        session.putInt("renwuweizhi", 0);
        session.putBoolean("renwubegin", true);
        player.setData(QuestData.SESSION, session);
    }

    public static CompoundTag snapshot(Player player) {
        return player.getData(QuestData.SESSION).copy();
    }

    public static CompoundTag journal(Player player) {
        return player.getData(QuestData.JOURNAL).copy();
    }

    public static boolean finished(Player player, String id) {
        String state = journal(player).getString(id + "zhuangtai");
        return state.equals("完成") || state.equals("失败");
    }
}
