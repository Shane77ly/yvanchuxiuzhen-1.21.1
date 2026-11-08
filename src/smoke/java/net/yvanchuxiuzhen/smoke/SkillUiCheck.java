package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryInventory;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.client.AccessoryScreen;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.client.SkillLoadoutScreen;
import net.yvanchuxiuzhen.system.skill.client.SkillScreen;
import net.yvanchuxiuzhen.system.skill.content.SkillLoadoutMenu;
import net.yvanchuxiuzhen.system.skill.content.SkillMenu;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 自然研习、装配页与选择页的客户端主路径、库存同步，以及格挡显示和自爆的死亡后延迟。 */
final class SkillUiCheck extends ActionCheck {
    private static final String CONTRIBUTION = "smoke:skill_ui";
    private static final String[] TIZHI = {"LevelNumberCorrect", "lagongtick", "zibaonumber", "zibaokouchu"};
    private static final CultivationValue[] VALUES = {CultivationValue.LEVEL_NUMBER, CultivationValue.POWER, CultivationValue.SOUL, CultivationValue.LIFETIME};
    private final double[] oldTizhi = new double[TIZHI.length];
    private final double[] oldValues = new double[VALUES.length];
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> learned = new ArrayList<>();
    private SkillState original;
    private SkillCooldowns cooldowns;
    private AccessoryInventory accessories;
    private ItemStack technique;
    private ItemStack studiedTechnique;
    private GameType gameMode;
    private SkillEntity guard;
    private BlockPos marker;
    private int panelPage;
    private int selectedSlot;
    private double answerCount;
    private double techniqueExperience;
    private boolean hadAnswerCount;
    private boolean shiftKey;
    private boolean sneaking;

    SkillUiCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("技能界面", report, levelId);
        script(
                new Action(10, p -> {
                    original = Skills.state(p);
                    cooldowns = p.getData(SkillData.COOLDOWNS);
                    accessories = Equipment.accessories(p);
                    technique = Techniques.slot(p).copy();
                    gameMode = p.gameMode.getGameModeForPlayer();
                    panelPage = p.getData(PanelNavigation.PAGE);
                    selectedSlot = p.getInventory().selected;
                    hadAnswerCount = p.getPersistentData().contains("daduishuliang");
                    answerCount = p.getPersistentData().getDouble("daduishuliang");
                    shiftKey = minecraft.options.keyShift.isDown();
                    sneaking = p.isShiftKeyDown();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    for (int i = 0; i < TIZHI.length; i++) oldTizhi[i] = Cultivation.tizhiDouble(p, TIZHI[i]);
                    for (int i = 0; i < VALUES.length; i++) oldValues[i] = Cultivation.get(p, VALUES[i]);
                    p.getInventory().clearContent();
                    Arena.level(p);
                    p.setData(SkillData.STATE, SkillState.EMPTY);
                    p.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    p.setData(EquipmentData.ACCESSORIES, new AccessoryData());
                    Techniques.setSlot(p, ItemStack.EMPTY);
                    p.setData(PanelNavigation.PAGE, 0);
                    PlayerAttributes.setContribution(p, CONTRIBUTION, new Contribution(Layer.BASE, Map.of(Stat.POWER_MAX, 1000.0, Stat.SOUL_MAX, 1000.0)));
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 0);
                    Cultivation.set(p, CultivationValue.POWER, 200);
                    Cultivation.set(p, CultivationValue.SOUL, 200);
                    Cultivation.setTizhiDouble(p, "LevelNumberCorrect", 0);
                    p.getInventory().setItem(0, book("book_006", SkillKind.SPELL, "火球术").copyWithCount(2));
                    p.getInventory().setItem(1, book("book_010", SkillKind.MOVEMENT, "检查身法"));
                    p.getInventory().setItem(2, book("book_023", SkillKind.FLIGHT, "检查轻功"));
                    p.getInventory().setItem(3, book("book_004", SkillKind.WEAPON_ART, "检查武器技"));
                    p.getInventory().setItem(4, book("book_005", SkillKind.SPELL, "水蛇鞭"));
                    p.getInventory().setItem(5, Techniques.createBook(new TechniqueDefinition(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_002"),
                            "检查功法", "黄阶一级", 1, "锻体期", 1, "无", "", Map.of(), Collections.nCopies(TechniqueStat.values().length, ""))));
                    p.getInventory().selected = 0;
                    p.getPersistentData().putDouble("daduishuliang", 5);
                    p.getInventory().add(new ItemStack(Items.ARROW, 8));
                    p.getInventory().add(new ItemStack(Items.SPECTRAL_ARROW));
                    for (int i = 1; i <= 9; i++) p.getInventory().add(item("skill_arrow_%02d".formatted(i)));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(8, p -> sneak(true)),
                new Action(8, p -> useBook(0)),
                new Action(8, p -> {
                    expectTrue("自然潜行右键在境界不足时不绑定、不消耗整栈且不清答题计数", Skills.book(p.getMainHandItem()).orElseThrow().owner().isEmpty()
                            && p.getMainHandItem().getCount() == 2 && p.getPersistentData().getDouble("daduishuliang") == 5 && inventorySynced(p));
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 11);
                    useBook(0);
                }),
                new Action(8, p -> {
                    expectTrue("自然潜行研习整栈绑定并清答题计数，客户端收到书组件", Skills.book(p.getMainHandItem()).orElseThrow().ownedBy(p.getUUID())
                            && p.getMainHandItem().getCount() == 2 && p.getPersistentData().getDouble("daduishuliang") == 0 && inventorySynced(p));
                    p.getPersistentData().putDouble("daduishuliang", 7);
                    useBook(0);
                }),
                new Action(8, p -> {
                    expectTrue("本人重复自然研习保留答题计数与整栈", p.getPersistentData().getDouble("daduishuliang") == 7
                            && p.getMainHandItem().getCount() == 2 && inventorySynced(p));
                    useBook(1);
                }),
                new Action(8, p -> useBook(2)),
                new Action(8, p -> useBook(3)),
                new Action(8, p -> useBook(4)),
                new Action(8, p -> useBook(5)),
                new Action(8, p -> {
                    boolean owned = true;
                    for (int slot = 0; slot < 5; slot++) {
                        ItemStack stack = p.getInventory().getItem(slot);
                        owned &= Skills.book(stack).orElseThrow().ownedBy(p.getUUID());
                        learned.add(stack.copy());
                    }
                    studiedTechnique = p.getInventory().getItem(5).copy();
                    expectTrue("四类技能书与功法均经自然入口研习并同步，后续成功清零答题计数", owned && inventorySynced(p)
                            && Techniques.book(studiedTechnique).orElseThrow().ownedBy(p.getUUID())
                            && p.getPersistentData().getDouble("daduishuliang") == 0);
                    sneak(false);
                }),
                new Action(8, p -> {
                    key("guicaidan", true);
                    key("guicaidan", false);
                }),
                new Action(8, p -> {
                    expectTrue("K键打开真实01属性页", p.containerMenu instanceof PlayerPanelMenu && minecraft.screen instanceof AttributeScreen
                            && p.getData(PanelNavigation.PAGE) == 1);
                    click("功法与技能");
                }),
                new Action(8, p -> {
                    expectTrue("01导航到真实04装配页与八槽加36格背包", p.containerMenu instanceof SkillLoadoutMenu
                            && minecraft.screen instanceof SkillLoadoutScreen && p.containerMenu.slots.size() == 44 && p.getData(PanelNavigation.PAGE) == 4);
                    minecraft.execute(() -> {
                        screenshots.take("skill-loadout");
                        equipOneSpell();
                        for (int slot = 36; slot <= 38; slot++) {
                            minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
                        }
                        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 40, 0, ClickType.QUICK_MOVE, minecraft.player);
                    });
                }),
                new Action(8, p -> {
                    expectTrue("普通左右点击装入一本技能，Shift搬入功法与三类槽，双端库存与鼠标余件一致", syncedSlot(p, 3, learned.get(0).copyWithCount(1))
                            && syncedSlot(p, 0, studiedTechnique)
                            && syncedSlot(p, 1, learned.get(1)) && syncedSlot(p, 2, learned.get(2)) && syncedSlot(p, 7, learned.get(3))
                            && p.getInventory().getItem(0).getCount() == 1 && p.getInventory().getItem(1).isEmpty()
                            && p.getInventory().getItem(2).isEmpty() && p.getInventory().getItem(3).isEmpty() && p.getInventory().getItem(5).isEmpty() && inventorySynced(p)
                            && p.containerMenu.getCarried().isEmpty() && minecraft.player.containerMenu.getCarried().isEmpty()
                            && Skills.spell(p, 1).isEmpty() && Skills.movement(p).isEmpty());
                    click("下一页技能");
                }),
                new Action(8, p -> {
                    expectTrue("04翻页保存第一页技能并同步，第二页装配槽为空", Skills.state(p).page() == 2 && Skills.state(minecraft.player).page() == 2
                            && ItemStack.matches(Skills.spell(p, 1), learned.get(0).copyWithCount(1))
                            && ItemStack.matches(Skills.spell(p, 1), Skills.spell(minecraft.player, 1))
                            && syncedSlot(p, 3, ItemStack.EMPTY) && syncedSlot(p, 4, ItemStack.EMPTY)
                            && syncedSlot(p, 5, ItemStack.EMPTY) && syncedSlot(p, 6, ItemStack.EMPTY));
                    minecraft.execute(() -> minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 39, 0, ClickType.QUICK_MOVE, minecraft.player));
                }),
                new Action(8, p -> {
                    expectTrue("第二页Shift装入另一技能并从双端背包移除", syncedSlot(p, 3, learned.get(4))
                            && p.getInventory().getItem(4).isEmpty() && inventorySynced(p));
                    click("上一页技能");
                }),
                new Action(8, p -> {
                    expectTrue("04翻回第一页恢复原书，第二页保存到第五技能槽", Skills.state(p).page() == 1 && Skills.state(minecraft.player).page() == 1
                            && syncedSlot(p, 3, learned.get(0).copyWithCount(1)) && ItemStack.matches(Skills.spell(p, 5), learned.get(4))
                            && ItemStack.matches(Skills.spell(p, 5), Skills.spell(minecraft.player, 5)));
                    minecraft.execute(() -> minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 3, 0, ClickType.QUICK_MOVE, minecraft.player));
                }),
                new Action(8, p -> {
                    expectTrue("04技能Shift取出与背包余本合并，双端不复制物品", syncedSlot(p, 3, ItemStack.EMPTY)
                            && ItemStack.matches(p.getInventory().getItem(0), learned.get(0)) && inventorySynced(p));
                    minecraft.execute(this::equipOneSpell);
                }),
                new Action(8, p -> {
                    expectTrue("再次普通装入一本后鼠标清空且保留背包余本", syncedSlot(p, 3, learned.get(0).copyWithCount(1))
                            && p.getInventory().getItem(0).getCount() == 1 && inventorySynced(p)
                            && p.containerMenu.getCarried().isEmpty() && minecraft.player.containerMenu.getCarried().isEmpty());
                    minecraft.execute(() -> minecraft.player.closeContainer());
                }),
                new Action(8, p -> {
                    expectTrue("关闭04把功法与四类装配交回所有者并同步，跨页技能与背包余本均保留", ItemStack.matches(Skills.spell(p, 1), learned.get(0).copyWithCount(1))
                            && ItemStack.matches(Techniques.slot(p), studiedTechnique) && ItemStack.matches(Techniques.slot(p), Techniques.slot(minecraft.player))
                            && ItemStack.matches(Skills.spell(p, 5), learned.get(4)) && ItemStack.matches(Skills.movement(p), learned.get(1))
                            && ItemStack.matches(Skills.flight(p), learned.get(2)) && ItemStack.matches(Skills.weaponArt(p), learned.get(3))
                            && ItemStack.matches(Skills.movement(p), Skills.movement(minecraft.player)) && ItemStack.matches(Skills.flight(p), Skills.flight(minecraft.player))
                            && ItemStack.matches(Skills.weaponArt(p), Skills.weaponArt(minecraft.player)) && p.getInventory().getItem(0).getCount() == 1 && inventorySynced(p));
                    key("guicaidan", true);
                    key("guicaidan", false);
                }),
                new Action(8, p -> {
                    expectTrue("K记忆04并重开载入保存的功法与四类单件槽", p.containerMenu instanceof SkillLoadoutMenu && minecraft.screen instanceof SkillLoadoutScreen
                            && p.getData(PanelNavigation.PAGE) == 4 && syncedSlot(p, 3, learned.get(0).copyWithCount(1))
                            && syncedSlot(p, 0, studiedTechnique)
                            && syncedSlot(p, 1, learned.get(1)) && syncedSlot(p, 2, learned.get(2)) && syncedSlot(p, 7, learned.get(3)));
                    techniqueExperience = Techniques.main(p).orElseThrow().exp();
                    NeoForge.EVENT_BUS.post(new CultivationExperienceEvent(p, 1));
                }),
                new Action(8, p -> {
                    expectTrue("04打开期间真实修炼事件增长功法，菜单原栈与本人组件同步", Techniques.main(p).orElseThrow().exp() == techniqueExperience + 1.5
                            && Techniques.main(minecraft.player).orElseThrow().exp() == techniqueExperience + 1.5
                            && syncedSlot(p, 0, Techniques.slot(p)));
                    click("饰品装备");
                }),
                new Action(8, p -> {
                    expectTrue("04关闭导航到03时保留功法成长并记忆当前页", p.containerMenu instanceof AccessoryMenu && minecraft.screen instanceof AccessoryScreen
                            && p.getData(PanelNavigation.PAGE) == 3 && Techniques.main(p).orElseThrow().exp() == techniqueExperience + 1.5
                            && ItemStack.matches(Techniques.slot(p), Techniques.slot(minecraft.player)));
                    minecraft.execute(() -> minecraft.player.closeContainer());
                }),
                new Action(8, p -> {
                    key("guicaidan", true);
                    key("guicaidan", false);
                }),
                new Action(8, p -> {
                    expectTrue("K记忆03后可经真实按钮返回04", p.containerMenu instanceof AccessoryMenu && minecraft.screen instanceof AccessoryScreen
                            && p.getData(PanelNavigation.PAGE) == 3);
                    click("功法与技能");
                }),
                new Action(8, p -> {
                    expectTrue("03返回04保留装配、功法成长与跨页技能", p.containerMenu instanceof SkillLoadoutMenu && minecraft.screen instanceof SkillLoadoutScreen
                            && syncedSlot(p, 3, learned.get(0).copyWithCount(1)) && ItemStack.matches(Skills.spell(p, 5), learned.get(4))
                            && Techniques.book(p.containerMenu.slots.get(0).getItem()).orElseThrow().exp() == techniqueExperience + 1.5
                            && syncedSlot(p, 0, Techniques.slot(p)));
                    click("属性显示");
                }),
                new Action(8, p -> {
                    expectTrue("04导航返回01属性页", p.containerMenu instanceof PlayerPanelMenu && minecraft.screen instanceof AttributeScreen
                            && p.getData(PanelNavigation.PAGE) == 1);
                    minecraft.execute(() -> minecraft.player.closeContainer());
                }),
                new Action(8, p -> {
                    key("guicaidan", true);
                    key("guicaidan", false);
                }),
                new Action(8, p -> {
                    expectTrue("K记忆01且导航后库存和第一技能保持双端一致", p.containerMenu instanceof PlayerPanelMenu && minecraft.screen instanceof AttributeScreen
                            && p.getData(PanelNavigation.PAGE) == 1 && inventorySynced(p)
                            && ItemStack.matches(Skills.spell(p, 1), Skills.spell(minecraft.player, 1)));
                    minecraft.execute(() -> minecraft.player.closeContainer());
                }),
                new Action(8, p -> {
                    // 属性页关闭后另起一步发按键，让客户端完成界面关闭与网络同步。
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> {
                    expectTrue("G键经网络打开真实无槽技能界面", p.containerMenu instanceof SkillMenu && minecraft.screen instanceof SkillScreen && p.containerMenu.slots.isEmpty());
                    click("上一页");
                    click("连招");
                }),
                new Action(8, p -> {
                    expectTrue("翻页和连招按钮写入服务端并同步本人", Skills.state(p).page() == 2 && Skills.state(minecraft.player).page() == 2 && Skills.state(p).combo());
                    click("下一页");
                    click("技能 1");
                }),
                new Action(8, p -> {
                    expectTrue("技能按钮选择当前页并关闭菜单", Skills.state(p).selectedSlot() == 1 && Skills.state(minecraft.player).selectedSlot() == 1
                            && ItemStack.matches(Skills.selected(p), learned.get(0).copyWithCount(1))
                            && ItemStack.matches(Skills.selected(p), Skills.selected(minecraft.player)) && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> {
                    minecraft.execute(() -> screenshots.take("skill-menu"));
                    click("箭种 2");
                }),
                new Action(8, p -> {
                    expectTrue("轮盘按钮选择普通箭并同步本人", Skills.arrowChoice(p) == 2 && Skills.arrowChoice(minecraft.player) == 2 && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> click("召唤丹火")),
                new Action(8, p -> {
                    ItemStack fire = p.getInventory().items.stream().filter(s -> s.is(item("danhuo").getItem())).findFirst().orElseThrow();
                    expectTrue("丹火按钮扣费并给出本人和火力数据", Cultivation.get(p, CultivationValue.POWER) == 178
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getDouble("power") == 22
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getString("ManaAttackUUID").equals(p.getStringUUID()) && !(p.containerMenu instanceof SkillMenu));
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(8, p -> click("神识扫描")),
                new Action(8, p -> {
                    expectTrue("扫描按钮扣除神识并关闭菜单", Cultivation.get(p, CultivationValue.SOUL) == 178 && !(p.containerMenu instanceof SkillMenu));
                    p.setData(SkillData.COOLDOWNS, new SkillCooldowns(List.of(200.0, 400.0, 600.0, 800.0, 1000.0, 1200.0)));
                    Cultivation.setTizhiDouble(p, "lagongtick", 85);
                    p.getPersistentData().putDouble("canjianjishicolddown", 0);
                    key("canjian", true);
                    guard = SkillSetup.<SkillEntity>entityType("gedang").get().create(p.serverLevel());
                    guard.setPos(p.getX() + 2, p.getY(), p.getZ());
                    p.serverLevel().addFreshEntity(guard);
                }),
                new Action(12, p -> {
                    expectTrue("C按下的格挡计时同步给本人", Skills.guardRemaining(p) > 0 && Skills.guardRemaining(minecraft.player) > 0);
                    minecraft.execute(() -> screenshots.take("skill-hud"));
                    key("canjian", false);
                }),
                new Action(8, p -> {
                    expectTrue("C松开结束显示，格挡实体到八刻移除", Skills.guardRemaining(p) == 0 && Skills.guardRemaining(minecraft.player) == 0 && guard.isRemoved());
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 21);
                    Cultivation.set(p, CultivationValue.LIFETIME, 10000);
                    Cultivation.setTizhiDouble(p, "zibaonumber", 0);
                    Cultivation.setTizhiDouble(p, "zibaokouchu", 0);
                    p.setGameMode(GameType.SURVIVAL);
                    marker = p.blockPosition().offset(2, 0, 0);
                    p.serverLevel().setBlock(marker, Blocks.STONE.defaultBlockState(), 3);
                    key("ganjian", true);
                    key("ganjian", false);
                }),
                new Action(50, p -> click("自爆")),
                new Action(100, p -> {
                    expectTrue("自爆先死亡并登记寿元代价，死亡后仍执行延迟爆炸", !p.isAlive() && Cultivation.tizhiDouble(p, "zibaonumber") == 1
                            && Cultivation.tizhiDouble(p, "zibaokouchu") == 3650 && Cultivation.get(p, CultivationValue.LIFETIME) <= 6350
                            && p.serverLevel().isEmptyBlock(marker));
                    minecraft.execute(() -> minecraft.player.respawn());
                })
        );
    }

    private static ItemStack item(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    private static ItemStack book(String id, SkillKind kind, String name) {
        ItemStack stack = item(id);
        stack.set(SkillData.SKILL.get(), new SkillBook(kind, name, 1, 1, "无", "", Optional.empty(), "",
                kind == SkillKind.SPELL ? Map.of("Firecolddown", 2.0) : Map.of(), Map.of()));
        return stack;
    }

    private void sneak(boolean down) {
        minecraft.execute(() -> {
            if (minecraft.options.keyShift.isDown() != down) {
                minecraft.options.keyShift.setDown(down || minecraft.options.toggleCrouch().get());
            }
        });
    }

    private void useBook(int hotbar) {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = hotbar;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        });
    }

    /** 客户端普通点击拿起整栈、右键放入一本，再把鼠标余本放回原热栏。 */
    private void equipOneSpell() {
        int id = minecraft.player.containerMenu.containerId;
        minecraft.gameMode.handleInventoryMouseClick(id, 35, 0, ClickType.PICKUP, minecraft.player);
        minecraft.gameMode.handleInventoryMouseClick(id, 3, 1, ClickType.PICKUP, minecraft.player);
        minecraft.gameMode.handleInventoryMouseClick(id, 35, 0, ClickType.PICKUP, minecraft.player);
    }

    private boolean syncedSlot(ServerPlayer player, int slot, ItemStack expected) {
        return ItemStack.matches(expected, player.containerMenu.slots.get(slot).getItem())
                && ItemStack.matches(expected, minecraft.player.containerMenu.slots.get(slot).getItem());
    }

    private boolean inventorySynced(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (!ItemStack.matches(player.getInventory().getItem(slot), minecraft.player.getInventory().getItem(slot))) return false;
        }
        return true;
    }

    private void key(String name, boolean down) {
        minecraft.execute(() -> Arrays.stream(minecraft.options.keyMappings).filter(k -> k.getName().equals("key.yvanchuxiuzhen." + name))
                .findFirst().orElseThrow().setDown(down));
    }

    private void click(String name) {
        minecraft.execute(() -> minecraft.screen.children().stream().filter(child -> child instanceof Button button && button.getMessage().getString().equals(name))
                .map(child -> (Button) child).findFirst().orElseThrow().onPress());
    }

    @Override
    protected void cleanup(ServerPlayer p) {
        p.closeContainer();
        p.setGameMode(gameMode);
        p.getInventory().clearContent();
        for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        p.getInventory().setChanged();
        p.setData(SkillData.STATE, original);
        p.setData(SkillData.COOLDOWNS, cooldowns);
        p.setData(EquipmentData.ACCESSORIES, new AccessoryData(accessories));
        p.setData(PanelNavigation.PAGE, panelPage);
        Techniques.setSlot(p, technique);
        p.getInventory().selected = selectedSlot;
        p.setShiftKeyDown(sneaking);
        if (hadAnswerCount) p.getPersistentData().putDouble("daduishuliang", answerCount);
        else p.getPersistentData().remove("daduishuliang");
        minecraft.execute(() -> minecraft.player.getInventory().selected = selectedSlot);
        sneak(shiftKey);
        PlayerAttributes.clearContribution(p, CONTRIBUTION);
        for (int i = 0; i < VALUES.length; i++) Cultivation.set(p, VALUES[i], oldValues[i]);
        for (int i = 0; i < TIZHI.length; i++) Cultivation.setTizhiDouble(p, TIZHI[i], oldTizhi[i]);
        p.getPersistentData().remove("canjianjishi");
        p.getPersistentData().remove("canjianjishicolddown");
        if (guard != null && !guard.isRemoved()) guard.discard();
        p.inventoryMenu.broadcastChanges();
    }
}
