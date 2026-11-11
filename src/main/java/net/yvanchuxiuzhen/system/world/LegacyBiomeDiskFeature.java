package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;

/** 保留1.18.2版本群系圆盘的列顺序与下落方块空气支撑，配置使用目标原生格式。 */
final class LegacyBiomeDiskFeature extends Feature<DiskConfiguration> {
    LegacyBiomeDiskFeature() {
        super(DiskConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<DiskConfiguration> context) {
        if (!context.level().getFluidState(context.origin()).is(FluidTags.WATER)) {
            return false;
        }
        DiskConfiguration config = context.config();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        // 来源内联圆盘均为固定方块，转换后的simple_state_provider只读取一次。
        BlockState replacement = config.stateProvider().getState(level, random, origin);
        boolean placed = false;
        int top = origin.getY() + config.halfHeight();
        int bottom = origin.getY() - config.halfHeight() - 1;
        boolean falling = replacement.getBlock() instanceof FallingBlock;
        int radius = config.radius().sample(random);

        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                int offsetX = x - origin.getX();
                int offsetZ = z - origin.getZ();
                if (offsetX * offsetX + offsetZ * offsetZ <= radius * radius) {
                    boolean placedAbove = false;
                    for (int y = top; y >= bottom; y--) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState original = level.getBlockState(pos);
                        boolean placedHere = false;
                        if (y > bottom && config.target().test(level, pos)) {
                            level.setBlock(pos, replacement, 2);
                            markAboveForPostProcessing(level, pos);
                            placed = true;
                            placedHere = true;
                        }
                        // 最低额外一层只读取支撑，判断使用写入前状态并替换刚放置的上一层。
                        if (falling && placedAbove && original.isAir()) {
                            BlockState support = replacement.is(Blocks.RED_SAND)
                                    ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                            level.setBlock(new BlockPos(x, y + 1, z), support, 2);
                        }
                        placedAbove = placedHere;
                    }
                }
            }
        }
        return placed;
    }
}
