package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.api.EconomyMenus;
import net.yvanchuxiuzhen.system.economy.content.CommerceMenu;
import net.yvanchuxiuzhen.system.economy.content.EconomyBlockEntity;
import net.yvanchuxiuzhen.system.economy.data.CommerceData;
import net.yvanchuxiuzhen.system.economy.data.EconomyBlockState.ShopState;

/** 玩家商铺的货物、三项物物交换、账户付款、收益和来源按次限购。 */
public final class ShopTrading {
    private ShopTrading() { }

    public static void placed(ServerPlayer player, EconomyBlockEntity block) {
        ShopState state = block.state().shop();
        if (player.isCreative()) state.tiandao = true;
        state.ownerUuid = player.getStringUUID();
        Direction facing = block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.direction = facing.getAxis() == Direction.Axis.X ? "东西" : "南北";
        block.changed();
    }

    public static void interact(ServerPlayer player, EconomyBlockEntity block) {
        ShopState state = block.state().shop();
        long now = player.level().getDayTime();
        int interval = state.refreshInterval;
        if (state.tiandao && interval >= 1) {
            if (state.refreshRecord + interval <= now || state.refreshRecord >= now) {
                state.refreshRecord = (int) now;
                state.remainingStock = state.stockLimit;
                state.playerPurchases.clear();
                message(player, "商店刷新！");
            }
            state.refreshCountdownText = "距离下次刷新商店，还有" + format((interval - (now - state.refreshRecord)) / 20) + "秒";
        }
        boolean owner = player.getStringUUID().equals(state.ownerUuid);
        if (owner) claim(player, block);
        block.changed();
        EconomyMenus.open(player, block.getBlockPos(), owner && player.isShiftKeyDown() ? CommerceMenu.Kind.PLAYER_SHOP_SETTINGS : CommerceMenu.Kind.PLAYER_SHOP);
    }

