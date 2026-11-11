package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.yvanchuxiuzhen.platform.ModId;

/** 豆科生长沿用来源标签分支、0.2随机概率与阶段结果。 */
final class LegumeGrowth {
    private static final TagKey<Block> JIAZI = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "jiazi"));

    private LegumeGrowth() {
    }

    static void tick(LegumeCrop crop, int stage, ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(JIAZI)) {
            if (Math.random() < 0.2) advance(crop, stage, level, pos, state);
            return;
        }

        BlockState soil = level.getBlockState(pos.below());
        Property<?> property = soil.getBlock().getStateDefinition().getProperty("moisture");
        int moisture = property instanceof IntegerProperty moistureProperty
                ? soil.getValue(moistureProperty) : -1;
        if (moisture < 7) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        } else if (Math.random() < 0.2) {
            advance(crop, stage, level, pos, state);
        }
    }

    static void bonemeal(LegumeCrop crop, int stage, ServerLevel level, BlockPos pos, BlockState state) {
        replace(level, pos, crop, crop.bonemealTarget(stage), state);
    }

    static void harvest(LegumeCrop crop, Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (int index = 0; index < crop.harvestEntityCount(); index++) {
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        crop.produce());
                entity.setPickUpDelay(10);
                level.addFreshEntity(entity);
            }
        }
        level.setBlock(pos, crop.block(crop.harvestedStage()).defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void advance(LegumeCrop crop, int stage, ServerLevel level, BlockPos pos, BlockState state) {
        int targetStage = crop.tickTarget(stage);
        if (targetStage != 0) replace(level, pos, crop, targetStage, state);
    }

    private static void replace(Level level, BlockPos pos, LegumeCrop crop, int targetStage, BlockState state) {
        level.setBlock(pos, crop.block(targetStage).withPropertiesOf(state), Block.UPDATE_ALL);
    }
}
