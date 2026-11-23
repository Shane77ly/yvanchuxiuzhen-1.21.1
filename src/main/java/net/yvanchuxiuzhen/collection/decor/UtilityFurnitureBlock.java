package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
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
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 都承盘和衣架打开来源菜单；紫砂壶按主手杯子顺序装入、逆序取回。 */
public class UtilityFurnitureBlock extends ContentBlock implements EntityBlock {
    private final UtilityFurnitureVariant variant;

    public UtilityFurnitureBlock(BlockTraits traits, UtilityFurnitureVariant variant) { super(traits); this.variant = variant; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new UtilityFurnitureBlockEntity(variant, pos, state); }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (variant != UtilityFurnitureVariant.TEAPOT) placed(state, level, pos);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (variant == UtilityFurnitureVariant.TEAPOT) placed(state, level, pos);
    }
    private static void placed(BlockState state, Level level, BlockPos pos) {
        ((UtilityFurnitureBlockEntity) level.getBlockEntity(pos)).placed(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
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
        UtilityFurnitureBlockEntity furniture = (UtilityFurnitureBlockEntity) level.getBlockEntity(pos);
        if (variant != UtilityFurnitureVariant.TEAPOT) {
            if (player instanceof ServerPlayer server) {
                server.openMenu(new SeamlessMenuProvider(furniture::createMenu, furniture.getDisplayName()), pos);
            }
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (held.is(UtilityFurnitureContent.cup())) {
            for (int slot = 0; slot < furniture.getContainerSize(); slot++) {
                if (furniture.getItem(slot).isEmpty()) {
                    furniture.setItem(slot, new ItemStack(UtilityFurnitureContent.cup()));
                    held.shrink(1);
                    return;
                }
            }
        } else {
            for (int slot = furniture.getContainerSize() - 1; slot >= 0; slot--) {
                if (furniture.getItem(slot).is(UtilityFurnitureContent.cup())) {
                    ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(UtilityFurnitureContent.cup()));
                    furniture.setItem(slot, ItemStack.EMPTY);
                    return;
                }
            }
        }
    }

    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return (UtilityFurnitureBlockEntity) level.getBlockEntity(pos);
    }
    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity furniture = level.getBlockEntity(pos);
        return furniture != null && furniture.triggerEvent(id, parameter);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (state.getBlock() != next.getBlock()) {
            if (level.getBlockEntity(pos) instanceof UtilityFurnitureBlockEntity furniture) {
                Containers.dropContents(level, pos, furniture);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, next, moved);
        }
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof UtilityFurnitureBlockEntity furniture ? AbstractContainerMenu.getRedstoneSignalFromContainer(furniture) : 0;
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
