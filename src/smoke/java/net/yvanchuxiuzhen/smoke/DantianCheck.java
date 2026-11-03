package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.DantianActions;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.Dantian;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;
import net.yvanchuxiuzhen.system.xiulian.client.DantianScreen;
import net.yvanchuxiuzhen.system.xiulian.client.LevelUpScreen;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/**
 * 金丹、元婴与丹田界面的实机检查：五行各 9、主修“无”五行 31 品 15 层、灵力上限 3000 时进入 21 层凝结第 1 档五行均衡金丹（105）；
 * 功法层数不足 25 时进入 31 层得普通元婴（201）；丹田界面的本命槽、体质列表、页码、元婴名与返回按钮，关闭后写回本命槽。
 */
final class DantianCheck extends SystemCheck {
    private static final String POWER_SOURCE = "smoke:dantian";
    private final Screenshots screenshots;
    private double weaponSpeed;

    DantianCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("丹田", report, levelId);
        this.screenshots = screenshots;
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                for (CultivationValue root : List.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER, CultivationValue.ROOT_TREE,
                        CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL)) {
                    Cultivation.set(player, root, 9);
                }
                PlayerAttributes.setContribution(player, POWER_SOURCE, new Contribution(Layer.BONUS, Map.of(Stat.POWER_MAX, 3000.0)));
                Cultivation.setMainTechnique(player, new MainTechnique(true, true, "测试功法", "无", 31, 15));
                for (int number = 1; number <= 12; number++) {
                    Cultivation.setTizhiBoolean(player, String.format("tizhiopen_%03d", number), number == 1);
                }
                Cultivation.setTizhiDouble(player, "tizhinumber", 1);
                Cultivation.setTizhiDouble(player, "ColddownCorrectPercentage", 0);
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 20);
                Cultivation.set(player, CultivationValue.EXPERIENCE, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
            });
            case 1 -> {
                if (!(minecraft.screen instanceof LevelUpScreen)) {
                    return local > 200 && failed("进入 21 层后没有打开升级界面");
                }
                onServer(player -> {
                    expect("进入结丹 层数", 21, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    expectTrue("五行均衡第 1 档金丹 tizhiopen_105", Cultivation.tizhiBoolean(player, "tizhiopen_105"));
                    expect("金丹 冷却修正", -0.2, Cultivation.tizhiDouble(player, "ColddownCorrectPercentage"));
                    expect("金丹 体质数 +1", 2, Cultivation.tizhiDouble(player, "tizhinumber"));
                    weaponSpeed = Cultivation.tizhiDouble(player, "WeaponCultivateSpeed");
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 30);
                    Cultivation.set(player, CultivationValue.EXPERIENCE, Cultivation.get(player, CultivationValue.EXPERIENCE_MAX));
                });
                PacketDistributor.sendToServer(new XiulianButtonPayload(0));
            }
            case 2 -> {
                if (local < 10 || !(minecraft.screen instanceof LevelUpScreen)) {
                    return local > 200 && failed("进入 31 层后没有打开升级界面");
                }
                onServer(player -> {
                    expect("进入元婴 层数", 31, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
                    expectTrue("功法层数不足得普通元婴 tizhiopen_201", Cultivation.tizhiBoolean(player, "tizhiopen_201"));
                    expect("普通元婴 本命培养速度 +1", weaponSpeed + 1, Cultivation.tizhiDouble(player, "WeaponCultivateSpeed"));
                    expect("元婴不加体质数", 2, Cultivation.tizhiDouble(player, "tizhinumber"));
                    expectTrue("本命槽接受妖丹", DantianActions.canHold(new ItemStack(item("cailiao_183"))));
                    expectTrue("本命槽不接受石头", !DantianActions.canHold(new ItemStack(Items.STONE)));
                    Cultivation.setBenming(player, new ItemStack(item("cailiao_183")));
                });
                PacketDistributor.sendToServer(new XiulianButtonPayload(0));
            }
            case 3 -> {
                if (local < 10) {
                    return false;
                }
                onServer(Dantian::open);
            }
            case 4 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("丹田界面已打开", minecraft.screen instanceof DantianScreen);
                expect("本命槽放入本命法宝", item("cailiao_183"), minecraft.player.containerMenu.getSlot(0).getItem().getItem());
                expect("体质列表第 1 行", "tizhiopen_001", Cultivation.tizhiString(minecraft.player, "tizhixianshi1id"));
                expect("体质列表第 2 行（金丹）", "tizhiopen_105", Cultivation.tizhiString(minecraft.player, "tizhixianshi2id"));
                expect("页码", 1.0, Cultivation.tizhiDouble(minecraft.player, "tizhipagenum"));
                expect("元婴名", "tizhiopen_201", Cultivation.tizhiString(minecraft.player, "yvanyingname"));
                expectTrue("本命显示标记", Cultivation.tizhiBoolean(minecraft.player, "benmingxianshi"));
                screenshots.take("xiulian_dantian.png");
                onServer(player -> player.containerMenu.getSlot(0).set(ItemStack.EMPTY));
                PacketDistributor.sendToServer(new XiulianButtonPayload(12));
            }
            case 5 -> {
                if (local < 10) {
                    return false;
                }
                expectTrue("返回按钮在没有面板时关闭界面", minecraft.screen == null);
                onServer(player -> {
                    expectTrue("取出后本命槽为空", Cultivation.benming(player).isEmpty());
                    expectTrue("取出后取消本命显示", !Cultivation.tizhiBoolean(player, "benmingxianshi"));
                    PlayerAttributes.clearContribution(player, POWER_SOURCE);
                    Cultivation.setMainTechnique(player, MainTechnique.NONE);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private boolean failed(String message) {
        fail(message);
        finish();
        return false;
    }
}
