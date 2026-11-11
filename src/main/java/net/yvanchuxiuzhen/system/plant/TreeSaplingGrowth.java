package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.yvanchuxiuzhen.platform.ModId;

/** 执行17种树苗共享的维度、概率入口，并按逐树来源规则放置模板与树干。 */
final class TreeSaplingGrowth {
    private static final int[][] CHESTNUT_UPPER = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
    private static final int[][] CHESTNUT_ROOT = {{1, 0}, {0, 1}, {1, 1}, {0, 0}};

    private TreeSaplingGrowth() {
    }

    static void grow(TreeSapling sapling, ServerLevel level, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        if (dimension.equals(id("fabao_009weidu")) || dimension.equals(id("xvkong"))) return;

        ResourceLocation template = chooseTemplate(sapling);
        if (template == null) return;

        placeTemplate(sapling, level, pos, template);
        placeTrunk(sapling, level, pos);
    }

    private static ResourceLocation chooseTemplate(TreeSapling sapling) {
        if (sapling.templateRule() == TreeSapling.TemplateRule.LAUREL_BRANCH) {
            if (Math.random() < 0.05) {
                return sapling.template();
            }
            if (Math.random() < 0.1) {
                return sapling.alternateTemplate();
            }
            return null;
        }
        if (!(Math.random() < 0.05)) return null;
        if (sapling.templateRule() == TreeSapling.TemplateRule.FIRST_40_PERCENT) {
            return Math.random() < 0.4 ? sapling.template() : sapling.alternateTemplate();
        }
        return sapling.template();
    }

    private static void placeTemplate(TreeSapling sapling, ServerLevel level, BlockPos pos, ResourceLocation templateId) {
        StructureTemplate template = level.getStructureManager().getOrCreate(templateId);
        if (sapling == TreeSapling.CHESTNUT) {
            BlockPos origin = pos.offset(-5, 5, -5);
            template.placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, Block.UPDATE_ALL);
            return;
        }

        Rotation rotation;
        BlockPos origin;
        if (Math.random() < 0.25) {
            origin = pos.offset(-sapling.xOffset(), sapling.yOffset(), sapling.zOffset());
            rotation = Rotation.COUNTERCLOCKWISE_90;
        } else if (Math.random() < 0.333) {
            origin = pos.offset(sapling.xOffset(), sapling.yOffset(), sapling.zOffset());
            rotation = Rotation.CLOCKWISE_180;
        } else if (Math.random() < 0.5) {
            origin = pos.offset(sapling.xOffset(), sapling.yOffset(), -sapling.zOffset());
            rotation = Rotation.CLOCKWISE_90;
        } else {
            origin = pos.offset(-sapling.xOffset(), sapling.yOffset(), -sapling.zOffset());
            rotation = Rotation.NONE;
        }
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(rotation)
                .setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, Block.UPDATE_ALL);
    }

    private static void placeTrunk(TreeSapling sapling, ServerLevel level, BlockPos pos) {
        BlockState log = sapling.log().defaultBlockState();
        switch (sapling.trunkRule()) {
            case VERTICAL -> {
                for (int height = sapling.trunkHeight() - 1; height >= 0; height--) {
                    level.setBlock(pos.above(height), log, Block.UPDATE_ALL);
                }
            }
            case CHESTNUT_2X2 -> {
                for (int height = 4; height >= 1; height--) {
                    for (int[] offset : CHESTNUT_UPPER) {
                        level.setBlock(pos.offset(offset[0], height, offset[1]), log, Block.UPDATE_ALL);
                    }
                }
                for (int[] offset : CHESTNUT_ROOT) {
                    level.setBlock(pos.offset(offset[0], 0, offset[1]), log, Block.UPDATE_ALL);
                }
            }
            case NANMU_3X7X3 -> {
                Block saplingBlock = BuiltInRegistries.BLOCK.get(id("nanmusapling"));
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 5; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos target = pos.offset(x, y, z);
                            if (!level.getBlockState(target).is(saplingBlock)) {
                                level.setBlock(target, log, Block.UPDATE_ALL);
                            }
                        }
                    }
                }
                level.setBlock(pos, log, Block.UPDATE_ALL);
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
