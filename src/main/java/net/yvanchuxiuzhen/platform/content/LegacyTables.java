package net.yvanchuxiuzhen.platform.content;

import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * 1.18.2 命名到 1.21.1 对象的对应表。来源方块类写的是 {@code Material.X}、{@code MaterialColor.X}、
 * {@code SoundType.X}；1.21.1 已没有 Material，其颜色、可燃、可替换与推动反应拆成 Properties 的独立设置。
 */
final class LegacyTables {
    /** 1.18.2 Material 的派生设置。 */
    record MaterialRule(MapColor color, boolean ignitedByLava, boolean replaceable, PushReaction pushReaction) {
    }

    static final Map<String, MaterialRule> MATERIALS = Map.ofEntries(
            Map.entry("WOOD", new MaterialRule(MapColor.WOOD, true, false, PushReaction.NORMAL)),
            Map.entry("PLANT", new MaterialRule(MapColor.PLANT, false, false, PushReaction.DESTROY)),
            Map.entry("STONE", new MaterialRule(MapColor.STONE, false, false, PushReaction.NORMAL)),
            Map.entry("GLASS", new MaterialRule(MapColor.NONE, false, false, PushReaction.NORMAL)),
            Map.entry("METAL", new MaterialRule(MapColor.METAL, false, false, PushReaction.NORMAL)),
            Map.entry("LEAVES", new MaterialRule(MapColor.PLANT, true, false, PushReaction.DESTROY)),
            Map.entry("FIRE", new MaterialRule(MapColor.NONE, false, true, PushReaction.DESTROY)),
            Map.entry("CLAY", new MaterialRule(MapColor.CLAY, false, false, PushReaction.NORMAL)),
            Map.entry("DIRT", new MaterialRule(MapColor.DIRT, false, false, PushReaction.NORMAL)),
            Map.entry("REPLACEABLE_WATER_PLANT", new MaterialRule(MapColor.WATER, false, true, PushReaction.DESTROY)),
            Map.entry("REPLACEABLE_PLANT", new MaterialRule(MapColor.PLANT, false, true, PushReaction.DESTROY)),
            Map.entry("TOP_SNOW", new MaterialRule(MapColor.SNOW, false, true, PushReaction.DESTROY)),
            Map.entry("WOOL", new MaterialRule(MapColor.WOOL, true, false, PushReaction.NORMAL)));

    static final Map<String, MapColor> MAP_COLORS = Map.ofEntries(
            Map.entry("WOOD", MapColor.WOOD), Map.entry("STONE", MapColor.STONE), Map.entry("GRASS", MapColor.GRASS),
            Map.entry("GOLD", MapColor.GOLD), Map.entry("ICE", MapColor.ICE), Map.entry("WOOL", MapColor.WOOL),
            Map.entry("CLAY", MapColor.CLAY), Map.entry("SAND", MapColor.SAND), Map.entry("DIRT", MapColor.DIRT),
            Map.entry("METAL", MapColor.METAL), Map.entry("WATER", MapColor.WATER),
            Map.entry("TERRACOTTA_ORANGE", MapColor.TERRACOTTA_ORANGE), Map.entry("COLOR_RED", MapColor.COLOR_RED),
            Map.entry("COLOR_GREEN", MapColor.COLOR_GREEN), Map.entry("COLOR_GRAY", MapColor.COLOR_GRAY),
            Map.entry("COLOR_BROWN", MapColor.COLOR_BROWN), Map.entry("COLOR_BLACK", MapColor.COLOR_BLACK),
            Map.entry("COLOR_BLUE", MapColor.COLOR_BLUE), Map.entry("COLOR_YELLOW", MapColor.COLOR_YELLOW),
            Map.entry("COLOR_PINK", MapColor.COLOR_PINK), Map.entry("COLOR_PURPLE", MapColor.COLOR_PURPLE),
            Map.entry("COLOR_CYAN", MapColor.COLOR_CYAN));

    static final Map<String, SoundType> SOUND_TYPES = Map.ofEntries(
            Map.entry("WOOD", SoundType.WOOD), Map.entry("GRASS", SoundType.GRASS), Map.entry("STONE", SoundType.STONE),
            Map.entry("GLASS", SoundType.GLASS), Map.entry("METAL", SoundType.METAL),
            Map.entry("AZALEA_LEAVES", SoundType.AZALEA_LEAVES), Map.entry("WOOL", SoundType.WOOL),
            Map.entry("GRAVEL", SoundType.GRAVEL), Map.entry("NETHER_WART", SoundType.NETHER_WART),
            Map.entry("SHROOMLIGHT", SoundType.SHROOMLIGHT), Map.entry("SCAFFOLDING", SoundType.SCAFFOLDING),
            Map.entry("VINE", SoundType.VINE), Map.entry("SLIME_BLOCK", SoundType.SLIME_BLOCK),
            Map.entry("SNOW", SoundType.SNOW));

    private LegacyTables() {
    }

    static SoundType customSound(Map<String, String> events) {
        return new SoundType(1.0F, 1.0F, event(events, "break"), event(events, "step"), event(events, "place"),
                event(events, "hit"), event(events, "fall"));
    }

    private static SoundEvent event(Map<String, String> events, String key) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.parse(events.get(key)));
    }

    /**
     * 1.18.2 的 {@code Tier.getLevel()}：木、金 0，石 1，铁 2，钻石 3，下界合金 4。1.21.1 的 Tier 不再有等级：
     * 本 Mod 的工具用 {@link LegacyTier} 保存来源等级，其他 Tier 按其不能正确采掘的方块标签还原。
     */
    static int toolLevel(Tier tier) {
        if (tier instanceof LegacyTier legacy) {
            return legacy.level();
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) {
            return 4;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) {
            return 3;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_IRON_TOOL) {
            return 2;
        }
        if (tier.getIncorrectBlocksForDrops() == BlockTags.INCORRECT_FOR_STONE_TOOL) {
            return 1;
        }
        return 0;
    }

    /** 按声音类型与方块组合派生的 BlockSetType：1.21.1 的门、活板门与按钮构造器会用它的声音覆盖 Properties 的声音。 */
    static BlockSetType setType(String name, BlockSetType base, SoundType sound, BlockSetType.PressurePlateSensitivity sensitivity) {
        return BlockSetType.register(new BlockSetType(name, base.canOpenByHand(), base.canOpenByWindCharge(),
                base.canButtonBeActivatedByArrows(), sensitivity, sound, base.doorClose(), base.doorOpen(),
                base.trapdoorClose(), base.trapdoorOpen(), base.pressurePlateClickOff(), base.pressurePlateClickOn(),
                base.buttonClickOff(), base.buttonClickOn()));
    }
}
