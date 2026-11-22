package net.yvanchuxiuzhen.system.economy.api;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.economy.network.CommerceButtonPayload;

/** 对话和NPC只经此入口设置商品会话、打开来源页面；账户操作由经济系统执行。 */
public final class EconomyMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<CommerceMenu>> SHOP = MENUS.register("shop_gui", () -> IMenuTypeExtension.create(CommerceMenu::shop));
    public static final Supplier<MenuType<CommerceMenu>> STONE_SALE = MENUS.register("bank_gu_ishougou", () -> IMenuTypeExtension.create(CommerceMenu::stoneSale));
    public static final Supplier<MenuType<CommerceMenu>> BASIC_SALE = MENUS.register("bank_gu_ishougou_2", () -> IMenuTypeExtension.create(CommerceMenu::basicSale));
    public static final Supplier<MenuType<CommerceMenu>> PLAYER_SHOP = MENUS.register("shopblock_gui", () -> IMenuTypeExtension.create(CommerceMenu::playerShop));
    public static final Supplier<MenuType<CommerceMenu>> PLAYER_SHOP_SETTINGS = MENUS.register("shopblock_guiset", () -> IMenuTypeExtension.create(CommerceMenu::playerShopSettings));
    public static final Supplier<MenuType<CommerceMenu>> AUCTION_SETTINGS = MENUS.register("paimaifangkuaigui", () -> IMenuTypeExtension.create(CommerceMenu::auctionSettings));
    public static final Supplier<MenuType<CommerceMenu>> AUCTION = MENUS.register("paimaihang", () -> IMenuTypeExtension.create(CommerceMenu::auction));

    private EconomyMenus() { }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                CommerceButtonPayload.TYPE, CommerceButtonPayload.STREAM_CODEC, CommerceButtonPayload::handle));
    }

    public static CompoundTag snapshot(Player player) { return player.getData(CommerceData.SESSION).copy(); }

    public static void setProductList(ServerPlayer player, String productList) {
        CompoundTag state = snapshot(player);
        state.putString("productlist", productList);
        player.setData(CommerceData.SESSION, state);
    }

    public static void setShopInformation(ServerPlayer player, String information) {
        CompoundTag state = snapshot(player);
        state.putString("shopinformation", information);
        player.setData(CommerceData.SESSION, state);
    }

    public static void open(ServerPlayer player, BlockPos pos, String sourceName) {
        if (sourceName.equals("银行")) { BankMenus.openLogin(player, pos); return; }
        if (sourceName.equals("免登银行")) { BankMenus.openBank(player, pos); return; }
        CommerceMenu.Kind kind = switch (sourceName) {
            case "商店" -> CommerceMenu.Kind.SHOP;
            case "出售" -> CommerceMenu.Kind.STONE_SALE;
            case "基础出售" -> CommerceMenu.Kind.BASIC_SALE;
            default -> throw new IllegalArgumentException("未知经济页面：" + sourceName);
        };
        open(player, pos, kind);
    }

    public static void open(ServerPlayer player, BlockPos pos, CommerceMenu.Kind kind) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new CommerceMenu(id, inventory, pos, kind),
                Component.translatable("menu.yvanchuxiuzhen." + kind.id())), buffer -> buffer.writeBlockPos(pos));
    }
}
