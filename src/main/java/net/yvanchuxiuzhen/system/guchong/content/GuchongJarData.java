package net.yvanchuxiuzhen.system.guchong.content;

import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.LivingEntity;

/** 蛊虫物品、蛊罐编号槽和实体之间的来源字段搬运。 */
public final class GuchongJarData {
    private static final String[] TEXT = {"guchongid", "LEVELname", "guchongname", "belongToMasterUUID", "team",
            "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL", "soul_Max_LEVEL", "soul_Rec_LEVEL",
            "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL", "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL"};
    private static final String[] NUMBERS = {"LEVELnumber", "experience_Max", "experience", "health_Max", "health_Num", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"};

    private GuchongJarData() { }

    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static void copy(CompoundTag from, String sourceSuffix, CompoundTag to, String suffix, boolean integers) {
        for (String key : TEXT) to.putString(key + suffix, from.getString(key + sourceSuffix));
        for (String key : NUMBERS) {
            if (integers) to.putInt(key + suffix, from.getInt(key + sourceSuffix));
            else to.putDouble(key + suffix, from.getDouble(key + sourceSuffix));
        }
        to.putDouble("speed_number" + suffix, from.getDouble("speed_number" + sourceSuffix));
        to.putBoolean("guchongxvnfu" + suffix, from.getBoolean("guchongxvnfu" + sourceSuffix));
    }

    public static void clear(CompoundTag data, String suffix) {
        for (String key : TEXT) data.putString(key + suffix, "");
        for (String key : NUMBERS) data.putDouble(key + suffix, 0);
        data.putDouble("speed_number" + suffix, 0);
        data.putBoolean("guchongxvnfu" + suffix, false);
    }

    public static ItemStack unpack(CompoundTag jar, int slot, boolean menu) {
        String suffix = Integer.toString(slot);
        String insectId = jar.getString("guchongid" + suffix);
        if (insectId.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse((insectId + "item").toLowerCase(Locale.ENGLISH))));
        CompoundTag data = new CompoundTag();
        copy(jar, suffix, data, "", false);
        if (menu) {
            data.remove("health_Num");
            data.putString("guguanuuid", jar.getString("guguanuuid" + suffix));
        }
        write(stack, data);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(jar.getString("guchongname" + suffix).replace("[", "").replace("]", "")));
        return stack;
    }

    public static void pack(ItemStack insect, CompoundTag jar, int slot, boolean integers) {
        String suffix = Integer.toString(slot);
        if (insect.isEmpty()) {
            clear(jar, suffix);
            return;
        }
        CompoundTag data = read(insect);
        copy(data, "", jar, suffix, integers);
        if (!integers && data.getString("guchongid").isEmpty()) {
            String path = BuiltInRegistries.ITEM.getKey(insect.getItem()).getPath();
            if (path.equals("guchong_01item") || path.equals("guchong_03item") || path.equals("guchong_04item")) {
                jar.putString("guchongid" + suffix, "yvanchuxiuzhen:" + path.substring(0, path.length() - 4));
            }
        }
        jar.putString("guchongname" + suffix, insect.getDisplayName().getString());
    }

    public static void capture(LivingEntity insect, CompoundTag jar, String team) {
        String suffix = new java.text.DecimalFormat("0").format(insect.getPersistentData().getDouble("guguanweizhi"));
        copy(insect.getPersistentData(), "", jar, suffix, false);
        jar.putString("guchongid" + suffix, BuiltInRegistries.ENTITY_TYPE.getKey(insect.getType()).toString());
        jar.putString("guchongname" + suffix, insect.getDisplayName().getString().replace("[", "").replace("]", ""));
        jar.putBoolean("guchongxvnfu" + suffix, true);
        jar.putString("team" + suffix, team);
        jar.putDouble("health_Num" + suffix, insect.getHealth());
        jar.putDouble("shifangnum", jar.getDouble("shifangnum") - 1);
    }
}
