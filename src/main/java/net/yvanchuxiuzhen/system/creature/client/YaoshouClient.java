package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouCreatures;

/** 编号妖兽的GeckoLib模型、动画、纹理及阴影装配。 */
public final class YaoshouClient {
    private YaoshouClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : YaoshouCreatures.models()) {
                event.registerEntityRenderer(CreatureSetup.<YaoshouEntity>entityType(row.id()).get(),
                        context -> new YaoshouRenderer<>(context, row));
            }
        });
    }
}
