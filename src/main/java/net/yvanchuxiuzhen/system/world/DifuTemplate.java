package net.yvanchuxiuzhen.system.world;

import com.mojang.datafixers.DataFixer;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagTypes;
import net.minecraft.util.datafix.DataFixTypes;

/** 内置地府模板的紧凑原始数据；方块按 Y、X、Z 索引，0 表示未声明，非零值为调色板序号加一。 */
public final class DifuTemplate {
    private final Vec3i size;
    private final byte[] cells;
    private final CompoundTag metadata;
    private final Map<Integer, CompoundTag> blockData;

    private DifuTemplate(Vec3i size, byte[] cells, CompoundTag metadata, Map<Integer, CompoundTag> blockData) {
        this.size = size;
        this.cells = cells;
        this.metadata = metadata;
        this.blockData = blockData;
    }

    public Vec3i size() { return size; }
    public int state(int index) { return Byte.toUnsignedInt(cells[index]) - 1; }
    public CompoundTag metadata() { return metadata; }
    public Map<Integer, CompoundTag> blockData() { return blockData; }

    public void exportCells(OutputStream output) throws IOException {
        try (var gzip = new GZIPOutputStream(output)) { gzip.write(cells); }
    }

    public static byte[] bundledCells() throws IOException {
        try (var stream = DifuTemplate.class.getResourceAsStream("/net/yvanchuxiuzhen/world/difu-cells.gz")) {
            if (stream == null) throw new FileNotFoundException("缺少内置地府状态表");
            try (var gzip = new GZIPInputStream(stream)) { return gzip.readNBytes(227 * 69 * 539 + 1); }
        }
    }

    /** 与内置模板摘要绑定的 1.21.1 结构元数据；数据包替换不使用此资源。 */
    public static CompoundTag bundledMetadata() throws IOException {
        try (var stream = DifuTemplate.class.getResourceAsStream("/net/yvanchuxiuzhen/world/difu-metadata.nbt")) {
            if (stream == null) throw new FileNotFoundException("缺少内置地府的升级元数据");
            return NbtIo.readCompressed(stream, NbtAccounter.create(4L << 20));
        }
    }

    /** 普通格只有坐标与状态编号；对同一调色板及全部嵌套实体数据执行原版结构升级。 */
    public CompoundTag upgradedMetadata(DataFixer fixer) {
        CompoundTag structure = metadata.copy();
        ListTag annotated = new ListTag();
        blockData.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            int index = entry.getKey();
            ListTag pos = new ListTag();
            pos.add(IntTag.valueOf(index / size.getZ() % size.getX()));
            pos.add(IntTag.valueOf(index / size.getZ() / size.getX()));
            pos.add(IntTag.valueOf(index % size.getZ()));
            CompoundTag block = new CompoundTag();
            block.put("pos", pos);
            block.putInt("state", state(index));
            block.put("nbt", entry.getValue().copy());
            annotated.add(block);
        });
        structure.put("blocks", annotated);
        return DataFixTypes.STRUCTURE.updateToCurrentVersion(fixer, structure, NbtUtils.getDataVersion(structure, 500));
    }

    /** 读取内置转换器产出的单调色板布局，不同时持有八百余万个方块复合标签。 */
    public static DifuTemplate read(InputStream compressed) throws IOException {
        try (var input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(compressed, 65536), 65536))) {
            if (input.readUnsignedByte() != Tag.TAG_COMPOUND) throw new IOException("地府模板根不是复合标签");
            input.readUTF();
            CompoundTag metadata = new CompoundTag();
            Vec3i size = null;
            byte[] cells = null;
            Map<Integer, CompoundTag> blockData = new HashMap<>();
            NbtAccounter accounter = NbtAccounter.unlimitedHeap();
            int highest = -1;
            for (int kind; (kind = input.readUnsignedByte()) != Tag.TAG_END;) {
                String key = input.readUTF();
                if (!key.equals("blocks")) {
                    metadata.put(key, TagTypes.getType(kind).load(input, accounter));
                    continue;
                }
                ListTag dimensions = metadata.getList("size", Tag.TAG_INT);
                if (dimensions.size() != 3 || cells != null || kind != Tag.TAG_LIST || input.readUnsignedByte() != Tag.TAG_COMPOUND) {
                    throw new IOException("地府模板尺寸或方块列表格式错误");
                }
                size = new Vec3i(dimensions.getInt(0), dimensions.getInt(1), dimensions.getInt(2));
                if (!size.equals(new Vec3i(227, 69, 539))) throw new IOException("非内置地府模板尺寸：" + size);
                int volume = size.getX() * size.getY() * size.getZ();
                int count = input.readInt();
                if (count != volume) throw new IOException("内置地府模板方块数与体积不符");
                cells = new byte[volume];
                for (int i = 0; i < count; i++) {
                    CompoundTag block = CompoundTag.TYPE.load(input, accounter);
                    ListTag pos = block.getList("pos", Tag.TAG_INT);
                    if (pos.size() != 3 || !block.contains("state", Tag.TAG_INT)) throw new IOException("地府方块缺少坐标或状态");
                    int x = pos.getInt(0), y = pos.getInt(1), z = pos.getInt(2), state = block.getInt("state");
                    if (x < 0 || x >= size.getX() || y < 0 || y >= size.getY() || z < 0 || z >= size.getZ() || state < 0 || state >= 255) {
                        throw new IOException("地府方块坐标或调色板序号越界");
                    }
                    int index = (y * size.getX() + x) * size.getZ() + z;
                    if (cells[index] != 0) throw new IOException("地府模板有重复坐标：" + pos);
                    cells[index] = (byte) (state + 1);
                    highest = Math.max(highest, state);
                    if (block.contains("nbt", Tag.TAG_COMPOUND)) blockData.put(index, block.getCompound("nbt"));
                }
            }
            if (cells == null || highest >= metadata.getList("palette", Tag.TAG_COMPOUND).size() || input.read() != -1) {
                throw new IOException("地府模板缺少方块、调色板不匹配或含多余数据");
            }
            return new DifuTemplate(size, cells, metadata, Map.copyOf(blockData));
        }
    }
}
