package net.yvanchuxiuzhen.system.guchong.content;

import java.text.DecimalFormat;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.guchong.api.GuchongState;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;

/** 蛊虫物品、蛊罐编号槽和实体之间的来源字段搬运。 */
public final class GuchongJarData {
    private static final String[] TEXT = {"guchongid", "LEVELname", "guchongname", "belongToMasterUUID", "team",
            "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL", "power_Rec_LEVEL", "soul_Max_LEVEL", "soul_Rec_LEVEL",
            "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL", "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL"};
    private static final String[] NUMBERS = {"LEVELnumber", "experience_Max", "experience", "health_Max", "health_Num", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"};

    private GuchongJarData() { }

    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(GuchongData.STATE.get(), GuchongState.EMPTY).toTag();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        GuchongState state = GuchongState.fromTag(data);
        if (state.isEmpty()) stack.remove(GuchongData.STATE.get());
        else stack.set(GuchongData.STATE.get(), state);
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

    /** 依来源先恢复等级及经验；物品身份、名称和当前生命不进入实体数据。 */
    public static void copyEntityProgress(CompoundTag from, String suffix, Entity insect, boolean integers) {
        CompoundTag data = insect.getPersistentData();
        data.putString("LEVELname", from.getString("LEVELname" + suffix));
        for (String key : new String[]{"LEVELnumber", "experience_Max", "experience"}) {
            copyEntityNumber(from, suffix, data, key, integers);
        }
    }

    /** 来源按字段顺序恢复资质，并在写生命、攻击和速度后立即更新原生属性。 */
    public static void restoreEntityAttributes(CompoundTag from, String suffix, Entity insect, boolean integers) {
        CompoundTag data = insect.getPersistentData();
        String[] keys = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
                "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"};
        for (String key : keys) {
            data.putString(key + "_LEVEL", from.getString(key + "_LEVEL" + suffix));
            copyEntityNumber(from, suffix, data, key, integers);
            if (key.equals("health_Max")) {
                LivingEntity living = (LivingEntity) insect;
                living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble(key));
                living.setHealth(integers ? from.getInt("health_Num" + suffix) : (float) from.getDouble("health_Num" + suffix));
            } else if (key.equals("attack_Num")) {
                ((LivingEntity) insect).getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble(key));
            }
        }
        data.putString("speed_LEVEL", from.getString("speed_LEVEL" + suffix));
        data.putDouble("speed_number", from.getDouble("speed_number" + suffix));
        ((LivingEntity) insect).getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.getDouble("speed_number"));
    }

    private static void copyEntityNumber(CompoundTag from, String suffix, CompoundTag to, String key, boolean integers) {
        if (integers) to.putInt(key, from.getInt(key + suffix));
        else to.putDouble(key, from.getDouble(key + suffix));
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
        // 来源只把槽位名称设置为显示名，不写入重建物品的自定义数据。
        data.remove("guchongname");
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
        Tag previousId = jar.get("guchongid" + suffix);
        copy(data, "", jar, suffix, integers);
        if (!integers && data.getString("guchongid").isEmpty()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(insect.getItem());
            String path = itemId.getPath();
            if (itemId.getNamespace().equals("yvanchuxiuzhen")
                    && (path.equals("guchong_01item") || path.equals("guchong_03item") || path.equals("guchong_04item"))) {
                jar.putString("guchongid" + suffix, "yvanchuxiuzhen:" + path.substring(0, path.length() - 4));
            } else if (previousId == null) {
                jar.remove("guchongid" + suffix);
            } else {
                jar.put("guchongid" + suffix, previousId);
            }
        }
        jar.putString("guchongname" + suffix, insect.getDisplayName().getString());
    }

    public static void capture(Entity insect, CompoundTag jar, String team) {
        String suffix = new DecimalFormat("0").format(insect.getPersistentData().getDouble("guguanweizhi"));
        copy(insect.getPersistentData(), "", jar, suffix, false);
        jar.putString("guchongid" + suffix, BuiltInRegistries.ENTITY_TYPE.getKey(insect.getType()).toString());
        jar.putString("guchongname" + suffix, insect.getDisplayName().getString().replace("[", "").replace("]", ""));
        jar.putBoolean("guchongxvnfu" + suffix, true);
        jar.putString("team" + suffix, team);
        jar.putDouble("health_Num" + suffix, insect instanceof LivingEntity living ? living.getHealth() : -1);
        jar.putDouble("shifangnum", jar.getDouble("shifangnum") - 1);
    }
}
