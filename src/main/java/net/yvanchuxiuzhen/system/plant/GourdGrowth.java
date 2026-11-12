package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;

/** 葫芦藤按来源顺序检查攀附位置，保留朝西末分支朝东及40刻调度的方向差异。 */
final class GourdGrowth {
    private static final TagKey<Block> GOURD_VINES = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "huluteng"));

    private GourdGrowth() {
    }

    static void attach(ServerLevel level, BlockPos pos) {
        Direction facing = !level.isEmptyBlock(pos.east()) ? Direction.WEST
                : !level.isEmptyBlock(pos.west()) ? Direction.EAST
                : !level.isEmptyBlock(pos.south()) ? Direction.NORTH
                : !level.isEmptyBlock(pos.north()) ? Direction.SOUTH : null;
        if (facing != null) {
            VineGrowth.copy(level, pos, block("shicai_039_plant_02").defaultBlockState());
            VineGrowth.face(level, pos, facing);
        }
    }

    static void grow(ServerLevel level, BlockPos pos, BlockState blockstate) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (level.getBlockState(new BlockPos(x, y, z)).getBlock() == block("shicai_039_plant_01")) {
            if (Math.random() < 0.5) {
                if (!level.isEmptyBlock(new BlockPos(x + 1, y, z))) {
                    VineGrowth.copy(level, new BlockPos(x, y, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y, z), Direction.WEST);
                } else if (!level.isEmptyBlock(new BlockPos(x - 1, y, z))) {
                    VineGrowth.copy(level, new BlockPos(x, y, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y, z), Direction.EAST);
                } else if (!level.isEmptyBlock(new BlockPos(x, y, z + 1))) {
                    VineGrowth.copy(level, new BlockPos(x, y, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y, z), Direction.NORTH);
                } else if (!level.isEmptyBlock(new BlockPos(x, y, z - 1))) {
                    VineGrowth.copy(level, new BlockPos(x, y, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y, z), Direction.SOUTH);
                }
            }
        } else if (level.getBlockState(new BlockPos(x, y, z)).getBlock() == block("shicai_039_plant_02")) {
            if (Math.random() < 0.5) {
                if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && level.isEmptyBlock(new BlockPos(x + 1, y + 1, z + 1)) && !level.isEmptyBlock(new BlockPos(x, y + 1, z + 1))
                        && !level.getBlockState(new BlockPos(x, y + 1, z + 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x + 1, y + 1, z + 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x + 1, y + 1, z + 1), Direction.EAST);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && level.isEmptyBlock(new BlockPos(x - 1, y + 1, z - 1)) && !level.isEmptyBlock(new BlockPos(x, y + 1, z - 1))
                        && !level.getBlockState(new BlockPos(x, y + 1, z - 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x - 1, y + 1, z - 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x - 1, y + 1, z - 1), Direction.WEST);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && level.isEmptyBlock(new BlockPos(x - 1, y + 1, z + 1)) && !level.isEmptyBlock(new BlockPos(x - 1, y + 1, z))
                        && !level.getBlockState(new BlockPos(x - 1, y + 1, z)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x - 1, y + 1, z + 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x - 1, y + 1, z + 1), Direction.SOUTH);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && level.isEmptyBlock(new BlockPos(x + 1, y + 1, z - 1)) && !level.isEmptyBlock(new BlockPos(x + 1, y + 1, z))
                        && !level.getBlockState(new BlockPos(x + 1, y + 1, z)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x + 1, y + 1, z - 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x + 1, y + 1, z - 1), Direction.NORTH);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && level.isEmptyBlock(new BlockPos(x + 1, y + 1, z)) && !level.isEmptyBlock(new BlockPos(x + 1, y + 1, z + 1))
                        && !level.getBlockState(new BlockPos(x + 1, y + 1, z + 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x + 1, y + 1, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x + 1, y + 1, z), Direction.NORTH);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && level.isEmptyBlock(new BlockPos(x - 1, y + 1, z)) && !level.isEmptyBlock(new BlockPos(x - 1, y + 1, z - 1))
                        && !level.getBlockState(new BlockPos(x - 1, y + 1, z - 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x - 1, y + 1, z), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x - 1, y + 1, z), Direction.SOUTH);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && level.isEmptyBlock(new BlockPos(x, y + 1, z + 1)) && !level.isEmptyBlock(new BlockPos(x - 1, y + 1, z + 1))
                        && !level.getBlockState(new BlockPos(x - 1, y + 1, z + 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x, y + 1, z + 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y + 1, z + 1), Direction.EAST);
                } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && level.isEmptyBlock(new BlockPos(x, y + 1, z - 1)) && !level.isEmptyBlock(new BlockPos(x + 1, y + 1, z - 1))
                        && !level.getBlockState(new BlockPos(x + 1, y + 1, z - 1)).is(GOURD_VINES)) {
                    VineGrowth.copy(level, new BlockPos(x, y + 1, z - 1), block("shicai_039_plant_02").defaultBlockState());
                    VineGrowth.face(level, new BlockPos(x, y + 1, z - 1), Direction.EAST);
                } else {
                    if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && level.isEmptyBlock(new BlockPos(x + 1, y, z)) && !level.isEmptyBlock(new BlockPos(x + 1, y, z + 1))
                            && !level.getBlockState(new BlockPos(x + 1, y, z + 1)).is(GOURD_VINES)) {
                        VineGrowth.copy(level, new BlockPos(x + 1, y, z), block("shicai_039_plant_03").defaultBlockState());
                        VineGrowth.face(level, new BlockPos(x + 1, y, z), Direction.EAST);
                    } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && level.isEmptyBlock(new BlockPos(x - 1, y, z)) && !level.isEmptyBlock(new BlockPos(x - 1, y, z - 1))
                            && !level.getBlockState(new BlockPos(x - 1, y, z - 1)).is(GOURD_VINES)) {
                        VineGrowth.copy(level, new BlockPos(x - 1, y, z), block("shicai_039_plant_03").defaultBlockState());
                        VineGrowth.face(level, new BlockPos(x - 1, y, z), Direction.WEST);
                    } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && level.isEmptyBlock(new BlockPos(x, y, z + 1)) && !level.isEmptyBlock(new BlockPos(x - 1, y, z + 1))
                            && !level.getBlockState(new BlockPos(x - 1, y, z + 1)).is(GOURD_VINES)) {
                        VineGrowth.copy(level, new BlockPos(x, y, z + 1), block("shicai_039_plant_03").defaultBlockState());
                        VineGrowth.face(level, new BlockPos(x, y, z + 1), Direction.SOUTH);
                    } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && level.isEmptyBlock(new BlockPos(x, y, z - 1)) && !level.isEmptyBlock(new BlockPos(x + 1, y, z - 1))
                            && !level.getBlockState(new BlockPos(x + 1, y, z - 1)).is(GOURD_VINES)) {
                        VineGrowth.copy(level, new BlockPos(x, y, z - 1), block("shicai_039_plant_03").defaultBlockState());
                        VineGrowth.face(level, new BlockPos(x, y, z - 1), Direction.NORTH);
                    }
                }
            }
        }
    }

    static void extend(ServerLevel level, BlockPos pos, BlockState blockstate) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (Math.random() < 0.3) {
            if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.NORTH && level.isEmptyBlock(new BlockPos(x, y, z - 1))
                    && (!level.isEmptyBlock(new BlockPos(x + 1, y, z - 1)) && !level.getBlockState(new BlockPos(x + 1, y, z - 1)).is(GOURD_VINES)
                            || !level.isEmptyBlock(new BlockPos(x - 1, y, z - 1)) && !level.getBlockState(new BlockPos(x - 1, y, z - 1)).is(GOURD_VINES))) {
                VineGrowth.copy(level, new BlockPos(x, y, z - 1), block("shicai_039_plant_03").defaultBlockState());
                VineGrowth.face(level, new BlockPos(x, y, z - 1), blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING));
            } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.SOUTH && level.isEmptyBlock(new BlockPos(x, y, z + 1))
                    && (!level.isEmptyBlock(new BlockPos(x + 1, y, z + 1)) && !level.getBlockState(new BlockPos(x + 1, y, z + 1)).is(GOURD_VINES)
                            || !level.isEmptyBlock(new BlockPos(x - 1, y, z + 1)) && !level.getBlockState(new BlockPos(x - 1, y, z + 1)).is(GOURD_VINES))) {
                VineGrowth.copy(level, new BlockPos(x, y, z + 1), block("shicai_039_plant_03").defaultBlockState());
                VineGrowth.face(level, new BlockPos(x, y, z + 1), blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING));
            } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST && level.isEmptyBlock(new BlockPos(x + 1, y, z))
                    && (!level.isEmptyBlock(new BlockPos(x + 1, y, z + 1)) && !level.getBlockState(new BlockPos(x + 1, y, z + 1)).is(GOURD_VINES)
                            || !level.isEmptyBlock(new BlockPos(x + 1, y, z - 1)) && !level.getBlockState(new BlockPos(x + 1, y, z - 1)).is(GOURD_VINES))) {
                VineGrowth.copy(level, new BlockPos(x + 1, y, z), block("shicai_039_plant_03").defaultBlockState());
                VineGrowth.face(level, new BlockPos(x + 1, y, z), blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING));
            } else if (blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST && level.isEmptyBlock(new BlockPos(x - 1, y, z))
                    && (!level.isEmptyBlock(new BlockPos(x - 1, y, z + 1)) && !level.getBlockState(new BlockPos(x - 1, y, z + 1)).is(GOURD_VINES)
                            || !level.isEmptyBlock(new BlockPos(x - 1, y, z - 1)) && !level.getBlockState(new BlockPos(x - 1, y, z - 1)).is(GOURD_VINES))) {
                VineGrowth.copy(level, new BlockPos(x - 1, y, z), block("shicai_039_plant_03").defaultBlockState());
                VineGrowth.face(level, new BlockPos(x - 1, y, z), blockstate.getValue(BlockStateProperties.HORIZONTAL_FACING));
            } else if (level.isEmptyBlock(new BlockPos(x, y - 1, z))) {
                level.setBlock(new BlockPos(x, y - 1, z), block("shicai_039_plant_04").defaultBlockState(), 3);
            }
        }
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
