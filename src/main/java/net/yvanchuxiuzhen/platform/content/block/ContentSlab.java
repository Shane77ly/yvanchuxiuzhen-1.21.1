package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 台阶，属性取自内容表行。 */
public class ContentSlab extends SlabBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentSlab(BlockTraits traits) {
        super(traits.properties());
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<SlabBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }
}
