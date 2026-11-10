package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;

/** 008莲藕从根上方扫描十格，分别按水域和空气分支生成茎、莲蓬或莲叶。 */
final class LotusGrowth {
    private static final TagKey<Block> SUPPORT = BlockTags.create(id("shicai_008plant"));

    private LotusGrowth() {
    }

    static void grow(Level level, BlockPos root) {
        if (Math.random() >= 0.2) return;
        for (int height = 1; height <= 10; height++) {
            BlockPos pos = root.above(height);
            if (level.getBlockState(pos).is(block(2))) continue;
            if (level.getFluidState(pos).createLegacyBlock().is(Blocks.WATER)) {
                level.setBlock(pos, block(2).defaultBlockState(), Block.UPDATE_ALL);
                BlockState current = level.getBlockState(pos);
                if (current.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    level.setBlock(pos, current.setValue(BlockStateProperties.WATERLOGGED, true), Block.UPDATE_ALL);
                }
            } else if (level.isEmptyBlock(pos)) {
                if (Math.random() < 0.3) {
                    level.setBlock(pos, block(2).defaultBlockState(), Block.UPDATE_ALL);
                } else if (Math.random() < 0.4) {
                    level.setBlock(pos, block(3).defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, block(4).defaultBlockState(), Block.UPDATE_ALL);
                    Direction direction = Math.random() < 0.25 ? Direction.NORTH
                            : Math.random() < 0.333 ? Direction.SOUTH : Math.random() < 0.5 ? Direction.WEST : Direction.EAST;
                    BlockState current = level.getBlockState(pos);
                    if (current.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        level.setBlock(pos, current.setValue(BlockStateProperties.HORIZONTAL_FACING, direction), Block.UPDATE_ALL);
                    }
                }
            }
            break;
        }
    }

    static void checkSupport(Level level, BlockPos pos) {
        if (!level.getBlockState(pos.below()).is(SUPPORT)) level.destroyBlock(pos, false);
    }

    static Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("shicai_008plant_0" + stage));
    }

    static ItemStack item(String path, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)), count);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
