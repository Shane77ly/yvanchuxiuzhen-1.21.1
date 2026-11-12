package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 普通作物四阶段的炖菜效果取各方块内容行；成熟阶段没有生长或骨粉入口。 */
public class ProduceBlock extends ContentFlower {
    protected final ProduceCrop crop;
    protected final int stage;

    public ProduceBlock(BlockTraits traits, ProduceCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(stage)) : drops;
    }

    /** 01至03阶段保留随机刻和计划刻；一次有效骨粉使用以50%概率直接成熟。 */
    public static final class Growing extends ProduceBlock implements BonemealableBlock {
        public Growing(BlockTraits traits, ProduceCrop crop, int stage) {
            super(traits, crop, stage);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            ProduceGrowth.tick(crop, stage, level, pos);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.tick(state, level, pos, random);
            ProduceGrowth.tick(crop, stage, level, pos);
        }

        @Override
        public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
            return true;
        }

        @Override
        public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
            return Math.random() < 0.5;
        }

        @Override
        public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
            ProduceGrowth.bonemeal(crop, level, pos);
        }
    }
}
