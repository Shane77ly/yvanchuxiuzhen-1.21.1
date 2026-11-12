package net.yvanchuxiuzhen.system.creature;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.creature.entity.Butterflies;
import net.yvanchuxiuzhen.system.creature.entity.Organisms;
import net.yvanchuxiuzhen.system.creature.entity.NaturalCreatures;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou053BlizzardEffect;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouCreatures;

/**
 * 生物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CreatureSetup {
    private static final ContentTables CONTENT = new ContentTables("creature");

    private CreatureSetup() {
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        CONTENT.effectBehavior("yaoshou_053baofengxve", new Yaoshou053BlizzardEffect());
        YaoshouCreatures.register(CONTENT, modBus);
        Butterflies.register(CONTENT, modBus);
        Organisms.register(CONTENT, modBus);
        NaturalCreatures.register(CONTENT, modBus);
        CONTENT.register(modBus);
    }
}
