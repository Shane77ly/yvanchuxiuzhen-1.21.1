package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** 普通作物先判满湿度，再判亮度和成长概率；骨粉只复制当前位置的兼容属性。 */
final class ProduceGrowth {
    private ProduceGrowth() {
    }

    static void tick(ProduceCrop crop, int stage, ServerLevel level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (level.getMaxLocalRawBrightness(pos) >= 9 && Math.random() < 0.2) {
            level.setBlock(pos, crop.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void bonemeal(ProduceCrop crop, ServerLevel level, BlockPos pos) {
        BlockState original = level.getBlockState(pos);
        BlockState result = crop.block(4).defaultBlockState();
        for (Property<?> property : original.getProperties()) {
            Property target = result.getBlock().getStateDefinition().getProperty(property.getName());
            if (target != null) {
                Comparable value = original.getValue(property);
                if (target.getPossibleValues().contains(value)) result = result.setValue(target, value);
            }
        }
        level.setBlock(pos, result, Block.UPDATE_ALL);
    }
}
