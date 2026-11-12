package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.yvanchuxiuzhen.platform.ModId;

/** 两种调味树苗按5%生长、独立顺序抽取模板旋转，并在同次回调从上至下补树干。 */
final class FruitTreeGrowth {
    private FruitTreeGrowth() {
    }

    static void grow(FruitTree tree, ServerLevel level, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        if (dimension.equals(id("fabao_009weidu")) || dimension.equals(id("xvkong"))) return;
        if (!(Math.random() < 0.05)) return;

        Rotation rotation;
        BlockPos origin;
        if (Math.random() < 0.25) {
            origin = pos.offset(-2, 1, 2);
            rotation = Rotation.COUNTERCLOCKWISE_90;
        } else if (Math.random() < 0.333) {
            origin = pos.offset(2, 1, 2);
            rotation = Rotation.CLOCKWISE_180;
        } else if (Math.random() < 0.5) {
            origin = pos.offset(2, 1, -2);
            rotation = Rotation.CLOCKWISE_90;
        } else {
            origin = pos.offset(-2, 1, -2);
            rotation = Rotation.NONE;
        }
        StructureTemplate template = level.getStructureManager().getOrCreate(tree.growthTemplate());
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings()
                .setRotation(rotation).setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, Block.UPDATE_ALL);

        BlockState log = tree.log().defaultBlockState();
        for (int height = tree.rootHeight() - 1; height >= 0; height--) {
            level.setBlock(pos.above(height), log, Block.UPDATE_ALL);
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
