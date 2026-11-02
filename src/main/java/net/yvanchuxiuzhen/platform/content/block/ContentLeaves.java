package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 树叶，属性取自内容表行。 */
public class ContentLeaves extends LeavesBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentLeaves(BlockTraits traits) {
        super(traits.properties());
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<LeavesBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        Boolean rule = traits.propagatesSkylight(state);
        return rule != null ? rule : super.propagatesSkylightDown(state, level, pos);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return traits.pushReaction(super.getPistonPushReaction(state));
    }
}
