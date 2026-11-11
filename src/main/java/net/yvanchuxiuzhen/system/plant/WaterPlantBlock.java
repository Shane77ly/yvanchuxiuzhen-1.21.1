package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 两种水生植物在放置后依据上方流体的方块态补写含水状态。 */
public final class WaterPlantBlock extends WaterloggedContentBlock {
    public WaterPlantBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moving) {
        super.onPlace(state, level, pos, previous, moving);
        if (level.getFluidState(pos.above()).createLegacyBlock().getFluidState().isSource()) {
            BlockState current = level.getBlockState(pos);
            level.setBlock(pos, current.setValue(BlockStateProperties.WATERLOGGED, true), 3);
        }
    }
}
