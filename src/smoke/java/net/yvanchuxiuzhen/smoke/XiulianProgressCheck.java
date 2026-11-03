package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.XiulianWorldData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;
import net.yvanchuxiuzhen.system.xiulian.client.LevelUpScreen;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/**
 * 修炼推进的实机检查（每秒一次的修炼循环）：待吸收修为转入、每日上限、突破（升级修正、寿元、属性点、境界名、升级界面）、
 * 生存模式下需要渡劫的层数开启雷劫、灵力恢复、骑乘闭关一天的结算。灵根在检查开始时固定为五行各 2（之和 10）。
 */
final class XiulianProgressCheck extends SystemCheck {
    private final Screenshots screenshots;
    private double experienceBefore;

    XiulianProgressCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("修炼推进", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                for (CultivationValue root : List.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER, CultivationValue.ROOT_TREE,
                        CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL)) {
                    Cultivation.set(player, root, 2);
                }
                Cultivation.set(player, CultivationValue.EXPERIENCE, 0);
                Cultivation.set(player, CultivationValue.EXPERIENCE_MAX, 1000);
                Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 50);
            });
            case 1 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> {
                    expect("待吸收修为转入", 50 * Cultivation.get(player, CultivationValue.EXPERIENCE_RATE), Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expect("待吸收修为清空", 0, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND));
                    XiulianWorldData.get(player.server).setDailyExperience(30);
                    Cultivation.setTizhiDouble(player, "dailyexperience", 0);
                    Cultivation.set(player, CultivationValue.EXPERIENCE_RATE, 1);
                    experienceBefore = Cultivation.get(player, CultivationValue.EXPERIENCE);
                    Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, 50);
                });
            }
            case 2 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> {
                    expect("每日上限截断", experienceBefore + 30, Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expect("当日已获修为", 30, Cultivation.tizhiDouble(player, "dailyexperience"));
                    XiulianWorldData.get(player.server).setDailyExperience(0);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                    Cultivation.setTizhiDouble(player, "LevelupCorrectHealth", 3);
                    Cultivation.set(player, CultivationValue.EXPERIENCE, 1100);
                    experienceBefore = Cultivation.get(player, CultivationValue.LIFETIME);
                });
            }
            case 3 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> {
                    expect("突破 层数", 1, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    expect("突破 境界名", "练气一层", Cultivation.get(player, CultivationText.LEVEL_NAME));
                    expect("突破 扣除所需修为", 100, Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expect("突破 新所需修为 (100 + 1 × 100) × 10 × 0.2", 400, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
                    expect("突破 寿元 +7300", experienceBefore + 7300, Cultivation.get(player, CultivationValue.LIFETIME));
                    expect("升级修正 生命成长", 23, PlayerAttributes.baseValue(player, Stat.HEALTH_MAX));
                    expect("升级文字 第 1 行", "进阶练气", Cultivation.tizhiString(player, "uplevelxianshi1"));
                    expect("升级文字 第 3 行", "属性点获得：5", Cultivation.tizhiString(player, "uplevelxianshi3"));
                    expect("升级文字 第 5 行", "§f血量上限:§a+3", Cultivation.tizhiString(player, "uplevelxianshi5"));
                });
            }
            case 4 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("升级界面已打开", minecraft.screen instanceof LevelUpScreen);
                screenshots.take("xiulian_levelup.png");
                PacketDistributor.sendToServer(new XiulianButtonPayload(0));
            }
            case 5 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("关闭升级界面", minecraft.screen == null);
                onServer(player -> {
                    player.setGameMode(GameType.SURVIVAL);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 20);
                    Cultivation.set(player, CultivationValue.EXPERIENCE, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
                });
            }
            case 6 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> {
                    expectTrue("20 层修为圆满开启雷劫", Cultivation.thunderOpen(player));
                    expect("雷劫开始后修为为 1", 1, Cultivation.get(player, CultivationValue.EXPERIENCE));
                    expect("雷劫中不突破", 20, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    Cultivation.setThunderOpen(player, false);
                    player.setGameMode(GameType.CREATIVE);
                    PlayerAttributes.setContribution(player, "smoke:recovery", new Contribution(Layer.BONUS, Map.of(Stat.POWER_REC, 4.0)));
                    Cultivation.set(player, CultivationValue.POWER, 5);
                });
            }
            case 7 -> {
                if (local < 30) {
                    return false;
                }
                onServer(player -> {
                    expectTrue("灵力恢复 " + Cultivation.get(player, CultivationValue.POWER), Cultivation.get(player, CultivationValue.POWER) > 5);
                    PlayerAttributes.clearContribution(player, "smoke:recovery");
                    Pig seat = EntityType.PIG.create(player.serverLevel());
                    seat.moveTo(player.getX(), player.getY(), player.getZ());
                    player.serverLevel().addFreshEntity(seat);
                    player.startRiding(seat, true);
                    Cultivation.setMainTechnique(player, new MainTechnique(true, true, "聚元功", 2, 3));
                    Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 3);
                    Cultivation.set(player, CultivationValue.CULTIVATION_COOLDOWN, 0);
                    Cultivation.setTizhiString(player, "xiuliankaiguan", "修炼");
                    experienceBefore = Cultivation.get(player, CultivationValue.LIFETIME);
                });
            }
            case 8 -> {
                if (local < 30) {
                    return false;
                }
                onServer(player -> {
                    double days = Cultivation.get(player, CultivationValue.CULTIVATION_DAYS);
                    expectTrue("闭关天数减少 " + days, days < 3);
                    expect("闭关每天寿元减 1", experienceBefore - (3 - days), Cultivation.get(player, CultivationValue.LIFETIME));
                    expect("闭关冷却随天数增加", 3 - days, Cultivation.get(player, CultivationValue.CULTIVATION_COOLDOWN));
                    player.stopRiding();
                    Cultivation.setTizhiString(player, "xiuliankaiguan", "");
                    Cultivation.set(player, CultivationValue.CULTIVATION_DAYS, 0);
                    Cultivation.setMainTechnique(player, MainTechnique.NONE);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }
}
