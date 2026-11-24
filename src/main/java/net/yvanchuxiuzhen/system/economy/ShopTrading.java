package net.yvanchuxiuzhen.system.economy;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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

/** 玩家商铺的货物、三项物物交换、账户付款、收益和来源按次限购。 */
public final class ShopTrading {
    private ShopTrading() { }

    public static void placed(ServerPlayer player, EconomyBlockEntity block) {
        CompoundTag state = block.state();
        if (player.isCreative()) state.putBoolean("tiandao", true);
        state.putString("belongToMasterUUID", player.getStringUUID());
        Direction facing = block.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        state.putString("fangxiang", facing.getAxis() == Direction.Axis.X ? "东西" : "南北");
        block.changed();
    }

    public static void interact(ServerPlayer player, EconomyBlockEntity block) {
        CompoundTag state = block.state();
        long now = player.level().getDayTime();
        int interval = state.getInt("shuaxintick");
        if (state.getBoolean("tiandao") && interval >= 1) {
            if (state.getInt("shuaxinjilu") + interval <= now || state.getInt("shuaxinjilu") >= now) {
                state.putInt("shuaxinjilu", (int) now);
                state.putInt("shengyvshangpin", state.getInt("xiangou"));
                state.put("playerxiangou", new CompoundTag());
                message(player, "商店刷新！");
            }
            state.putString("shuaxinshuaxin", "距离下次刷新商店，还有" + format((interval - (now - state.getInt("shuaxinjilu"))) / 20) + "秒");
        }
        boolean owner = player.getStringUUID().equals(state.getString("belongToMasterUUID"));
        if (owner) claim(player, block);
        block.changed();
        EconomyMenus.open(player, block.getBlockPos(), owner && player.isShiftKeyDown() ? CommerceMenu.Kind.PLAYER_SHOP_SETTINGS : CommerceMenu.Kind.PLAYER_SHOP);
    }

