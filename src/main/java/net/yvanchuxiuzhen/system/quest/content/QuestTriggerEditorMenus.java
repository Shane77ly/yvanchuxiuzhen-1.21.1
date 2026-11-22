package net.yvanchuxiuzhen.system.quest.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.quest.QuestTriggerEditorRuntime;
import net.yvanchuxiuzhen.system.quest.network.QuestTriggerEditorPayload;

/** 来源任务方块编辑菜单的独立注册身份。 */
public final class QuestTriggerEditorMenus {
    private static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<QuestTriggerEditorMenu>> EDITOR = TYPES.register("renwufangkuai_gui",
            () -> IMenuTypeExtension.create(QuestTriggerEditorMenu::new));

    private QuestTriggerEditorMenus() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        modBus.addListener(QuestTriggerEditorPayload::register);
        NeoForge.EVENT_BUS.addListener(QuestTriggerEditorRuntime::rightClick);
    }
}
