package net.yvanchuxiuzhen.smoke;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.DifuTemplate;

/** 读取实际数据包而不落块，与独立扫描的全部调色板序号摘要核对。 */
final class DifuTemplateCheck extends SystemCheck {
    private final boolean export;

    DifuTemplateCheck(List<String> report, String levelId, boolean export) {
        super("地府全模板读取", report, levelId);
        this.export = export;
    }

    @Override protected boolean step(int step, int local) {
        onServer(this::read);
        finish();
        return true;
    }

    private void read(ServerPlayer player) {
        var resource = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "structure/difujiegou.nbt");
        try (var file = player.server.getResourceManager().open(resource)) {
            var template = DifuTemplate.read(file);
            var size = template.size();
            var palette = template.metadata().getList("palette", Tag.TAG_COMPOUND);
            var entities = template.metadata().getList("entities", Tag.TAG_COMPOUND);
            int volume = size.getX() * size.getY() * size.getZ();
            var digest = MessageDigest.getInstance("SHA-256");
            int air = 0;
            for (int i = 0; i < volume; i++) {
                int state = template.state(i);
                digest.update((byte) (state + 1));
                if (state == 41) air++;
            }
            expect("全部坐标的状态摘要", "412f81b2669673abb9cadb737d0c98d3dc0d3cdfe1336c7e46c84e7423539b5d", HexFormat.of().formatHex(digest.digest()));
            expectTrue("完整尺寸、239个状态及显式空气", volume == 8_442_357 && palette.size() == 239 && air == 7_777_913 && palette.getCompound(41).getString("Name").equals("minecraft:air"));
            expectTrue("三实体及十个头颅方块实体原始数据", entities.size() == 3 && entities.getCompound(2).getCompound("nbt").getString("id").equals("yvanchuxiuzhen:yanwang") && template.blockData().size() == 10 && template.blockData().values().stream().allMatch(nbt -> nbt.getString("id").equals("minecraft:skull")));
            var fixed = template.upgradedMetadata(player.server.getFixerUpper());
            if (export) {
                var dir = Path.of(System.getProperty("yvanchuxiuzhen.smoke"));
                NbtIo.writeCompressed(fixed, dir.resolve("difu-metadata.nbt"));
                try (var out = Files.newOutputStream(dir.resolve("difu-cells.gz"))) { template.exportCells(out); }
            } else {
                expectTrue("随包元数据与原版升级结果逐字段一致", fixed.equals(DifuTemplate.bundledMetadata()));
                expect("随包逐格状态与全表摘要一致", "412f81b2669673abb9cadb737d0c98d3dc0d3cdfe1336c7e46c84e7423539b5d", HexFormat.of().formatHex(digest.digest(DifuTemplate.bundledCells())));
            }
            var fixedBlocks = fixed.getList("blocks", Tag.TAG_COMPOUND);
            var fixedPalette = fixed.getList("palette", Tag.TAG_COMPOUND);
            boolean sameNames = fixedPalette.size() == palette.size();
            for (int i = 0; i < fixedPalette.size(); i++) sameNames &= fixedPalette.getCompound(i).getString("Name").equals(palette.getCompound(i).getString("Name"));
            expectTrue("原版升级保留239个调色板序号及其方块身份", sameNames);
            boolean skulls = fixedBlocks.size() == 10;
            for (int i = 0; i < fixedBlocks.size(); i++) {
                var block = fixedBlocks.getCompound(i);
                var pos = block.getList("pos", Tag.TAG_INT);
                int index = (pos.getInt(1) * size.getX() + pos.getInt(0)) * size.getZ() + pos.getInt(2);
                skulls &= template.blockData().containsKey(index) && template.state(index) == block.getInt("state") && block.getCompound("nbt").getString("id").equals("minecraft:skull");
            }
            var fixedEntities = fixed.getList("entities", Tag.TAG_COMPOUND);
            expectTrue("升级保留十个头颅坐标、状态与三实体", skulls && fixedEntities.size() == 3 && fixedEntities.getCompound(0).getCompound("nbt").getString("id").equals("minecraft:sheep") && fixedEntities.getCompound(2).getCompound("nbt").getString("id").equals("yvanchuxiuzhen:yanwang"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
