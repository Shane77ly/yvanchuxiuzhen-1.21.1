package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.attribute.api.DamageElement;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;
import net.yvanchuxiuzhen.system.xiulian.Constitution;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.client.BirthScreen;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/**
 * 修炼系统的实机检查：开启出身规则后按登录流程随机灵根与体质并打开出身界面（截图），经网络按钮锁定、随机、切换出身，
 * 关闭界面后体质生效；公开操作的加减与同步、灵根提交给 attribute；死亡重生与退出重进后保持。
 * 冒烟世界创建时关闭了出身规则，以免内容检查期间弹出出身界面。
 */
final class XiulianCheck extends SystemCheck {
    private double gold;
    private String constitution;
    private final Screenshots screenshots;

    XiulianCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("修炼", report, levelId);
        this.screenshots = screenshots;
    }

    private static void press(int button) {
        PacketDistributor.sendToServer(new XiulianButtonPayload(button));
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                // 属性检查留下的灵根加值会叠加到五行系数上，先撤销。
                PlayerAttributes.clearContribution(player, "smoke:growth");
                player.serverLevel().getGameRules().getRule(XiulianRules.BIRTH_SELECTION).set(true, player.server);
                BirthSelection.onLogin(player);
                double sum = 0;
                boolean inRange = true;
                for (CultivationValue root : BirthSelection.ROOTS) {
                    double value = Cultivation.get(player, root);
                    sum += value;
                    inRange &= value >= 0 && value <= 9;
                }
                expectTrue("随机灵根在 0–9 之间", inRange);
                expect("阴阳 = 金 + 火 − 木 − 水", Cultivation.get(player, CultivationValue.ROOT_GOLD) + Cultivation.get(player, CultivationValue.ROOT_FIRE)
                        - Cultivation.get(player, CultivationValue.ROOT_TREE) - Cultivation.get(player, CultivationValue.ROOT_WATER), Cultivation.get(player, CultivationValue.YINYANG));
                constitution = Cultivation.get(player, CultivationText.CONSTITUTION);
                expectTrue("随机体质为前 10 种 " + constitution, Constitution.applied(constitution).ordinal() < 10 && !constitution.isEmpty());
                expect("火灵根提交给 attribute", Cultivation.get(player, CultivationValue.ROOT_FIRE) * 0.1,
                        PlayerAttributes.elementCoefficient(player, DamageElement.FIRE));
                gold = Cultivation.get(player, CultivationValue.ROOT_GOLD);
                expectTrue("灵根之和 " + sum, sum >= 0);
            });
            case 1 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("出身界面已打开", minecraft.screen instanceof BirthScreen);
                expect("客户端 体质", constitution, Cultivation.get(minecraft.player, CultivationText.CONSTITUTION));
                screenshots.take("xiulian_birth.png");
                press(0);
                press(5);
                press(7);
            }
            case 2 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("锁定金灵根", Cultivation.tizhiBoolean(minecraft.player, "LockGold"));
                expect("锁定次数用尽", 0, Cultivation.get(minecraft.player, CultivationValue.LIFTLOCK));
                expect("随机后金灵根不变", gold, Cultivation.get(minecraft.player, CultivationValue.ROOT_GOLD));
                expect("随机次数减 1", 9, Cultivation.get(minecraft.player, CultivationValue.RANDOMLIFT));
                expect("下一个出身", "世家纨绔", Cultivation.get(minecraft.player, CultivationText.BIRTH));
                onServer(player -> {
                    Objective objective = player.getScoreboard().getObjective("suijishu");
                    expectTrue("随机体质写入记分项 suijishu", objective != null);
                    int roll = player.getScoreboard().getOrCreatePlayerScore(player, objective).get();
                    expect("体质取自记分项", Constitution.rolled(roll).title(), Cultivation.get(player, CultivationText.CONSTITUTION));
                    constitution = Cultivation.get(player, CultivationText.CONSTITUTION);
                });
                press(0);
                press(6);
            }
            case 3 -> {
                if (local < 20) {
                    return false;
                }
                expectTrue("解锁金灵根", !Cultivation.tizhiBoolean(minecraft.player, "LockGold"));
                expect("解锁返还次数", 1, Cultivation.get(minecraft.player, CultivationValue.LIFTLOCK));
                expect("上一个出身", "流浪侠客", Cultivation.get(minecraft.player, CultivationText.BIRTH));
                minecraft.player.closeContainer();
            }
            case 4 -> {
                if (local < 20) {
                    return false;
                }
                onServer(player -> {
                    Constitution applied = Constitution.applied(constitution);
                    expectTrue("关闭后体质标记 " + applied.openKey(), Cultivation.tizhiBoolean(player, applied.openKey()));
                    expect("体质计数", 1, Cultivation.tizhiDouble(player, "tizhinumber"));
                    for (Constitution.Bonus bonus : applied.bonuses()) {
                        expect("体质修正 " + bonus.key(), bonus.amount(), Cultivation.tizhiDouble(player, bonus.key()));
                    }
                    Cultivation.add(player, CultivationValue.POWER, -5);
                    Cultivation.set(player, CultivationValue.LIFETIME, 10000);
                });
            }
            case 5 -> {
                if (local < 10) {
                    return false;
                }
                expect("客户端 灵力", 15, Cultivation.get(minecraft.player, CultivationValue.POWER));
                expect("客户端 寿元", 10000, Cultivation.get(minecraft.player, CultivationValue.LIFETIME));
                kill();
            }
            case 6 -> {
                return respawned(local);
            }
            case 7 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> checkKept("重生后", player));
                leaveWorld();
            }
            case 8 -> {
                return reopened(local);
            }
            case 9 -> {
                if (!inWorld(local)) {
                    return false;
                }
                onServer(player -> checkKept("重进后", player));
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private void checkKept(String label, ServerPlayer player) {
        expect(label + " 体质", constitution, Cultivation.get(player, CultivationText.CONSTITUTION));
        expect(label + " 灵力", 4, Cultivation.get(player, CultivationValue.POWER));
        expect(label + " 寿元", 9970, Cultivation.get(player, CultivationValue.LIFETIME));
        expect(label + " 体质计数", 1, Cultivation.tizhiDouble(player, "tizhinumber"));
        expect(label + " 火灵根系数", Cultivation.get(player, CultivationValue.ROOT_FIRE) * 0.1, PlayerAttributes.elementCoefficient(player, DamageElement.FIRE));
        expectTrue(label + " 未重开出身界面", !(minecraft.screen instanceof BirthScreen));
    }
}
