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

/** 蔬菜生长阶段与开花韭菜共用骨粉入口；开花韭菜不再推进阶段。 */
public class VegetableGrowingBlock extends ContentFlower implements BonemealableBlock {
    protected final VegetableCrop crop;
    protected final int stage;

    public VegetableGrowingBlock(BlockTraits traits, VegetableCrop crop, int stage) {
        super(traits);
        this.crop = crop;
        this.stage = stage;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        VegetableGrowth.tick(crop, stage, level, pos, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        VegetableGrowth.tick(crop, stage, level, pos, random);
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
        VegetableGrowth.bonemeal(crop, stage, level, pos, state, random);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() && stage < 4 ? List.of(crop.fixedDrop(stage)) : drops;
    }
}
