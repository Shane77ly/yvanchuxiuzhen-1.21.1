package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.cooking.SteamerRuntime;

/** 蒸锅与笼屉的库存掉落、比较器及自然方块入口；蒸锅先由事件开窗。 */
public abstract class SteamerBlock extends ContentBlock implements EntityBlock {
    protected SteamerBlock(BlockTraits traits) {
        super(traits);
    }

    public abstract boolean tray();

    public abstract IntegerProperty animationProperty();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(animationProperty());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamerBlockEntity(pos, state);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SteamerBlockEntity pot ? pot : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        SteamerRuntime.used(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        SteamerRuntime.used(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity pot = level.getBlockEntity(pos);
        return pot != null && pot.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SteamerBlockEntity pot) {
            Containers.dropContents(level, pos, pot);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof SteamerBlockEntity pot
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(pot) : 0;
    }

    public static final class Pot extends SteamerBlock {
        public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 3);

        public Pot(BlockTraits traits) { super(traits); }

        @Override
        public boolean tray() { return false; }

        @Override
        public IntegerProperty animationProperty() { return ANIMATION; }
    }

    public static final class Tray extends SteamerBlock {
        public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 3);

        public Tray(BlockTraits traits) { super(traits); }

        @Override
        public boolean tray() { return true; }

        @Override
        public IntegerProperty animationProperty() { return ANIMATION; }
    }
}
