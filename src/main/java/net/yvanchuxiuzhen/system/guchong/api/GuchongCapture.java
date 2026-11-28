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
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 摄虫网的服务端收纳操作；蛊虫字段和物品组件只由蛊虫系统写入。 */
public final class GuchongCapture {
    private GuchongCapture() { }

    public enum Result { CAPTURED, OWNED, NO_ITEM }

    /** 按来源先拒绝已认主目标；物品映射成功后，即使主人不在当前世界也移除目标。 */
    public static Result captureWild(ServerLevel level, Entity insect, String ownerUuid) {
        if (insect.getData(GuchongData.RUNTIME).tamed()) return Result.OWNED;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(insect.getType()).toString();
        ItemStack captured = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse((id + "item").toLowerCase(Locale.ENGLISH))));
        if (captured.isEmpty()) return Result.NO_ITEM;
        CompoundTag data = new CompoundTag();
        data.putString("guchongid", id);
        EntityStats stats = EntityAttributes.get(insect);
        GuchongJarData.writeStatsToTag(stats, data, "");
        data.putString("team", Relations.entityTeam(insect));
        data.putDouble("health_Num", insect instanceof LivingEntity living ? living.getHealth() : -1);
        data.putBoolean("guchongxvnfu", false);
        GuchongJarData.write(captured, data);
        captured.set(DataComponents.CUSTOM_NAME, Component.literal(insect.getDisplayName().getString().replace("[", "").replace("]", "")));
        if (GuchongBehavior.findEntity(level, ownerUuid) instanceof Player owner) ItemHandlerHelper.giveItemToPlayer(owner, captured);
        insect.discard();
        return Result.CAPTURED;
    }
}
