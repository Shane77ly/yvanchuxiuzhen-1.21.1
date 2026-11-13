package net.yvanchuxiuzhen.system.creature;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouShared;
import net.yvanchuxiuzhen.system.guchong.api.GuchongUpgradeEvent;
import net.yvanchuxiuzhen.system.guchong.api.GuchongVictimDataEvent;

/** 蛊虫击杀成长所需的生物等级读取与来源死者属性重算消费者。 */
public final class GuchongGrowthConsumer {
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private GuchongGrowthConsumer() { }

    public static void onVictimData(GuchongVictimDataEvent event) {
        CompoundTag data = event.victim().getPersistentData();
        event.respond(data.getDouble("LEVELnumber"), data.getDouble("additional_experience"));
    }

    public static void onUpgrade(GuchongUpgradeEvent event) {
        // 来源按死者的妖兽标签选择分支，命中后不重算升级蛊虫。
        if (event.victim().getType().is(BEASTS)) {
            event.selectVictimRecalculation();
            if (event.victim() instanceof Mob mob) {
                CompoundTag data = mob.getPersistentData();
                YaoshouShared.calculateAttributes(mob, data, data.getDouble("LEVELnumber"));
            }
        }
    }
}
