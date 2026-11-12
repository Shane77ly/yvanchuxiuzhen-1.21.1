package net.yvanchuxiuzhen.system.world;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** 自然景观与随机结构模板的来源采样、基底、世界条件及放置后回调。 */
final class WorldTemplateFeature extends Feature<NoneFeatureConfiguration> {
    enum HeightMode {
        GROUND, FLOATING, UNDERGROUND
    }

    record Parameters(ResourceLocation template, Set<ResourceLocation> dimensions, List<ResourceLocation> baseBlocks,
            int chance, int countBound, int countMinimum, Heightmap.Types heightmap, HeightMode heightMode,
            BlockPos offset, boolean randomTransform, BlockIgnoreProcessor processor, boolean randomStructure,
            boolean huaguang) {
    }

    private final Parameters parameters;
    private StructureTemplate template;

    WorldTemplateFeature(Parameters parameters) {
        super(NoneFeatureConfiguration.CODEC);
        this.parameters = parameters;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!parameters.dimensions().contains(level.getLevel().dimension().location())) {
            return false;
        }
        if (template == null) {
            template = level.getLevel().getStructureManager().getOrCreate(parameters.template());
        }
        boolean anyPlaced = false;
        RandomSource random = context.random();
        if (random.nextInt(1000000) + 1 <= parameters.chance()) {
            int count = random.nextInt(parameters.countBound()) + parameters.countMinimum();
            for (int attempt = 0; attempt < count; attempt++) {
                int x = context.origin().getX() + random.nextInt(16);
                int z = context.origin().getZ() + random.nextInt(16);
                int y = level.getHeight(parameters.heightmap(), x, z);
                y = switch (parameters.heightMode()) {
                    case GROUND -> y - 1;
                    case FLOATING -> y + random.nextInt(64) + 16;
                    case UNDERGROUND -> Mth.nextInt(random, 8 + level.getMinBuildHeight(),
                            Math.max(y, 9 + level.getMinBuildHeight()));
                };
                if (!parameters.baseBlocks().isEmpty() && !parameters.baseBlocks().contains(
                        BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, y, z)).getBlock()))) {
                    continue;
                }
                BlockPos origin = new BlockPos(x, y, z).offset(parameters.offset());
                if (parameters.randomStructure() && !WorldGeneration.randomStructuresAllowed(level.getLevel().getServer())) {
                    continue;
                }
                Mirror mirror = parameters.randomTransform() ? Mirror.values()[random.nextInt(2)] : Mirror.NONE;
                Rotation rotation = parameters.randomTransform() ? Rotation.values()[random.nextInt(3)] : Rotation.NONE;
                StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation)
                        .setRandom(random).addProcessor(parameters.processor()).setIgnoreEntities(false);
                if (template.placeInWorld(level, origin, origin, settings, random, 2)) {
                    if (parameters.huaguang()) {
                        appendHuaguang(level, origin);
                    }
                    anyPlaced = true;
                }
            }
        }
        return anyPlaced;
    }

    private void appendHuaguang(WorldGenLevel level, BlockPos origin) {
        // 来源后置过程只接受ServerLevel；自然生成区域不能透过getLevel绕过该条件。
        if (level instanceof ServerLevel serverLevel) {
            for (int layer = 1; layer <= 4; layer++) {
                StructureTemplate upper = serverLevel.getStructureManager().getOrCreate(parameters.template());
                BlockPos upperOrigin = origin.above(layer * 48);
                upper.placeInWorld(serverLevel, upperOrigin, upperOrigin,
                        new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE)
                                .setIgnoreEntities(false), serverLevel.random, 3);
            }
        }
    }
}
