package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** 按来源地物各自的维度、概率、基底与锚点放置自然树木模板。 */
final class NaturalTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final ResourceLocation templateId;
    private final Set<ResourceLocation> dimensions;
    private final List<Block> baseBlocks;
    private final int chance;
    private final Heightmap.Types heightmap;
    private final int horizontalOffset;
    private final boolean randomTransform;
    private StructureTemplate template;

    NaturalTreeFeature(ResourceLocation templateId, Set<ResourceLocation> dimensions, List<Block> baseBlocks,
            int chance, Heightmap.Types heightmap, int horizontalOffset, boolean randomTransform) {
        super(NoneFeatureConfiguration.CODEC);
        this.templateId = templateId;
        this.dimensions = dimensions;
        this.baseBlocks = baseBlocks;
        this.chance = chance;
        this.heightmap = heightmap;
        this.horizontalOffset = horizontalOffset;
        this.randomTransform = randomTransform;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!dimensions.contains(level.getLevel().dimension().location())) return false;
        if (template == null) template = level.getLevel().getStructureManager().getOrCreate(templateId);

        boolean anyPlaced = false;
        RandomSource random = context.random();
        if (random.nextInt(1000000) + 1 <= chance) {
            int count = random.nextInt(1) + 1;
            for (int attempt = 0; attempt < count; attempt++) {
                int x = context.origin().getX() + random.nextInt(16);
                int z = context.origin().getZ() + random.nextInt(16);
                int y = level.getHeight(heightmap, x, z) - 1;
                if (!baseBlocks.contains(level.getBlockState(new BlockPos(x, y, z)).getBlock())) continue;
                BlockPos origin = new BlockPos(x - horizontalOffset, y + 1, z - horizontalOffset);
                Mirror mirror = randomTransform ? Mirror.values()[random.nextInt(2)] : Mirror.NONE;
                Rotation rotation = randomTransform ? Rotation.values()[random.nextInt(3)] : Rotation.NONE;
                if (template.placeInWorld(level, origin, origin, new StructurePlaceSettings()
                        .setMirror(mirror).setRotation(rotation).setRandom(random)
                        .addProcessor(BlockIgnoreProcessor.AIR).setIgnoreEntities(false), random, 2)) {
                    anyPlaced = true;
                }
            }
        }
        return anyPlaced;
    }
}
