package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 040/041 木生菌类的贴附、含水、支撑检查与阶段掉落行为。 */
public class WoodFungusBlock extends WaterloggedContentBlock {
    protected final FungusCrop crop;
    protected final int stage;

    public WoodFungusBlock(BlockTraits traits, FungusCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(stage)) : drops;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                   boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        Direction supportDirection = state.getValue(BlockStateProperties.FACING).getOpposite();
        if (level.isEmptyBlock(pos.relative(supportDirection))) {
            Block.dropResources(level.getBlockState(pos), level, pos, null);
            level.destroyBlock(pos, false);
        }
    }

    /** 随机刻按20%推进；骨粉按来源规则直接成熟。 */
    public static final class Growing extends WoodFungusBlock implements BonemealableBlock {
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
