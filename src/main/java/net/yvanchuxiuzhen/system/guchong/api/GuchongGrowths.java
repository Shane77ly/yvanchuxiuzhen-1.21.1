package net.yvanchuxiuzhen.system.guchong.api;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;

/** 来源击杀升级后的养蛊经验操作，主人查找与职业写入由所属系统承担。 */
public final class GuchongGrowths {
    private static final TagKey<EntityType<?>> INSECTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchongentity"));

    private GuchongGrowths() { }

    public static void awardOwnerExperience(ServerLevel level, Entity insect, double newLevel) {
        if (!insect.getType().is(INSECTS)) return;
        String ownerId = insect.getPersistentData().getString("belongToMasterUUID");
        if (ownerId.isEmpty()) return;
        Entity owner = Objects.requireNonNull(GuchongBehavior.findEntity(level, ownerId));
        // 来源玩家能力只附着真实玩家；其他实体和假玩家没有职业数据。
        if (owner instanceof ServerPlayer player && !(owner instanceof FakePlayer)) {
            Professions.addExperience(player, Profession.INSECT, newLevel * 10);
        }
    }
}