    private static void claim(ServerPlayer player, EconomyBlockEntity block) {
        int value = block.state().getInt("cunchulingshi");
        if (value >= 1) {
            // 来源领取仅入账，不清零历史收益；后续右键仍按该字段领取。
            BankAccounts.creditValue(player, value);
            message(player, "§60灵石已入账！");
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
        if (!player.getStringUUID().equals(block.state().getString("belongToMasterUUID"))) return;
        CompoundTag state = block.state();
        switch (operation) {
            case 0 -> {
                state.putInt("xiangou", block.getItem(0).getCount());
                state.putInt("shengyvshangpin", block.getItem(0).getCount());
                state.putInt("shuaxinjilu", (int) player.level().getDayTime());
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
            case 2 -> state.putInt("danrenxiangou", state.getInt("danrenxiangou") > 0 ? state.getInt("danrenxiangou") - 1 : 64);
            case 3 -> state.putInt("danrenxiangou", state.getInt("danrenxiangou") < 64 ? state.getInt("danrenxiangou") + 1 : 0);
            case 4 -> state.putInt("lingshineed", 0);
            case 5, 6, 7, 8, 9, 10, 11, 12 -> state.putInt("lingshineed", state.getInt("lingshineed") + (int) Math.pow(10, operation - 5));
            case 13 -> state.putInt("shuaxintick", 0);
            case 14 -> state.putInt("shuaxintick", state.getInt("shuaxintick") + 20);
            case 15 -> state.putInt("shuaxintick", state.getInt("shuaxintick") + 1200);
            case 16 -> state.putInt("shuaxintick", state.getInt("shuaxintick") + 12000);
            case 17 -> state.putInt("shuaxintick", state.getInt("shuaxintick") + 72000);
            case 18 -> explain(player);
            default -> { }
        }
        block.changed();
    }

    private static void buy(ServerPlayer player, EconomyBlockEntity block) {
        CompoundTag state = block.state();
        if (state.getInt("shengyvshangpin") < 1) { message(player, "§4商品已售罄，请等待一段时间后再来购买。"); return; }
        int limit = state.getInt("danrenxiangou");
        CompoundTag purchases = state.getCompound("playerxiangou");
        String playerKey = player.getStringUUID() + "num";
        if (limit != 0 && purchases.getInt(playerKey) >= limit) { message(player, "§4您已达限购上限，请等待一段时间后再来购买。"); return; }
        int price = state.getInt("lingshineed");
        BankAccount account = BankAccounts.snapshot(player);
        int sum = 0;
        for (int tier = 9; tier >= 1; tier--) sum += account.balance(tier) * BankAccount.weight(tier);
        if (price >= 1 && price > sum) { message(player, "§4灵石不足！"); return; }
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        boolean rejected = false;
        for (int slot = 1; slot <= 3; slot++) {
            ItemStack requirement = block.getItem(slot);
            if (requirement.isEmpty()) continue;
            // 来源只在Inventory.contains为真时核数；整类物品缺失不会设置拒绝标记。
            if (!player.getInventory().contains(requirement)) continue;
            int count = 0;
            if (inventory != null) {
                for (int index = 0; index < inventory.getSlots(); index++) {
                    ItemStack candidate = inventory.getStackInSlot(index);
                    if (candidate.getItem() == requirement.getItem() && (currency(requirement).isEmpty() || currency(requirement).equals(currency(candidate)))) count += candidate.getCount();
                }
            }
            if (count < requirement.getCount()) {
                message(player, "§4" + requirement.getDisplayName().getString() + "数量不够！");
                rejected = true;
                break;
            }
        }
        if (rejected) return;
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
            if (inventory != null) for (int index = 0; index < inventory.getSlots(); index++) {
                ItemStack candidate = inventory.getStackInSlot(index);
                if (candidate.getItem() != requirement.getItem() || !currency(requirement).equals(currency(candidate))) continue;
                if (candidate.getCount() >= need) { candidate.shrink(need); need = 0; }
                else {
                    // 来源先清空该栈，再读取数量相减，因此不足栈不会减少后续所需数量。
                    candidate.shrink(candidate.getCount());
                    need -= candidate.getCount();
                }
            }
            if (!state.getBoolean("tiandao")) {
                ItemStack revenue = requirement.copy();
                revenue.setCount(block.getItem(slot + 3).getCount() + requirement.getCount());
                block.setItem(slot + 3, revenue);
                if (block.getItem(slot + 3).getCount() >= 50) notify = true;
            }
        }
        if (notify) for (ServerPlayer owner : player.serverLevel().players()) if (owner.getStringUUID().equals(state.getString("belongToMasterUUID"))) {
            BlockPos pos = block.getBlockPos();
            message(owner, "交易方块内物品已超过50，请尽快领取！坐标：" + (double) pos.getX() + "," + (double) pos.getY() + "," + (double) pos.getZ());
        }
        ItemStack product = block.getItem(0).copy();
        product.setCount(1);
        ItemHandlerHelper.giveItemToPlayer(player, product);
        if (!state.getBoolean("tiandao")) {
            ItemStack stock = block.getItem(0).copy();
            stock.shrink(1);
            block.setItem(0, stock);
            state.putInt("cunchulingshi", state.getInt("cunchulingshi") + price);
        }
        state.putInt("shengyvshangpin", state.getInt("shengyvshangpin") - 1);
        if (limit > 0) purchases.putInt(playerKey, purchases.getInt(playerKey) + 1);
        block.changed();
        player.containerMenu.broadcastChanges();
    }

    private static String currency(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("huobi"); }
    private static void explain(ServerPlayer player) {
        try (var stream = ShopTrading.class.getResourceAsStream("/economy/shop_explanation.json"); var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            for (var line : JsonParser.parseReader(reader).getAsJsonArray()) message(player, line.getAsString());
        } catch (IOException error) { throw new UncheckedIOException(error); }
    }
    private static String format(double value) { return new DecimalFormat("0").format(value); }
    private static void message(ServerPlayer player, String text) { player.displayClientMessage(Component.literal(text), false); }
}
