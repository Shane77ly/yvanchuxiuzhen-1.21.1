package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.BossCreatures;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss01Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss02Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss0201Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss04Entity;

/** 妖兽首领与斧刃子实体的来源模型渲染装配。 */
public final class BossCreatureClient {
    private BossCreatureClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : BossCreatures.models()) {
                switch (row.id()) {
                    case "ycxzboss_01" -> event.registerEntityRenderer(CreatureSetup.<Boss01Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "ycxzboss_02" -> event.registerEntityRenderer(CreatureSetup.<Boss02Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "ycxzboss_0201" -> event.registerEntityRenderer(CreatureSetup.<Boss0201Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "ycxzboss_04" -> event.registerEntityRenderer(CreatureSetup.<Boss04Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    default -> throw new IllegalStateException("未知首领身份：" + row.id());
                }
            }
        });
    }
}
