package net.yvanchuxiuzhen.system.plant;

import java.util.List;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 蔬菜族成熟阶段的统一右键采收、阶段回退和来源差异掉落。 */
public class VegetableHarvestBlock extends VegetableGrowingBlock {
    public VegetableHarvestBlock(BlockTraits traits, VegetableCrop crop, int stage) {
        super(traits, crop, stage);
    }

    /** 兼容既有韭菜注册；新注册优先传入具体作物。 */
    public VegetableHarvestBlock(BlockTraits traits, int stage) {
        this(traits, VegetableCrop.SHICAI_016, stage);
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

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.harvestDrop(stage)) : drops;
    }

    private void harvest(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        level.setBlock(pos, crop.block(2).defaultBlockState(), Block.UPDATE_ALL);
        drop(level, pos, crop.harvestDrop(stage));
        if (crop == VegetableCrop.SHICAI_016 && stage == 4) {
            drop(level, pos, VegetableCrop.item("shicai_01602"));
        }
    }

    private static void drop(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
