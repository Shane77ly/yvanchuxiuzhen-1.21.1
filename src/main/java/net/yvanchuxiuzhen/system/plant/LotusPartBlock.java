package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 008莲蓬及含水茎、叶在邻居更新时仅检查下方莲藕标签，失去支撑不掉落。 */
public final class LotusPartBlock extends ContentBlock {
    public LotusPartBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos fromPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, movedByPiston);
        LotusGrowth.checkSupport(level, pos);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(LotusGrowth.item("shicai_00801", 4)) : drops;
    }

    public static final class Waterlogged extends WaterloggedContentBlock {
        private final boolean leaf;

        public Waterlogged(BlockTraits traits, boolean leaf) {
            super(traits);
            this.leaf = leaf;
        }

        @Override
        protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos fromPos,
                boolean movedByPiston) {
            super.neighborChanged(state, level, pos, neighbor, fromPos, movedByPiston);
            LotusGrowth.checkSupport(level, pos);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            List<ItemStack> drops = super.getDrops(state, params);
            if (!drops.isEmpty()) return drops;
            return leaf ? List.of(new ItemStack(Blocks.LILY_PAD)) : List.of(ItemStack.EMPTY);
        }
    }
}
