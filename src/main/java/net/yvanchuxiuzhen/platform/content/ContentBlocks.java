package net.yvanchuxiuzhen.platform.content;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.platform.content.block.ContentButton;
import net.yvanchuxiuzhen.platform.content.block.ContentDoor;
import net.yvanchuxiuzhen.platform.content.block.ContentFalling;
import net.yvanchuxiuzhen.platform.content.block.ContentFence;
import net.yvanchuxiuzhen.platform.content.block.ContentFenceGate;
import net.yvanchuxiuzhen.platform.content.block.ContentLeaves;
import net.yvanchuxiuzhen.platform.content.block.ContentPane;
import net.yvanchuxiuzhen.platform.content.block.ContentPressurePlate;
import net.yvanchuxiuzhen.platform.content.block.ContentSlab;
import net.yvanchuxiuzhen.platform.content.block.ContentStairs;
import net.yvanchuxiuzhen.platform.content.block.ContentTrapDoor;
import net.yvanchuxiuzhen.platform.content.block.ContentWall;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 按内容表行的工厂类型创建方块；未知工厂类型直接报错，不回退为普通方块。 */
final class ContentBlocks {
    private static final Map<String, BlockSetType> SET_TYPES = new HashMap<>();

    private ContentBlocks() {
    }

    static Block create(BlockRow row) {
        BlockTraits traits = new BlockTraits(row);
        String factory = row.identity().factory();
        if (!factory.equals("block") && (!row.state().states().isEmpty() || row.state().placement().isPresent())) {
            throw new IllegalArgumentException("原版模板方块不应带自定义状态：" + row.id());
        }
        return switch (factory) {
            case "block" -> traits.waterlogged()
                    ? ContentBlock.create(traits, WaterloggedContentBlock::new)
                    : ContentBlock.create(traits, ContentBlock::new);
            case "slab" -> new ContentSlab(traits);
            case "stairs" -> new ContentStairs(traits);
            case "wall" -> new ContentWall(traits);
            case "leaves" -> new ContentLeaves(traits);
            case "fence" -> new ContentFence(traits);
            case "fence_gate" -> new ContentFenceGate(traits);
            case "pane" -> new ContentPane(traits);
            case "falling" -> new ContentFalling(traits);
            case "button" -> new ContentButton(traits, setType(row, "button", BlockSetType.OAK));
            case "pressure_plate" -> new ContentPressurePlate(traits, setType(row, "plate",
                    row.identity().material().equals("WOOD") ? BlockSetType.OAK : BlockSetType.STONE));
            case "door" -> new ContentDoor(traits, setType(row, "door", metalOr(row)));
            case "trapdoor" -> new ContentTrapDoor(traits, setType(row, "trapdoor", metalOr(row)));
            default -> throw new IllegalArgumentException("未知方块工厂 " + factory + "：" + row.id());
        };
    }

    private static BlockSetType metalOr(BlockRow row) {
        return row.identity().material().equals("METAL") ? BlockSetType.IRON : BlockSetType.OAK;
    }

    /**
     * 1.21.1 的门、活板门与按钮构造器会用 BlockSetType 的声音覆盖 Properties 的声音，
     * 因此按 (用途, 基础类型, 音效) 派生保留来源音效的 BlockSetType；压力板固定为任意实体触发，与来源一致。
     */
    private static BlockSetType setType(BlockRow row, String use, BlockSetType base) {
        String soundName = row.identity().sound().left()
                .orElseThrow(() -> new IllegalArgumentException("模板方块不支持自定义音效：" + row.id()));
        String name = "yvanchuxiuzhen_" + use + "_" + base.name() + "_" + soundName.toLowerCase();
        SoundType sound = LegacyTables.SOUND_TYPES.get(soundName);
        return SET_TYPES.computeIfAbsent(name, key -> LegacyTables.setType(key, base, sound,
                use.equals("plate") ? BlockSetType.PressurePlateSensitivity.EVERYTHING : base.pressurePlateSensitivity()));
    }
}
