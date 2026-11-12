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

/** 霓裳草01–06阶段的掉落与骨粉入口，01–05另有来源刻更新。 */
public final class NichangGrassBlock extends ContentFlower implements BonemealableBlock {
    private final int stage;

    public NichangGrassBlock(BlockTraits traits, int stage) {
        super(traits);
        this.stage = stage;
    }

    public static NichangGrassBlock create(BlockTraits traits) {
        int stage = Integer.parseInt(traits.row().id().substring("nichanggrass_0".length()));
        return new NichangGrassBlock(traits, stage);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(NichangGrassCrop.INSTANCE.seedDrop()) : drops;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (stage < 6) NichangGrassGrowth.tick(stage, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (stage < 6) NichangGrassGrowth.tick(stage, level, pos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        NichangGrassGrowth.tick(stage, level, pos);
    }
}
