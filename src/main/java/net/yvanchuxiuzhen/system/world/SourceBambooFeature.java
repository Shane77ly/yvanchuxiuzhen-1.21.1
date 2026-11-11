package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源竹子生成算法，使用锁定版本的竹子地基与泥土成员。 */
final class SourceBambooFeature extends Feature<ProbabilityFeatureConfiguration> {
    private static final TagKey<Block> PLANTABLE = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_bamboo_plantable_on"));
    private static final TagKey<Block> DIRT = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_worldgen_dirt"));
    private static final BlockState BAMBOO_TRUNK = Blocks.BAMBOO.defaultBlockState()
            .setValue(BambooStalkBlock.AGE, 1).setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE)
            .setValue(BambooStalkBlock.STAGE, 0);
    private static final BlockState BAMBOO_FINAL_LARGE = BAMBOO_TRUNK
            .setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE).setValue(BambooStalkBlock.STAGE, 1);
    private static final BlockState BAMBOO_TOP_LARGE = BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE);
    private static final BlockState BAMBOO_TOP_SMALL = BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL);

    SourceBambooFeature() {
        super(ProbabilityFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
        int placed = 0;
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos.MutableBlockPos position = origin.mutable();
        BlockPos.MutableBlockPos podzolPosition = origin.mutable();
        if (level.isEmptyBlock(position)) {
            if (level.getBlockState(position.below()).is(PLANTABLE)) {
                int height = random.nextInt(12) + 5;
                if (random.nextFloat() < context.config().probability) {
                    int radius = random.nextInt(4) + 1;
                    for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                        for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                            int dx = x - origin.getX();
                            int dz = z - origin.getZ();
                            if (dx * dx + dz * dz <= radius * radius) {
                                podzolPosition.set(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1, z);
                                if (level.getBlockState(podzolPosition).is(DIRT)) {
                                    level.setBlock(podzolPosition, Blocks.PODZOL.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
                for (int index = 0; index < height && level.isEmptyBlock(position); index++) {
                    level.setBlock(position, BAMBOO_TRUNK, 2);
                    position.move(Direction.UP);
                }
                if (position.getY() - origin.getY() >= 3) {
                    level.setBlock(position, BAMBOO_FINAL_LARGE, 2);
                    level.setBlock(position.move(Direction.DOWN), BAMBOO_TOP_LARGE, 2);
                    level.setBlock(position.move(Direction.DOWN), BAMBOO_TOP_SMALL, 2);
                }
            }
            placed++;
        }
        return placed > 0;
    }
}
