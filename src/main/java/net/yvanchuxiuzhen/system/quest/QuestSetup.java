package net.yvanchuxiuzhen.system.quest;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.quest.data.BankDialogueData;

/**
 * 任务系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class QuestSetup {
    private static final ContentTables CONTENT = new ContentTables("quest");

    private QuestSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        BankDialogueData.register(modBus);
        NeoForge.EVENT_BUS.addListener(BankDialogue::begin);
        NeoForge.EVENT_BUS.addListener(BankDialogue::tick);
        NeoForge.EVENT_BUS.addListener(BankDialogue::login);
    }
}
