package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.yvanchuxiuzhen.platform.ModId;

/** 竹林随机选择器的草与蕨：保留提供器随机及来源 Forge 土壤支撑结果。 */
final class SourceTreeGroundcoverFeature extends Feature<SimpleBlockConfiguration> {
    private static final TagKey<Block> GROUND = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_tree_sapling_ground"));

    SourceTreeGroundcoverFeature() {
        super(SimpleBlockConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> context) {
        BlockPos position = context.origin();
        BlockState state = context.config().toPlace().getState(context.random(), position);
        if (context.level().getBlockState(position.below()).is(GROUND)) {
            context.level().setBlock(position, state, 2);
            return true;
        }
        return false;
    }
}
