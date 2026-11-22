package net.yvanchuxiuzhen.system.economy.content;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.economy.CommerceTrading;
import net.yvanchuxiuzhen.system.economy.CommerceUpdates;
import net.yvanchuxiuzhen.system.economy.Auctions;
import net.yvanchuxiuzhen.system.economy.EconomyLayout;
import net.yvanchuxiuzhen.system.economy.ShopTrading;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;

/** 七页经济菜单共用来源槽位、服务端按钮和独立库存归还生命周期。 */
public final class CommerceMenu extends BankInventoryMenu {
    public enum Kind {
        SHOP("shop_gui", "ShopGUI"), STONE_SALE("bank_gu_ishougou", "BankGUIshougou"), BASIC_SALE("bank_gu_ishougou_2", "BankGUIshougou2"),
        PLAYER_SHOP("shopblock_gui", "ShopblockGui"), PLAYER_SHOP_SETTINGS("shopblock_guiset", "ShopblockGuiset"),
        AUCTION_SETTINGS("paimaifangkuaigui", "Paimaifangkuaigui"), AUCTION("paimaihang", "Paimaihang");
        private final String id;
        private final String page;
        Kind(String id, String page) { this.id = id; this.page = page; }
        public String id() { return id; }
        public String page() { return page; }
    }
    private final Kind kind;

    public static CommerceMenu shop(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.SHOP); }
    public static CommerceMenu stoneSale(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.STONE_SALE); }
    public static CommerceMenu basicSale(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.BASIC_SALE); }
    public static CommerceMenu playerShop(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.PLAYER_SHOP); }
    public static CommerceMenu playerShopSettings(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.PLAYER_SHOP_SETTINGS); }
    public static CommerceMenu auctionSettings(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.AUCTION_SETTINGS); }
    public static CommerceMenu auction(int id, Inventory inventory, FriendlyByteBuf data) { return new CommerceMenu(id, inventory, data.readBlockPos(), Kind.AUCTION); }

    public CommerceMenu(int id, Inventory inventory, BlockPos pos, Kind kind) {
        super(type(kind), id, inventory, pos, EconomyLayout.page(kind.page()).slots().size(), returns(kind));
        this.kind = kind;
        EconomyLayout.Page layout = EconomyLayout.page(kind.page());
        for (var coordinates : layout.slots()) {
            int index = coordinates.get(0);
            boolean display = kind == Kind.SHOP || kind == Kind.PLAYER_SHOP || kind == Kind.AUCTION;
            addCustom(new SlotItemHandler(items, index, coordinates.get(1), coordinates.get(2)) {
                @Override public boolean mayPickup(Player player) { return !display; }
                @Override public boolean mayPlace(ItemStack stack) {
                    if (display) return false;
                    return kind != Kind.STONE_SALE || index != 0 || stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "bank")));
                }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + (row + 1) * 9, layout.inventory_x() + column * 18, layout.inventory_y() + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, layout.inventory_x() + column * 18, layout.inventory_y() + 58));
        if (inventory.player instanceof ServerPlayer player) {
            if (kind == Kind.SHOP) CommerceUpdates.opened(player);
            else if (kind == Kind.PLAYER_SHOP) CommerceUpdates.later(2, () -> BankAccounts.refresh(player));
        }
    }

    private static MenuType<CommerceMenu> type(Kind kind) {
        return switch (kind) {
            case SHOP -> EconomyMenus.SHOP.get();
            case STONE_SALE -> EconomyMenus.STONE_SALE.get();
            case BASIC_SALE -> EconomyMenus.BASIC_SALE.get();
            case PLAYER_SHOP -> EconomyMenus.PLAYER_SHOP.get();
            case PLAYER_SHOP_SETTINGS -> EconomyMenus.PLAYER_SHOP_SETTINGS.get();
            case AUCTION_SETTINGS -> EconomyMenus.AUCTION_SETTINGS.get();
            case AUCTION -> EconomyMenus.AUCTION.get();
        };
    }

    public Kind kind() { return kind; }

    public String sourcePage() {
        return kind.page();
    }
    public BlockPos position() { return pos; }
    private static int[] returns(Kind kind) {
        return switch (kind) {
            case STONE_SALE -> new int[]{0, 1}; case BASIC_SALE -> new int[]{0};
            case PLAYER_SHOP_SETTINGS -> new int[]{0, 1, 2, 3};
            case AUCTION_SETTINGS -> IntStream.range(0, 20).toArray();
            default -> new int[0];
        };
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if (kind == Kind.AUCTION && player instanceof ServerPlayer serverPlayer) Auctions.closed(serverPlayer, pos);
    }

    public void button(ServerPlayer player, int operation) {
        if (kind == Kind.SHOP) {
            int selection = switch (operation) { case 0 -> 0; case 1 -> 3; case 2 -> 6; case 3 -> 9; case 6 -> 1; case 7 -> 4; case 8 -> 7; case 9 -> 10; case 10 -> 2; case 11 -> 5; case 12 -> 8; case 13 -> 11; default -> -1; };
            if (selection >= 0) CommerceTrading.buy(player, selection);
            else if (operation == 4 || operation == 5) CommerceTrading.page(player, operation == 5 ? 1 : -1);
            else if (operation == 14) CommerceTrading.explainShop(player);
        } else if (kind == Kind.STONE_SALE) {
            if (operation == 0) player.closeContainer();
            else if (operation == 1) CommerceTrading.explainSale(player);
            else if (operation == 2) CommerceTrading.sell(player, true);
            else if (operation == 3) CommerceTrading.refreshSale(player, true);
        } else if (kind == Kind.BASIC_SALE) {
            if (operation == 0) CommerceTrading.sell(player, false);
            else if (operation == 1) CommerceTrading.refreshSale(player, false);
        } else if (kind == Kind.PLAYER_SHOP || kind == Kind.PLAYER_SHOP_SETTINGS) {
            ShopTrading.button(player, pos, kind == Kind.PLAYER_SHOP_SETTINGS, operation);
        } else if (kind == Kind.AUCTION_SETTINGS) {
            Auctions.button(player, pos, operation);
        }
    }
}
