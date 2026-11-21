package net.yvanchuxiuzhen.system.plant;

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

/** 韭菜第三、四阶段可右键收割，回到第二阶段；开花时另外产出韭菜花。 */
public final class VegetableHarvestBlock extends VegetableGrowingBlock {
    public VegetableHarvestBlock(BlockTraits traits, int stage) {
        super(traits, VegetableCrop.SHICAI_016, stage);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        harvest(level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        harvest(level, pos);
        return InteractionResult.SUCCESS;
    }

    private void harvest(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        level.setBlock(pos, crop.block(2).defaultBlockState(), Block.UPDATE_ALL);
        drop(level, pos, VegetableCrop.item("shicai_016"));
        if (stage == 4) {
            drop(level, pos, VegetableCrop.item("shicai_01602"));
        }
    }

    private static void drop(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
