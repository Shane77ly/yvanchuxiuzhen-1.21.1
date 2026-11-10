package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 未成熟及采摘后阶段的湿度枯萎、亮度生长与骨粉；02505骨粉沿用普通生长。 */
public final class GrainGrowingBlock extends GrainBlock implements BonemealableBlock {
    private final int stage;

    public GrainGrowingBlock(BlockTraits traits, GrainCrop crop, int stage) {
        super(traits, crop, false);
        this.stage = stage;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        grow(level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        grow(level, pos);
    }

    private void grow(ServerLevel level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (level.getMaxLocalRawBrightness(pos) >= 9 && Math.random() < 0.2) {
            level.setBlock(pos, crop.block(crop.nextStage(stage)).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return Math.random() < 0.5;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (crop == GrainCrop.SHICAI_025 && stage == 5) {
            grow(level, pos);
        } else {
            // 本族各阶段均无状态属性，来源同名属性复制在这些方块上等同于成熟默认状态。
            level.setBlock(pos, crop.block(4).defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
