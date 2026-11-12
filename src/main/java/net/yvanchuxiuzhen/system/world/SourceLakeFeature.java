package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源湖泊生成：以锁定材料成员恢复边界判定，保持椭球、随机和方块写入顺序。 */
final class SourceLakeFeature extends Feature<LakeFeature.Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();
    private static final TagKey<Block> SOLID = tag("legacy_lake_solid");
    private static final TagKey<Block> LIQUID = tag("legacy_lake_liquid");
    private static final TagKey<Block> CANNOT_REPLACE = tag("legacy_worldgen_features_cannot_replace");
    private static final TagKey<Block> BARRIER_CANNOT_REPLACE = tag("legacy_lake_barrier_cannot_replace");

    SourceLakeFeature() {
        super(LakeFeature.Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<LakeFeature.Configuration> context) {
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        LakeFeature.Configuration configuration = context.config();
        if (origin.getY() <= level.getMinBuildHeight() + 4) {
            return false;
        }
        origin = origin.below(4);
        boolean[] cavity = new boolean[2048];
        int ellipsoids = random.nextInt(4) + 4;
        for (int ellipsoid = 0; ellipsoid < ellipsoids; ellipsoid++) {
            double sizeX = random.nextDouble() * 6.0 + 3.0;
            double sizeY = random.nextDouble() * 4.0 + 2.0;
            double sizeZ = random.nextDouble() * 6.0 + 3.0;
            double centerX = random.nextDouble() * (16.0 - sizeX - 2.0) + 1.0 + sizeX / 2.0;
            double centerY = random.nextDouble() * (8.0 - sizeY - 4.0) + 2.0 + sizeY / 2.0;
            double centerZ = random.nextDouble() * (16.0 - sizeZ - 2.0) + 1.0 + sizeZ / 2.0;
            for (int x = 1; x < 15; x++) {
                for (int z = 1; z < 15; z++) {
                    for (int y = 1; y < 7; y++) {
                        double dx = (x - centerX) / (sizeX / 2.0);
                        double dy = (y - centerY) / (sizeY / 2.0);
                        double dz = (z - centerZ) / (sizeZ / 2.0);
                        double distance = dx * dx + dy * dy + dz * dz;
                        if (distance < 1.0) {
                            cavity[(x * 16 + z) * 8 + y] = true;
                        }
                    }
                }
            }
        }

        BlockState fluid = configuration.fluid().getState(random, origin);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    if (isBoundary(cavity, x, y, z)) {
                        BlockState state = level.getBlockState(origin.offset(x, y, z));
                        if (y >= 4 && state.is(LIQUID)) {
                            return false;
                        }
                        if (y < 4 && !state.is(SOLID) && level.getBlockState(origin.offset(x, y, z)) != fluid) {
                            return false;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    if (cavity[(x * 16 + z) * 8 + y]) {
                        BlockPos position = origin.offset(x, y, z);
                        if (canReplace(level.getBlockState(position))) {
                            boolean upperHalf = y >= 4;
                            level.setBlock(position, upperHalf ? AIR : fluid, 2);
                            if (upperHalf) {
                                level.scheduleTick(position, AIR.getBlock(), 0);
                                markAboveForPostProcessing(level, position);
                            }
                        }
                    }
                }
            }
        }

        BlockState barrier = configuration.barrier().getState(random, origin);
        if (!barrier.isAir()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 8; y++) {
                        if (isBoundary(cavity, x, y, z) && (y < 4 || random.nextInt(2) != 0)) {
                            BlockState state = level.getBlockState(origin.offset(x, y, z));
                            if (state.is(SOLID) && !state.is(BARRIER_CANNOT_REPLACE)) {
                                BlockPos position = origin.offset(x, y, z);
                                level.setBlock(position, barrier, 2);
                                markAboveForPostProcessing(level, position);
                            }
                        }
                    }
                }
            }
        }

        if (fluid.getFluidState().is(FluidTags.WATER)) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos position = origin.offset(x, 4, z);
                    if (level.getBiome(position).value().shouldFreeze(level, position, false)
                            && canReplace(level.getBlockState(position))) {
                        level.setBlock(position, Blocks.ICE.defaultBlockState(), 2);
                    }
                }
            }
        }
        return true;
    }

    private static boolean isBoundary(boolean[] cavity, int x, int y, int z) {
        return !cavity[(x * 16 + z) * 8 + y]
                && (x < 15 && cavity[((x + 1) * 16 + z) * 8 + y]
                || x > 0 && cavity[((x - 1) * 16 + z) * 8 + y]
                || z < 15 && cavity[(x * 16 + z + 1) * 8 + y]
                || z > 0 && cavity[(x * 16 + (z - 1)) * 8 + y]
                || y < 7 && cavity[(x * 16 + z) * 8 + y + 1]
                || y > 0 && cavity[(x * 16 + z) * 8 + (y - 1)]);
    }

    private static boolean canReplace(BlockState state) {
        return !state.is(CANNOT_REPLACE);
    }

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
