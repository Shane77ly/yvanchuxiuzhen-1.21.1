package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** 四阶段蔬菜先检查土壤湿度，再以光照和20%概率推进；骨粉保留蒜苔的普通生长分支。 */
final class VegetableGrowth {
    private VegetableGrowth() {
    }

    static void grow(VegetableCrop crop, int stage, BlockState state, ServerLevel level, BlockPos pos) {
        if (stage >= 4) return;
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (level.getMaxLocalRawBrightness(pos) >= 9 && Math.random() < 0.2
                && state.is(crop.block(stage))) {
            level.setBlock(pos, crop.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    static void bonemeal(VegetableCrop crop, int stage, ServerLevel level, BlockPos pos, BlockState state) {
        if (crop == VegetableCrop.SHICAI_015 && stage == 3) {
            grow(crop, stage, state, level, pos);
        } else if (stage < 4 && state.is(crop.block(stage))) {
            level.setBlock(pos, crop.block(4).defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
