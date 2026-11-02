package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 楼梯（基础状态为空气，与来源一致），属性取自内容表行。 */
public class ContentStairs extends StairBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentStairs(BlockTraits traits) {
        super(Blocks.AIR.defaultBlockState(), traits.properties());
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<StairBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }
}
