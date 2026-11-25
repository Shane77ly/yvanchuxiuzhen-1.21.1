package net.yvanchuxiuzhen.system.quest.api;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.quest.data.QuestData;

/** 对话与任务目录的公开入口；调用方提交强类型模板或模板 ID，客户端只读当前投影。 */
public final class DialogueActions {
    private DialogueActions() {
    }

    public static void request(ServerPlayer player, String templateId) {
        request(player, QuestTemplates.require(templateId));
    }

    public static void request(ServerPlayer player, QuestTemplateDefinition template) {
        CompoundTag session = snapshot(player);
        writeTemplate(session, template);
        session.putInt("renwuweizhi", 0);
        session.putBoolean("renwubegin", true);
        player.setData(QuestData.SESSION, session);
    }

    public static void writeTemplate(CompoundTag session, QuestTemplateDefinition template) {
        if (template == null) {
            clearTemplate(session);
            return;
        }
        session.putString("template_id", template.id());
        session.put("template", QuestTemplateDefinition.CODEC.encodeStart(NbtOps.INSTANCE, template).getOrThrow());
    }

    public static void clearTemplate(CompoundTag session) {
        session.remove("template_id");
        session.remove("template");
    }

    public static Optional<QuestTemplateDefinition> activeTemplate(Player player) {
        return readTemplate(player.getData(QuestData.SESSION));
    }

    public static Optional<QuestTemplateDefinition> readTemplate(CompoundTag tag) {
        Tag raw = tag.get("template");
        if (raw == null) return Optional.empty();
        return QuestTemplateDefinition.CODEC.parse(NbtOps.INSTANCE, raw).result();
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
