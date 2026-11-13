package net.yvanchuxiuzhen.system.guchong.api;

import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;

/** 摄虫网的服务端收纳操作；蛊虫字段和物品组件只由蛊虫系统写入。 */
public final class GuchongCapture {
    private static final String[] TEXT = {"LEVELname", "team", "health_Max_LEVEL", "health_Rec_LEVEL", "power_Max_LEVEL",
            "power_Rec_LEVEL", "soul_Max_LEVEL", "soul_Rec_LEVEL", "attack_Num_LEVEL", "attack_Def_LEVEL", "mana_Att_LEVEL",
            "mana_Def_LEVEL", "soul_Att_LEVEL", "soul_Def_LEVEL", "speed_LEVEL"};
    private static final String[] NUMBERS = {"LEVELnumber", "experience_Max", "experience", "health_Max", "health_Rec",
            "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def",
            "soul_Att", "soul_Def", "speed_number"};

    private GuchongCapture() { }

    public enum Result { CAPTURED, OWNED, NO_ITEM }

    /** 按来源先拒绝已认主目标；物品映射成功后，即使主人不在当前世界也移除目标。 */
    public static Result captureWild(ServerLevel level, Entity insect, String ownerUuid) {
        CompoundTag source = insect.getPersistentData();
        if (source.getBoolean("guchongxvnfu")) return Result.OWNED;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(insect.getType()).toString();
        ItemStack captured = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse((id + "item").toLowerCase(Locale.ENGLISH))));
        if (captured.isEmpty()) return Result.NO_ITEM;
        CompoundTag data = new CompoundTag();
        data.putString("guchongid", id);
        for (String key : TEXT) data.putString(key, source.getString(key));
        for (String key : NUMBERS) data.putDouble(key, source.getDouble(key));
        data.putDouble("health_Num", insect instanceof LivingEntity living ? living.getHealth() : -1);
        data.putBoolean("guchongxvnfu", false);
        GuchongJarData.write(captured, data);
        captured.set(DataComponents.CUSTOM_NAME, Component.literal(insect.getDisplayName().getString().replace("[", "").replace("]", "")));
        if (GuchongBehavior.findEntity(level, ownerUuid) instanceof Player owner) ItemHandlerHelper.giveItemToPlayer(owner, captured);
        insect.discard();
        return Result.CAPTURED;
    }
}
