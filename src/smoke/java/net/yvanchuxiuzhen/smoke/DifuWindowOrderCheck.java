package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;

/** 原生整模板与逐区块独立放置对照，比较跨界支撑及同刻方块、流体队列。 */
final class DifuWindowOrderCheck extends SystemCheck {
    DifuWindowOrderCheck(List<String> report, String levelId) {
        super("地府分窗顺序对照", report, levelId);
    }

    @Override protected boolean step(int step, int local) {
        onServer(this::compare);
        finish();
        return true;
    }

    private void compare(ServerPlayer player) {
        var level = player.serverLevel();
        var source = new BlockPos(15, 220, 0);
        for (int x = 0; x <= 1; x++) {
            for (int y : List.of(0, 2)) {
                level.setBlock(source.offset(x, y, 0), Blocks.SAND.defaultBlockState(), 2);
                level.setBlock(source.offset(x, y, 2), Blocks.WATER.defaultBlockState(), 2);
            }
        }
        level.setBlock(source.offset(1, 0, 4), Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(source.offset(0, 0, 4), Blocks.WALL_TORCH.defaultBlockState()
                .setValue(WallTorchBlock.FACING, Direction.WEST), 2);
        var template = new StructureTemplate();
        template.fillFromWorld(level, source, new Vec3i(2, 3, 5), false, Blocks.AIR);
        var whole = source.south(16);
        var split = source.south(32);
        expectTrue("原生整模板放置", template.placeInWorld(level, whole, whole, new StructurePlaceSettings(), level.random, 3));
        for (int x = 0; x <= 1; x++) {
            var box = BoundingBox.fromCorners(split.offset(x, 0, 0), split.offset(x, 2, 4));
            expectTrue("独立窗口放置 " + x, template.placeInWorld(level, split, split,
                    new StructurePlaceSettings().setBoundingBox(box), level.random, 3));
        }
        expectTrue("整模板火把保留", level.getBlockState(whole.south(4)).is(Blocks.WALL_TORCH));
        expectTrue("独立窗口先收尾时火把失去跨界支撑", level.isEmptyBlock(split.south(4)));
        var vanilla = List.of("0/0", "1/0", "0/2", "1/2");
        var chunkFirst = List.of("0/0", "0/2", "1/0", "1/2");
        for (boolean fluid : List.of(false, true)) {
            String kind = fluid ? "水" : "沙";
            expect(kind + "整模板同刻顺序", vanilla, order(level, whole, fluid));
            expect(kind + "独立窗口同刻顺序", chunkFirst, order(level, split, fluid));
        }
        roundTrip(level, whole, "沙", p -> (LevelChunkTicks<Block>) level.getChunkAt(p).getBlockTicks(),
                BuiltInRegistries.BLOCK, Blocks.SAND, 0);
        roundTrip(level, whole, "水", p -> (LevelChunkTicks<Fluid>) level.getChunkAt(p).getFluidTicks(),
                BuiltInRegistries.FLUID, Fluids.WATER, 2);
    }

    private <T> void roundTrip(ServerLevel level, BlockPos origin, String label,
            Function<BlockPos, LevelChunkTicks<T>> live, Registry<T> registry, T type, int z) {
        var reference = new ArrayList<ScheduledTick<T>>();
        var vanilla = new ArrayList<ScheduledTick<T>>();
        var preserved = new ArrayList<ScheduledTick<T>>();
        long time = level.getGameTime();
        for (int x = 0; x < 2; x++) {
            var anchor = origin.offset(x, 0, z);
            var queue = new LevelChunkTicks<T>();
            live.apply(anchor).getAll().filter(t -> t.type() == type &&
                    (t.pos().equals(anchor) || t.pos().equals(anchor.above(2)))).forEach(queue::schedule);
            queue.getAll().forEach(reference::add);
            var raw = queue.save(time, v -> registry.getKey(v).toString());
            var decoded = LevelChunkTicks.load(raw, id -> registry.getOptional(ResourceLocation.parse(id)), level.getChunkAt(anchor).getPos());
            decoded.unpack(time);
            decoded.getAll().forEach(vanilla::add);

            // 暂存样本保留原生字段，额外保存原序号；读回到独立队列，不修改世界存档格式。
            var archived = new ListTag();
            queue.getAll().forEach(t -> {
                var entry = SavedTick.saveTick(t, v -> registry.getKey(v).toString(), time);
                entry.putLong("order", t.subTickOrder());
                archived.add(entry);
            });
            var restored = new LevelChunkTicks<T>();
            for (int i = 0; i < archived.size(); i++) {
                var entry = archived.getCompound(i);
                restored.schedule(SavedTick.loadTick(entry, id -> registry.getOptional(ResourceLocation.parse(id)))
                        .orElseThrow().unpack(time, entry.getLong("order")));
            }
            restored.getAll().forEach(preserved::add);
        }
        expect(label + "原生分区块解包只有两个不同的序号", 2, vanilla.stream().map(ScheduledTick::subTickOrder).distinct().count());
        reference.sort(Comparator.comparingLong(ScheduledTick::subTickOrder));
        preserved.sort(Comparator.comparingLong(ScheduledTick::subTickOrder));
        expectTrue(label + "暂存序号恢复四格的类型、坐标、到期刻、优先级和次序", reference.size() == 4 && reference.equals(preserved));
    }

    private List<String> order(ServerLevel level, BlockPos origin, boolean fluid) {
        var ticks = new ArrayList<ScheduledTick<?>>();
        for (int x = 0; x <= 1; x++) {
            var chunk = level.getChunkAt(origin.east(x));
            LevelChunkTicks<?> container = (LevelChunkTicks<?>) (fluid ? chunk.getFluidTicks() : chunk.getBlockTicks());
            container.getAll().filter(t -> t.type() == (fluid ? Fluids.WATER : Blocks.SAND)
                    && t.pos().getX() >= origin.getX() && t.pos().getX() <= origin.getX() + 1
                    && t.pos().getZ() == origin.getZ() + (fluid ? 2 : 0)
                    && (t.pos().getY() == origin.getY() || t.pos().getY() == origin.getY() + 2)).forEach(ticks::add);
        }
        expectTrue("采样为同一到期刻及优先级", ticks.size() == 4 && ticks.stream().allMatch(t ->
                t.triggerTick() == ticks.getFirst().triggerTick() && t.priority() == ticks.getFirst().priority()));
        ticks.sort(Comparator.comparingLong(t -> t.subTickOrder()));
        return ticks.stream().map(t -> (t.pos().getX() - origin.getX()) + "/" + (t.pos().getY() - origin.getY())).toList();
    }
}
