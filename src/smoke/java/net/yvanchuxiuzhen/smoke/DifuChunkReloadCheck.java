package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** 实际卸载与重载两个相邻区块，核对保存延迟、计划刻顺序与跨界支撑。 */
final class DifuChunkReloadCheck extends SystemCheck {
    private static final BlockPos EDGE = new BlockPos(4111, 260, 4096);
    private static final List<ChunkPos> CHUNKS = List.of(new ChunkPos(EDGE), new ChunkPos(EDGE.east()));
    private final Set<ChunkPos> unloaded = new HashSet<>();
    private final Set<ChunkPos> reread = new HashSet<>();
    private final List<ScheduledTick<Block>> blocks = new ArrayList<>();
    private final List<ScheduledTick<Fluid>> fluids = new ArrayList<>();
    private ServerLevel level;
    private long absentSince = -1;
    private boolean requested;

    DifuChunkReloadCheck(List<String> report, String levelId) {
        super("地府区块真实重载", report, levelId);
    }

    @Override protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(p -> {
                level = p.serverLevel();
                NeoForge.EVENT_BUS.register(this);
                CHUNKS.forEach(c -> level.getChunk(c.x, c.z));
                scheduleRow(0);
                level.setBlock(EDGE, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 2);
                scheduleRow(1);
            });
            return true;
        }
        if (local % 5 == 0) onServer(p -> advance(local));
        return false;
    }

    private void advance(int local) {
        var source = level.getChunkSource();
        if (!requested && unloaded.containsAll(CHUNKS) && CHUNKS.stream().allMatch(c -> source.getChunkNow(c.x, c.z) == null)) {
            if (absentSince < 0) absentSince = level.getGameTime();
            if (level.getGameTime() - absentSince >= 10) {
                source.chunkMap.flushWorker();
                for (var c : CHUNKS) {
                    var tag = source.chunkMap.read(c).join().orElseThrow();
                    checkDisk(tag, c, blocks, BuiltInRegistries.BLOCK, "block_ticks");
                    checkDisk(tag, c, fluids, BuiltInRegistries.FLUID, "fluid_ticks");
                }
                requested = true;
                var c = CHUNKS.getFirst();
                level.setChunkForced(c.x, c.z, true);
            }
        }
        if (requested && reread.containsAll(CHUNKS) && CHUNKS.stream().allMatch(c -> source.isPositionTicking(c.toLong()))) {
            verifyQueue(blocks, c -> (LevelChunkTicks<Block>) c.getBlockTicks(), "方块");
            verifyQueue(fluids, c -> (LevelChunkTicks<Fluid>) c.getFluidTicks(), "流体");
            finishShape();
            closeCheck();
        } else if (local >= 900) {
            fail("等待两块卸载和重新可执行计划刻超时：卸载=" + unloaded + " 重载=" + reread);
            closeCheck();
        }
    }

    private void scheduleRow(int row) {
        for (int x = 0; x < 2; x++) {
            var pos = EDGE.offset(x, row, 4);
            long due = level.getGameTime() + 2400;
            var block = new ScheduledTick<>(Blocks.SAND, pos, due, TickPriority.HIGH, level.nextSubTickCount());
            ScheduledTick<Fluid> fluid = new ScheduledTick<>(Fluids.WATER, pos.south(), due, TickPriority.HIGH, level.nextSubTickCount());
            level.getBlockTicks().schedule(block);
            level.getFluidTicks().schedule(fluid);
            blocks.add(block);
            fluids.add(fluid);
        }
    }

    private <T> void checkDisk(CompoundTag tag, ChunkPos c, List<ScheduledTick<T>> original, Registry<T> registry, String key) {
        var saved = new ArrayList<SavedTick<T>>();
        SavedTick.loadTickList(tag.getList(key, Tag.TAG_COMPOUND), id -> registry.getOptional(ResourceLocation.parse(id)), c, saved::add);
        for (var t : original) if (new ChunkPos(t.pos()).equals(c)) {
            expectTrue(key + " 保存类型、坐标、剩余延迟和优先级 " + t.pos(), saved.contains(new SavedTick<>(
                    t.type(), t.pos(), (int) (t.triggerTick() - tag.getLong("LastUpdate")), t.priority())));
        }
    }

    private <T> void verifyQueue(List<ScheduledTick<T>> original, Function<LevelChunk, LevelChunkTicks<T>> get, String label) {
        var actual = new ArrayList<ScheduledTick<T>>();
        for (var c : CHUNKS) get.apply(level.getChunk(c.x, c.z)).getAll().filter(t -> original.stream().anyMatch(o -> o.pos().equals(t.pos()) && o.type() == t.type())).forEach(actual::add);
        expect(label + "重载记录数", original.size(), actual.size());
        for (var t : original) expectTrue(label + "读回优先级且到期时间后移 " + t.pos(), actual.stream().anyMatch(a ->
                a.type() == t.type() && a.pos().equals(t.pos()) && a.priority() == t.priority() && a.triggerTick() > t.triggerTick()));
        expectTrue(label + "原生重载重新分配序号", actual.size() == 4 && actual.stream().allMatch(t -> t.subTickOrder() < 0));
        // 仅在夹具坐标恢复原始绝对时间与序号，不修改存盘格式或其他计划刻。
        for (var c : CHUNKS) get.apply(level.getChunk(c.x, c.z)).removeIf(actual::contains);
        for (var t : original) get.apply(level.getChunkAt(t.pos())).schedule(t);
        var restored = new ArrayList<ScheduledTick<T>>();
        for (var c : CHUNKS) get.apply(level.getChunk(c.x, c.z)).getAll().filter(original::contains).forEach(restored::add);
        restored.sort(Comparator.comparingLong(ScheduledTick::subTickOrder));
        expectTrue(label + "恢复后四条完整记录及相对顺序一致", restored.equals(original));
    }

    private void finishShape() {
        expectTrue("未统一收尾的火把从区域文件读回", level.getBlockState(EDGE).is(Blocks.WALL_TORCH));
        level.setBlock(EDGE.east(), Blocks.GOLD_BLOCK.defaultBlockState(), 3);
        var shape = new BitSetDiscreteVoxelShape(2, 1, 1);
        shape.fill(0, 0, 0);
        shape.fill(1, 0, 0);
        StructureTemplate.updateShapeAtEdge(level, 3, shape, EDGE);
        for (var pos : List.of(EDGE, EDGE.east())) {
            var state = level.getBlockState(pos);
            var updated = Block.updateFromNeighbourShapes(state, level, pos);
            if (updated != state) level.setBlock(pos, updated, 18);
            level.blockUpdated(pos, updated.getBlock());
        }
        expectTrue("统一收尾后火把保留", level.getBlockState(EDGE).is(Blocks.WALL_TORCH));
        level.removeBlock(EDGE.east(), false);
        expectTrue("去掉跨界支撑立即掉落火把", level.isEmptyBlock(EDGE));
    }

    @SubscribeEvent public void unload(ChunkEvent.Unload event) {
        if (event.getLevel() == level && CHUNKS.contains(event.getChunk().getPos())) unloaded.add(event.getChunk().getPos());
    }

    @SubscribeEvent public void load(ChunkEvent.Load event) {
        if (requested && event.getLevel() == level && !event.isNewChunk() && CHUNKS.contains(event.getChunk().getPos())) reread.add(event.getChunk().getPos());
    }

    private void closeCheck() {
        if (requested) {
            var c = CHUNKS.getFirst();
            level.setChunkForced(c.x, c.z, false);
        }
        NeoForge.EVENT_BUS.unregister(this);
        finish();
    }
}
