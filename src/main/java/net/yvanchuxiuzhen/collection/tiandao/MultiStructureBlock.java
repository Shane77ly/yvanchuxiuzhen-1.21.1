package net.yvanchuxiuzhen.collection.tiandao;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 天道多结构生成器每二十刻读取自身配置，保持来源无右键配置界面的入口。 */
public final class MultiStructureBlock extends ContentBlock implements EntityBlock {
    public MultiStructureBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiStructureBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        super.onPlace(state, level, pos, previous, moved);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof MultiStructureBlockEntity generator) {
            generator.generate(level);
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MultiStructureBlockEntity generator ? generator : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity generator = level.getBlockEntity(pos);
        return generator != null && generator.triggerEvent(id, parameter);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }
}
