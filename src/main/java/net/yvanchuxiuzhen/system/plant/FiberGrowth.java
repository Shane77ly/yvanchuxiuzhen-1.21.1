package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** 保留棉花与麻共享的 20% 单阶段生长，以及各自的成熟操作。 */
final class FiberGrowth {
    private FiberGrowth() {
    }

    static void tick(FiberCrop crop, int stage, ServerLevel level, BlockPos pos) {
        if (stage < crop.stages() && Math.random() < 0.2
                && level.getBlockState(pos).is(crop.block(stage))) {
            level.setBlock(pos, crop.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    static void harvestCotton(FiberCrop crop, Level level, BlockPos pos) {
        if (level.isClientSide) return;
        level.setBlock(pos, crop.block(crop.stages() - 1).defaultBlockState(), Block.UPDATE_ALL);
        drop(level, pos, crop.fiber());
        if (Math.random() < 0.3) drop(level, pos, crop.seed());
    }

    static void breakHemp(Level level, BlockPos pos, FiberCrop crop) {
        if (level.isClientSide) return;
        drop(level, pos, crop.seed());
        if (Math.random() < 0.3) drop(level, pos, crop.fiber());
    }

    private static void drop(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
