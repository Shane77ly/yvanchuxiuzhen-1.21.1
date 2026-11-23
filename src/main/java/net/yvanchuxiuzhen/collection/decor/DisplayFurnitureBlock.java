package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.content.BlockDisplayLock;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 展示柜开三槽菜单；博古架右键换件、潜行依来源保存并切换形态。 */
public class DisplayFurnitureBlock extends ContentBlock implements EntityBlock {
    private final DisplayFurnitureVariant variant;

    public DisplayFurnitureBlock(BlockTraits traits, DisplayFurnitureVariant variant) { super(traits); this.variant = variant; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DisplayFurnitureBlockEntity(variant, pos, state); }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        ((DisplayFurnitureBlockEntity) level.getBlockEntity(pos)).placed(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        interact(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private void interact(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        DisplayFurnitureBlockEntity display = (DisplayFurnitureBlockEntity) level.getBlockEntity(pos);
        if (BlockDisplayLock.locked(display)) return;
        if (variant.cabinet()) {
            if (player instanceof ServerPlayer server) {
                server.openMenu(new SeamlessMenuProvider(display::createMenu, display.getDisplayName()), pos);
            }
        } else if (player.isShiftKeyDown()) {
            CompoundTag saved = display.saveWithFullMetadata(level.registryAccess());
            display.setRemoved();
            BlockState next = DisplayFurnitureContent.block(variant.next()).defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, level.getBlockState(pos).getValue(BlockStateProperties.HORIZONTAL_FACING));
            if (next.hasProperty(BlockStateProperties.WATERLOGGED) && level.getBlockState(pos).hasProperty(BlockStateProperties.WATERLOGGED)) {
                next = next.setValue(BlockStateProperties.WATERLOGGED, level.getBlockState(pos).getValue(BlockStateProperties.WATERLOGGED));
            }
            level.setBlock(pos, next, 3);
            ((DisplayFurnitureBlockEntity) level.getBlockEntity(pos)).loadWithComponents(saved, level.registryAccess());
        } else {
            ItemStack held = player.getMainHandItem();
            ItemStack previous = display.handler(null).getStackInSlot(0).copy();
            if (!held.isEmpty()) {
                if (!previous.isEmpty()) {
                    ItemHandlerHelper.giveItemToPlayer(player, previous.copyWithCount(1));
                    display.setItem(0, ItemStack.EMPTY);
                }
                display.setItem(0, held.copyWithCount(1));
                held.shrink(1);
            } else if (!previous.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, previous.copyWithCount(1));
                display.setItem(0, ItemStack.EMPTY);
            }
        }
    }

    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return (DisplayFurnitureBlockEntity) level.getBlockEntity(pos);
    }
    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity display = level.getBlockEntity(pos);
        return display != null && display.triggerEvent(id, parameter);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (state.getBlock() != next.getBlock()) {
            if (level.getBlockEntity(pos) instanceof DisplayFurnitureBlockEntity display) {
                Containers.dropContents(level, pos, display);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, next, moved);
        }
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof DisplayFurnitureBlockEntity display ? AbstractContainerMenu.getRedstoneSignalFromContainer(display) : 0;
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(DisplayFurnitureContent.block(variant.cabinet() ? variant : DisplayFurnitureVariant.STAND_1))) : drops;
    }
    @Override protected FluidState getFluidState(BlockState state) {
        return state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
}
