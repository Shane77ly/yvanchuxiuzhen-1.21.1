package net.yvanchuxiuzhen.system.craft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 三种展示蛹共用的支撑检查，库存内容不参与判断。 */
public final class ChrysalisDisplayBlock extends WaterloggedContentBlock {
    public ChrysalisDisplayBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        Block box = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canhe"));
        if (!level.getBlockState(pos.below()).is(box)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
