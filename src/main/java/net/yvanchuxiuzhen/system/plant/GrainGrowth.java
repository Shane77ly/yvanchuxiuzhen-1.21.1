package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** 谷物族共享的湿度枯萎、亮度成长和骨粉推进规则。 */
final class GrainGrowth {
    private GrainGrowth() {
    }

    static void tick(GrainCrop crop, int stage, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (level.getMaxLocalRawBrightness(pos) >= 9 && random.nextFloat() < 0.2F) {
            level.setBlock(pos, crop.block(crop.nextStage(stage)).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    static void bonemeal(GrainCrop crop, int stage, ServerLevel level, BlockPos pos, RandomSource random) {
        if (crop == GrainCrop.SHICAI_025 && stage == 5) {
            tick(crop, stage, level, pos, random);
        } else {
            level.setBlock(pos, crop.block(4).defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
