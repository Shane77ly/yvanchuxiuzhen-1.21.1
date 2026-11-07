package net.yvanchuxiuzhen.system.gongfa;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** 来源 {@code TechniqueCommand}：按参数生成功法书并掉落在命令执行位置。 */
final class TechniqueCommands {
    private static final int PICKUP_DELAY = 10;

    private TechniqueCommands() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(TechniqueCommands::onRegister);
    }

    private static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("technique").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString())
                        .executes(context -> run(context, parse(context))))
                .executes(context -> run(context, List.of())));
    }

    private static List<String> parse(CommandContext<CommandSourceStack> context) {
        String raw = StringArgumentType.getString(context, "arguments").trim();
        return raw.isEmpty() ? List.of() : Arrays.asList(raw.split("\\s+"));
    }

    private static int run(CommandContext<CommandSourceStack> context, List<String> arguments) {
        ItemStack book = TechniqueLibrary.fromArguments(arguments);
        if (book.isEmpty()) {
            return 0;
        }
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 position = source.getPosition();
        ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, book);
        entity.setPickUpDelay(PICKUP_DELAY);
        level.addFreshEntity(entity);
        return 0;
    }
}
