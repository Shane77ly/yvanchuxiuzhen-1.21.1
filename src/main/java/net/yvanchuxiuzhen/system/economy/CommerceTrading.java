package net.yvanchuxiuzhen.system.economy;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.BankCards;
import net.yvanchuxiuzhen.system.economy.api.EconomyItems;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 商品分页、两种货币购买及普通/灵石收购；灵石按面额从低到高扣款。 */
public final class CommerceTrading {
    public static final GameRules.Key<GameRules.BooleanValue> SHOP_SELL = GameRules.register("shopsell", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    private CommerceTrading() { }

    public static void initialize() { }

    private static Slot slot(ServerPlayer player, int index) {
        if (player.containerMenu instanceof Supplier<?> supplier && supplier.get() instanceof Map<?, ?> slots) return (Slot) slots.get(index);
        return null;
    }

    public static void opened(ServerPlayer player) {
        CompoundTag state = EconomyMenus.snapshot(player);
        state.putInt("shoppage", 0);
        player.setData(CommerceData.SESSION, state);
        refreshShop(player);
        BankAccounts.refresh(player);
    }

    public static void page(ServerPlayer player, int direction) {
        CompoundTag state = EconomyMenus.snapshot(player);
        int maximum = EconomyMenus.shopCatalog(player).products().size() / 12;
        int page = state.getInt("shoppage");
        page = direction > 0 ? page < maximum ? page + 1 : 0 : page > 0 ? page - 1 : maximum;
        state.putInt("shoppage", page);
        player.setData(CommerceData.SESSION, state);
        refreshShop(player);
    }

    public static void refreshShop(ServerPlayer player) {
        CompoundTag state = EconomyMenus.snapshot(player);
        ShopCatalogDefinition catalog = EconomyMenus.shopCatalog(player);
        ShopCatalogDefinition.ShopInfo info = catalog.info();
        state.putString("storename", info.storeName());
        state.putString("storeowner", info.ownerName());
        state.putString("storedescription", info.description());
        for (int index = 0; index < 12; index++) {
            Slot target = slot(player, index);
            if (target != null) target.set(ItemStack.EMPTY);
            state.putString("shopjiagetxt" + suffix(index), "");
            state.putString("shopnametxt" + suffix(index), "");
        }
        List<ShopCatalogDefinition.Product> products = catalog.products();
        int page = Math.max(0, state.getInt("shoppage"));
        int count = Math.min(12, Math.max(0, products.size() - page * 12));
        for (int index = 0; index < count; index++) {
            ShopCatalogDefinition.Product product = products.get(page * 12 + index);
            ItemStack item = EconomyItems.create(product);
            Slot target = slot(player, index);
            if (target != null) target.set(item);
            state.putString("shopnametxt" + suffix(index), item.getDisplayName().getString() + "(" + format(product.price()) + ")");
            state.putString("shopjiagetxt" + suffix(index), priceText(product.price(), product.currency().isStones(), true));
        }
        player.containerMenu.broadcastChanges();
        player.setData(CommerceData.SESSION, state);
    }

    public static void buy(ServerPlayer player, int selection) {
        CompoundTag state = EconomyMenus.snapshot(player);
        state.putInt("playerxvanze", selection);
        List<ShopCatalogDefinition.Product> products = EconomyMenus.shopCatalog(player).products();
        int offset = selection + state.getInt("shoppage") * 12;
        if (offset >= 0 && offset < products.size()) {
            ShopCatalogDefinition.Product product = products.get(offset);
            if (product.currency().isStones()) buyStones(player, selection, product.price());
            else buyCoins(player, selection, product.price());
        } else message(player, "空");
        CompoundTag current = EconomyMenus.snapshot(player);
        current.putInt("playerxvanze", 0);
        player.setData(CommerceData.SESSION, current);
    }

    private static void buyCoins(ServerPlayer player, int selection, double price) {
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        double money = 0;
        if (inventory != null) for (int index = 0; index < inventory.getSlots(); index++) {
            ItemStack stack = inventory.getStackInSlot(index);
            money += coinUnit(stack) * stack.getCount();
        }
        if (money < price) { message(player, "还差" + format(price - money)); return; }
        Slot target = slot(player, selection);
        ItemStack goods = target == null ? ItemStack.EMPTY : target.getItem().copy();
        goods.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, goods);
        if (inventory != null) for (int index = 0; index < inventory.getSlots(); index++) {
            ItemStack stack = inventory.getStackInSlot(index);
            if (coinUnit(stack) > 0) player.getInventory().clearOrCountMatchingItems(other -> stack.getItem() == other.getItem(), stack.getCount(), player.inventoryMenu.getCraftSlots());
        }
        giveCoins(player, money - price);
    }

