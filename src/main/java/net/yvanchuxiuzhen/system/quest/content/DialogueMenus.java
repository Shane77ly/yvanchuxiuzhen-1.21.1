package net.yvanchuxiuzhen.system.quest.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源无物品槽的文字页和四选项页。 */
public final class DialogueMenus {
    private static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BountyBoardMenu>> BOUNTY = TYPES.register("xvanshangling_gui", () -> IMenuTypeExtension.create(BountyBoardMenu::new));
    public static final Supplier<MenuType<DialogueMenu>> TEXT = TYPES.register("duihua_gui_01", () -> IMenuTypeExtension.create(
            (id, inventory, buffer) -> new DialogueMenu(id, inventory, buffer.readBlockPos(), false)));
    public static final Supplier<MenuType<DialogueMenu>> CHOICES = TYPES.register("duihua_gui_02", () -> IMenuTypeExtension.create(
            (id, inventory, buffer) -> new DialogueMenu(id, inventory, buffer.readBlockPos(), true)));
    public static final Supplier<MenuType<QuestJournalMenu>> JOURNAL = TYPES.register("renwu_gui", () -> IMenuTypeExtension.create(
            (id, inventory, buffer) -> new QuestJournalMenu(id, inventory, buffer.readBlockPos())));

    private DialogueMenus() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
