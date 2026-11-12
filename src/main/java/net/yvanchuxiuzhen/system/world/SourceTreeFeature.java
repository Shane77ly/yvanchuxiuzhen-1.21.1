package net.yvanchuxiuzhen.system.world;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源树生成：保留三组写入集合、装饰顺序及跨边界六层叶距离更新。 */
final class SourceTreeFeature extends Feature<TreeConfiguration> {
    static final TagKey<Block> DIRT = tag("legacy_tree_dirt");
    private static final TagKey<Block> LEAVES = tag("legacy_tree_leaves");
    private static final TagKey<Block> LOGS = tag("legacy_tree_logs");
    private static final TagKey<Block> REPLACEABLE_PLANT = tag("legacy_tree_replaceable_plant");

    SourceTreeFeature() {
        super(TreeConfiguration.CODEC);
    }

    static boolean validTreePos(LevelSimulatedReader level, BlockPos position) {
        return level.isStateAtPosition(position, state -> state.isAir() || state.is(LEAVES)
                || state.is(REPLACEABLE_PLANT) || state.is(Blocks.WATER));
    }

    static boolean isFree(LevelSimulatedReader level, BlockPos position) {
        return validTreePos(level, position) || level.isStateAtPosition(position, state -> state.is(LOGS));
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        WorldGenLevel level = context.level();
        TreeConfiguration configuration = context.config();
        RandomSource random = context.random();
        Set<BlockPos> trunks = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();
        Set<BlockPos> decorations = new HashSet<>();
        BiConsumer<BlockPos, BlockState> trunkSetter = setter(level, trunks);
        BiConsumer<BlockPos, BlockState> leafSetter = setter(level, leaves);
        BiConsumer<BlockPos, BlockState> decorationSetter = setter(level, decorations);
        FoliagePlacer.FoliageSetter foliageSetter = new FoliagePlacer.FoliageSetter() {
            @Override
            public void set(BlockPos position, BlockState state) {
                leafSetter.accept(position, state);
            }

            @Override
            public boolean isSet(BlockPos position) {
                return leaves.contains(position);
            }
        };
        if (!placeTree(level, random, context.origin(), trunkSetter, foliageSetter, configuration)
                || trunks.isEmpty() && leaves.isEmpty()) {
            return false;
        }
        if (!configuration.decorators.isEmpty()) {
            TreeDecorator.Context decoratorContext = new TreeDecorator.Context(level, decorationSetter,
                    random, trunks, leaves, Set.of());
            for (TreeDecorator decorator : configuration.decorators) {
                decorator.place(decoratorContext);
            }
        }
        return BoundingBox.encapsulatingPositions(Iterables.concat(trunks, leaves, decorations)).map(bounds -> {
            DiscreteVoxelShape shape = updateLeaves(level, bounds, trunks, decorations);
            StructureTemplate.updateShapeAtEdge(level, 3, shape, bounds.minX(), bounds.minY(), bounds.minZ());
            return true;
        }).orElse(false);
    }

    private static BiConsumer<BlockPos, BlockState> setter(WorldGenLevel level, Set<BlockPos> positions) {
        return (position, state) -> {
            positions.add(position.immutable());
            level.setBlock(position, state, 19);
        };
    }

    private static boolean placeTree(WorldGenLevel level, RandomSource random, BlockPos position,
            BiConsumer<BlockPos, BlockState> trunkSetter, FoliagePlacer.FoliageSetter leafSetter,
            TreeConfiguration configuration) {
        int height = configuration.trunkPlacer.getTreeHeight(random);
        int foliageHeight = configuration.foliagePlacer.foliageHeight(random, height, configuration);
        int foliageRadius = configuration.foliagePlacer.foliageRadius(random, height - foliageHeight);
        if (position.getY() < level.getMinBuildHeight() + 1
                || position.getY() + height + 1 > level.getMaxBuildHeight()) {
            return false;
        }
        OptionalInt clipped = configuration.minimumSize.minClippedHeight();
        int freeHeight = maximumFreeHeight(level, height, position, configuration);
        if (freeHeight < height && (clipped.isEmpty() || freeHeight < clipped.getAsInt())) {
            return false;
        }
        List<FoliagePlacer.FoliageAttachment> attachments = configuration.trunkPlacer.placeTrunk(
                level, trunkSetter, random, freeHeight, position, configuration);
        for (FoliagePlacer.FoliageAttachment attachment : attachments) {
            configuration.foliagePlacer.createFoliage(level, leafSetter, random, configuration,
                    freeHeight, attachment, foliageHeight, foliageRadius);
        }
        return true;
    }

    private static int maximumFreeHeight(LevelSimulatedReader level, int height, BlockPos position,
            TreeConfiguration configuration) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = 0; y <= height + 1; y++) {
            int radius = configuration.minimumSize.getSizeAtHeight(height, y);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    cursor.setWithOffset(position, x, y, z);
                    if (!isFree(level, cursor) || !configuration.ignoreVines
                            && level.isStateAtPosition(cursor, state -> state.is(Blocks.VINE))) {
                        return y - 2;
                    }
                }
            }
        }
        return height;
    }

    private static DiscreteVoxelShape updateLeaves(LevelAccessor level, BoundingBox bounds,
            Set<BlockPos> trunks, Set<BlockPos> decorations) {
        List<Set<BlockPos>> layers = new ArrayList<>();
        for (int layer = 0; layer < 6; layer++) {
            layers.add(new HashSet<>());
        }
        DiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(bounds.getXSpan(), bounds.getYSpan(), bounds.getZSpan());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (BlockPos decoration : new ArrayList<>(decorations)) {
            fill(shape, bounds, decoration);
        }
        for (BlockPos trunk : new ArrayList<>(trunks)) {
            fill(shape, bounds, trunk);
            for (Direction direction : Direction.values()) {
                cursor.setWithOffset(trunk, direction);
                if (!trunks.contains(cursor)) {
                    BlockState state = level.getBlockState(cursor);
                    if (state.hasProperty(BlockStateProperties.DISTANCE)) {
                        layers.get(0).add(cursor.immutable());
                        level.setBlock(cursor, state.setValue(BlockStateProperties.DISTANCE, 1), 19);
                        fill(shape, bounds, cursor);
                    }
                }
            }
        }
        for (int layer = 1; layer < 6; layer++) {
            Set<BlockPos> previous = layers.get(layer - 1);
            Set<BlockPos> current = layers.get(layer);
            for (BlockPos position : previous) {
                fill(shape, bounds, position);
                for (Direction direction : Direction.values()) {
                    cursor.setWithOffset(position, direction);
                    if (!previous.contains(cursor) && !current.contains(cursor)) {
                        BlockState state = level.getBlockState(cursor);
                        if (state.hasProperty(BlockStateProperties.DISTANCE)
                                && state.getValue(BlockStateProperties.DISTANCE) > layer + 1) {
                            level.setBlock(cursor, state.setValue(BlockStateProperties.DISTANCE, layer + 1), 19);
                            fill(shape, bounds, cursor);
                            current.add(cursor.immutable());
                        }
                    }
                }
            }
        }
        return shape;
    }

    private static void fill(DiscreteVoxelShape shape, BoundingBox bounds, BlockPos position) {
        if (bounds.isInside(position)) {
            shape.fill(position.getX() - bounds.minX(), position.getY() - bounds.minY(), position.getZ() - bounds.minZ());
        }
    }

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }
}