    private static void claim(ServerPlayer player, EconomyBlockEntity block) {
        ShopState state = block.state().shop();
        int value = state.storedStones;
        if (value >= 1) {
            state.storedStones = 0;
            BankAccounts.creditValue(player, value);
            message(player, "§6" + value + "灵石已入账！");
        }
        boolean items = false;
        for (int slot = 4; slot <= 6; slot++) if (!block.getItem(slot).isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, block.getItem(slot).copy());
            block.setItem(slot, ItemStack.EMPTY);
            items = true;
        }
        if (items) message(player, "物品已取出。");
    }

    public static void button(ServerPlayer player, BlockPos pos, boolean settings, int operation) {
        if (!(player.level().getBlockEntity(pos) instanceof EconomyBlockEntity block) || block.auction()) return;
        if (!settings) { if (operation == 0) buy(player, block); return; }
        ShopState state = block.state().shop();
        if (!player.getStringUUID().equals(state.ownerUuid)) return;
        switch (operation) {
            case 0 -> {
                state.stockLimit = block.getItem(0).getCount();
                state.remainingStock = block.getItem(0).getCount();
                state.refreshRecord = (int) player.level().getDayTime();
                BlockPos above = pos.above();
                if (player.level().isEmptyBlock(above)) {
                    player.level().setBlock(above, Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
                    CommerceUpdates.later(5, () -> player.serverLevel().setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
                }
            }
            case 1 -> {
                player.closeContainer();
                Block.dropResources(block.getBlockState(), player.level(), pos, null);
                player.level().destroyBlock(pos, false);
            }
            case 2 -> state.perPlayerLimit = state.perPlayerLimit > 0 ? state.perPlayerLimit - 1 : 64;
            case 3 -> state.perPlayerLimit = state.perPlayerLimit < 64 ? state.perPlayerLimit + 1 : 0;
            case 4 -> state.stonesNeeded = 0;
            case 5, 6, 7, 8, 9, 10, 11, 12 -> state.stonesNeeded += (int) Math.pow(10, operation - 5);
            case 13 -> state.refreshInterval = 0;
            case 14 -> state.refreshInterval += 20;
            case 15 -> state.refreshInterval += 1200;
            case 16 -> state.refreshInterval += 12000;
            case 17 -> state.refreshInterval += 72000;
            case 18 -> explain(player);
            default -> { }
        }
        block.changed();
    }

    private static void buy(ServerPlayer player, EconomyBlockEntity block) {
        ShopState state = block.state().shop();
        if (state.remainingStock < 1) { message(player, "§4商品已售罄，请等待一段时间后再来购买。"); return; }
        int limit = state.perPlayerLimit;
        String playerKey = player.getStringUUID() + "num";
        if (limit != 0 && state.playerPurchases.getOrDefault(playerKey, 0) >= limit) { message(player, "§4您已达限购上限，请等待一段时间后再来购买。"); return; }
        int price = state.stonesNeeded;
        BankAccount account = BankAccounts.snapshot(player);
        int sum = 0;
        for (int tier = 9; tier >= 1; tier--) sum += account.balance(tier) * BankAccount.weight(tier);
        if (price >= 1 && price > sum) { message(player, "§4灵石不足！"); return; }
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        int[] available = new int[inventory == null ? 0 : inventory.getSlots()];
        for (int index = 0; index < available.length; index++) {
            available[index] = inventory.getStackInSlot(index).getCount();
        }
        for (int slot = 1; slot <= 3; slot++) {
            ItemStack requirement = block.getItem(slot);
            if (requirement.isEmpty()) continue;
            int missing = requirement.getCount();
            if (inventory != null) {
                for (int index = 0; index < inventory.getSlots() && missing > 0; index++) {
                    if (available[index] <= 0) continue;
                    ItemStack candidate = inventory.getStackInSlot(index);
                    if (!matches(candidate, requirement)) continue;
                    int take = Math.min(available[index], missing);
                    available[index] -= take;
                    missing -= take;
                }
            }
            if (missing > 0) {
                message(player, "§4" + requirement.getDisplayName().getString() + "数量不够！");
                return;
            }
        }
        if (price >= 1) {
            double change = (double) sum - price;
            for (int tier = 1; tier <= 9; tier++) BankAccounts.setBalance(player, tier, 0);
            BankAccounts.creditValue(player, change);
            BankAccounts.refresh(player);
        }
        boolean notify = false;
        for (int slot = 1; slot <= 3; slot++) {
            ItemStack requirement = block.getItem(slot);
            if (requirement.isEmpty()) continue;
            int need = requirement.getCount();
            if (inventory != null) for (int index = 0; index < inventory.getSlots() && need > 0; index++) {
                ItemStack candidate = inventory.getStackInSlot(index);
                if (!matches(candidate, requirement)) continue;
                int take = Math.min(candidate.getCount(), need);
                candidate.shrink(take);
                need -= take;
            }
            if (!state.tiandao) {
                ItemStack revenue = requirement.copy();
                revenue.setCount(block.getItem(slot + 3).getCount() + requirement.getCount());
                block.setItem(slot + 3, revenue);
                if (block.getItem(slot + 3).getCount() >= 50) notify = true;
            }
        }
        if (notify) for (ServerPlayer owner : player.serverLevel().players()) if (owner.getStringUUID().equals(state.ownerUuid)) {
            BlockPos pos = block.getBlockPos();
            message(owner, "交易方块内物品已超过50，请尽快领取！坐标：" + (double) pos.getX() + "," + (double) pos.getY() + "," + (double) pos.getZ());
        }
        ItemStack product = block.getItem(0).copy();
        product.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, product);
        if (!state.tiandao) {
            ItemStack stock = block.getItem(0).copy();
            stock.shrink(1);
            block.setItem(0, stock);
            state.storedStones += price;
        }
        state.remainingStock -= 1;
        if (limit > 0) state.playerPurchases.merge(playerKey, 1, Integer::sum);
        block.changed();
        player.containerMenu.broadcastChanges();
    }

    private static boolean matches(ItemStack candidate, ItemStack requirement) {
        return candidate.getItem() == requirement.getItem()
                && (currency(requirement).isEmpty() || currency(requirement).equals(currency(candidate)));
    }

    private static String currency(ItemStack stack) { return stack.getOrDefault(CommerceData.CURRENCY.get(), ""); }
    private static void explain(ServerPlayer player) {
        try (var stream = ShopTrading.class.getResourceAsStream("/economy/shop_explanation.json"); var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            for (var line : JsonParser.parseReader(reader).getAsJsonArray()) message(player, line.getAsString());
        } catch (IOException error) { throw new UncheckedIOException(error); }
    }
    private static String format(double value) { return new DecimalFormat("0").format(value); }
    private static void message(ServerPlayer player, String text) { player.displayClientMessage(Component.literal(text), false); }
}
