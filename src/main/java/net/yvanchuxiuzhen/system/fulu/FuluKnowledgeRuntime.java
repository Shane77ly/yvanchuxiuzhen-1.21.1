package net.yvanchuxiuzhen.system.fulu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;

/** 玩家知识按获得顺序追加，保留符头与符胆不同的反馈、持久标志和书本消费规则。 */
public final class FuluKnowledgeRuntime {
    private FuluKnowledgeRuntime() {
    }

    public static boolean unlockHead(ServerPlayer player, int head) {
        FuluKnowledge current = player.getData(FuluData.KNOWLEDGE);
        if (current.visible().heads().contains(head)) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.head_known"), false);
            return false;
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.NEUTRAL, 1, 1);
        List<Integer> heads = new ArrayList<>(current.heads());
        if (head != 0 || !heads.isEmpty()) heads.add(head);
        player.setData(FuluData.KNOWLEDGE, new FuluKnowledge(heads, current.pillars(), current.cores()));
        Component title = (head == 0 ? Component.empty()
                : Component.translatable(String.format(Locale.ROOT, "txt.yvanchuxiuzhen.huafu_futou_%02d", head)))
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withBold(true).withItalic(false)
                        .withUnderlined(false).withStrikethrough(false).withObfuscated(false));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.head_learned").withStyle(ChatFormatting.GOLD), false);
        player.getPersistentData().putBoolean("shaohui", true);
        return true;
    }

    public static boolean unlockCore(ServerPlayer player, int core) {
        FuluKnowledge current = player.getData(FuluData.KNOWLEDGE);
        if (current.visible().cores().contains(core)) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.core_known"), false);
            return false;
        }
        List<Integer> cores = new ArrayList<>(current.cores());
        if (core != 0 || !cores.isEmpty()) cores.add(core);
        player.setData(FuluData.KNOWLEDGE, new FuluKnowledge(current.heads(), current.pillars(), cores));
        if (learningBook(player.getMainHandItem())) player.getMainHandItem().shrink(1);
        else if (learningBook(player.getOffhandItem())) player.getOffhandItem().shrink(1);
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.core_learned").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    private static boolean learningBook(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("fuluget");
    }

    /** 原命令以参数4作为知识编号，参数3保留原位；玩家按执行维度中的显示名匹配。 */
    public static boolean command(CommandSourceStack source, String raw) {
        String[] arguments = raw.trim().split("\\s+");
        if (arguments.length < 3 || !arguments[1].equals("unlock")) return false;
        boolean head = arguments[2].equals("futou");
        if (!head && !arguments[2].equals("fudan")) return false;
        String ordinal = arguments.length > 4 ? arguments[4] : "";
        int index = 0;
        for (int value = 1; value <= (head ? 11 : 8); value++) {
            if (ordinal.equals(Integer.toString(value))) {
                index = value;
                break;
            }
        }
        // 来源未知编号仍追加空项并触发成功反馈，符胆也仍消费学习物品。
        for (ServerPlayer player : source.getLevel().players()) {
            if (player.getDisplayName().getString().equals(arguments[0])) {
                if (head) unlockHead(player, index);
                else unlockCore(player, index);
            } else if (source.getEntity() instanceof ServerPlayer executor) {
                executor.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.fulu.player_not_found", arguments[0])
                        .withStyle(ChatFormatting.DARK_RED), false);
            }
        }
        return true;
    }
}
