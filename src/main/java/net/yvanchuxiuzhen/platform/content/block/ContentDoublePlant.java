package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 高两格的植物，属性、形状与可种植的地面取自内容表行；没有 ground 字段时沿用原版的泥土类判定。 */
public class ContentDoublePlant extends DoublePlantBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentDoublePlant(BlockTraits traits) {
        super(traits.properties());
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<DoublePlantBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected boolean mayPlaceOn(BlockState ground, BlockGetter level, BlockPos pos) {
        Boolean rule = traits.mayPlaceOn(ground);
        return rule != null ? rule : super.mayPlaceOn(ground, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return traits.shape(state, level, pos, super.getShape(state, level, pos, context));
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
}
