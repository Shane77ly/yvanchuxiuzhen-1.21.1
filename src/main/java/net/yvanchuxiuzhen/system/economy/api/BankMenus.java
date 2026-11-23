package net.yvanchuxiuzhen.system.economy.api;

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
import net.yvanchuxiuzhen.system.economy.content.BankLoginMenu;
import net.yvanchuxiuzhen.system.economy.content.BankMenu;
import net.yvanchuxiuzhen.system.economy.network.BankButtonPayload;

/** 银行公开打开入口与菜单身份；对话调用者传入来源打开坐标，按钮只操作当前服务端菜单。 */
public final class BankMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BankMenu>> BANK = MENUS.register("bank_gui", () -> IMenuTypeExtension.create(BankMenu::new));
    public static final Supplier<MenuType<BankLoginMenu>> LOGIN = MENUS.register("bank_gu_idenglu", () -> IMenuTypeExtension.create(BankLoginMenu::new));

    private BankMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener(BankMenus::registerPayloads);
    }

    public static void openLogin(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new BankLoginMenu(id, inventory, pos),
                Component.translatable("menu.yvanchuxiuzhen.bank_login")), buffer -> buffer.writeBlockPos(pos));
    }

    public static void openBank(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, owner) -> new BankMenu(id, inventory, pos),
                Component.translatable("menu.yvanchuxiuzhen.bank")), buffer -> buffer.writeBlockPos(pos));
    }

    public static void explain(ServerPlayer player) {
        BankCards.message(player, "explain.1");
        BankCards.message(player, "explain.2");
        BankCards.message(player, "explain.3");
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(BankButtonPayload.TYPE, BankButtonPayload.STREAM_CODEC, BankButtonPayload::handle);
    }
}
