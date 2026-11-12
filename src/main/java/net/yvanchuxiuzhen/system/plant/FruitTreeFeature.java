package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.yvanchuxiuzhen.platform.ModId;

/** 花椒与八角自然地物只在人间按3%概率、草方块基底放置来源专属模板。 */
public final class FruitTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation DIMENSION = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian");
    private final FruitTree tree;
    private StructureTemplate template;

    public FruitTreeFeature(FruitTree tree) {
        super(NoneFeatureConfiguration.CODEC);
        this.tree = tree;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!level.getLevel().dimension().location().equals(DIMENSION)) return false;
        if (template == null) template = level.getLevel().getStructureManager().getOrCreate(tree.naturalTemplate());
        boolean anyPlaced = false;
        RandomSource random = context.random();
        if (random.nextInt(1000000) + 1 <= 30000) {
            int attempts = random.nextInt(1) + 1;
            for (int attempt = 0; attempt < attempts; attempt++) {
                int x = context.origin().getX() + random.nextInt(16);
                int z = context.origin().getZ() + random.nextInt(16);
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (!level.getBlockState(new BlockPos(x, y, z)).is(Blocks.GRASS_BLOCK)) continue;
                BlockPos origin = new BlockPos(x - 2, y + 1, z - 2);
                if (template.placeInWorld(level, origin, origin, new StructurePlaceSettings()
                        .setMirror(Mirror.NONE).setRotation(Rotation.NONE).setRandom(random)
                        .addProcessor(BlockIgnoreProcessor.AIR).setIgnoreEntities(false), random, 2)) {
                    anyPlaced = true;
                }
            }
        }
        return anyPlaced;
    }
}
