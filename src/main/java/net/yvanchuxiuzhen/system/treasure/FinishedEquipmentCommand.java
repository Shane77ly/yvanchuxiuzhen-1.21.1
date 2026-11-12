package net.yvanchuxiuzhen.system.treasure;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;

/** /lianqi 是宝箱成品入口：通过坐标方块的槽 0 生成、读回并掉落，不写玩家档案。 */
final class FinishedEquipmentCommand {
    private FinishedEquipmentCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("lianqi").requires(source -> source.hasPermission(1))
                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(context -> run(context.getSource(),
                        Arrays.asList(StringArgumentType.getString(context, "arguments").trim().split("\\s+")))))
                .executes(context -> run(context.getSource(), List.of())));
    }

    private static int run(CommandSourceStack source, List<String> arguments) {
        BlockPos pos = BlockPos.containing(source.getPosition());
        IItemHandler handler = source.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler == null || !handler.getStackInSlot(0).isEmpty()) return 0;
        // 原生 item replace block 只写 Container；仅有物品能力的方块不会产出正常成品。
        if (!(source.getLevel().getBlockEntity(pos) instanceof Container container)) return 0;
        ItemStack product = Equipment.createProduct(source.registryAccess(), arguments);
        if (product.isEmpty()) return 0;
        container.setItem(0, product);
        ItemStack result = handler.getStackInSlot(0).copy();
        ItemEntity drop = new ItemEntity(source.getLevel(), source.getPosition().x, source.getPosition().y, source.getPosition().z, result);
        drop.setPickUpDelay(10);
        source.getLevel().addFreshEntity(drop);
        if (handler instanceof IItemHandlerModifiable modifiable) modifiable.setStackInSlot(0, ItemStack.EMPTY);
        return 0;
    }
}
