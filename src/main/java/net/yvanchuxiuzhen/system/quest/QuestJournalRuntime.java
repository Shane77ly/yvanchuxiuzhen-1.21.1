package net.yvanchuxiuzhen.system.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplates;
import net.yvanchuxiuzhen.system.quest.content.QuestJournalMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 任务列表由服务端会话保存分类、原始目录页码、八行快照及当前选择，目录列表以强类型 ListTag 存储。 */
public final class QuestJournalRuntime {
    private static final String UNFINISHED = "未完成任务";
    private static final String FINISHED = "已完成任务";
    private static final List<String> ROW_FIELDS = List.of("name", "UUID", "shuoming", "xvqiu", "jiangli");

    private QuestJournalRuntime() {
    }

    public static void open(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        player.openMenu(new SeamlessMenuProvider((id, inventory, ignored) -> new QuestJournalMenu(id, inventory, pos),
                Component.literal("任务列表")), pos);
        CompoundTag state = DialogueActions.snapshot(player);
        if (state.getString("rendeleibie").isEmpty()) state.putString("rendeleibie", UNFINISHED);
        for (String field : List.of("renwuname", "TXTUUID", "TXTxianshi", "renwuxvqiu", "renwujiangli")) state.putString(field, "");
        for (int row = 1; row <= 8; row++) {
            for (String field : ROW_FIELDS) state.putString("renwuxianshi" + row + field, "");
            state.putBoolean("renwuxianshi" + row + "gaojirenwu", false);
            state.putString("text0" + row, "");
        }
        CompoundTag journal = DialogueActions.journal(player);
        ListTag idList = journal.getList("quest_ids", Tag.TAG_STRING);
        List<String> ids = new ArrayList<>(idList.size());
        for (int index = 0; index < idList.size(); index++) ids.add(idList.getString(index));
        // 来源先以整数除以八，再求上取整；页码范围也包含该结果。
        state.putInt("renwuzongye", ids.size() / 8);
        int row = 1;
        for (int index = state.getInt("renwuyenum") * 8; index < ids.size() && row <= 8; index++) {
            String id = ids.get(index);
            boolean unfinished = journal.getString(id + "zhuangtai").equals("未完成");
            boolean showingUnfinished = state.getString("rendeleibie").equals(UNFINISHED);
            if (!(unfinished && showingUnfinished) && !(!unfinished && state.getString("rendeleibie").equals(FINISHED))) continue;
            String prefix = "renwuxianshi" + row;
            for (String field : ROW_FIELDS) state.putString(prefix + field, journal.getString(id + field));
            if (unfinished) {
                state.putString(prefix + "name", journal.getString(id + "name") + "（进行中）");
                state.putBoolean(prefix + "gaojirenwu", journal.getBoolean(id + "gaojirenwu"));
            }
            row++;
        }
        player.setData(QuestData.SESSION, state);
    }

    public static boolean click(ServerPlayer player, int button) {
        if (button < 0 || button > 12) return false;
        CompoundTag state = DialogueActions.snapshot(player);
        if (button <= 1) {
            state.putString("rendeleibie", state.getString("rendeleibie").equals(UNFINISHED) ? FINISHED : UNFINISHED);
            player.setData(QuestData.SESSION, state);
            open(player);
        } else if (button <= 3) {
            int page = state.getInt("renwuyenum") + (button == 2 ? -1 : 1);
            if (page < 0) page = state.getInt("renwuzongye");
            if (page > state.getInt("renwuzongye")) page = 0;
            state.putInt("renwuyenum", page);
            player.setData(QuestData.SESSION, state);
            open(player);
        } else if (button <= 11) {
            select(player, state, button - 3);
        } else {
            continueTask(player, state);
        }
        return true;
    }

    private static void select(ServerPlayer player, CompoundTag state, int row) {
        String prefix = "renwuxianshi" + row;
        state.putString("renwuname", state.getString(prefix + "name"));
        state.putString("TXTUUID", state.getString(prefix + "UUID"));
        String description = state.getString(prefix + "shuoming");
        state.putString("TXTxianshi", description);
        for (int line = 1; line <= 8; line++) state.putString("text0" + line, "");
        int lines = Math.max(1, (int) Math.ceil(description.length() / 18.0));
        for (int line = 0; line < lines; line++) {
            state.putString("text" + String.format(Locale.ROOT, "%02d", line + 1),
                    description.substring(line * 18, Math.min(description.length(), (line + 1) * 18)));
        }
        state.putString("renwuxvqiu", state.getString(prefix + "xvqiu"));
        state.putString("renwujiangli", state.getString(prefix + "jiangli"));
        state.putBoolean("gaojirenwu", state.getBoolean(prefix + "gaojirenwu"));
        player.setData(QuestData.SESSION, state);
    }

    private static void continueTask(ServerPlayer player, CompoundTag state) {
        if (!state.getString("rendeleibie").equals(FINISHED)) {
            String id = state.getString("TXTUUID");
            if (id.isEmpty()) {
                player.displayClientMessage(Component.literal("???任务是空的"), false);
            } else {
                CompoundTag journal = DialogueActions.journal(player);
                QuestTemplateDefinition template = QuestTemplateDefinition.CODEC.parse(NbtOps.INSTANCE,
                                journal.get(id + "template")).result()
                        .or(() -> QuestTemplates.find(journal.getString(id + "template_id")))
                        .orElse(null);
                if (template == null) {
                    player.displayClientMessage(Component.literal("???任务模板缺失：" + id), false);
                } else {
                    DialogueActions.writeTemplate(state, template);
                    state.putInt("renwuweizhi", journal.getInt(id + "jindu"));
                    if (state.getBoolean("gaojirenwu")) {
                        state.putBoolean("renwubegin", true);
                    } else {
                        state.putBoolean("taskbegin", true);
                    }
                }
            }
        }
        player.setData(QuestData.SESSION, state);
    }
}
