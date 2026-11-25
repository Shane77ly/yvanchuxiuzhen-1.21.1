package net.yvanchuxiuzhen.system.dungeon.content;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.dungeon.network.DungeonActionPayload;

/** 四个来源菜单保持独立注册身份，打开时只发送当前方块的展示快照。 */
public final class DungeonMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<DungeonMenu>> OPEN = register("fubenopen");
    public static final Supplier<MenuType<DungeonMenu>> MANAGE = register("tiandaofuben_gui");
    public static final Supplier<MenuType<DungeonMenu>> CONTROLLER = register("fubenblock_gui");
    public static final Supplier<MenuType<DungeonMenu>> MARKER = register("fubenentity_gui");

    private DungeonMenus() {
    }

    private static Supplier<MenuType<DungeonMenu>> register(String id) {
        return MENUS.register(id, () -> IMenuTypeExtension.create(DungeonMenu::new));
    }

    public static MenuType<DungeonMenu> type(int kind) { return List.of(OPEN, MANAGE, CONTROLLER, MARKER).get(kind).get(); }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                DungeonActionPayload.TYPE, DungeonActionPayload.STREAM_CODEC, DungeonActionPayload::handle));
    }

    public static void open(ServerPlayer player, DungeonBlockEntity block, int kind) {
        block.changed();
        player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new DungeonMenu(id, inventory, block.getBlockPos(), kind),
                Component.translatable("menu.yvanchuxiuzhen.dungeon." + kind)), buffer -> {
                    buffer.writeBlockPos(block.getBlockPos());
                    buffer.writeVarInt(kind);
                    buffer.writeNbt(block.state());
                });
    }
}
