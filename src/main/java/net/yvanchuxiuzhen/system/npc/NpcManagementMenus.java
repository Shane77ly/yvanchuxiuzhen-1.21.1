package net.yvanchuxiuzhen.system.npc;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.npc.network.NpcManagementPayload;

/** 天道NPC设置菜单与设置提交载荷。 */
public final class NpcManagementMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<NpcManagementMenu>> MENU = MENUS.register("tiandao_np_cset",
            () -> IMenuTypeExtension.create(NpcManagementMenu::new));

    private NpcManagementMenus() { }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(NpcManagementPayload.TYPE, NpcManagementPayload.STREAM_CODEC, NpcManagementPayload::handle);
    }
}
