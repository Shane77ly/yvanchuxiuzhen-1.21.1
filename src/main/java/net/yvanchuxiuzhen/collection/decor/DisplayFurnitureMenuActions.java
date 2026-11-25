package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/** 博古架独立菜单保留按钮的返还后换型，以及玩家刻中按玩家坐标清槽的来源回调。 */
public final class DisplayFurnitureMenuActions {
    private DisplayFurnitureMenuActions() { }

    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.containerMenu instanceof DisplayFurnitureMenu menu
                && !menu.cabinet() && menu.getSlot(0).getItem().isEmpty()) {
            IItemHandler handler = player.level().getCapability(Capabilities.ItemHandler.BLOCK, player.blockPosition(), null);
            if (handler instanceof IItemHandlerModifiable inventory) inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public static void switchShape(Player player, BlockPos pos) {
        Level level = player.level();
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler != null && !handler.getStackInSlot(0).isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, handler.getStackInSlot(0).copyWithCount(1));
            if (handler instanceof IItemHandlerModifiable inventory) inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
        String direction = level.getBlockEntity(pos) instanceof DisplayFurnitureBlockEntity furniture ? furniture.displayDirection() : "";
        BlockState state = level.getBlockState(pos);
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : state.hasProperty(BlockStateProperties.FACING) ? state.getValue(BlockStateProperties.FACING)
                : state.hasProperty(BlockStateProperties.AXIS) ? Direction.fromAxisAndDirection(state.getValue(BlockStateProperties.AXIS), Direction.AxisDirection.POSITIVE)
                : Direction.NORTH;
        DisplayFurnitureVariant next = DisplayFurnitureVariant.STAND_1;
        for (DisplayFurnitureVariant variant : DisplayFurnitureVariant.values()) {
            if (!variant.cabinet() && state.is(DisplayFurnitureContent.block(variant))) { next = variant.next(); break; }
        }
        level.setBlock(pos, DisplayFurnitureContent.block(next).defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof DisplayFurnitureBlockEntity updated) updated.setDisplayDirection(direction);
        BlockState changed = level.getBlockState(pos);
        if (BlockStateProperties.HORIZONTAL_FACING.getPossibleValues().contains(facing)) {
            level.setBlock(pos, changed.setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        }
    }
}
