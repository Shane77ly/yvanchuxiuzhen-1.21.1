package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentDoublePlant;

/** 高草仅在下半株被体型达标且未潜行的实体接触时切换为摇摆动画方块。 */
public final class BigGrassBlock extends ContentDoublePlant {
    public BigGrassBlock(BlockTraits traits) {
        super(traits);
    }

    public static Block block() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "biggrass"));
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() != asItem();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        BlockState current = level.getBlockState(pos);
        if ((entity.getBbWidth() >= 1 || entity.getBbHeight() >= 1) && !entity.isShiftKeyDown()
                && current.hasProperty(HALF) && current.getValue(HALF) == DoubleBlockHalf.LOWER) {
            level.setBlock(pos, AnimatedBigGrassBlock.block().defaultBlockState(), Block.UPDATE_ALL);
            BlockState animation = level.getBlockState(pos);
            if (animation.hasProperty(AnimatedBigGrassBlock.ANIMATION)) {
                level.setBlock(pos, animation.setValue(AnimatedBigGrassBlock.ANIMATION, 1), Block.UPDATE_ALL);
            }
        }
    }
}
