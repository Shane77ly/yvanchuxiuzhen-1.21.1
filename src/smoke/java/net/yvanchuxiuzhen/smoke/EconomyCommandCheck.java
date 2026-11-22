package net.yvanchuxiuzhen.smoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.economy.EconomyPrices;
import net.yvanchuxiuzhen.system.economy.api.BankAccount;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;
import net.yvanchuxiuzhen.system.economy.data.BankData;

/** 经共享命令验证银行入账与维度八类价格写入，结束恢复原账户及价格。 */
final class EconomyCommandCheck extends SystemCheck {
    private static final List<String> PRICE_KEYS = List.of("gongfajiage", "tuzhijiage", "zhuangbeijiage", "danyaojiage",
            "fulujiage", "shipinjiage", "yaocaijiage", "cailiaojiage");
    private static final BankAccount CREDITED = new BankAccount(14, 13, 14, 15, 16, 17, 18, 19, 20);
    private final Map<String, Double> originalPrices = new LinkedHashMap<>();
    private BankAccount originalAccount;
    private EconomyPrices prices;

    EconomyCommandCheck(List<String> report, String levelId) {
        super("经济管理命令", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step > 0 && local < 10) return false;
        switch (step) {
            case 0 -> onServer(player -> {
                originalAccount = BankAccounts.snapshot(player);
                prices = EconomyPrices.get(player.serverLevel());
                for (String key : PRICE_KEYS) originalPrices.put(key, prices.multiplier(key));
                player.setData(BankData.ACCOUNT, new BankAccount(11, 12, 13, 14, 15, 16, 17, 18, 19));
                command(player, "numerical depositinbank 87383.75");
            });
            case 1 -> {
                expect("真实入账命令向本人同步九级余额", CREDITED, BankAccounts.snapshot(minecraft.player));
                onServer(player -> {
                    expect("正价值命令逐档入账且保留旧余额及小数截断", CREDITED, BankAccounts.snapshot(player));
                    for (String value : List.of("0", "-16", "0.75")) {
                        command(player, "numerical depositinbank " + value);
                        expect("价值" + value + "的命令不改变账户", CREDITED, BankAccounts.snapshot(player));
                    }
                    for (int index = 0; index < PRICE_KEYS.size(); index++) {
                        command(player, "set " + PRICE_KEYS.get(index) + " " + (index + 1.25) + " true");
                    }
                });
            }
            case 2 -> {
                onServer(player -> {
                    for (int index = 0; index < PRICE_KEYS.size(); index++) {
                        String key = PRICE_KEYS.get(index);
                        expect("真实set命令写入执行维度" + key, index + 1.25, prices.multiplier(key));
                    }
                    expect("八类价格命令不改银行账户", CREDITED, BankAccounts.snapshot(player));
                });
                expect("无效入账与价格命令后本人余额保持", CREDITED, BankAccounts.snapshot(minecraft.player));
                onServer(player -> {
                    player.setData(BankData.ACCOUNT, originalAccount);
                    player.syncData(BankData.ACCOUNT);
                    originalPrices.forEach(prices::set);
                });
            }
            case 3 -> {
                onServer(player -> {
                    expect("恢复服务端原银行账户", originalAccount, BankAccounts.snapshot(player));
                    originalPrices.forEach((key, value) -> expect("恢复原维度价格" + key, value.doubleValue(), prices.multiplier(key)));
                });
                expect("恢复原账户同步给本人", originalAccount, BankAccounts.snapshot(minecraft.player));
                finish();
            }
            default -> throw new IllegalStateException("未知经济管理命令检查步骤 " + step);
        }
        return true;
    }

    private static void command(ServerPlayer player, String arguments) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(1),
                "yvanchuxitong " + player.getDisplayName().getString() + " " + arguments);
    }
}
