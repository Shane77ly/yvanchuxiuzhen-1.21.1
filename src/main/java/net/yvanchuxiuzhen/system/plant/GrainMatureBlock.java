package net.yvanchuxiuzhen.system.plant;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 成熟植株点击后回到05，并按来源每轮重抽的循环生成独立食材实体。 */
public final class GrainMatureBlock extends GrainBlock {
    public GrainMatureBlock(BlockTraits traits, GrainCrop crop) {
        super(traits, crop, true);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        harvest(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        harvest(level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    private void harvest(Level level, BlockPos pos) {
        level.setBlock(pos, crop.block(5).defaultBlockState(), Block.UPDATE_ALL);
        for (int index = 0; index < new Random().nextInt(3) + 1; index++) {
            if (!level.isClientSide) {
                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, crop.produce());
                item.setPickUpDelay(10);
                level.addFreshEntity(item);
            }
        }
    }
}
