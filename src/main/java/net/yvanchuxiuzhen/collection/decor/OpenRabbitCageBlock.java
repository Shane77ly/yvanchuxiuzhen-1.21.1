package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 开笼每二十刻引导动物；碰撞关闭后原生实体留在闭笼内。 */
public final class OpenRabbitCageBlock extends RabbitCageBlock implements EntityBlock {
    public OpenRabbitCageBlock(BlockTraits traits) { super(traits, true); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RabbitCageBlockEntity(pos, state); }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        level.scheduleTick(pos, this, 20);
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        RabbitCages.tick(level, pos, (RabbitCageBlockEntity) level.getBlockEntity(pos));
        level.scheduleTick(pos, this, 20);
    }

    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level instanceof ServerLevel server) RabbitCages.capture(server, pos, entity);
    }

    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RabbitCageBlockEntity cage ? cage : null;
    }

    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RabbitCageBlockEntity cage) {
                Containers.dropContents(level, pos, cage);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, next, moved);
        }
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RabbitCageBlockEntity cage ? AbstractContainerMenu.getRedstoneSignalFromContainer(cage) : 0;
    }
}
