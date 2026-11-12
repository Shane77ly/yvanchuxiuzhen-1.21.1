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
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 052/053支架作物的水浸状态、来源生长分支、催熟与成熟采摘。 */
public class LegumeSupportBlock extends WaterloggedContentBlock {
    protected final LegumeCrop crop;
    protected final int stage;

    public LegumeSupportBlock(BlockTraits traits, LegumeCrop crop, int stage) {
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

    public static final class Growing extends LegumeSupportBlock implements BonemealableBlock {
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

    public static final class Harvestable extends LegumeSupportBlock {
        public Harvestable(BlockTraits traits, LegumeCrop crop, int stage) {
            super(traits, crop, stage);
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                BlockHitResult hit) {
            LegumeGrowth.harvest(crop, level, pos);
            return InteractionResult.SUCCESS;
        }

        @Override
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit) {
            LegumeGrowth.harvest(crop, level, pos);
            return ItemInteractionResult.SUCCESS;
        }
    }
}
