package net.yvanchuxiuzhen.system.world.api;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** 玩法结构的固定坐标放置入口；模板、实体与方块实体数据由原生结构服务加载。 */
public final class WorldStructures {
    private WorldStructures() {
    }

    /** 来源副本与画里乾坤使用无旋转、无镜像、包含实体和完整方块更新。 */
    public static void place(ServerLevel level, ResourceLocation id, BlockPos origin) {
        StructureTemplate template = level.getStructureManager().getOrCreate(id);
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, 3);
    }
}
