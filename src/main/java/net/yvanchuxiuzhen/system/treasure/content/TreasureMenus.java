package net.yvanchuxiuzhen.system.treasure.content;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.network.TreasureToolButtonPayload;

/** 天道宝箱设置页只登记来源的零槽菜单，不绑定打开时的物品快照。 */
public final class TreasureMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<TreasureToolMenu>> TOOL = MENUS.register("tiandaobaoxiang",
            () -> IMenuTypeExtension.create(TreasureToolMenu::new));

    private TreasureMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                TreasureToolButtonPayload.TYPE, TreasureToolButtonPayload.STREAM_CODEC, TreasureToolButtonPayload::handle));
    }

    public static void open(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new TreasureToolMenu(id, inventory, pos),
                Component.translatable("menu.yvanchuxiuzhen.treasure_tool")), buffer -> buffer.writeBlockPos(pos));
    }
}
