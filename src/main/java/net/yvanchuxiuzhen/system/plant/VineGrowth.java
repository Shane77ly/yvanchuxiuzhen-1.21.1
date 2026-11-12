package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

/** 藤蔓生长保留来源分支顺序、独立概率抽样及目标位置原有兼容属性。 */
final class VineGrowth {
    private static final Direction[] HORIZONTAL = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};

    private VineGrowth() {
    }

    static void tick(VineCrop crop, ServerLevel level, BlockPos pos, BlockState state) {
        switch (crop) {
            case SHICAI_034, SHICAI_036 -> horizontal(crop, level, pos, state);
            case SHICAI_035, SHICAI_038 -> climbing(VineCrop.SHICAI_035, level, pos);
            case SHICAI_037 -> climbing(VineCrop.SHICAI_037, level, pos);
            case SHICAI_039 -> {
                if (state.is(crop.block(3))) GourdGrowth.extend(level, pos, state);
                else GourdGrowth.grow(level, pos, state);
            }
        }
    }

    static void bonemeal(VineCrop crop, ServerLevel level, BlockPos pos) {
        if (crop == VineCrop.SHICAI_039) {
            GourdGrowth.attach(level, pos);
            return;
        }
        int next = crop.firstStage() + 1;
        level.setBlock(pos, crop.block(next).defaultBlockState(), Block.UPDATE_ALL);
        Direction facing;
        if (crop == VineCrop.SHICAI_034) {
            facing = level.isEmptyBlock(pos.east()) ? Direction.EAST : level.isEmptyBlock(pos.west()) ? Direction.WEST
                    : level.isEmptyBlock(pos.south()) ? Direction.SOUTH : Direction.NORTH;
        } else {
            facing = randomDirection(false);
        }
        face(level, pos, facing);
    }

    private static void horizontal(VineCrop crop, ServerLevel level, BlockPos pos, BlockState original) {
        BlockState state = level.getBlockState(pos);
        if (state.is(crop.block(1))) {
            if (Math.random() < 0.3) {
                copy(level, pos, crop.block(2).defaultBlockState());
                face(level, pos, randomDirection(false));
            }
        } else if (state.is(crop.block(2))) {
            if (Math.random() < 0.2) spread(crop, level, pos, 3, false);
        } else if (state.is(crop.block(3))) {
            if (Math.random() < 0.3) {
                int neighbors = 0;
                for (Direction direction : HORIZONTAL) {
                    if (isStem(crop, level.getBlockState(pos.relative(direction)))) neighbors++;
                }
                if (neighbors <= 1 || Math.random() < 0.1) spread(crop, level, pos, 3, false);
                else spread(crop, level, pos, 4, true);
            }
        } else if (state.is(crop.block(4)) && Math.random() < 0.2) {
            Direction facing = original.getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (isStem(crop, level.getBlockState(pos.relative(facing)))) copy(level, pos, crop.block(5).defaultBlockState());
        }
    }

    private static boolean isStem(VineCrop crop, BlockState state) {
        return state.is(crop.block(2)) || state.is(crop.block(3));
    }

    private static void spread(VineCrop crop, ServerLevel level, BlockPos pos, int stage, boolean reverse) {
        for (int attempt = 0; attempt < 4; attempt++) {
            Direction direction = randomDirection(false);
            BlockPos target = pos.relative(direction);
            if (level.isEmptyBlock(target)) {
                copy(level, target, crop.block(stage).defaultBlockState());
                face(level, target, reverse ? direction.getOpposite() : direction);
                break;
            }
        }
    }

    private static void climbing(VineCrop crop, ServerLevel level, BlockPos pos) {
        if (Math.random() >= 0.2) return;
        int first = crop.firstStage();
        BlockState state = level.getBlockState(pos);
        if (state.is(crop.block(first))) {
            copy(level, pos, crop.block(first + 1).defaultBlockState());
            face(level, pos, randomDirection(true));
        } else if (state.is(crop.block(first + 1)) || state.is(crop.block(first + 2))) {
            BlockPos above = pos.above();
            boolean supported = !level.isEmptyBlock(above.east()) || !level.isEmptyBlock(above.west())
                    || !level.isEmptyBlock(above.north()) || !level.isEmptyBlock(above.south());
            if (supported && level.isEmptyBlock(above)) {
                copy(level, above, crop.block(first + 1).defaultBlockState());
                face(level, above, randomDirection(true));
            } else {
                copy(level, pos, crop.block(first + 2).defaultBlockState());
            }
        }
    }

    private static Direction randomDirection(boolean climbing) {
        if (Math.random() < 0.25) return climbing ? Direction.NORTH : Direction.EAST;
        if (Math.random() < 0.333) return climbing ? Direction.SOUTH : Direction.WEST;
        if (Math.random() < 0.5) return climbing ? Direction.WEST : Direction.SOUTH;
        return climbing ? Direction.EAST : Direction.NORTH;
    }

    static void face(Level level, BlockPos pos, Direction facing) {
        level.setBlock(pos, level.getBlockState(pos).setValue(BlockStateProperties.HORIZONTAL_FACING, facing), Block.UPDATE_ALL);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void copy(Level level, BlockPos pos, BlockState result) {
        BlockState original = level.getBlockState(pos);
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
