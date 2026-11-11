package net.yvanchuxiuzhen.system.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源晶洞生成：保留近似逆平方根、层阈值、裂缝、随机调用及晶簇放置次序。 */
final class SourceGeodeFeature extends Feature<GeodeConfiguration> {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final TagKey<Block> INVALID_BLOCKS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_geode_invalid_blocks"));

    SourceGeodeFeature() {
        super(GeodeConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeodeConfiguration> context) {
        GeodeConfiguration configuration = context.config();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        List<DistributionPoint> points = new ArrayList<>();
        int pointCount = configuration.distributionPoints.sample(random);
        WorldgenRandom noiseRandom = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
        NormalNoise noise = NormalNoise.create(noiseRandom, -4, 1.0);
        List<BlockPos> crackPoints = new ArrayList<>();
        double wallAdjustment = (double) pointCount / (double) configuration.outerWallDistance.getMaxValue();
        GeodeLayerSettings layers = configuration.geodeLayerSettings;
        GeodeBlockSettings blocks = configuration.geodeBlockSettings;
        GeodeCrackSettings cracks = configuration.geodeCrackSettings;
        double fillingThreshold = 1.0 / Math.sqrt(layers.filling);
        double innerThreshold = 1.0 / Math.sqrt(layers.innerLayer + wallAdjustment);
        double middleThreshold = 1.0 / Math.sqrt(layers.middleLayer + wallAdjustment);
        double outerThreshold = 1.0 / Math.sqrt(layers.outerLayer + wallAdjustment);
        double crackThreshold = 1.0 / Math.sqrt(cracks.baseCrackSize + random.nextDouble() / 2.0
                + (pointCount > 3 ? wallAdjustment : 0.0));
        boolean generateCrack = (double) random.nextFloat() < cracks.generateCrackChance;
        int invalidBlocks = 0;

        for (int point = 0; point < pointCount; point++) {
            int x = configuration.outerWallDistance.sample(random);
            int y = configuration.outerWallDistance.sample(random);
            int z = configuration.outerWallDistance.sample(random);
            BlockPos position = origin.offset(x, y, z);
            BlockState state = level.getBlockState(position);
            if (state.isAir() || state.is(INVALID_BLOCKS)) {
                invalidBlocks++;
                if (invalidBlocks > configuration.invalidBlocksThreshold) {
                    return false;
                }
            }
            points.add(new DistributionPoint(position, configuration.pointOffset.sample(random)));
        }

        if (generateCrack) {
            int direction = random.nextInt(4);
            int extent = pointCount * 2 + 1;
            if (direction == 0) {
                crackPoints.add(origin.offset(extent, 7, 0));
                crackPoints.add(origin.offset(extent, 5, 0));
                crackPoints.add(origin.offset(extent, 1, 0));
            } else if (direction == 1) {
                crackPoints.add(origin.offset(0, 7, extent));
                crackPoints.add(origin.offset(0, 5, extent));
                crackPoints.add(origin.offset(0, 1, extent));
            } else if (direction == 2) {
                crackPoints.add(origin.offset(extent, 7, extent));
                crackPoints.add(origin.offset(extent, 5, extent));
                crackPoints.add(origin.offset(extent, 1, extent));
            } else {
                crackPoints.add(origin.offset(0, 7, 0));
                crackPoints.add(origin.offset(0, 5, 0));
                crackPoints.add(origin.offset(0, 1, 0));
            }
        }

        List<BlockPos> potentialPlacements = new ArrayList<>();
        Predicate<BlockState> canReplace = isReplaceable(blocks.cannotReplace);
        int minimum = configuration.minGenOffset;
        int maximum = configuration.maxGenOffset;
        for (BlockPos position : BlockPos.betweenClosed(origin.offset(minimum, minimum, minimum),
                origin.offset(maximum, maximum, maximum))) {
            double noiseValue = noise.getValue(position.getX(), position.getY(), position.getZ()) * configuration.noiseMultiplier;
            double density = 0.0;
            double crackDensity = 0.0;
            for (DistributionPoint point : points) {
                density += fastInvSqrt(position.distSqr(point.position()) + (double) point.offset()) + noiseValue;
            }
            for (BlockPos crackPoint : crackPoints) {
                crackDensity += fastInvSqrt(position.distSqr(crackPoint) + (double) cracks.crackPointOffset) + noiseValue;
            }
            if (!(density < outerThreshold)) {
                if (generateCrack && crackDensity >= crackThreshold && density < fillingThreshold) {
                    safeSetBlock(level, position, Blocks.AIR.defaultBlockState(), canReplace);
                    for (Direction direction : DIRECTIONS) {
                        BlockPos neighbour = position.relative(direction);
                        FluidState fluid = level.getFluidState(neighbour);
                        if (!fluid.isEmpty()) {
                            level.scheduleTick(neighbour, fluid.getType(), 0);
                        }
                    }
                } else if (density >= fillingThreshold) {
                    safeSetBlock(level, position, blocks.fillingProvider.getState(random, position), canReplace);
                } else if (density >= innerThreshold) {
                    boolean alternate = (double) random.nextFloat() < configuration.useAlternateLayer0Chance;
                    if (alternate) {
                        safeSetBlock(level, position, blocks.alternateInnerLayerProvider.getState(random, position), canReplace);
                    } else {
                        safeSetBlock(level, position, blocks.innerLayerProvider.getState(random, position), canReplace);
                    }
                    if ((!configuration.placementsRequireLayer0Alternate || alternate)
                            && (double) random.nextFloat() < configuration.usePotentialPlacementsChance) {
                        potentialPlacements.add(position.immutable());
                    }
                } else if (density >= middleThreshold) {
                    safeSetBlock(level, position, blocks.middleLayerProvider.getState(random, position), canReplace);
                } else if (density >= outerThreshold) {
                    safeSetBlock(level, position, blocks.outerLayerProvider.getState(random, position), canReplace);
                }
            }
        }

        for (BlockPos position : potentialPlacements) {
            BlockState placement = Util.getRandom(blocks.innerPlacements, random);
            for (Direction direction : DIRECTIONS) {
                if (placement.hasProperty(BlockStateProperties.FACING)) {
                    placement = placement.setValue(BlockStateProperties.FACING, direction);
                }
                BlockPos neighbour = position.relative(direction);
                BlockState existing = level.getBlockState(neighbour);
                if (placement.hasProperty(BlockStateProperties.WATERLOGGED)) {
                    placement = placement.setValue(BlockStateProperties.WATERLOGGED, existing.getFluidState().isSource());
                }
                if (BuddingAmethystBlock.canClusterGrowAtState(existing)) {
                    safeSetBlock(level, neighbour, placement, canReplace);
                    break;
                }
            }
        }
        return true;
    }

    private static double fastInvSqrt(double value) {
        double half = 0.5 * value;
        long bits = Double.doubleToRawLongBits(value);
        bits = 6910469410427058090L - (bits >> 1);
        double approximation = Double.longBitsToDouble(bits);
        return approximation * (1.5 - half * approximation * approximation);
    }

    private record DistributionPoint(BlockPos position, int offset) {
    }
}
