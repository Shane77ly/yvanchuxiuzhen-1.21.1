package net.yvanchuxiuzhen.system.gongfa.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.gongfa.Comprehension;

/**
 * 参悟台（来源 {@code CanwutaiBlockBlock}）：放置时写入参悟模式与初始说明（{@code CanwutaiBlockFangZhiFangKuaiShi}），
 * 右键打开参悟界面，拆除时掉出槽内物品，比较器按槽内物品输出信号。
 */
public final class ComprehensionTableBlock extends ContentBlock implements EntityBlock {
    public ComprehensionTableBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComprehensionTableBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ComprehensionTableBlockEntity table) {
            Comprehension.placed(table);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ComprehensionTableBlockEntity table) {
            player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new ComprehensionMenu(id, inventory, table, pos),
                    Component.literal("Canwutai Block")), buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ComprehensionTableBlockEntity table) {
            Containers.dropContents(level, pos, table);
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
        return level.getBlockEntity(pos) instanceof ComprehensionTableBlockEntity table ? AbstractContainerMenu.getRedstoneSignalFromContainer(table) : 0;
    }
}
