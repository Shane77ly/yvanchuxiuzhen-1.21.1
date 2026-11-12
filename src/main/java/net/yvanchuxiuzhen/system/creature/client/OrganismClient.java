package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Organism09Entity;
import net.yvanchuxiuzhen.system.creature.entity.Organism10Entity;
import net.yvanchuxiuzhen.system.creature.entity.Organism11Entity;
import net.yvanchuxiuzhen.system.creature.entity.Organism12Entity;
import net.yvanchuxiuzhen.system.creature.entity.Organisms;

/** Organism09–12的GeckoLib模型、动画、纹理、阴影及缩放装配。 */
public final class OrganismClient {
    private OrganismClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : Organisms.models()) {
                switch (row.id()) {
                    case "organism_09" -> event.registerEntityRenderer(CreatureSetup.<Organism09Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "organism_10" -> event.registerEntityRenderer(CreatureSetup.<Organism10Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "organism_11" -> event.registerEntityRenderer(CreatureSetup.<Organism11Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "organism_12" -> event.registerEntityRenderer(CreatureSetup.<Organism12Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    default -> throw new IllegalStateException("未知环境动物身份：" + row.id());
                }
            }
        });
    }
}
