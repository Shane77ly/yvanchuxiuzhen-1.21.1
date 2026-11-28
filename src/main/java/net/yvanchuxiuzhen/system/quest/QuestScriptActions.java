package net.yvanchuxiuzhen.system.quest;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition.EffectNode;

/** 聊天和界面对话共用玩家位置、无施法实体的脚本上下文。 */
final class QuestScriptActions {
    private QuestScriptActions() {}

    private static CommandSourceStack source(ServerPlayer player) {
        return new CommandSourceStack(CommandSource.NULL, player.position(), Vec2.ZERO, player.serverLevel(), 4,
                "", Component.empty(), player.server, null).withSuppressedOutput();
    }

    static void command(ServerPlayer player, String command) {
        player.server.getCommands().performPrefixedCommand(source(player), command);
    }

    /** 保留来源未转义的文字参数；解析失败时不发送，选择锁仍由聊天节点持有。 */
    static void tell(ServerPlayer player, String json) {
        CommandSourceStack source = source(player);
        StringReader name = new StringReader(player.getDisplayName().getString());
        StringReader message = new StringReader(json);
        var context = CommandBuildContext.simple(player.registryAccess(), player.level().enabledFeatures());
        try {
            var selector = EntityArgument.players().parse(name, source);
            if (name.canRead()) return;
            Component component = ComponentArgument.textComponent(context).parse(message);
            if (message.canRead()) return;
            for (ServerPlayer target : selector.findPlayers(source)) {
                target.sendSystemMessage(ComponentUtils.updateForEntity(source, component, target, 0), false);
            }
        } catch (CommandSyntaxException invalidArgument) {
            // 原版收件人、文字或动态组件解析失败后，停止此次发送。
        }
    }

    static void effect(ServerPlayer player, EffectNode action) {
        boolean give = action.op().equals("加");
        if (give && (action.duration() < 1 || action.duration() > 1_000_000 || action.amplifier() < 0 || action.amplifier() > 255)) return;
        ResourceLocation id = ResourceLocation.tryParse(action.effectId());
        if (id == null) return;
        var effect = BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
        if (effect == null) return;
        for (Entity entity : targets(player)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (give) {
                int ticks = effect.value().isInstantenous() ? action.duration() : action.duration() * 20;
                target.addEffect(new MobEffectInstance(effect, ticks, action.amplifier(), false, true), null);
            } else {
                target.removeEffect(effect);
            }
        }
    }

    /** 来源将显示名作为完整目标参数，无法解析时仍推进任务游标。 */
    private static List<? extends Entity> targets(ServerPlayer player) {
        CommandSourceStack source = source(player);
        StringReader input = new StringReader(player.getDisplayName().getString());
        try {
            var selector = EntityArgument.entities().parse(input, source);
            return input.canRead() ? List.of() : selector.findEntities(source);
        } catch (CommandSyntaxException invalidTarget) {
            return List.of();
        }
    }
}
