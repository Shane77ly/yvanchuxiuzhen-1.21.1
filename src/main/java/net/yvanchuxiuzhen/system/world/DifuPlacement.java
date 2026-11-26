package net.yvanchuxiuzhen.system.world;

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;
import net.yvanchuxiuzhen.system.world.terrain.mixin.StructureRepositoryAccess;

/** 内置地府无旋转、无处理器的原点放置，按原生三组排序，全局完成补水及形状更新后才生成实体。 */
final class DifuPlacement {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "difujiegou");
    private static final String SHA256 = "ab3e3cf6587eecc6d195058ccf0ddd2fc64ee9ad4f2f4de7f0ae2e17da12000b";
    private static final Direction[] WATER_NEIGHBORS = {Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private final byte[] cells;
    private final ServerLevel level;
    private final BlockPos origin;
    private final int sx, sy, sz, volume;
    private final BlockState[] palette;
    private final boolean[] full;
    private final Map<Integer, CompoundTag> data = new HashMap<>();
    private final BitSet annotated = new BitSet(), changed = new BitSet(), sources = new BitSet(), pending = new BitSet();
    private final BitSetDiscreteVoxelShape shape;
    private final CompoundTag metadata;

    static void place(ServerLevel level, BlockPos origin) {
        var manager = level.getStructureManager();
        if (!((StructureRepositoryAccess) manager).yvanchuxiuzhen$structureRepository().containsKey(ID)
                && !Files.exists(manager.createAndValidatePathToGeneratedStructure(ID, ".nbt"))
                && !(SharedConstants.IS_RUNNING_IN_IDE && Files.exists(Path.of(StructureUtils.testStructuresDir, ID.getPath() + ".snbt")))) {
            var resource = level.getServer().getResourceManager().getResource(ID.withPath("structure/difujiegou.nbt"));
            if (resource.isPresent()) {
                try {
                    var hasher = Hashing.sha256().newHasher();
                    try (var in = resource.get().open()) {
                        byte[] buf = new byte[65536];
                        for (int n; (n = in.read(buf)) >= 0;) hasher.putBytes(buf, 0, n);
                    }
                    if (hasher.hash().toString().equals(SHA256)) {
                        var placement = new DifuPlacement(level, origin);
                        var notifications = (DifuStructureNotifications) level;
                        notifications.yvanchuxiuzhen$compactDifuNotifications(true);
                        try {
                            placement.placeAll();
                        } finally {
                            notifications.yvanchuxiuzhen$compactDifuNotifications(false);
                        }
                        return;
                    }
                } catch (IOException e) { throw new UncheckedIOException("读取地府结构失败", e); }
            }
        }
        WorldStructures.place(level, ID, origin);
    }

    private DifuPlacement(ServerLevel level, BlockPos origin) throws IOException {
        cells = DifuTemplate.bundledCells();
        this.level = level;
        this.origin = origin;
        metadata = DifuTemplate.bundledMetadata();
        var size = metadata.getList("size", Tag.TAG_INT);
        sx = size.getInt(0); sy = size.getInt(1); sz = size.getInt(2); volume = sx * sy * sz;
        if (cells.length != volume) throw new IOException("内置地府状态表尺寸不符");
        var states = metadata.getList("palette", Tag.TAG_COMPOUND);
        palette = new BlockState[states.size()]; full = new boolean[states.size()];
        var lookup = level.registryAccess().lookupOrThrow(Registries.BLOCK);
        for (int i = 0; i < palette.length; i++) {
            var state = NbtUtils.readBlockState(lookup, states.getCompound(i));
            palette[i] = state;
            full[i] = !state.getBlock().hasDynamicShape() && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        }
        for (Tag t : metadata.getList("blocks", Tag.TAG_COMPOUND)) {
            var block = (CompoundTag) t;
            var p = block.getList("pos", Tag.TAG_INT);
            int index = (p.getInt(1) * sx + p.getInt(0)) * sz + p.getInt(2);
            data.put(index, block.getCompound("nbt")); annotated.set(index);
        }
        shape = new BitSetDiscreteVoxelShape(sx, sy, sz);
    }

    private void placeAll() {
        int finalChunkX = (origin.getX() + sx - 1) >> 4;
        for (int group = 0; group < 3; group++) {
            for (int i = 0; i < volume; i++) {
                // 原点对齐区块；沿模板底层首次扫过 X 时预订下一列，不改变原生三组写入顺序。
                if (group == 0 && i < sx * sz && i % (16 * sz) == 0) {
                    int nextX = ((origin.getX() + i / sz) >> 4) + 1;
                    if (i == 0) requestColumn(nextX - 1);
                    if (nextX <= finalChunkX) requestColumn(nextX);
                }
                int state = state(i);
                if (group == group(i, state)) placeBlock(i, palette[state]);
            }
        }
        finishWater();
        if (!changed.isEmpty()) {
            StructureTemplate.updateShapeAtEdge(level, 3, shape, origin);
            for (int group = 0; group < 3; group++) {
                for (int i = changed.nextSetBit(0); i >= 0; i = changed.nextSetBit(i + 1)) {
                    if (group != group(i, state(i))) continue;
                    var pos = pos(i);
                    var old = level.getBlockState(pos);
                    var next = Block.updateFromNeighbourShapes(old, level, pos);
                    if (old != next) level.setBlock(pos, next, 18);
                    level.blockUpdated(pos, next.getBlock());
                    if (annotated.get(i) && level.getBlockEntity(pos) instanceof BlockEntity entity) entity.setChanged();
                }
            }
        }
        metadata.put("blocks", new ListTag());
        var entities = new StructureTemplate();
        entities.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), metadata);
        entities.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.random, 3);
    }

    private int state(int index) { return Byte.toUnsignedInt(cells[index]) - 1; }

    private void requestColumn(int x) {
        for (int z = origin.getZ() >> 4; z <= (origin.getZ() + sz - 1) >> 4; z++) {
            var chunk = new ChunkPos(x, z);
            level.getChunkSource().addRegionTicket(TicketType.UNKNOWN, chunk, 0, chunk);
        }
    }

    private int group(int i, int state) { return annotated.get(i) ? 2 : full[state] ? 0 : 1; }

    private BlockPos pos(int i) { return origin.offset(i / sz % sx, i / sz / sx, i % sz); }

    private void placeBlock(int i, BlockState state) {
        var pos = pos(i);
        var fluid = level.getFluidState(pos);
        if (annotated.get(i)) {
            Clearable.tryClear(level.getBlockEntity(pos));
            level.setBlock(pos, Blocks.BARRIER.defaultBlockState(), 20);
        }
        if (!level.setBlock(pos, state, 3)) return;
        changed.set(i); shape.fill(i / sz % sx, i / sz / sx, i % sz);
        if (annotated.get(i)) {
            var entity = level.getBlockEntity(pos);
            if (entity != null) entity.loadWithComponents(data.get(i), level.registryAccess());
        }
        if (state.getFluidState().isSource()) sources.set(i);
        else if (state.getBlock() instanceof LiquidBlockContainer container) {
            container.placeLiquid(level, pos, state, fluid);
            if (!fluid.isSource()) pending.set(i);
        }
    }

    private void finishWater() {
        boolean progressed;
        do {
            progressed = false;
            for (int group = 0; group < 3; group++) {
                for (int i = pending.nextSetBit(0); i >= 0; i = pending.nextSetBit(i + 1)) {
                    if (group != group(i, state(i))) continue;
                    var pos = pos(i);
                    var fluid = level.getFluidState(pos);
                    for (var direction : WATER_NEIGHBORS) {
                        if (fluid.isSource()) break;
                        var adjacent = pos.relative(direction);
                        var neighbor = level.getFluidState(adjacent);
                        int x = adjacent.getX() - origin.getX(), y = adjacent.getY() - origin.getY(), z = adjacent.getZ() - origin.getZ();
                        if (neighbor.isSource() && !(x >= 0 && x < sx && y >= 0 && y < sy && z >= 0 && z < sz && sources.get((y * sx + x) * sz + z))) fluid = neighbor;
                    }
                    var state = level.getBlockState(pos);
                    if (fluid.isSource() && state.getBlock() instanceof LiquidBlockContainer container) {
                        container.placeLiquid(level, pos, state, fluid);
                        pending.clear(i); progressed = true;
                    }
                }
            }
        } while (progressed);
    }
}
