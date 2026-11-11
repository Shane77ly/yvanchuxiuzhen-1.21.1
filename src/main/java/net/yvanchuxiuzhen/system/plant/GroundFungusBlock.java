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

/** 042–045 地面菌类的来源生长、骨粉与阶段掉落行为。 */
public class GroundFungusBlock extends ContentFlower {
    protected final FungusCrop crop;
    protected final int stage;

    public GroundFungusBlock(BlockTraits traits, FungusCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(stage)) : drops;
    }

    /** 随机刻按20%推进；骨粉保留来源50%成功率与单阶段推进。 */
    public static final class Growing extends GroundFungusBlock implements BonemealableBlock {
        public Growing(BlockTraits traits, FungusCrop crop, int stage) {
            super(traits, crop, stage);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            tick(state, level, pos, random);
        }

        @Override
        protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            super.tick(state, level, pos, random);
            FungusGrowth.tick(crop, stage, level, pos);
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
            FungusGrowth.bonemeal(crop, stage, level, pos);
        }
    }
}
