package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 豆科花类方块的掉落、随机刻、骨粉与成熟采摘入口。 */
public class LegumeFlowerBlock extends ContentFlower {
    protected final LegumeCrop crop;
    protected final int stage;

    public LegumeFlowerBlock(BlockTraits traits, LegumeCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        ItemStack fallback = crop.drop(stage);
        return drops.isEmpty() && !fallback.isEmpty() ? List.of(fallback) : drops;
    }

    public static class Growing extends LegumeFlowerBlock implements BonemealableBlock {
        public Growing(BlockTraits traits, LegumeCrop crop, int stage) {
            super(traits, crop, stage);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            tick(state, level, pos, random);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.tick(state, level, pos, random);
            LegumeGrowth.tick(crop, stage, level, pos, state);
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
            return crop.hasBonemeal(stage);
        }

        @Override
        public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
            return Math.random() < 0.5;
        }

        @Override
        public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
            LegumeGrowth.bonemeal(crop, stage, level, pos, state);
        }
    }

    public static final class Harvestable extends LegumeFlowerBlock {
        public Harvestable(BlockTraits traits, LegumeCrop crop, int stage) {
            super(traits, crop, stage);
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
            LegumeGrowth.harvest(crop, level, pos);
        }
    }
}
