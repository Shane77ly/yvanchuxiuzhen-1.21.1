package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 栅栏门（木质开关声），属性取自内容表行。 */
public class ContentFenceGate extends FenceGateBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentFenceGate(BlockTraits traits) {
        super(traits.properties(), SoundEvents.FENCE_GATE_OPEN, SoundEvents.FENCE_GATE_CLOSE);
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<FenceGateBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }
}
