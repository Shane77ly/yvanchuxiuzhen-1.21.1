package net.yvanchuxiuzhen.system.quest.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenus;
import net.yvanchuxiuzhen.system.quest.content.BountyBlockEntities;
import net.yvanchuxiuzhen.system.quest.content.QuestBlockEntities;

/** 对话客户端装配独立于专服加载。 */
public final class QuestClientSetup {
    private QuestClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuestClientSetup::screens);
        modBus.addListener(QuestClientSetup::renderers);
    }

    private static void screens(RegisterMenuScreensEvent event) {
        event.register(DialogueMenus.TEXT.get(), DialogueScreen::new);
        event.register(DialogueMenus.CHOICES.get(), DialogueScreen::new);
        event.register(DialogueMenus.BOUNTY.get(), BountyBoardScreen::new);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BountyBlockEntities.BOARD.get(), BountyBoardRenderer::new);
        event.registerBlockEntityRenderer(QuestBlockEntities.DISGUISED.get(), DisguisedQuestRenderer::new);
    }
}
