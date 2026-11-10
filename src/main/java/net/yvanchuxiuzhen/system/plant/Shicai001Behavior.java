package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.yvanchuxiuzhen.platform.ModId;

/** 水稻八阶段的来源状态转换、枯萎掉落与骨粉成熟过程。 */
final class Shicai001Behavior {
    private Shicai001Behavior() {
    }

    static void randomTick(int stage, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (stage >= 8 || state.hasProperty(DoublePlantBlock.HALF)
                && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) return;
        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(state, level, pos, null);
            level.destroyBlock(pos, false);
            return;
        }
        if (Math.random() >= 0.2) return;
        if (stage < 4) {
            level.setBlock(pos, block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
        } else if (stage == 4) {
            placeTransition(5, level, pos);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            placeTransition(stage + 1, level, pos);
        }
    }

    static void bonemeal(ServerLevel level, BlockPos pos, BlockState original) {
        if (!isGrowing(original)) return;
        copyCompatibleProperties(level.getBlockState(pos), block(8).defaultBlockState(), level, pos);
        copyCompatibleProperties(level.getBlockState(pos.above()), block(8).defaultBlockState(), level, pos.above());
        BlockState upper = level.getBlockState(pos.above());
        if (upper.hasProperty(DoublePlantBlock.HALF)) {
            level.setBlock(pos.above(), upper.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
    }

    static Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_001_plant_0" + stage));
    }

    static boolean isGrowing(BlockState state) {
        for (int stage = 1; stage <= 7; stage++) if (state.is(block(stage))) return true;
        return false;
    }

    private static void placeTransition(int stage, ServerLevel level, BlockPos lowerPos) {
        BlockState lower = block(stage).defaultBlockState();
        level.setBlock(lowerPos.above(), lower, Block.UPDATE_ALL);
        BlockState upper = level.getBlockState(lowerPos.above());
        if (upper.hasProperty(DoublePlantBlock.HALF)) {
            level.setBlock(lowerPos.above(), upper.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
        level.setBlock(lowerPos, lower, Block.UPDATE_ALL);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyCompatibleProperties(BlockState source, BlockState destination, ServerLevel level, BlockPos pos) {
        BlockState result = destination;
        for (Property<?> property : source.getProperties()) {
            Property target = result.getBlock().getStateDefinition().getProperty(property.getName());
            if (target != null) {
                Comparable value = source.getValue(property);
                if (target.getPossibleValues().contains(value)) result = result.setValue(target, value);
            }
        }
        level.setBlock(pos, result, Block.UPDATE_ALL);
    }
}
