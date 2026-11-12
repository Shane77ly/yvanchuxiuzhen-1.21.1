package net.yvanchuxiuzhen.system.plant;

import java.util.ArrayList;
import java.util.List;
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
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 桑苗生长放置树冠与主干；根部在首个服务器刻末写入，保留来源事件队列时序。 */
public final class MulberryGrowth {
    private static final List<RootPlacement> ROOTS = new ArrayList<>();

    private MulberryGrowth() {
    }

    public static void grow(ServerLevel level, BlockPos pos) {
        ResourceLocation dimension = level.dimension().location();
        if (dimension.equals(id("fabao_009weidu")) || dimension.equals(id("xvkong"))) return;
        if (!(Math.random() < 0.05)) return;
        if (!level.canSeeSkyFromBelowWater(pos.above())) return;

        Rotation rotation;
        BlockPos origin;
        if (Math.random() < 0.25) {
            origin = pos.offset(-3, 3, 3);
            rotation = Rotation.COUNTERCLOCKWISE_90;
        } else if (Math.random() < 0.333) {
            origin = pos.offset(3, 3, 3);
            rotation = Rotation.CLOCKWISE_180;
        } else if (Math.random() < 0.5) {
            origin = pos.offset(3, 3, -3);
            rotation = Rotation.CLOCKWISE_90;
        } else {
            origin = pos.offset(-3, 3, -3);
            rotation = Rotation.NONE;
        }
        StructureTemplate template = level.getStructureManager().getOrCreate(id("mulberry_01"));
        template.placeInWorld(level, origin, origin, new StructurePlaceSettings()
                .setRotation(rotation).setMirror(Mirror.NONE).setIgnoreEntities(false), level.random, Block.UPDATE_ALL);

        BlockState leaves = block("mulberryleaves");
        level.setBlock(pos.offset(1, 2, 1), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(1, 2, 0), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(1, 2, -1), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(0, 2, 1), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(0, 2, -1), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(-1, 2, 1), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(-1, 2, 0), leaves, Block.UPDATE_ALL);
        level.setBlock(pos.offset(-1, 2, -1), leaves, Block.UPDATE_ALL);
        BlockState log = block("mulberrylog");
        level.setBlock(pos.above(2), log, Block.UPDATE_ALL);
        level.setBlock(pos.above(), log, Block.UPDATE_ALL);
        ROOTS.add(new RootPlacement(level, pos.immutable()));
    }

    public static void afterServerTick(ServerTickEvent.Post event) {
        List<RootPlacement> ready = new ArrayList<>();
        ROOTS.removeIf(root -> {
            if (root.level().getServer() != event.getServer()) return false;
            ready.add(root);
            return true;
        });
        for (RootPlacement root : ready) {
            root.level().setBlock(root.pos(), block("mulberrylog"), Block.UPDATE_ALL);
        }
    }

    public static void stopped(ServerStoppedEvent event) {
        ROOTS.removeIf(root -> root.level().getServer() == event.getServer());
    }

    private static BlockState block(String path) {
        return BuiltInRegistries.BLOCK.get(id(path)).defaultBlockState();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private record RootPlacement(ServerLevel level, BlockPos pos) {
    }
}
