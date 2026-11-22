package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** 蔬菜族共享的湿度枯萎、亮度成长和骨粉推进规则。 */
final class VegetableGrowth {
    private VegetableGrowth() {
    }

    static void tick(VegetableCrop crop, int stage, ServerLevel level, BlockPos pos, RandomSource random) {
        if (stage >= 4) return;
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (level.getMaxLocalRawBrightness(pos) >= 9 && random.nextFloat() < 0.2F
                && level.getBlockState(pos).is(crop.block(stage))) {
            level.setBlock(pos, crop.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    static void bonemeal(VegetableCrop crop, int stage, ServerLevel level, BlockPos pos, BlockState state,
            RandomSource random) {
        if (crop == VegetableCrop.SHICAI_015 && stage == 3) {
            tick(crop, stage, level, pos, random);
        } else if (stage < 4 && state.is(crop.block(stage))) {
            level.setBlock(pos, crop.block(4).defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
