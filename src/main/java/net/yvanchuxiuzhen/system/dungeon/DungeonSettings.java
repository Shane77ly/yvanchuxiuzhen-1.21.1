package net.yvanchuxiuzhen.system.dungeon;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;

/** 管理页面只应用非空字段；简介按十六个UTF-16字符拆分，未选攻击复选框不清旧值。 */
public final class DungeonSettings {
    public static final List<String> FIELDS = List.of("fubenname", "fubenid", "fubenjieshao", "fubenjiangli", "fubenshujv",
            "fubenminlevel", "fubenmaxlevel", "fubenpeoplemax", "fubenyvlan", "fubenjieshujishiMax");

    private DungeonSettings() {
    }

    public static void entrance(DungeonBlockEntity block, List<String> values) {
        for (int index = 0; index < Math.min(FIELDS.size(), values.size()); index++) {
            String value = values.get(index);
            if (value.isEmpty()) continue;
            switch (index) {
                case 0 -> block.setDungeonName(value);
                case 1 -> block.setTemplateId(value);
                case 2 -> {
                    block.setDescription(value);
                    for (int line = 1; line <= 4; line++) block.setTextLine(line, "");
                    int lines = (int) Math.ceil(value.length() / 16.0);
                    for (int line = 0; line < lines && line < 4; line++) {
                        block.setTextLine(line + 1, value.substring(line * 16, Math.min(value.length(), (line + 1) * 16)));
                    }
                }
                case 3 -> block.setRewardSummary(value);
                case 4 -> block.setDefinitionId(value);
                case 5 -> block.setMinLevel(number(value));
                case 6 -> block.setMaxLevel(number(value));
                case 7 -> block.setMaxPeople(number(value));
                case 8 -> block.setPreview(number(value));
                case 9 -> block.setCooldownMax(number(value));
                default -> { }
            }
        }
        block.changed();
    }

    public static void controller(DungeonBlockEntity block, List<String> values) {
        String side = values.isEmpty() ? "" : values.getFirst();
        DungeonDefinitions.configure(block, number(side));
    }

    public static void marker(DungeonBlockEntity block, List<String> values, boolean attack) {
        String id = values.isEmpty() ? "" : values.getFirst();
        block.setMarkerId(id);
        Direction direction = block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        block.setMarkerFacing(switch (direction) {
            case NORTH -> -90;
            case SOUTH -> 90;
            case WEST -> 180;
            default -> 0;
        });
        if (attack) block.setMarkerAttack(true);
        block.changed();
    }

    private static double number(String value) {
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException exception) { return 0; }
    }
}
