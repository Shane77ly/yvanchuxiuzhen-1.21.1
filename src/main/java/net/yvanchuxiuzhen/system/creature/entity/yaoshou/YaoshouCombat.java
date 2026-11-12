package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.HeavyHitEvent;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 妖兽对来源重击事件的响应。 */
public final class YaoshouCombat {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private YaoshouCombat() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(YaoshouCombat::onHeavyHit);
    }

    private static void onHeavyHit(HeavyHitEvent event) {
        if (!event.victim().getType().is(YAOSHOU)) return;
        if (!(event.victim() instanceof YaoshouEntity entity)) return;
        if (entity.getPersistentData().getString("jineng").isEmpty()) return;
        entity.getPersistentData().putBoolean("beidaduan", true);
        YaoshouShared.finishSkill(entity);
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type("daduan"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(), 1, 0, 0, 0, 0);
        }
    }
}
