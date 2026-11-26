package net.yvanchuxiuzhen.system.dungeon;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;

/** 副本中央标题从每位区域玩家的位置选择全服最近玩家；等距时保留玩家列表次序。 */
final class DungeonTitles {
    private static final Style RESULT_STYLE = Style.EMPTY.withBold(true).withItalic(false).withUnderlined(false)
            .withStrikethrough(false).withObfuscated(false);
    private static final Component FAILURE = Component.literal("失 败").setStyle(RESULT_STYLE.withColor(ChatFormatting.BLACK));
    private static final Component SUCCESS = Component.literal("通 关").setStyle(RESULT_STYLE.withColor(ChatFormatting.GOLD));

    private DungeonTitles() {
    }

    static void prepare(List<ServerPlayer> sources, String name) {
        if (sources.isEmpty()) return;
        ServerPlayer first = sources.getFirst();
        Component title = parseName(CommandBuildContext.simple(first.registryAccess(), first.level().enabledFeatures()), name);
        if (title == null) return;
        for (ServerPlayer source : sources) {
            ServerPlayer target = nearest(source);
            if (target == null) continue;
            try {
                Component resolved = ComponentUtils.updateForEntity(source.createCommandSourceStack().withSuppressedOutput().withPermission(4), title, target, 0);
                target.connection.send(new ClientboundSetTitleTextPacket(resolved));
            } catch (CommandSyntaxException invalidComponent) {
                // 来源按每位执行者独立解析动态组件；无法解析时不发送给该目标。
            }
        }
    }

    /** 名称按来源直接嵌入组件参数。无效 JSON 或组件后仍有输入时，该次标题不显示。 */
    @Nullable
    private static Component parseName(CommandBuildContext context, String name) {
        StringReader input = new StringReader("[{\"text\":\"" + name + "\",\"color\":\"black\",\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}]");
        try {
            Component component = ComponentArgument.textComponent(context).parse(input);
            return input.canRead() ? null : component;
        } catch (CommandSyntaxException invalidName) {
            return null;
        }
    }

    static void result(ServerPlayer source, boolean success) {
        ServerPlayer target = nearest(source);
        if (target != null) target.connection.send(new ClientboundSetTitleTextPacket(success ? SUCCESS : FAILURE));
    }

    @Nullable
    private static ServerPlayer nearest(ServerPlayer source) {
        return source.server.getPlayerList().getPlayers().stream()
                .min(Comparator.comparingDouble(player -> player.distanceToSqr(source.position()))).orElse(null);
    }
}
