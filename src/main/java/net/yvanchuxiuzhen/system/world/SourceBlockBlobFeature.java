package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源石团算法，保留三次随机团块与方块更新标志4。 */
final class SourceBlockBlobFeature extends Feature<BlockStateConfiguration> {
    private static final TagKey<Block> SOURCE_STONE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_worldgen_stone"));
    private static final TagKey<Block> SOURCE_DIRT = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_worldgen_dirt"));

    SourceBlockBlobFeature() {
        super(BlockStateConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockStateConfiguration configuration = context.config();
        while (origin.getY() > level.getMinBuildHeight() + 3) {
            if (!level.isEmptyBlock(origin.below())) {
                BlockState ground = level.getBlockState(origin.below());
                if (ground.is(SOURCE_DIRT) || ground.is(SOURCE_STONE)) {
                    break;
                }
            }
            origin = origin.below();
        }
        if (origin.getY() <= level.getMinBuildHeight() + 3) {
            return false;
        }
        for (int blob = 0; blob < 3; blob++) {
            int x = random.nextInt(2);
            int y = random.nextInt(2);
            int z = random.nextInt(2);
            float radius = (float) (x + y + z) * 0.333F + 0.5F;
            for (BlockPos position : BlockPos.betweenClosed(origin.offset(-x, -y, -z), origin.offset(x, y, z))) {
                if (position.distSqr(origin) <= (double) (radius * radius)) {
                    level.setBlock(position, configuration.state, 4);
                }
            }
            origin = origin.offset(-1 + random.nextInt(2), -random.nextInt(2), -1 + random.nextInt(2));
        }
        return true;
    }
}
