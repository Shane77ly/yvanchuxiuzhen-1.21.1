package net.yvanchuxiuzhen.system.skill;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;

/**
 * 生成技能书的四个命令（来源 {@code JinengCommand}、{@code SmanshipCommand}、{@code ShenfaCommand}、{@code FlyskillCommand}）：
 * 参数按空白分隔，含义见 {@link SkillLibrary#fromArguments}。{@code /jineng} 把书掉落在命令位置（加 0.5），拾取延迟 10 刻，
 * 没有权限要求（Q36）；其余三个要求权限 1，书直接放进执行者的背包，执行者不是玩家时没有效果。
 */
public final class SkillCommands {
    private static final int PICKUP_DELAY = 10;

    private SkillCommands() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SkillCommands::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(command("jineng", 0, SkillKind.SPELL));
        event.getDispatcher().register(command("smanship", 1, SkillKind.WEAPON_ART));
        event.getDispatcher().register(command("shenfa", 1, SkillKind.MOVEMENT));
        event.getDispatcher().register(command("flyskill", 1, SkillKind.FLIGHT));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command(String name, int permission, SkillKind kind) {
        return Commands.literal(name).requires(source -> source.hasPermission(permission))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> run(context, kind)));
    }

    private static int run(CommandContext<CommandSourceStack> context, SkillKind kind) {
        List<String> arguments = Arrays.asList(StringArgumentType.getString(context, "arguments").trim().split("\\s+"));
        ItemStack book = SkillLibrary.fromArguments(kind, arguments);
        if (book.isEmpty()) {
            return 0;
        }
        if (kind == SkillKind.SPELL) {
            drop(context.getSource().getLevel(), context.getSource().getPosition(), book);
        } else if (context.getSource().getEntity() instanceof ServerPlayer player) {
            ItemHandlerHelper.giveItemToPlayer(player, book);
        }
        return 0;
    }

    /** 把书掉落在位置（加 0.5）处，拾取延迟 10 刻。 */
    static void drop(ServerLevel level, Vec3 position, ItemStack book) {
        ItemEntity entity = new ItemEntity(level, position.x + 0.5, position.y + 0.5, position.z + 0.5, book);
        entity.setPickUpDelay(PICKUP_DELAY);
        level.addFreshEntity(entity);
    }
}
