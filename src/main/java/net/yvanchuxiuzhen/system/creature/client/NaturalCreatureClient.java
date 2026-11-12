package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Moshou001Entity;
import net.yvanchuxiuzhen.system.creature.entity.Moshou003Entity;
import net.yvanchuxiuzhen.system.creature.entity.NaturalCreatures;
import net.yvanchuxiuzhen.system.creature.entity.TaomujingEntity;
import net.yvanchuxiuzhen.system.creature.entity.xiuzhe.Xiuzhe0101Entity;

/** 魔兽、桃木精与修者的来源模型和渲染装配。 */
public final class NaturalCreatureClient {
    private NaturalCreatureClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : NaturalCreatures.models()) {
                switch (row.id()) {
                    case "moshou_001" -> event.registerEntityRenderer(CreatureSetup.<Moshou001Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "moshou_003" -> event.registerEntityRenderer(CreatureSetup.<Moshou003Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "taomujing" -> event.registerEntityRenderer(CreatureSetup.<TaomujingEntity>entityType(row.id()).get(),
                            context -> new TaomujingRenderer(context, row));
                    case "xiuzhe_0101" -> event.registerEntityRenderer(CreatureSetup.<Xiuzhe0101Entity>entityType(row.id()).get(),
                            context -> new Xiuzhe0101Renderer(context, row));
                    default -> throw new IllegalStateException("未知自然生物身份：" + row.id());
                }
            }
        });
    }
}
