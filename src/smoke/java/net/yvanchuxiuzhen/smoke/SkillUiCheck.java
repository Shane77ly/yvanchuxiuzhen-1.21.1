package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.panel.PanelNavigation;
import net.yvanchuxiuzhen.panel.PlayerPanelMenu;
import net.yvanchuxiuzhen.panel.client.AttributeScreen;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.smoke.ActionCheck.Action;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryInventory;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;
import net.yvanchuxiuzhen.system.equipment.client.AccessoryScreen;
import net.yvanchuxiuzhen.system.equipment.content.AccessoryMenu;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.relation.api.Relations;
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
import net.yvanchuxiuzhen.system.yihuo.FireBlockEntity;
import net.yvanchuxiuzhen.system.yihuo.api.FireHeatEvent;

/** 自然研习与藏宝、装配与选择、丹火与格挡，以及装配页死亡和关闭后重登的真实客户端链。 */
final class SkillUiCheck extends SystemCheck {
    private static final String CONTRIBUTION = "smoke:skill_ui";
    private static final String[] TIZHI = {"LevelNumberCorrect", "lagongtick", "zibaonumber", "zibaokouchu"};
    private static final CultivationValue[] VALUES = {CultivationValue.LEVEL_NUMBER, CultivationValue.POWER, CultivationValue.SOUL, CultivationValue.LIFETIME};
    private final double[] oldTizhi = new double[TIZHI.length];
    private final double[] oldValues = new double[VALUES.length];
    private final List<ItemStack> inventory = new ArrayList<>();
    private final List<ItemStack> learned = new ArrayList<>();
    private final List<ItemStack> fireHotbar = new ArrayList<>();
    private final List<Cow> fireTargets = new ArrayList<>();
    private final List<Long> heatTicks = new ArrayList<>();
    private final List<ItemStack> lifecycleBooks = new ArrayList<>();
    private ItemStack pageCycleRemainder;
    private ItemStack pageThreeBook;
    private ItemStack pageFourBook;
    private ActionCheck actions;
    private BlockPos loadoutDeathPos;
    private SkillState original;
    private SkillCooldowns cooldowns;
    private AccessoryInventory accessories;
    private ItemStack technique;
    private ItemStack studiedTechnique;
    private ItemStack treasureHotbar;
    private GameType gameMode;
    private SkillEntity guard;
    private BlockPos marker;
    private ServerLevel fireLevel;
    private BlockPos firePos;
    private BlockPos foreignFirePos;
    private BlockPos vanillaFirePos;
    private BlockPos campfirePos;
    private Cow fireYang;
    private Cow fireYin;
    private Cow fireMissingSource;
    private int panelPage;
    private int selectedSlot;
    private int fireSelectedSlot;
    private int treasureSelectedSlot;
    private int[] treasureBefore;
    private double answerCount;
    private double techniqueExperience;
    private double heatAmount;
    private boolean hadAnswerCount;
    private boolean shiftKey;
    private boolean sneaking;
    private boolean foreignFireRejected;
    private boolean unitHeat = true;
    private boolean treasureValid;
    private String treasureResult;
    private final Consumer<FireHeatEvent> fireHeat = event -> {
        if (event.level() == fireLevel && event.pos().equals(firePos.above())) {
            heatTicks.add(event.level().getGameTime());
            heatAmount += event.amount();
            unitHeat &= event.amount() == 1;
        }
    };

    SkillUiCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("技能界面", report, levelId);
        script(report, levelId,
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
                    p.closeContainer();
                    Skills.openLoadout(p);
                    p.closeContainer();
                    Skills.openLoadout(p);
                }),
                new Action(8, p -> {
                    expectTrue("04第二次关闭发生在两刻初始化前，来源关闭过程将未装入空槽写回当前页与单件装配", p.containerMenu instanceof SkillLoadoutMenu
                            && minecraft.screen instanceof SkillLoadoutScreen && Techniques.slot(p).isEmpty()
                            && Skills.spell(p, 1).isEmpty() && Skills.movement(p).isEmpty() && Skills.flight(p).isEmpty()
                            && Skills.weaponArt(p).isEmpty());
                    Techniques.setSlot(p, studiedTechnique.copy());
                    Skills.setSpell(p, 1, learned.get(0).copyWithCount(1));
                    Skills.setMovement(p, learned.get(1).copy());
                    Skills.setFlight(p, learned.get(2).copy());
                    Skills.setWeaponArt(p, learned.get(3).copy());
                    ((SkillLoadoutMenu) p.containerMenu).initialize(p);
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
                    pageCycleRemainder = p.getInventory().getItem(0).copy();
                    pageThreeBook = markedPageBook(3);
                    p.getInventory().setItem(0, pageThreeBook.copy());
                    p.inventoryMenu.broadcastChanges();
                    click("下一页技能");
                }),
                new Action(8, p -> {
                    expectTrue("真实按钮进入第三技能页并载入空槽", Skills.state(p).page() == 3 && Skills.state(minecraft.player).page() == 3
                            && syncedSlot(p, 3, ItemStack.EMPTY) && inventorySynced(p));
                    minecraft.execute(this::equipOneSpell);
                }),
                new Action(8, p -> {
                    expectTrue("第三页经真实普通点击装入独立标记书", syncedSlot(p, 3, pageThreeBook.copyWithCount(1))
                            && ItemStack.matches(p.getInventory().getItem(0), pageThreeBook.copyWithCount(1)) && inventorySynced(p));
                    pageFourBook = markedPageBook(4);
                    p.getInventory().setItem(0, pageFourBook.copy());
                    p.inventoryMenu.broadcastChanges();
                    click("下一页技能");
                }),
                new Action(8, p -> {
                    expectTrue("真实按钮进入第四技能页并载入空槽", Skills.state(p).page() == 4 && Skills.state(minecraft.player).page() == 4
                            && syncedSlot(p, 3, ItemStack.EMPTY) && inventorySynced(p));
                    minecraft.execute(this::equipOneSpell);
                }),
                new Action(8, p -> {
                    expectTrue("第四页经真实普通点击装入独立标记书", syncedSlot(p, 3, pageFourBook.copyWithCount(1))
                            && ItemStack.matches(p.getInventory().getItem(0), pageFourBook.copyWithCount(1)) && inventorySynced(p));
                    click("下一页技能");
                }),
                new Action(8, p -> {
                    boolean allPagesSaved = Skills.state(p).page() == 1 && Skills.state(minecraft.player).page() == 1
                            && ItemStack.matches(Skills.spell(p, 1), learned.get(0).copyWithCount(1))
                            && ItemStack.matches(Skills.spell(minecraft.player, 1), Skills.spell(p, 1))
                            && ItemStack.matches(Skills.spell(p, 5), learned.get(4))
                            && ItemStack.matches(Skills.spell(minecraft.player, 5), Skills.spell(p, 5))
                            && ItemStack.matches(Skills.spell(p, 9), pageThreeBook.copyWithCount(1))
                            && ItemStack.matches(Skills.spell(minecraft.player, 9), Skills.spell(p, 9))
                            && ItemStack.matches(Skills.spell(p, 13), pageFourBook.copyWithCount(1))
                            && ItemStack.matches(Skills.spell(minecraft.player, 13), Skills.spell(p, 13))
                            && syncedSlot(p, 3, learned.get(0).copyWithCount(1));
                    expectTrue("真实按钮完成1到2到3到4到1循环，四页技能分别保存至01/05/09/13并双端一致", allPagesSaved);
                    p.getInventory().setItem(0, pageCycleRemainder.copy());
                    p.inventoryMenu.broadcastChanges();
                    minecraft.execute(() -> {
                        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, 3, 0, ClickType.QUICK_MOVE, minecraft.player);
                    });
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
                    expectTrue("关闭04把功法与四类装配交回所有者并同步，四页技能及背包余本均保留", ItemStack.matches(Skills.spell(p, 1), learned.get(0).copyWithCount(1))
                            && ItemStack.matches(Techniques.slot(p), studiedTechnique) && ItemStack.matches(Techniques.slot(p), Techniques.slot(minecraft.player))
                            && ItemStack.matches(Skills.spell(p, 5), learned.get(4)) && ItemStack.matches(Skills.movement(p), learned.get(1))
                            && ItemStack.matches(Skills.spell(p, 9), pageThreeBook.copyWithCount(1)) && ItemStack.matches(Skills.spell(p, 13), pageFourBook.copyWithCount(1))
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
                    expectTrue("丹火按钮扣费并给出本人和火力数据" + " 实际灵力=" + Cultivation.get(p, CultivationValue.POWER)
                                    + "，数量=" + fire.getCount() + "，物品数据=" + fire.get(DataComponents.CUSTOM_DATA).copyTag(), Cultivation.get(p, CultivationValue.POWER) == 178
                            && fire.getCount() == 1 && fire.getMaxStackSize() == 1 && inventorySynced(p)
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getDouble("power") == 22
                            && fire.get(DataComponents.CUSTOM_DATA).copyTag().getString("ManaAttackUUID").equals(p.getStringUUID()) && !(p.containerMenu instanceof SkillMenu));
                    fireSelectedSlot = p.getInventory().selected;
                    p.getInventory().removeItemNoUpdate(p.getInventory().items.indexOf(fire));
                    for (int slot = 4; slot <= 7; slot++) fireHotbar.add(p.getInventory().getItem(slot).copy());
                    p.getInventory().setItem(4, fire);
                    ItemStack foreign = fire.copy();
                    CompoundTag tags = foreign.get(DataComponents.CUSTOM_DATA).copyTag();
                    tags.putString("ManaAttackUUID", "00000000-0000-0000-0000-000000000003");
                    tags.putDouble("power", 7);
                    foreign.set(DataComponents.CUSTOM_DATA, CustomData.of(tags));
                    p.getInventory().setItem(5, foreign);
                    p.getInventory().setItem(6, fire.copy());
                    p.getInventory().setItem(7, fire.copy());
                    fireLevel = p.serverLevel();
                    firePos = p.blockPosition().offset(2, 0, 0);
                    foreignFirePos = firePos.offset(0, 0, 1);
                    vanillaFirePos = p.blockPosition().offset(0, 0, 2);
                    campfirePos = p.blockPosition().offset(-2, 0, 0);
                    fireLevel.setBlockAndUpdate(vanillaFirePos, Blocks.FIRE.defaultBlockState());
                    fireLevel.setBlockAndUpdate(campfirePos, Blocks.CAMPFIRE.defaultBlockState());
                    NeoForge.EVENT_BUS.addListener(fireHeat);
                    p.setGameMode(GameType.SURVIVAL);
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(4, p -> minecraft.execute(() -> useFire(4, firePos.below()))),
                new Action(4, p -> {
                    BlockEntity serverFire = fireLevel.getBlockEntity(firePos);
                    BlockEntity clientFire = minecraft.level.getBlockEntity(firePos);
                    CompoundTag data = serverFire == null ? new CompoundTag() : serverFire.getPersistentData();
                    CompoundTag clientData = clientFire == null ? new CompoundTag() : clientFire.getPersistentData();
                    boolean spent = p.getInventory().getItem(4).isEmpty() && minecraft.player.getInventory().getItem(4).isEmpty();
                    expectTrue("本人丹火经客户端放置并同步燃料与身份 实际燃料=" + fuel(serverFire) + "/" + fuel(clientFire)
                                    + "，累计供热=" + heatAmount + "，已消费=" + spent + "，UUID=" + data.getString("ManaAttackUUID") + "/" + clientData.getString("ManaAttackUUID")
                                    + "，团队=" + data.getString("team") + "/" + clientData.getString("team"),
                            serverFire instanceof FireBlockEntity && fuel(serverFire) + heatAmount == 10 && fuel(clientFire) == fuel(serverFire)
                                    && spent && data.getString("ManaAttackUUID").equals(p.getStringUUID()) && data.getString("team").equals(Relations.entityTeam(p))
                                    && clientData.getString("ManaAttackUUID").equals(p.getStringUUID()) && clientData.getString("team").equals(Relations.entityTeam(p)));
                    minecraft.execute(() -> useFire(5, foreignFirePos.below()));
                }),
                new Action(4, p -> {
                    foreignFireRejected = fireLevel.isEmptyBlock(foreignFirePos) && p.getInventory().getItem(5).getCount() == 1
                            && minecraft.player.getInventory().getItem(5).getCount() == 1;
                    minecraft.execute(() -> {
                        useFire(5, firePos);
                        useFire(6, vanillaFirePos);
                        useFire(7, campfirePos);
                    });
                }),
                new Action(44, p -> {
                    boolean spent = true;
                    for (int slot = 5; slot <= 7; slot++) spent &= p.getInventory().getItem(slot).isEmpty() && minecraft.player.getInventory().getItem(slot).isEmpty();
                    double fireFuel = fuel(fireLevel.getBlockEntity(firePos));
                    double clientFuel = fuel(minecraft.level.getBlockEntity(firePos));
                    double campFuel = fuel(fireLevel.getBlockEntity(campfirePos));
                    expectTrue("异主拒绝新放但可补料，普通火与营火消费并同步 实际拒绝=" + foreignFireRejected + "，已消费=" + spent
                                    + "，燃料=" + fireFuel + "/" + clientFuel + "，累计供热=" + heatAmount + "，营火=" + campFuel,
                            foreignFireRejected && spent && inventorySynced(p) && fireFuel + heatAmount == 17 && clientFuel == fireFuel && campFuel == 22);
                    fireLevel.removeBlock(vanillaFirePos, false);
                    fireLevel.removeBlock(campfirePos, false);
                }),
                new Action(4, p -> {
                    boolean regular = heatTicks.size() >= 2;
                    for (int i = 1; i < heatTicks.size(); i++) regular &= heatTicks.get(i) - heatTicks.get(i - 1) == 20;
                    double fireFuel = fuel(fireLevel.getBlockEntity(firePos));
                    expectTrue("自然计划刻每二十刻扣一燃料并提交等量热量 实际游戏刻=" + heatTicks + "，热量=" + heatAmount + "，燃料=" + fireFuel,
                            regular && unitHeat && heatAmount == heatTicks.size() && fireFuel + heatAmount == 17);
                    NeoForge.EVENT_BUS.unregister(fireHeat);
                    fireLevel.removeBlock(firePos, false);
                    fireYang = fireCow(firePos.offset(4, 0, 0));
                    fireYin = fireCow(firePos.offset(4, 0, 4));
                    fireMissingSource = fireCow(firePos.offset(-4, 0, 4));
                    burn(fireYang, "effect_023", 1, "yanghuotick", p.getStringUUID());
                    burn(fireYin, "effect_024", 0, "yinhuotick", p.getStringUUID());
                    burn(fireMissingSource, "effect_023", 0, "yanghuotick", "");
                }),
                new Action(4, p -> {
                    double yangDamage = 500 - fireYang.getHealth();
                    double yinDamage = 500 - fireYin.getHealth();
                    double missingDamage = 500 - fireMissingSource.getHealth();
                    expectTrue("自然效果刻阳火十六阴火八，阳火清无敌且空来源无伤害 实际生命差=" + yangDamage + "/" + yinDamage + "/" + missingDamage
                                    + "，无敌时间=" + fireYang.invulnerableTime + "/" + fireYin.invulnerableTime,
                            yangDamage == 16 && yinDamage == 8 && missingDamage == 0 && fireYang.invulnerableTime == 0 && fireYin.invulnerableTime > 0);
                    for (Cow target : fireTargets) {
                        StatusEffects.remove(target, "effect_023");
                        StatusEffects.remove(target, "effect_024");
                        target.setHealth(500);
                        target.invulnerableTime = 0;
                        target.setDeltaMovement(Vec3.ZERO);
                        target.getRandom().setSeed(1);
                    }
                    fireYin.setPos(fireYang.position().add(1, 0, 0));
                    Relations.setTeam(fireYin, "smoke_fire_neighbor");
                    Relations.addFriendlyTeam(fireYang, "smoke_fire_neighbor");
                    StatusEffects.add(fireYang, "effect_024", 200, 2);
                    fireYang.getPersistentData().putDouble("yinhuotick", 0);
                    burn(fireYang, "effect_023", 1, "yanghuotick", fireMissingSource.getStringUUID());
                }),
                new Action(8, p -> {
                    double yangDamage = 500 - fireYang.getHealth();
                    double neighborDamage = 500 - fireYin.getHealth();
                    boolean removed = !StatusEffects.has(fireYang, "effect_023") && !StatusEffects.has(fireYang, "effect_024");
                    expectTrue("阴阳爆燃波及受燃者友方Mob并移除双效果 实际生命差=" + yangDamage + "/" + neighborDamage + "，双效果已移除=" + removed,
                            yangDamage == 50 && neighborDamage == 50 && removed);
                    clearFireScene(p);
                    treasureHotbar = p.getInventory().getItem(8).copy();
                    treasureSelectedSlot = p.getInventory().selected;
                    stageTreasure(p, "技能书", 0);
                }),
                new Action(8, p -> useBook(8)),
                new Action(8, p -> {
                    int[] delta = treasureCounts(p);
                    Arrays.setAll(delta, i -> delta[i] - treasureBefore[i]);
                    int left = p.getInventory().getItem(8).getCount();
                    boolean synced = inventorySynced(p);
                    treasureValid = left == 1 && Arrays.equals(delta, new int[]{-1, 0, 0}) && synced;
                    treasureResult = "技能零级=" + Arrays.toString(delta) + "，余量=" + left + "，同步=" + synced;
                    stageTreasure(p, "技能书", 1);
                }),
                new Action(8, p -> useBook(8)),
                new Action(8, p -> {
                    int[] delta = treasureCounts(p);
                    Arrays.setAll(delta, i -> delta[i] - treasureBefore[i]);
                    int left = p.getInventory().getItem(8).getCount();
                    boolean synced = inventorySynced(p);
                    treasureValid &= left == 1 && Arrays.equals(delta, new int[]{0, 1, 0}) && synced;
                    treasureResult += "；技能一级=" + Arrays.toString(delta) + "，余量=" + left + "，同步=" + synced;
                    stageTreasure(p, "功法书", 1);
                }),
                new Action(8, p -> useBook(8)),
                new Action(8, p -> {
                    int[] delta = treasureCounts(p);
                    Arrays.setAll(delta, i -> delta[i] - treasureBefore[i]);
                    int left = p.getInventory().getItem(8).getCount();
                    boolean synced = inventorySynced(p);
                    expectTrue("藏宝自然右键各消费一本，技能零级无奖励、一级只新增一本未绑定书、功法一级只新增一本未研习功法 实际总量/未绑定技能/未研习功法变化："
                                    + treasureResult + "；功法一级=" + Arrays.toString(delta) + "，余量=" + left + "，同步=" + synced,
                            treasureValid && left == 1 && Arrays.equals(delta, new int[]{0, 0, 1}) && synced);
                    p.getInventory().setItem(8, treasureHotbar);
                    p.getInventory().selected = treasureSelectedSlot;
                    p.getInventory().setChanged();
                    p.inventoryMenu.broadcastChanges();
                    minecraft.execute(() -> minecraft.player.getInventory().selected = treasureSelectedSlot);
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

    /** 原有动作序列结束后保留前置快照，由外层生命周期检查统一还原。 */
    private void script(List<String> report, String levelId, Action... steps) {
        actions = new ActionCheck("技能界面", report, levelId) {
            {
                script(steps);
            }

            @Override
            protected void cleanup(ServerPlayer player) {
            }
        };
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> actions.tick(local);
            case 1 -> after(local, 20, p -> stageLoadoutLifecycle(p, "death"));
            case 2 -> after(local, 10, p -> {
                boolean opened = p.containerMenu instanceof SkillLoadoutMenu && minecraft.screen instanceof SkillLoadoutScreen;
                expectTrue("死亡前打开真实04装配页，八槽初始为空", opened && lifecycleSlotsEmpty(p));
                if (opened) minecraft.execute(this::fillLifecycleLoadout);
            });
            case 3 -> after(local, 10, p -> {
                expectTrue("死亡前八本书经真实Shift点击进入菜单，尚未写回装配字段", lifecycleMenuMatches(p)
                        && lifecycleStateEmpty(p) && lifecycleInventoryEmpty(p) && inventorySynced(p));
                loadoutDeathPos = p.blockPosition();
                p.kill();
            });
            case 4 -> respawned(local);
            case 5 -> {
                if (!inWorld(local)) yield false;
                onServer(p -> {
                    boolean dropped = !p.serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(loadoutDeathPos).inflate(16),
                            entity -> lifecycleItem(entity.getItem())).isEmpty();
                    expectTrue("04打开期间死亡后八槽写回并复制给重生玩家，双端一致且背包与地面无重复书", lifecycleStateMatches(p)
                            && lifecycleInventoryEmpty(p) && inventorySynced(p) && !dropped);
                    key("guicaidan", true);
                    key("guicaidan", false);
                });
                yield true;
            }
            case 6 -> after(local, 10, p -> {
                expectTrue("死亡后K重开04，八槽原书及完整组件在真实菜单恢复", lifecycleMenuMatches(p));
                minecraft.execute(() -> minecraft.player.closeContainer());
            });
            case 7 -> after(local, 10, p -> stageLoadoutLifecycle(p, "closed"));
            case 8 -> after(local, 10, p -> {
                boolean opened = p.containerMenu instanceof SkillLoadoutMenu && minecraft.screen instanceof SkillLoadoutScreen;
                expectTrue("重登前打开新的04编辑场景，八槽初始为空", opened && lifecycleSlotsEmpty(p));
                if (opened) minecraft.execute(this::fillLifecycleLoadout);
            });
            case 9 -> after(local, 10, p -> {
                expectTrue("重登前八槽真实装入新书，正常关闭前不提前写回", lifecycleMenuMatches(p)
                        && lifecycleStateEmpty(p) && lifecycleInventoryEmpty(p) && inventorySynced(p));
                minecraft.execute(() -> minecraft.player.closeContainer());
            });
            case 10 -> {
                if (local < 10) yield false;
                onServer(p -> expectTrue("正常关闭04先写回八槽并同步本人，再退出存档", p.containerMenu == p.inventoryMenu
                        && lifecycleStateMatches(p) && lifecycleInventoryEmpty(p) && inventorySynced(p)));
                leaveWorld();
                yield true;
            }
            case 11 -> reopened(local);
            case 12 -> inWorld(local);
            case 13 -> after(local, 10, p -> {
                expectTrue("正常关闭后退出重登，八槽完整组件和04分页从磁盘读回且双端一致", lifecycleStateMatches(p)
                        && p.getData(PanelNavigation.PAGE) == 4 && lifecycleInventoryEmpty(p) && inventorySynced(p));
                key("guicaidan", true);
                key("guicaidan", false);
            });
            case 14 -> after(local, 10, p -> {
                expectTrue("重登后K打开真实04，八槽保存的书可再次载入", lifecycleMenuMatches(p));
                minecraft.execute(() -> minecraft.player.closeContainer());
            });
            default -> {
                if (local < 10) yield false;
                onServer(this::cleanup);
                finish();
                yield true;
            }
        };
    }

    private boolean after(int local, int ticks, Consumer<ServerPlayer> action) {
        if (local < ticks) return false;
        onServer(action);
        return true;
    }

    /** 每槽使用独立标记的已研习书，使原有装配、背包和旧掉落不能满足本场景断言。 */
    private void stageLoadoutLifecycle(ServerPlayer player, String stage) {
        player.closeContainer();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setData(SkillData.STATE, SkillState.EMPTY);
        Techniques.setSlot(player, ItemStack.EMPTY);
        player.setData(PanelNavigation.PAGE, 4);
        lifecycleBooks.clear();
        List<ItemStack> sources = List.of(studiedTechnique, learned.get(1), learned.get(2), learned.get(0), learned.get(4), learned.get(0), learned.get(4), learned.get(3));
        for (int slot = 0; slot < sources.size(); slot++) {
            ItemStack stack = sources.get(slot).copyWithCount(1);
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            data.putString("smoke_loadout_lifecycle", stage + ":" + slot);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            lifecycleBooks.add(stack.copy());
            player.getInventory().setItem(slot, stack);
        }
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        Skills.openLoadout(player);
    }

    private void fillLifecycleLoadout() {
        for (int slot = 35; slot < 43; slot++) {
            minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, ClickType.QUICK_MOVE, minecraft.player);
        }
    }

    private boolean lifecycleMenuMatches(ServerPlayer player) {
        if (!(player.containerMenu instanceof SkillLoadoutMenu) || !(minecraft.screen instanceof SkillLoadoutScreen)) return false;
        for (int slot = 0; slot < lifecycleBooks.size(); slot++) {
            if (!syncedSlot(player, slot, lifecycleBooks.get(slot))) return false;
        }
        return true;
    }

    private boolean lifecycleSlotsEmpty(ServerPlayer player) {
        for (int slot = 0; slot < 8; slot++) {
            if (!syncedSlot(player, slot, ItemStack.EMPTY)) return false;
        }
        return true;
    }

    private boolean lifecycleStateEmpty(ServerPlayer player) {
        return Techniques.slot(player).isEmpty() && Skills.movement(player).isEmpty() && Skills.flight(player).isEmpty()
                && Skills.weaponArt(player).isEmpty() && Skills.state(player).spells().stream().allMatch(ItemStack::isEmpty);
    }

    private boolean lifecycleStateMatches(ServerPlayer player) {
        for (Player holder : List.of(player, minecraft.player)) {
            if (Skills.state(holder).page() != 1 || !ItemStack.matches(Techniques.slot(holder), lifecycleBooks.get(0))
                    || !ItemStack.matches(Skills.movement(holder), lifecycleBooks.get(1)) || !ItemStack.matches(Skills.flight(holder), lifecycleBooks.get(2))
                    || !ItemStack.matches(Skills.weaponArt(holder), lifecycleBooks.get(7))) return false;
            for (int slot = 1; slot <= SkillState.SPELL_SLOTS; slot++) {
                ItemStack expected = slot <= 4 ? lifecycleBooks.get(slot + 2) : ItemStack.EMPTY;
                if (!ItemStack.matches(Skills.spell(holder, slot), expected)) return false;
            }
        }
        return true;
    }

    private boolean lifecycleInventoryEmpty(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (lifecycleItem(player.getInventory().getItem(slot))) return false;
        }
        return true;
    }

    private static boolean lifecycleItem(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("smoke_loadout_lifecycle");
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

    private void stageTreasure(ServerPlayer player, String type, double level) {
        ItemStack stack = item("book_006").copyWithCount(2);
        CompoundTag data = new CompoundTag();
        data.putString("TreasureTypeBig", "书架");
        data.putString("TreasureTypeSmall", type);
        data.putDouble("TreasureLEVEL", level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        player.getInventory().setItem(8, stack);
        treasureBefore = treasureCounts(player);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
    }

    /** 按物品数量统计，避免随机奖励合并入已有栈时误按格数计算。 */
    private static int[] treasureCounts(ServerPlayer player) {
        int[] counts = new int[3];
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            counts[0] += stack.getCount();
            if (Skills.book(stack).filter(book -> book.owner().isEmpty()).isPresent()) counts[1] += stack.getCount();
            if (Techniques.book(stack).filter(book -> book.owner().isEmpty()).isPresent()) counts[2] += stack.getCount();
        }
        return counts;
    }

    /** 客户端选择热栏并发送真实方块使用包。 */
    private void useFire(int hotbar, BlockPos pos) {
        minecraft.player.getInventory().selected = hotbar;
        minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos).add(0, .5, 0), Direction.UP, pos, false));
    }

    private static double fuel(BlockEntity block) {
        return block == null ? Double.NaN : block.getPersistentData().getDouble("power");
    }

    private Cow fireCow(BlockPos pos) {
        Cow target = EntityType.COW.create(fireLevel);
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setSilent(true);
        target.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        target.setHealth(500);
        fireLevel.addFreshEntity(target);
        fireTargets.add(target);
        return target;
    }

    /** 效果与计时是前置数据；伤害由下一次自然实体刻结算。 */
    private static void burn(Cow target, String effect, int amplifier, String timer, String attacker) {
        target.getPersistentData().putString("fireAttackUUID", attacker);
        target.getPersistentData().putDouble(timer, 19);
        target.invulnerableTime = 0;
        target.getRandom().setSeed(1);
        StatusEffects.add(target, effect, 200, amplifier);
    }

    private void clearFireScene(ServerPlayer player) {
        if (fireLevel == null) return;
        NeoForge.EVENT_BUS.unregister(fireHeat);
        for (Cow target : fireTargets) target.discard();
        fireTargets.clear();
        for (BlockPos pos : List.of(firePos, foreignFirePos, vanillaFirePos, campfirePos)) fireLevel.removeBlock(pos, false);
        for (int i = 0; i < fireHotbar.size(); i++) player.getInventory().setItem(4 + i, fireHotbar.get(i));
        fireHotbar.clear();
        player.setGameMode(gameMode);
        player.getInventory().selected = fireSelectedSlot;
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        minecraft.execute(() -> minecraft.player.getInventory().selected = fireSelectedSlot);
        fireLevel = null;
    }

    /** 客户端普通点击拿起整栈、右键放入一本，再把鼠标余本放回原热栏。 */
    private void equipOneSpell() {
        int id = minecraft.player.containerMenu.containerId;
        minecraft.gameMode.handleInventoryMouseClick(id, 35, 0, ClickType.PICKUP, minecraft.player);
        minecraft.gameMode.handleInventoryMouseClick(id, 3, 1, ClickType.PICKUP, minecraft.player);
        minecraft.gameMode.handleInventoryMouseClick(id, 35, 0, ClickType.PICKUP, minecraft.player);
    }

    private ItemStack markedPageBook(int page) {
        ItemStack stack = learned.get(4).copyWithCount(2);
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putString("smoke_skill_page", Integer.toString(page));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
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

    private void cleanup(ServerPlayer p) {
        clearFireScene(p);
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
