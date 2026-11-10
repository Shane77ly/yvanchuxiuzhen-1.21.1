package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 藤蔓阶段保留内容表朝向、含水和掉落；生长入口按来源逐阶段挂载。 */
public class VineBlock extends WaterloggedContentBlock {
    private final VineCrop crop;
    private final int stage;

    public VineBlock(BlockTraits traits, VineCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(stage)) : drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (crop == VineCrop.SHICAI_039 && (stage == 2 || stage == 3)) level.scheduleTick(pos, this, 40);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (crop.hasTick(stage)) VineGrowth.tick(crop, level, pos, state);
        if (crop == VineCrop.SHICAI_039 && (stage == 2 || stage == 3)) level.scheduleTick(pos, this, 40);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!hasUse()) return super.useWithoutItem(state, level, pos, player, hit);
        harvest(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!hasUse()) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        harvest(level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    private boolean hasUse() {
        return crop == VineCrop.SHICAI_035 && stage == 4 || crop == VineCrop.SHICAI_038 && stage == 3;
    }

    private void harvest(Level level, BlockPos pos) {
        if (crop == VineCrop.SHICAI_035) {
            VineGrowth.copy(level, pos, crop.block(3).defaultBlockState());
            if (!level.isClientSide) {
                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, crop.produce());
                item.setPickUpDelay(10);
                level.addFreshEntity(item);
            }
        }
        // 038的use挂在03，而共享采摘过程只匹配02；该入口保留成功返回且不产出。
    }
}
