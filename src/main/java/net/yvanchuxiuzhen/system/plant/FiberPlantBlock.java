package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 棉花与麻各阶段的掉落和收获行为。 */
public class FiberPlantBlock extends ContentFlower {
    protected final FiberCrop crop;
    protected final int stage;

    public FiberPlantBlock(BlockTraits traits, FiberCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    public static FiberPlantBlock create(BlockTraits traits) {
        String blockId = traits.row().id();
        FiberCrop crop = FiberCrop.fromBlockId(blockId);
        int stage = crop.stageFromBlockId(blockId);
        if (crop == FiberCrop.COTTON && stage == crop.stages()) {
            return new CottonMature(traits, crop);
        }
        if (crop == FiberCrop.HEMP && stage == crop.stages()) {
            return new HempMature(traits, crop);
        }
        return new Growing(traits, crop, stage);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(stage)) : drops;
    }

    public static final class Growing extends FiberPlantBlock {
        public Growing(BlockTraits traits, FiberCrop crop, int stage) {
            super(traits, crop, stage);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            FiberGrowth.tick(crop, stage, level, pos);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.tick(state, level, pos, random);
            FiberGrowth.tick(crop, stage, level, pos);
        }
    }

    public static final class CottonMature extends FiberPlantBlock {
        public CottonMature(BlockTraits traits, FiberCrop crop) {
            super(traits, crop, crop.stages());
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                BlockHitResult hit) {
            FiberGrowth.harvestCotton(crop, level, pos);
            return InteractionResult.SUCCESS;
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit) {
            FiberGrowth.harvestCotton(crop, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
    }

    public static final class HempMature extends FiberPlantBlock {
        public HempMature(BlockTraits traits, FiberCrop crop) {
            super(traits, crop, crop.stages());
        }

        @Override
        public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                boolean willHarvest, FluidState fluid) {
            boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
            FiberGrowth.breakHemp(level, pos, crop);
            return removed;
        }
    }
}
