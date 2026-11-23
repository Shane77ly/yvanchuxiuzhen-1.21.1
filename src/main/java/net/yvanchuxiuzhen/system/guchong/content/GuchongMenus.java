package net.yvanchuxiuzhen.system.guchong.content;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
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
import net.yvanchuxiuzhen.system.guchong.network.GuchongReleaseButtonPayload;

/** 三档手持蛊罐与蛊笛释放菜单保持来源注册标识。 */
public final class GuchongMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<Integer, Supplier<MenuType<TaoguanMenu>>> TYPES = new HashMap<>();
    public static final Supplier<MenuType<GuchongReleaseMenu>> RELEASE = MENUS.register("guchongshifang_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new GuchongReleaseMenu(id, inventory, buffer.readBlockPos())));

    static {
        for (int number = 1; number <= 3; number++) {
            int grade = number;
            TYPES.put(grade, MENUS.register("taoguan_0" + grade + "_gui",
                    () -> IMenuTypeExtension.create((id, inventory, buffer) -> new TaoguanMenu(id, inventory, grade, buffer.readBlockPos()))));
        }
    }

    private GuchongMenus() { }
    public static void register(IEventBus bus) {
        MENUS.register(bus);
        bus.addListener(GuchongMenus::registerPayloads);
    }
    public static MenuType<TaoguanMenu> type(int grade) { return TYPES.get(grade).get(); }

    public static void openRelease(ServerPlayer player) {
        BlockPos position = player.blockPosition();
        player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new GuchongReleaseMenu(id, inventory, position),
                Component.translatable("menu.yvanchuxiuzhen.guchong_release")), buffer -> buffer.writeBlockPos(position));
        if (player.containerMenu instanceof GuchongReleaseMenu menu) menu.populate(player);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(GuchongReleaseButtonPayload.TYPE, GuchongReleaseButtonPayload.STREAM_CODEC,
                GuchongReleaseButtonPayload::handle);
    }
}
