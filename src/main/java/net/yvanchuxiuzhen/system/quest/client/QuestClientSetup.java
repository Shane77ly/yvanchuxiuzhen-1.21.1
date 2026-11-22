package net.yvanchuxiuzhen.system.quest.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenus;
import net.yvanchuxiuzhen.system.quest.content.BountyBlockEntities;
import net.yvanchuxiuzhen.system.quest.content.QuestBlockEntities;
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.quest.network.OpenQuestJournalPayload;
import org.lwjgl.glfw.GLFW;

/** 对话客户端装配独立于专服加载。 */
public final class QuestClientSetup {
    private static final KeyMapping JOURNAL = new KeyMapping("key.yvanchuxiuzhen.renwuanjian", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "key.categories.misc") {
        @Override
        public void setDown(boolean value) {
            boolean pressed = value && !isDown();
            super.setDown(value);
            Minecraft minecraft = Minecraft.getInstance();
            if (pressed && minecraft.player != null && minecraft.screen == null) PacketDistributor.sendToServer(new OpenQuestJournalPayload());
        }
    };

    private QuestClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(QuestClientSetup::screens);
        modBus.addListener(QuestClientSetup::renderers);
        modBus.addListener((RegisterKeyMappingsEvent event) -> event.register(JOURNAL));
    }

    private static void screens(RegisterMenuScreensEvent event) {
        event.register(DialogueMenus.TEXT.get(), DialogueScreen::new);
        event.register(DialogueMenus.CHOICES.get(), DialogueScreen::new);
        event.register(DialogueMenus.BOUNTY.get(), BountyBoardScreen::new);
        event.register(DialogueMenus.JOURNAL.get(), QuestJournalScreen::new);
        event.register(QuestTriggerEditorMenus.EDITOR.get(), QuestTriggerEditorScreen::new);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BountyBlockEntities.BOARD.get(), BountyBoardRenderer::new);
        event.registerBlockEntityRenderer(QuestBlockEntities.DISGUISED.get(), DisguisedQuestRenderer::new);
    }
}
