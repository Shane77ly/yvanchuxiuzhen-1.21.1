package net.yvanchuxiuzhen.system.quest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.quest.content.BountyBoardBlock;
import net.yvanchuxiuzhen.system.quest.content.BountyBlockEntities;
import net.yvanchuxiuzhen.system.quest.content.DisguisedQuestBlock;
import net.yvanchuxiuzhen.system.quest.content.QuestBlockEntities;
import net.yvanchuxiuzhen.system.quest.data.BankDialogueData;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenus;
import net.yvanchuxiuzhen.system.quest.content.QuestTriggerEditorMenus;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import net.yvanchuxiuzhen.system.quest.network.OpenQuestJournalPayload;

/**
 * 任务系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class QuestSetup {
    private static final ContentTables CONTENT = new ContentTables("quest")
            .blockFactory("quest:bounty", traits -> ContentBlock.create(traits, BountyBoardBlock::new))
            .blockFactory("quest:disguised", traits -> ContentBlock.create(traits, DisguisedQuestBlock::new));

    private QuestSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        BankDialogueData.register(modBus);
        QuestData.register(modBus);
        modBus.addListener(OpenQuestJournalPayload::register);
        DialogueMenus.register(modBus);
        QuestTriggerEditorMenus.register(modBus);
        BountyBlockEntities.register(modBus);
        QuestBlockEntities.register(modBus);
        NeoForge.EVENT_BUS.addListener(DisguisedQuestRuntime::structureReleased);
        NeoForge.EVENT_BUS.addListener(QuestBlockRefresh::rightClick);
        NeoForge.EVENT_BUS.addListener(BountyRuntime::death);
        NeoForge.EVENT_BUS.addListener(DialogueRuntime::request);
        NeoForge.EVENT_BUS.addListener(DialogueRuntime::requestNpc);
        NeoForge.EVENT_BUS.addListener(QiankunDialogue::request);
        NeoForge.EVENT_BUS.addListener(DialogueRuntime::tick);
        NeoForge.EVENT_BUS.addListener(DialogueRuntime::login);
        NeoForge.EVENT_BUS.addListener(QuestChatCommand::register);
        NeoForge.EVENT_BUS.addListener(BankDialogue::begin);
        NeoForge.EVENT_BUS.addListener(BankDialogue::tick);
        NeoForge.EVENT_BUS.addListener(BankDialogue::login);
    }
}