    private static void buyStones(ServerPlayer player, int selection, double price) {
        BankAccount account = BankAccounts.snapshot(player);
        double value = 0;
        for (int tier = 1; tier <= 9; tier++) if (account.balance(tier) >= 1) value += BankAccount.weight(tier) * account.balance(tier);
        if (value < price) { message(player, "灵石还差一点点。"); return; }
        for (int tier = 1; tier <= 9 && price >= 1; tier++) {
            int balance = account.balance(tier);
            if (balance < 1) continue;
            double available = (double) balance * BankAccount.weight(tier);
            account = account.withBalance(tier, 0);
            if (available >= price) {
                double change = available - price;
                for (int lower = tier; lower >= 2; lower--) {
                    int unit = BankAccount.weight(lower);
                    if (change >= unit) {
                        double count = Math.floor(change / unit);
                        change -= count * unit;
                        account = account.withBalance(lower, (int) (account.balance(lower) + count));
                    }
                }
                if (change >= 1) account = account.withBalance(1, (int) (account.balance(1) + change));
                break;
            }
            price -= available;
        }
        Slot target = slot(player, selection);
        ItemStack goods = target == null ? ItemStack.EMPTY : target.getItem().copy();
        goods.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, goods);
        for (int tier = 1; tier <= 9; tier++) BankAccounts.setBalance(player, tier, account.balance(tier));
        BankAccounts.refresh(player);
    }

    public static void refreshSale(ServerPlayer player, boolean stones) {
        CompoundTag state = EconomyMenus.snapshot(player);
        Slot target = slot(player, stones ? 1 : 0);
        ItemStack goods = target == null ? ItemStack.EMPTY : target.getItem();
        if (stones) {
            state.putString("bankceshi1", goods.isEmpty() ? "......" : "");
            state.putString("bankceshi2", "");
            if (!goods.isEmpty()) {
                double value = CommerceValuation.value(player.serverLevel(), goods, state);
                if (value >= 1) state.putString("bankceshi2", "总价值：" + priceText(value, true, false));
                else { state.putString("bankceshi1", "此物无法在此卖出。"); state.putString("bankceshi2", ""); }
            }
        } else if (!goods.isEmpty()) {
            double price = CommerceCatalog.basicPrice(BuiltInRegistries.ITEM.getKey(goods.getItem()).toString());
            double value = price * goods.getCount();
            state.putString("bankceshi1", price == 0 ? "“我们不收这种物品。”" : goods.getDisplayName().getString() + "单价" + format(price) + "文，总价值为" + format(value) + "文。");
            state.putString("bankceshi2", price == 0 ? "" : "相当于" + basicSaleText(value));
        }
        player.setData(CommerceData.SESSION, state);
    }

    public static void sell(ServerPlayer player, boolean stones) {
        if (stones && !player.serverLevel().getGameRules().getBoolean(SHOP_SELL)) return;
        Slot target = slot(player, stones ? 1 : 0);
        ItemStack goods = target == null ? ItemStack.EMPTY : target.getItem();
        if (goods.isEmpty()) { if (stones) message(player, "§4请先放置需要卖出的物品！"); return; }
        CompoundTag state = EconomyMenus.snapshot(player);
        double value = stones ? CommerceValuation.value(player.serverLevel(), goods, state)
                : CommerceCatalog.basicPrice(BuiltInRegistries.ITEM.getKey(goods.getItem()).toString()) * goods.getCount();
        if (value < 1) return;
        if (stones) {
            target.set(ItemStack.EMPTY);
            Slot card = slot(player, 0);
            boolean deposit = card != null && BankCards.ownedBy(card.getItem(), player);
            if (deposit) BankAccounts.creditValue(player, value); else giveStones(player, value);
            message(player, (deposit ? "已存入" : "获得") + priceText(value, true, false));
        } else {
            giveCoins(player, value);
            target.set(ItemStack.EMPTY);
        }
        player.containerMenu.broadcastChanges();
        if (stones) refreshSale(player, true);
    }

    public static double coinUnit(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return switch (id) { case "yvanchuxiuzhen:money_01" -> 1; case "yvanchuxiuzhen:money_02" -> 10; case "yvanchuxiuzhen:money_03" -> 100; case "yvanchuxiuzhen:money_04" -> 1000; default -> 0; };
    }

    public static void giveCoins(ServerPlayer player, double value) {
        int[] units = {1, 10, 100, 1000};
        for (int tier = 4; tier >= 1; tier--) if (value >= units[tier - 1]) {
            double count = tier == 1 ? value : Math.floor(value / units[tier - 1]);
            ItemStack coin = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "money_0" + tier)), (int) count);
            ItemHandlerHelper.giveItemToPlayer(player, coin);
            value -= count * units[tier - 1];
        }
    }

    public static void giveStones(ServerPlayer player, double value) {
        for (int tier = 9; tier >= 1; tier--) if (value >= BankAccount.weight(tier)) {
            double count = tier == 1 ? value : Math.floor(value / BankAccount.weight(tier));
            ItemStack stone = BankAccounts.spiritStone(tier);
            Materials.setInstance(stone, tier * 5, 4);
            stone.setCount((int) count);
            ItemHandlerHelper.giveItemToPlayer(player, stone);
            value -= count * BankAccount.weight(tier);
        }
    }

    public static String priceText(double value, boolean stones, boolean compact) {
        if (!stones) {
            StringBuilder text = new StringBuilder();
            int[] units = {1000, 100, 10, 1};
            String[] names = {"金元宝", "银元宝", "贯", "文"};
            for (int index = 0; index < 4; index++) if (value >= units[index]) {
                double count = index == 3 ? value : Math.floor(value / units[index]);
                value -= count * units[index]; text.append(format(count)).append(names[index]);
            }
            return text.toString();
        }
        StringBuilder text = new StringBuilder();
        String[] tiers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        int kinds = 0;
        for (int tier = 9; tier >= 1; tier--) if (value >= BankAccount.weight(tier)) {
            double count = tier == 1 ? value : Math.floor(value / BankAccount.weight(tier));
            value -= count * BankAccount.weight(tier); kinds++;
            if (!compact || kinds <= 2) text.append(tiers[tier - 1]).append("级灵石").append(format(count)).append("枚 ");
        }
        if (compact && kinds > 2) text.append(" 等");
        return text.toString();
    }

    private static String basicSaleText(double value) {
        StringBuilder text = new StringBuilder();
        for (int unit : new int[]{1000, 100}) if (value >= unit) {
            double count = Math.floor(value / unit); value -= count * unit;
            text.append(format(count)).append(unit == 1000 ? "枚金元宝 " : "枚银元宝 ");
        }
        return text.append(format(value)).append("文").toString();
    }

    private static String suffix(int index) { return new DecimalFormat("00").format(index); }
    private static String format(double value) { return new DecimalFormat("0").format(value); }
    private static void message(ServerPlayer player, String text) { player.displayClientMessage(Component.literal(text), false); }
    public static void explainShop(ServerPlayer player) {
        message(player, "1、购买货物需要在天地银行办理一枚通宝玉，然后在通宝玉里存钱，没有办法直接用灵石购买商品。");
        message(player, "2、如果jei模组的界面挡住了商店页面，可以通过按下快接键 ctrl + O 来临时关闭jei。");
    }
    public static void explainSale(ServerPlayer player) {
        message(player, "这是通宝商盟收购材料的界面。"); message(player, "在左侧的物品槽中放入想要卖出的物品，然后点击右侧的按钮即可卖出。");
        message(player, "那个环形的按钮按下后，可以显示要卖出的物品的价格，请妥善利用。"); message(player, "卖出的物品会优先换取高品灵石，请放心出售~");
    }
}
