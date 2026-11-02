package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 按钮（按下 30 tick），属性取自内容表行。 */
public class ContentButton extends ButtonBlock implements TraitedBlock {
    private final BlockTraits traits;

    public ContentButton(BlockTraits traits, BlockSetType type) {
        super(type, 30, traits.properties());
        this.traits = traits;
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    public MapCodec<ButtonBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }
}
