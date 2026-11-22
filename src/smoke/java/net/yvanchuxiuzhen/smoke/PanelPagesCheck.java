package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.system.attribute.VanillaAttributes;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.attribute.data.AttributeAttachments;
import net.yvanchuxiuzhen.system.attribute.data.AttributeData;
import net.yvanchuxiuzhen.system.cooking.api.Alcohol;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.client.ProfessionScreen;
import net.yvanchuxiuzhen.system.profession.content.ProfessionMenu;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPoint;
import net.yvanchuxiuzhen.system.xiulian.client.RetreatScreen;
import net.yvanchuxiuzhen.system.xiulian.client.SkillPointScreen;
import net.yvanchuxiuzhen.system.xiulian.content.RetreatMenu;
import net.yvanchuxiuzhen.system.xiulian.content.SkillPointMenu;
import net.yvanchuxiuzhen.system.xiulian.data.CultivationData;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;

/** 玩家02/05/06/07/08页的真实控件、数据同步与有限结算检查；不覆盖坐具和功法自然修炼。 */
final class PanelPagesCheck extends SystemCheck {
    private static final List<String> POINT_BUTTONS = List.of("增加气血上限", "增加灵力上限", "增加精力上限",
            "增加物理攻击", "增加法术攻击", "增加精神攻击");
    private final Screenshots screenshots;
    private final double[] initialStats = new double[SkillPoint.values().length];
    private CultivationData originalCultivation;
    private AttributeData originalAttributes;
    private ProfessionData originalProfessions;
    private List<MobEffectInstance> originalEffects;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private float originalHealth;
    private boolean originalNoGravity;
    private boolean originalCollectionPresent;
    private double originalCollection;
    private double originalAlcohol;
    private int originalPage;

    PanelPagesCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("玩家分页", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step > 0 && local < 10) return false;
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (!screen(AttributeScreen.class, "01入口")) return true;
                button("属性加点").onPress();
            }
            case 2 -> {
                if (!screen(SkillPointScreen.class, "01到02")) return true;
                expectTrue("02客户端有36个玩家背包槽", minecraft.player.containerMenu instanceof SkillPointMenu
                        && minecraft.player.containerMenu.slots.size() == 36);
                onServer(player -> {
                    expect("02记页", 2, player.getData(PanelNavigation.PAGE).intValue());
                    expect("02加点前可用点数", 6, Cultivation.get(player, CultivationValue.SKILL_POINTS));
                    for (SkillPoint point : SkillPoint.values()) {
                        initialStats[point.ordinal()] = PlayerAttributes.baseValue(player, point.stat());
                    }
                });
                POINT_BUTTONS.forEach(name -> button(name).onPress());
            }
            case 3 -> {
                if (!screen(SkillPointScreen.class, "六项加点后仍在02")) return true;
                onServer(player -> {
                    expect("六次原生按钮消息各扣一点", 0, Cultivation.get(player, CultivationValue.SKILL_POINTS));
                    for (SkillPoint point : SkillPoint.values()) {
                        expect(point + "服务端已分配", 1, Cultivation.get(player, point.spent()));
                        expect(point + "服务端成长", initialStats[point.ordinal()] + point.amount(), PlayerAttributes.baseValue(player, point.stat()));
                    }
                });
                expect("02剩余点数同步", 0, Cultivation.get(minecraft.player, CultivationValue.SKILL_POINTS));
                for (SkillPoint point : SkillPoint.values()) {
                    expect(point + "客户端分配计数", 1, Cultivation.get(minecraft.player, point.spent()));
                    expect(point + "客户端属性同步", initialStats[point.ordinal()] + point.amount(), PlayerAttributes.baseValue(minecraft.player, point.stat()));
                }
                screenshots.take("panel-page-02.png");
                button(POINT_BUTTONS.getFirst()).onPress();
            }
            case 4 -> {
                onServer(player -> {
                    expect("不足时保持零点", 0, Cultivation.get(player, CultivationValue.SKILL_POINTS));
                    expect("不足时不重复增加已分配数", 1, Cultivation.get(player, CultivationValue.SKILL_POINTS_HEALTH_MAX));
                    expect("不足时不增加生命成长", initialStats[0] + 4, PlayerAttributes.baseValue(player, Stat.HEALTH_MAX));
                });
                if (!screen(SkillPointScreen.class, "不足拒绝后仍在02")) return true;
                button("副职业").onPress();
            }
            case 5 -> {
                if (!screen(ProfessionScreen.class, "02到05")) return true;
                expectTrue("05客户端有36个玩家背包槽", minecraft.player.containerMenu instanceof ProfessionMenu
                        && minecraft.player.containerMenu.slots.size() == 36);
                onServer(player -> {
                    expectTrue("05服务端菜单与36槽", player.containerMenu instanceof ProfessionMenu && player.containerMenu.slots.size() == 36);
                    expect("05记页", 5, player.getData(PanelNavigation.PAGE).intValue());
                });
                for (Profession profession : Profession.values()) {
                    int value = profession.ordinal() + 1;
                    expect(profession + "等级同步到双列显示查询", value, Professions.level(minecraft.player, profession));
                    expect(profession + "经验同步到双列显示查询", value, Professions.experience(minecraft.player, profession));
                    expect(profession + "中文等级文本", profession.displayName() + "等级：" + value, Professions.levelText(minecraft.player, profession));
                    expect(profession + "经验文本", value + ".0/100.0", Professions.experienceText(minecraft.player, profession));
                }
                screenshots.take("panel-page-05.png");
                button("属性显示").onPress();
            }
            case 6 -> {
                if (!screen(AttributeScreen.class, "05到01")) return true;
                button("副职业").onPress();
            }
            case 7 -> {
                if (!screen(ProfessionScreen.class, "01到05")) return true;
                button("属性加点").onPress();
            }
            case 8 -> {
                if (!screen(SkillPointScreen.class, "05到02")) return true;
                button("修炼").onPress();
            }
            case 9 -> {
                if (!retreat(6, "02到06")) return true;
                expect("06初始设定天数", 0, Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum"));
                button("增加一天").onPress();
                button("增加十天").onPress();
                button("增加一百天").onPress();
            }
            case 10 -> {
                if (!retreat(6, "06日数增加后")) return true;
                onServer(player -> expect("06三个增日按钮结算", 111, Cultivation.tizhiDouble(player, "xiuliandaynum")));
                expect("06日数同步", 111, Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum"));
                screenshots.take("panel-page-06.png");
                button("减少一天").onPress();
                button("减少十天").onPress();
                button("减少一百天").onPress();
            }
            case 11 -> {
                if (!retreat(6, "06日数减少后")) return true;
                onServer(player -> expect("06三个减日按钮结算", 0, Cultivation.tizhiDouble(player, "xiuliandaynum")));
                expect("06减日同步", 0, Cultivation.tizhiDouble(minecraft.player, "xiuliandaynum"));
                button("观想").onPress();
            }
            case 12 -> {
                if (!retreat(7, "06到07")) return true;
                screenshots.take("panel-page-07.png");
                onServer(player -> {
                    Cultivation.set(player, CultivationValue.LIFETIME, 100);
                    Cultivation.set(player, CultivationValue.VIGOROUS, 5);
                    Cultivation.set(player, CultivationValue.POWER, 1);
                    Cultivation.set(player, CultivationValue.SOUL, 1);
                    Alcohol.add(player, 10 - Alcohol.amount(player));
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                    player.setHealth(1);
                });
                button("观想").onPress();
            }
            case 13 -> {
                expectTrue("07成功观想关闭菜单", minecraft.screen == null);
                onServer(player -> {
                    expect("07扣除三天寿元", 97, Cultivation.get(player, CultivationValue.LIFETIME));
                    expect("07罡气向零减少三点", 2, Cultivation.get(player, CultivationValue.VIGOROUS));
                    expect("07灵力恢复到上限", PlayerAttributes.value(player, Stat.POWER_MAX), Cultivation.get(player, CultivationValue.POWER));
                    expect("07精力恢复到上限", PlayerAttributes.value(player, Stat.SOUL_MAX), Cultivation.get(player, CultivationValue.SOUL));
                    expect("07恢复生命", player.getMaxHealth(), player.getHealth());
                    expect("07清除酒精", 0, Alcohol.amount(player));
                    expectTrue("07移除反胃", !player.hasEffect(MobEffects.CONFUSION));
                    expect("07保持主分页记忆为6", 6, player.getData(PanelNavigation.PAGE).intValue());
                });
                key();
            }
            case 14 -> {
                if (!retreat(6, "观想后K恢复06")) return true;
                expect("观想寿元同步", 97, Cultivation.get(minecraft.player, CultivationValue.LIFETIME));
                button("观想").onPress();
            }
            case 15 -> {
                if (!retreat(7, "06再次进入07")) return true;
                button("参悟功法").onPress();
            }
            case 16 -> {
                if (!retreat(8, "07到08")) return true;
                screenshots.take("panel-page-08.png");
                minecraft.player.closeContainer();
            }
            case 17 -> {
                expectTrue("08原生关闭消息完成", minecraft.screen == null);
                key();
            }
            case 18 -> {
                if (!retreat(6, "关闭08后K恢复06")) return true;
                button("副职业").onPress();
            }
            case 19 -> {
                if (!screen(ProfessionScreen.class, "06到05")) return true;
                button("修炼").onPress();
            }
            case 20 -> {
                if (!retreat(6, "05到06")) return true;
                onServer(this::restore);
                finish();
            }
            default -> throw new IllegalStateException("未知玩家分页检查步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        originalCultivation = player.getData(XiulianAttachments.CULTIVATION);
        originalAttributes = player.getData(AttributeAttachments.ATTRIBUTES);
        originalProfessions = player.getData(ProfessionAttachments.PROGRESS);
        originalEffects = player.getActiveEffects().stream().map(MobEffectInstance::new).toList();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalHealth = player.getHealth();
        originalNoGravity = player.isNoGravity();
        originalPage = player.getData(PanelNavigation.PAGE);
        originalAlcohol = Alcohol.amount(player);
        originalCollectionPresent = player.getPersistentData().contains("collection_LEVEL");
        originalCollection = player.getPersistentData().getDouble("collection_LEVEL");
        player.setData(XiulianAttachments.CULTIVATION, new CultivationData());
        player.setData(AttributeAttachments.ATTRIBUTES, AttributeData.EMPTY);
        VanillaAttributes.apply(player, AttributeData.EMPTY.totals());
        player.setData(ProfessionAttachments.PROGRESS, new ProfessionData());
        player.removeAllEffects();
        player.setNoGravity(true);
        player.teleportTo(originalPosition.x, 300, originalPosition.z);
        Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 10);
        Cultivation.set(player, CultivationValue.SKILL_POINTS, 6);
        Cultivation.set(player, CultivationValue.LIFETIME, 1000);
        for (Profession profession : Profession.values()) {
            Professions.setLevel(player, profession, profession.ordinal() + 1);
            Professions.setExperience(player, profession, profession.ordinal() + 1);
        }
        PanelNavigation.openAttributes(player);
    }

    private boolean screen(Class<?> type, String label) {
        boolean opened = type.isInstance(minecraft.screen);
        expectTrue(label + "真实客户端界面", opened);
        if (!opened) {
            onServer(this::restore);
            finish();
        }
        return opened;
    }

    private boolean retreat(int mode, String label) {
        if (!screen(RetreatScreen.class, label)) return false;
        boolean correct = minecraft.player.containerMenu instanceof RetreatMenu menu && menu.mode() == mode;
        expectTrue(label + "子页模式" + mode + "与36槽", correct && minecraft.player.containerMenu.slots.size() == 36);
        onServer(player -> expectTrue(label + "服务端模式与主分页记忆", player.containerMenu instanceof RetreatMenu menu
                && menu.mode() == mode && player.getData(PanelNavigation.PAGE) == 6));
        if (!correct) {
            onServer(this::restore);
            finish();
        }
        return correct;
    }

    private Button button(String name) {
        return minecraft.screen.children().stream()
                .filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow();
    }

    private void key() {
        var key = Arrays.stream(minecraft.options.keyMappings)
                .filter(mapping -> mapping.getName().equals("key.yvanchuxiuzhen.guicaidan")).findFirst().orElseThrow();
        key.setDown(true);
        key.setDown(false);
    }

    private void restore(ServerPlayer player) {
        player.closeContainer();
        player.setData(XiulianAttachments.CULTIVATION, originalCultivation);
        player.syncData(XiulianAttachments.CULTIVATION);
        player.setData(AttributeAttachments.ATTRIBUTES, originalAttributes);
        VanillaAttributes.apply(player, originalAttributes.totals());
        player.setData(ProfessionAttachments.PROGRESS, originalProfessions);
        player.syncData(ProfessionAttachments.PROGRESS);
        player.setData(PanelNavigation.PAGE, originalPage);
        Alcohol.add(player, originalAlcohol - Alcohol.amount(player));
        if (originalCollectionPresent) player.getPersistentData().putDouble("collection_LEVEL", originalCollection);
        else player.getPersistentData().remove("collection_LEVEL");
        player.removeAllEffects();
        originalEffects.forEach(player::addEffect);
        player.setNoGravity(originalNoGravity);
        player.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
        player.setYRot(originalYaw);
        player.setXRot(originalPitch);
        player.fallDistance = 0;
        player.setHealth(originalHealth);
    }
}
