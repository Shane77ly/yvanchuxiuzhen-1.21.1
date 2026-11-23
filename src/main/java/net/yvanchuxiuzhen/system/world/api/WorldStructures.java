package net.yvanchuxiuzhen.system.world.api;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.world.WorldGeneration;

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

    /** 放置地下或水下密闭结构前封堵外围 1 格壳层的流体，放置后再清除内部残留流体以防渗水。 */
    public static void placeSealed(ServerLevel level, ResourceLocation id, BlockPos origin, BlockState shellSeal) {
        StructureTemplate template = level.getStructureManager().getOrCreate(id);
        Vec3i size = template.getSize();
        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + size.getX() - 1;
        int maxY = minY + size.getY() - 1;
        int maxZ = minZ + size.getZ() - 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX - 1; x <= maxX + 1; x++) {
            for (int y = minY - 1; y <= maxY + 1; y++) {
                for (int z = minZ - 1; z <= maxZ + 1; z++) {
                    if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) continue;
                    cursor.set(x, y, z);
                    if (!level.getFluidState(cursor).isEmpty()) {
                        level.setBlock(cursor, shellSeal, Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings().setRotation(Rotation.NONE)
                .setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, 3);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (!level.getFluidState(cursor).isEmpty()) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** 服务端主线程读取来源首次登录生成的全局传送阵中心，初始化完成前返回空。 */
    public static Optional<Vec3> teleportArrayCenter(ServerLevel level) {
        return WorldGeneration.teleportArrayCenter(level);
    }
}
