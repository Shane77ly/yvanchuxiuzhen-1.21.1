package net.yvanchuxiuzhen.system.treasure;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.data.TreasureData;

/** 来源宝箱发放与视线七格内的方块设置命令，均要求权限一级。 */
final class TreasureCommands {
    private TreasureCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("baoxiangopen").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> open(context.getSource(),
                        arguments(StringArgumentType.getString(context, "arguments")))))
                .executes(context -> open(context.getSource(), List.of())));
        event.getDispatcher().register(Commands.literal("baoxiangset").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> set(context.getSource(),
                        arguments(StringArgumentType.getString(context, "arguments")))))
                .executes(context -> set(context.getSource(), List.of())));
    }

    private static int open(CommandSourceStack source, List<String> arguments) {
        TreasureRewards.open(source.getLevel(), source.getPosition(), argument(arguments, 0), argument(arguments, 1),
                TreasureRewards.number(argument(arguments, 2)), (int) TreasureRewards.number(argument(arguments, 3)),
                (int) TreasureRewards.number(argument(arguments, 4)));
        return 0;
    }

    private static int set(CommandSourceStack source, List<String> arguments) {
        Entity entity = source.getEntity();
        if (entity == null) return 0;
        BlockHitResult hit = source.getLevel().clip(new ClipContext(entity.getEyePosition(), entity.getEyePosition().add(entity.getViewVector(1).scale(7)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        BlockPos pos = hit.getBlockPos();
        BlockEntity target = source.getLevel().getBlockEntity(pos);
        if (target == null) return 0;
        TreasureContainerConfig config = new TreasureContainerConfig(argument(arguments, 0), argument(arguments, 1),
                (int) TreasureRewards.number(argument(arguments, 2)), (int) TreasureRewards.number(argument(arguments, 3)),
                (int) TreasureRewards.number(argument(arguments, 4)));
        target.setData(TreasureData.BLOCK_CONTAINER, config);
        target.setChanged();
        if (!config.category().isEmpty()) {
            source.sendSuccess(() -> Component.literal("宝箱大类型：" + config.category()), false);
            source.sendSuccess(() -> Component.literal("宝箱小类型：" + config.subtype()), false);
            source.sendSuccess(() -> Component.literal("宝箱等级：" + (int) config.level()), false);
            source.sendSuccess(() -> Component.literal("掉落数量：" + (int) config.minimum() + "~" + (int) config.maximum()), false);
        } else {
            source.sendSuccess(() -> Component.literal("未成功设置"), false);
        }
        return 0;
    }

    private static List<String> arguments(String text) {
        return Arrays.asList(text.trim().split("\\s+"));
    }

    private static String argument(List<String> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : "";
    }
}
