package net.yvanchuxiuzhen.system.dungeon;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;

/** 来源管理串只应用非空字段；简介按十六个UTF-16字符拆分，未选攻击复选框不清旧值。 */
public final class DungeonSettings {
    public static final List<String> FIELDS = List.of("fubenname", "fubenid", "fubenjieshao", "fubenjiangli", "fubenshujv",
            "fubenminlevel", "fubenmaxlevel", "fubenpeoplemax", "fubenyvlan", "fubenjieshujishiMax");
    private static final DecimalFormat TWO = new DecimalFormat("00");

    private DungeonSettings() {
    }

    public static void entrance(DungeonBlockEntity block, String settings) {
        String[] fields = settings.split(",");
        CompoundTag data = block.getPersistentData();
        for (int field = 0; field < FIELDS.size(); field++) {
            String key = FIELDS.get(field);
            int selected = 0;
            for (int index = 0; index < fields.length; index++) {
                if (fields[index].contains(key + ":")) { selected = index; break; }
            }
            String value = fields[selected].replace(key + ":", "");
            if (value.isEmpty()) continue;
            if (field < 5) data.putString(key, value);
            else data.putDouble(key, DungeonRewards.number(value));
            if (key.equals("fubenjieshao")) {
                for (int line = 1; line <= 4; line++) data.putString("text" + TWO.format(line), "");
                int lines = (int) Math.ceil(value.length() / 16.0);
                for (int line = 0; line < lines; line++) data.putString("text" + TWO.format(line + 1), value.substring(line * 16, Math.min(value.length(), (line + 1) * 16)));
            }
        }
        block.changed();
    }

    public static void controller(DungeonBlockEntity block, String side) {
        DungeonDefinitions.configure(block, DungeonRewards.number(side));
    }

    public static void marker(DungeonBlockEntity block, String id, boolean attack) {
        CompoundTag data = block.getPersistentData();
        data.putString("zhanweifuid", id);
        Direction direction = block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        data.putInt("entityshengchengchaoxiang", switch (direction) {
            case NORTH -> -90;
            case SOUTH -> 90;
            case WEST -> 180;
            default -> 0;
        });
        if (attack) data.putBoolean("entityshengchenggongji", true);
        block.changed();
    }
}
