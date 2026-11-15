package net.yvanchuxiuzhen.collection.tiandao;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.Unbreakable;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;

/** 以来源类型顺序编辑物品根标签；原生字段按目标组件提交，其余根键保留实际NBT类型。 */
public final class NbtEditorRuntime {
    private static final String[] TYPES = {"aaa-", "bbb-", "ccc-", "ddd-", "eee-", "fff-", "ggg-", "hhh-"};

    private NbtEditorRuntime() {
    }

    public static void apply(ServerPlayer player, Slot slot, String state) {
        String[] entries = state.split(",");
        String name = value(entries, "nbtname:");
        String content = value(entries, "nbtcontent:");
        if (name.isEmpty() || content.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.nbt_editor.missing_input"), false);
            return;
        }
        int type = -1;
        for (int index = 0; index < TYPES.length; index++) {
            if (value(entries, TYPES[index]).equals("1")) {
                type = index;
                break;
            }
        }
        if (type < 0) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.nbt_editor.choose_type"), false);
            return;
        }
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return;
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        boolean written = true;
        if (type == 0) {
            if (content.equals("true")) data.putBoolean(name, true);
            else if (content.equals("false")) data.putBoolean(name, false);
            else written = false;
        } else if (type == 1) {
            data.putString(name, content);
        } else {
            double number = number(content);
            switch (type) {
                case 2 -> data.putByte(name, (byte) number);
                case 3 -> data.putDouble(name, number);
                case 4 -> data.putFloat(name, (float) number);
                case 5 -> data.putInt(name, (int) number);
                case 6 -> data.putLong(name, (long) number);
                case 7 -> data.putShort(name, (short) number);
                default -> throw new IllegalStateException("未知NBT编辑类型 " + type);
            }
        }
        if (written) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            nativeField(stack, name, data);
            Equipment.updateRootField(stack, name, data.get(name));
        }
        stack.setCount(1);
        slot.set(stack);
    }

    private static void nativeField(ItemStack stack, String name, CompoundTag data) {
        switch (name) {
            case "Damage" -> stack.set(DataComponents.DAMAGE, Math.max(0, data.getInt(name)));
            case "CustomModelData" -> stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(data.getInt(name)));
            case "Unbreakable" -> {
                if (data.getBoolean(name)) stack.set(DataComponents.UNBREAKABLE, new Unbreakable((data.getInt("HideFlags") & 4) == 0));
                else stack.remove(DataComponents.UNBREAKABLE);
            }
            default -> { }
        }
    }

    /** 来源数值入口先按double解析，非法输入实际写0，各整数类型随后执行Java强制转换。 */
    private static double number(String content) {
        try {
            return Double.parseDouble(content.trim());
        } catch (NumberFormatException error) {
            return 0;
        }
    }

    /** 来源使用包含匹配并去掉全部前缀；未命中时读取第一段，保留逗号及前缀碰撞语义。 */
    private static String value(String[] entries, String prefix) {
        if (entries.length == 0) return "";
        for (String entry : entries) if (entry.contains(prefix)) return entry.replace(prefix, "");
        return entries[0].replace(prefix, "");
    }
}
