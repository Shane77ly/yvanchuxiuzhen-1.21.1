package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.data.BankData;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 银行账户的唯一写入口；外部消费者提交确定的级别与数量，不写附件或菜单投影。 */
public final class BankAccounts {
    private BankAccounts() {
    }

    public static BankAccount snapshot(Player player) {
        return player.getData(BankData.ACCOUNT);
    }

    public static int balance(Player player, int tier) {
        return snapshot(player).balance(tier);
    }

    public static void setBalance(ServerPlayer player, int tier, int amount) {
        player.setData(BankData.ACCOUNT, snapshot(player).withBalance(tier, amount));
    }

    public static void add(ServerPlayer player, int tier, int amount) {
        setBalance(player, tier, balance(player, tier) + amount);
    }

    /** 来源菜单先把堆叠数量存为double，再加到int余额并截断，因此不使用整型加法溢出。 */
    public static void depositCount(ServerPlayer player, int tier, int count) {
        setBalance(player, tier, (int) (balance(player, tier) + (double) count));
    }

    public static void creditValue(ServerPlayer player, double value) {
        if (value > 0) player.setData(BankData.ACCOUNT, snapshot(player).creditValue(value));
    }

    /** 每次取一枚，保持来源材料等级为灵石级别乘5、品质为4，库存不足由原生给予流程掉落。 */
    public static boolean withdrawOne(ServerPlayer player, int tier) {
        int amount = balance(player, tier);
        if (amount < 1) return false;
        ItemStack stack = spiritStone(tier);
        Materials.setInstance(stack, tier * 5, 4);
        add(player, tier, -1);
        ItemHandlerHelper.giveItemToPlayer(player, stack);
        return true;
    }

    public static ItemStack spiritStone(int tier) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_19" + tier)));
    }

    public static BankDisplay display(Player player) {
        return player.getData(BankData.DISPLAY);
    }

    public static void refresh(ServerPlayer player) {
        player.setData(BankData.DISPLAY, new BankDisplay(player.getDisplayName().getString(), snapshot(player), true));
    }
}
