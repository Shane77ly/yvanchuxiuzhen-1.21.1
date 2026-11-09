package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotMenu;
import net.yvanchuxiuzhen.system.cooking.client.CookingPotScreen;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;
import net.yvanchuxiuzhen.system.fulu.FuluRanged;
import net.yvanchuxiuzhen.system.fulu.FuluUse;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;
import net.yvanchuxiuzhen.system.fulu.client.DrawingTableScreen;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableMenu;
import net.yvanchuxiuzhen.system.fulu.content.TalismanBeast;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanSummon;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 核对两锅配方加载、墨料坯自然生产闭环，以及符箓知识、画符台、烧灰架、传承之光与持续效果符（02、24 号）的施用付款、生效和结束。 */
final class FuluCookingCheck extends ActionCheck {
    private static final int HOTBAR = 8;

    private List<ItemStack> inventory;
    private int selected;
    private BlockPos potPos;
    private BlockPos tablePos;
    private final List<BlockState> potBlocks = new ArrayList<>();
    private final List<BlockState> tableBlocks = new ArrayList<>();
    private FuluKnowledge knowledge;
    private ProfessionProgress rune;
    private double runeBefore;
    private BlockPos clayPos;
    private final List<BlockState> clayBlocks = new ArrayList<>();
    private ProfessionProgress cooking;
    private double cookingBefore;
    private BlockPos burnerPos;
    private BlockPos inheritancePos;
    private final Map<BlockPos, BlockState> inheritanceBlocks = new LinkedHashMap<>();
    private final Map<BlockPos, BlockState> burnerBlocks = new LinkedHashMap<>();
    private static final List<String> TALISMAN_KEYS = List.of("fulunumber", "fuluxiaoguo02", "fulu02level", "fulu02tick", "fuluxiaoguo24",
            "fulu24level", "fulu24tick", "fuluxiaoguo13", "fulu13level", "fulu13tick", "fulu1301tick", "fululevel46", "fuluxiaoguo17",
            "fulu17level", "fulu17tick", "fulu1701tick", "fuluxiaoguo21", "fulu21level", "fulu21tick", "fuluxiaoguo39", "fulu39level", "fulu39tick");
    private GameType gameMode;
    private double soul;
    private final Map<String, Double> talismanState = new LinkedHashMap<>();
    private Mob dummy;
    private BlockPos oreAt;
    private BlockState oreBefore;

    FuluCookingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("fulucooking", report, levelId);
        script(
                new Action(4, p -> {
                    inventory = new ArrayList<>();
                    for (int i = 0; i < p.getInventory().getContainerSize(); i++) inventory.add(p.getInventory().getItem(i).copy());
                    p.getInventory().clearContent();
                    selected = p.getInventory().selected;
                    potPos = p.blockPosition().offset(4, 1, 2);
                    tablePos = p.blockPosition().offset(4, 1, 5);
                    for (int below = 0; below < 3; below++) {
                        potBlocks.add(p.serverLevel().getBlockState(potPos.below(below)));
                        tableBlocks.add(p.serverLevel().getBlockState(tablePos.below(below)));
                    }
                    var recipes = p.serverLevel().getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                            .map(RecipeHolder::value).map(recipe -> recipe.getResultItem(p.registryAccess()).getItem()).toList();
                    expectTrue("铁锅与砂锅的自然合成配方由目标数据包加载",
                            recipes.contains(item("fooddaguo").getItem()) && recipes.contains(item("fooddunguo").getItem()));

                    p.serverLevel().setBlockAndUpdate(potPos.below(2), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(potPos.below(), Blocks.CAMPFIRE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(potPos, block("fooddaguo"));
                    CookingPotBlockEntity pot = pot(p);
                    pot.setItem(0, material("cailiao_021", 25));
                    pot.setItem(1, material("cailiao_001", 15));
                    pot.setItem(2, material("cailiao_060", 5));
                    pot.setItem(3, item("yanhuiitem", 3));
                    pot.setItem(4, item("fuluzhushaitem", 3));
                    pot.changed();
                }),
                new Action(6, p -> useBlock(potPos)),
                new Action(6, p -> {
                    expectTrue("真实右键铁锅打开双方 14 槽菜单与原生界面", p.containerMenu instanceof CookingPotMenu
                            && minecraft.player.containerMenu instanceof CookingPotMenu && minecraft.screen instanceof CookingPotScreen);
                    minecraft.execute(() -> {
                        screenshots.take("cooking-iron-pot.png");
                        minecraft.screen.children().stream()
                                .filter(child -> child instanceof Button button && button.getMessage().getString().equals("开始烹饪"))
                                .map(child -> (Button) child).findFirst().orElseThrow().onPress();
                    });
                }),
                new Action(16, p -> {
                    CompoundTag state = pot(p).getPersistentData();
                    expectTrue("开始按钮：材料平均 15 级、三类各扣一件，烟灰与朱砂各扣三份，进入烹饪并关闭界面",
                            state.getBoolean("cooking") && state.getInt("cookanimation") == 9
                                    && pot(p).getItem(1).isEmpty() && pot(p).getItem(2).isEmpty()
                                    && pot(p).getItem(3).isEmpty() && pot(p).getItem(4).isEmpty()
                                    && p.containerMenu == p.inventoryMenu);
                }),
                new Action(4, p -> {
                    expectTrue("开始满 10 服务端刻后，槽 0 被覆盖为玄阶墨料坯，烹饪仍在进行（实际 "
                                    + BuiltInRegistries.ITEM.getKey(pot(p).getItem(0).getItem()) + "×" + pot(p).getItem(0).getCount() + "）",
                            pot(p).getItem(0).is(item("motiao_0201").getItem()) && pot(p).getItem(0).getCount() == 1
                                    && pot(p).getPersistentData().getBoolean("cooking"));
                }),
                new Action(4, p -> useBlock(potPos)),
                new Action(4, p -> useBlock(potPos)),
                new Action(4, p -> useBlock(potPos)),
                new Action(4, p -> useBlock(potPos)),
                new Action(4, p -> {
                    expectTrue("四次翻炒后仍在烹饪", pot(p).getPersistentData().getBoolean("cooking")
                            && pot(p).getPersistentData().getDouble("cooknumber") == 4);
                    useBlock(potPos);
                }),
                new Action(6, p -> {
                    CompoundTag state = pot(p).getPersistentData();
                    CookingPotBlockEntity restored = (CookingPotBlockEntity) BlockEntity.loadStatic(potPos, p.serverLevel().getBlockState(potPos),
                            pot(p).saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("第五次翻炒结束烹饪并清零计数，产物保留，保存重读一致",
                            !state.getBoolean("cooking") && state.getDouble("cooknumber") == 0 && state.getDouble("cookanimation") == 0
                                    && pot(p).getItem(0).is(item("motiao_0201").getItem())
                                    && restored.getPersistentData().equals(state)
                                    && ItemStack.matches(restored.getItem(0), pot(p).getItem(0)));
                    pot(p).clearContent();

                    knowledge = p.getData(FuluData.KNOWLEDGE);
                    rune = p.getData(ProfessionAttachments.PROGRESS).get(Profession.RUNE);
                    Professions.setLevel(p, Profession.RUNE, 0);
                    Professions.setExperience(p, Profession.RUNE, 0);
                    runeBefore = Professions.experience(p, Profession.RUNE);
                    p.setData(FuluData.KNOWLEDGE, FuluKnowledge.INITIAL);
                    p.serverLevel().setBlockAndUpdate(tablePos.below(2), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(tablePos.below(), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(tablePos, block("huafutai"));
                    useBlock(tablePos);
                }),
                new Action(8, p -> {
                    expectTrue("真实右键画符台打开双方菜单与原生界面", p.containerMenu instanceof DrawingTableMenu
                            && minecraft.player.containerMenu instanceof DrawingTableMenu && minecraft.screen instanceof DrawingTableScreen);
                    minecraft.execute(() -> screenshots.take("fulu-drawing-table.png"));
                }),
                new Action(4, p -> {
                    DrawingTableBlockEntity table = table(p);
                    for (int button = 3; button < 6; button++) DrawingTableRuntime.button(table, p, button);
                    CompoundTag state = table.getPersistentData();
                    expectTrue("初始知识下三类选择分别取符头 1、天地柱 1、符胆 1，预览符箓与耗墨来自首个匹配配方",
                            state.getInt("futou") == 1 && state.getInt("tiandizhu") == 1 && state.getInt("fudan") == 1
                                    && state.getString("fuluname").equals(ModId.VALUE + ":itemfulu_01") && state.getInt("needmotiao") == 2);
                    table.setItem(0, item("fulufuzhi", 1));
                    table.setItem(1, item("motiao_01", 3));
                    DrawingTableRuntime.button(table, p, 6);
                    ItemStack result = table.getItem(2);
                    CompoundTag data = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    expectTrue("绘制：产出对应符箓并记天地柱品阶，扣一张符纸与两份墨，画符经验增加 8（实际 "
                                    + (Professions.experience(p, Profession.RUNE) - runeBefore) + "）",
                            result.is(item("itemfulu_01").getItem()) && data.getInt("fululevel") == 1
                                    && table.getItem(0).isEmpty() && table.getItem(1).getCount() == 1
                                    && Professions.experience(p, Profession.RUNE) - runeBefore == 8);
                    DrawingTableBlockEntity restored = (DrawingTableBlockEntity) BlockEntity.loadStatic(tablePos, p.serverLevel().getBlockState(tablePos),
                            table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("画符台共享选择与库存保存重读一致", restored.getPersistentData().equals(state)
                            && ItemStack.matches(restored.getItem(2), result));
                    p.closeContainer();
                }),
                new Action(8, p -> {
                    boolean learned = Fulu.unlockHead(p, 2);
                    boolean repeated = Fulu.unlockHead(p, 2);
                    boolean core = Fulu.unlockCore(p, 2);
                    FuluKnowledge current = Fulu.knowledge(p);
                    expectTrue("解锁追加稳定编号，重复学习被拒绝且不重复追加", learned && !repeated && core
                            && current.heads().equals(List.of(1, 2)) && current.cores().equals(List.of(1, 2))
                            && p.getData(FuluData.KNOWLEDGE).heads().equals(List.of(1, 2)));
                    clayPos = p.blockPosition().offset(4, 1, -2);
                    for (int below = 0; below < 3; below++) clayBlocks.add(p.serverLevel().getBlockState(clayPos.below(below)));
                    cooking = p.getData(ProfessionAttachments.PROGRESS).get(Profession.COOKING);
                    cookingBefore = Professions.experience(p, Profession.COOKING);
                    p.serverLevel().setBlockAndUpdate(clayPos.below(2), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(clayPos.below(), Blocks.CAMPFIRE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(clayPos, block("fooddunguo"));
                    ItemStack recipe = new ItemStack(Items.PAPER);
                    CompoundTag recipeData = new CompoundTag();
                    recipeData.putString("MenuType", "砂锅");
                    recipeData.putString("MenuName", "测试菜谱");
                    recipeData.putString("Menuid", ModId.VALUE + ":food_007");
                    recipeData.putString("needcontainer", "无");
                    recipeData.putString("cailiao00", ModId.VALUE + ":shicai_001");
                    recipeData.putInt("cookanimation", 2);
                    recipe.set(DataComponents.CUSTOM_DATA, CustomData.of(recipeData));
                    CookingPotBlockEntity clay = pot(p, clayPos);
                    clay.setItem(13, recipe);
                    clay.setItem(5, item("shicai_001", 2));
                    clay.changed();
                    useBlock(clayPos);
                }),
                new Action(6, p -> {
                    expectTrue("真实右键砂锅打开双方菜单与原生界面（服务端 " + p.containerMenu.getClass().getSimpleName() + "，客户端 "
                            + minecraft.player.containerMenu.getClass().getSimpleName() + "，界面 " + (minecraft.screen == null ? "无" : minecraft.screen.getClass().getSimpleName())
                            + "，BE " + pot(p, clayPos).getPersistentData() + "）", p.containerMenu instanceof CookingPotMenu
                            && minecraft.player.containerMenu instanceof CookingPotMenu && minecraft.screen instanceof CookingPotScreen);
                    minecraft.execute(() -> {
                        screenshots.take("cooking-clay-pot.png");
                        minecraft.screen.children().stream()
                                .filter(child -> child instanceof Button button && button.getMessage().getString().equals("开始烹饪"))
                                .map(child -> (Button) child).findFirst().orElseThrow().onPress();
                    });
                }),
                new Action(4, p -> {
                    CookingPotBlockEntity clay = pot(p, clayPos);
                    CompoundTag state = clay.getPersistentData();
                    expectTrue("菜谱烹饪：食材扣一件并产出菜品，覆盖结果槽，菜谱不消耗，经验按 belongToLevel 平方加 5",
                            state.getBoolean("cooking") && clay.getItem(5).getCount() == 1
                                    && clay.getItem(4).is(item("food_007").getItem()) && !clay.getItem(13).isEmpty()
                                    && state.getInt("cookexp") == 5 && p.containerMenu == p.inventoryMenu);
                }),
                new Action(4, p -> useBlock(clayPos)),
                new Action(4, p -> useBlock(clayPos)),
                new Action(4, p -> useBlock(clayPos)),
                new Action(4, p -> useBlock(clayPos)),
                new Action(4, p -> useBlock(clayPos)),
                new Action(6, p -> {
                    CompoundTag state = pot(p, clayPos).getPersistentData();
                    expectTrue("五次砂锅翻炒结束并清零，厨艺经验增加 5（实际 " + (Professions.experience(p, Profession.COOKING) - cookingBefore) + "），经验字段清零",
                            !state.getBoolean("cooking") && state.getDouble("cooknumber") == 0 && state.getDouble("cookexp") == 0
                                    && Professions.experience(p, Profession.COOKING) - cookingBefore == 5
                                    && pot(p, clayPos).getItem(4).is(item("food_007").getItem()));
                    burnerPos = p.blockPosition().offset(-4, 1, 0);
                    for (int offset = -1; offset <= 3; offset++) burnerBlocks.put(burnerPos.above(offset), p.serverLevel().getBlockState(burnerPos.above(offset)));
                    p.serverLevel().setBlockAndUpdate(burnerPos.below(), Blocks.STONE.defaultBlockState());
                    for (int offset = 0; offset <= 2; offset++) p.serverLevel().setBlockAndUpdate(burnerPos.above(offset), Blocks.AIR.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(burnerPos.above(3), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(burnerPos, block("huafushaohui"));
                    ((AshBurnerBlockEntity) p.serverLevel().getBlockEntity(burnerPos)).setItem(0, new ItemStack(Items.OAK_LOG));
                    p.getInventory().setItem(HOTBAR, new ItemStack(Items.FLINT_AND_STEEL));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(6, p -> useBlock(burnerPos)),
                new Action(425, p -> {
                    AshBurnerBlockEntity burner = (AshBurnerBlockEntity) p.serverLevel().getBlockEntity(burnerPos);
                    expectTrue("打火石点燃烧灰架：begin 置位并开始计时（进度 " + burner.getPersistentData().getInt("ranshaotick") + "）",
                            burner.getPersistentData().getBoolean("begin") && !burner.getItem(0).isEmpty()
                                    && burner.getPersistentData().getInt("ranshaotick") > 0);
                }),
                new Action(4, p -> {
                    AshBurnerBlockEntity burner = (AshBurnerBlockEntity) p.serverLevel().getBlockEntity(burnerPos);
                    expectTrue("满 40 个十刻周期后烧掉原木，并在上方两格的平整底面下生成一层烟灰（实际 "
                                    + BuiltInRegistries.BLOCK.getKey(p.serverLevel().getBlockState(burnerPos.above(2)).getBlock()) + "）",
                            burner.getItem(0).isEmpty() && p.serverLevel().getBlockState(burnerPos.above(2)).is(block("yanhui_01").getBlock()));
                    inheritancePos = p.blockPosition().offset(-3, 1, 3);
                    for (int offset = -1; offset <= 0; offset++) inheritanceBlocks.put(inheritancePos.above(offset), p.serverLevel().getBlockState(inheritancePos.above(offset)));
                    p.getInventory().setItem(HOTBAR, ItemStack.EMPTY);
                    p.inventoryMenu.broadcastChanges();
                    p.serverLevel().setBlockAndUpdate(inheritancePos.below(), Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(inheritancePos, block("chuancheng"));
                    BlockEntity inheritance = p.serverLevel().getBlockEntity(inheritancePos);
                    inheritance.getPersistentData().putInt("weizhi", 5);
                    inheritance.setChanged();
                }),
                new Action(6, p -> useBlock(inheritancePos)),
                new Action(4, p -> {
                    expectTrue("使用传承之光经命令入口授予所存符头 5，方块与共享选择不变（符头 " + Fulu.knowledge(p).heads() + "）",
                            Fulu.knowledge(p).heads().contains(5)
                                    && p.serverLevel().getBlockState(inheritancePos).is(block("chuancheng").getBlock())
                                    && p.serverLevel().getBlockEntity(inheritancePos).getPersistentData().getInt("weizhi") == 5);
                    gameMode = p.gameMode.getGameModeForPlayer();
                    soul = Cultivation.get(p, CultivationValue.SOUL);
                    TALISMAN_KEYS.forEach(key -> talismanState.put(key, Cultivation.tizhiDouble(p, key)));
                    p.setGameMode(GameType.SURVIVAL);
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_02", 3, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(6, p -> {
                    expectTrue("02 号符一阶：付 1²×10 精力，登记生效、品阶与生效数，消耗一张（精力 " + Cultivation.get(p, CultivationValue.SOUL) + "）",
                            Math.abs(Cultivation.get(p, CultivationValue.SOUL) - 10) < 2.5 && Cultivation.tizhiBoolean(p, "fuluxiaoguo02")
                                    && Cultivation.tizhiDouble(p, "fulu02level") == 1 && Cultivation.tizhiDouble(p, "fulunumber") == 1
                                    && p.getInventory().getItem(HOTBAR).getCount() == 2);
                    useHeld();
                }),
                new Action(6, p -> {
                    expectTrue("效果未结束时再次使用：来源先付款后判断，精力再扣而符不消耗（精力 " + Cultivation.get(p, CultivationValue.SOUL) + "）",
                            Cultivation.get(p, CultivationValue.SOUL) < 2.5 && p.getInventory().getItem(HOTBAR).getCount() == 2
                                    && Cultivation.tizhiDouble(p, "fulunumber") == 1);
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    dummy = (Mob) EntityType.ZOMBIE.create(p.serverLevel());
                    dummy.moveTo(p.getX() + 1.5, p.getY(), p.getZ(), 0, 0);
                    dummy.setNoAi(true);
                    dummy.setPersistenceRequired();
                    p.serverLevel().addFreshEntity(dummy);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_24", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(130, p -> {
                    expectTrue("24 号符一阶：付 10 精力并登记，周身 3.5 格内的非友方被固定火伤害命中（目标生命 " + dummy.getHealth() + "，存活 " + dummy.isAlive() + "）",
                            Cultivation.tizhiBoolean(p, "fuluxiaoguo24") && p.getInventory().getItem(HOTBAR).isEmpty()
                                    && (dummy.getHealth() < dummy.getMaxHealth() || !dummy.isAlive()));
                }),
                new Action(4, p -> {
                    expectTrue("02 号符满 80+20×1 刻后结束：清零计数、品阶与生效标记，生效数只剩 24 号符的一份（标记 " + Cultivation.tizhiBoolean(p, "fuluxiaoguo02")
                                    + "，计数 " + Cultivation.tizhiDouble(p, "fulu02tick") + "，生效数 " + Cultivation.tizhiDouble(p, "fulunumber") + "）",
                            !Cultivation.tizhiBoolean(p, "fuluxiaoguo02") && Cultivation.tizhiDouble(p, "fulu02level") == 0
                                    && Cultivation.tizhiDouble(p, "fulu02tick") == 0 && Cultivation.tizhiDouble(p, "fulunumber") == 1);
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_13", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_17", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(12, p -> {
                }),
                new Action(4, p -> {
                    List<? extends TalismanProjectile> shots = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanProjectile.class), shot -> true);
                    long fireballs = shots.stream().filter(shot -> shot.getType().toString().endsWith("46")).count();
                    long meteors = shots.stream().filter(shot -> shot.getType().toString().endsWith("17")).count();
                    expectTrue("13 号符每 5 刻发火球、17 号符从头顶 32 格处落陨石，弹体实体存在（火球 " + fireballs + "，陨石 " + meteors + "）",
                            Cultivation.tizhiBoolean(p, "fuluxiaoguo13") && Cultivation.tizhiBoolean(p, "fuluxiaoguo17") && fireballs > 0 && meteors > 0);
                    shots.forEach(shot -> shot.discard());
                    p.removeAllEffects();
                    Cultivation.editTizhi(p, tizhi -> TALISMAN_KEYS.stream().filter(key -> key.startsWith("fuluxiaoguo")).forEach(key -> tizhi.putBoolean(key, false)));
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "duyao_01")).orElseThrow(), 600, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 5));
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_21", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("21 号符一阶：清除等级不高于 1 的毒药 01，保留等级 5 的虚弱，登记生效",
                            !p.hasEffect(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "duyao_01")).orElseThrow())
                                    && p.hasEffect(MobEffects.WEAKNESS) && Cultivation.tizhiBoolean(p, "fuluxiaoguo21") && p.getInventory().getItem(HOTBAR).isEmpty());
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_39", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("39 号符一阶：获得 12000 刻水下呼吸并登记生效（剩余 " + (p.hasEffect(MobEffects.WATER_BREATHING) ? p.getEffect(MobEffects.WATER_BREATHING).getDuration() : 0) + "）",
                            p.hasEffect(MobEffects.WATER_BREATHING) && p.getEffect(MobEffects.WATER_BREATHING).getDuration() > 11900
                                    && Cultivation.tizhiBoolean(p, "fuluxiaoguo39"));
                    p.removeAllEffects();
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_25", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("25 号符一阶：获得 3600 刻速度与 1200 刻力量（等级 1），消耗一张",
                            p.hasEffect(MobEffects.MOVEMENT_SPEED) && p.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 1
                                    && p.hasEffect(MobEffects.DAMAGE_BOOST) && p.getEffect(MobEffects.DAMAGE_BOOST).getDuration() <= 1200
                                    && p.getInventory().getItem(HOTBAR).isEmpty());
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_11", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("11 号符一阶：吸收等级 6、抗性等级 0（精力 " + Cultivation.get(p, CultivationValue.SOUL) + "，效果 " + p.getActiveEffects() + "）",
                            p.hasEffect(MobEffects.ABSORPTION) && p.getEffect(MobEffects.ABSORPTION).getAmplifier() == 6
                                    && p.hasEffect(MobEffects.DAMAGE_RESISTANCE) && p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 0);
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    Cultivation.setTizhiDouble(p, "fululevel46", 0);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_46", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("46 号符使用后品阶写入玩家持久数据而非体质（来源 Q66；精力 " + Cultivation.get(p, CultivationValue.SOUL) + "，持久 " + p.getPersistentData().getDouble("fululevel46") + "，体质 " + Cultivation.tizhiDouble(p, "fululevel46") + "）",
                            p.getPersistentData().getDouble("fululevel46") == 1 && Cultivation.tizhiDouble(p, "fululevel46") == 0);
                
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_05", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    expectTrue("05 号符：品阶经使用者持久数据交给气剑，首刻读取后清零",
                            p.getInventory().getItem(HOTBAR).isEmpty() && p.getPersistentData().getDouble("fulu05level") == 0);
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_32", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(24, p -> {
                }),
                new Action(4, p -> {
                    long pieces = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanProjectile.class), shot -> shot.getType().toString().endsWith("32")).size();
                    expectTrue("32 号符：主弹 16 刻或命中时炸出子弹，使用者的分裂标记被主弹取走（子弹 " + pieces + "）",
                            !p.getPersistentData().getBoolean("fulu32shoufa") && pieces >= 2);
                
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_14", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    var summons = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanSummon.class), mob -> true);
                    expectTrue("14 号符：召唤出携带符名、品阶与召唤者 UUID 的飞行体（数量 " + summons.size() + "）",
                            summons.size() == 1 && summons.get(0).getPersistentData().getString("fuluname").equals(TalismanSummon.AIRBURST)
                                    && summons.get(0).getPersistentData().getDouble("fululevel") == 1
                                    && summons.get(0).getPersistentData().getString("belongToMasterUUID").equals(p.getStringUUID()));
                }),
                new Action(24, p -> {
                }),
                new Action(4, p -> {
                    expectTrue("空爆符满 20 刻后飞行体爆炸并消失",
                            p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanSummon.class), mob -> true).isEmpty());
                
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_26", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    useHeld();
                }),
                new Action(4, p -> {
                    var beasts = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanBeast.class), mob -> true);
                    expectTrue("26 号符：召唤神压符动画实体，携带品阶、符名与召唤者 UUID（数量 " + beasts.size() + "）",
                            beasts.size() == 1 && beasts.get(0).getPersistentData().getString("fuluname").equals("神压符")
                                    && beasts.get(0).getPersistentData().getDouble("fululevel") == 1
                                    && beasts.get(0).getPersistentData().getString("belongToMasterUUID").equals(p.getStringUUID()));
                    minecraft.execute(() -> screenshots.take("fulu-beast-26.png"));
                }),
                new Action(44, p -> {
                }),
                new Action(4, p -> {
                    expectTrue("神压符 20 刻拍击、5 刻停顿、15 刻碎裂后消失",
                            p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanBeast.class), mob -> true).isEmpty());
                
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_28", 2, 1));
                    p.inventoryMenu.broadcastChanges();
                    FuluRanged.release(p, p.getInventory().getItem(HOTBAR), 28);
                }),
                new Action(4, p -> {
                    expectTrue("28 号远程符松手：付精力、消耗持有的一张，弹体第一刻读取品阶并清零使用者的 fululevel（精力 " + Cultivation.get(p, CultivationValue.SOUL)
                                    + "，余 " + p.getInventory().getItem(HOTBAR).getCount() + "）",
                            Cultivation.get(p, CultivationValue.SOUL) < 12 && p.getInventory().getItem(HOTBAR).getCount() == 1
                                    && p.getPersistentData().getDouble("fululevel") == 0);
                
                    oreAt = p.blockPosition().offset(2, -3, 0);
                    oreBefore = p.serverLevel().getBlockState(oreAt);
                    p.serverLevel().setBlockAndUpdate(oreAt, block("cailiao_20901"));
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_36", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    FuluUse.activate(p, p.getInventory().getItem(HOTBAR), 36);
                }),
                new Action(4, p -> {
                    expectTrue("36 号符：水平 ±3、脚下方 20 格内的矿石标签方块被破坏，符冷却 200 刻并消耗一张",
                            p.serverLevel().getBlockState(oreAt).isAir() && p.getInventory().getItem(HOTBAR).isEmpty()
                                    && p.getCooldowns().isOnCooldown(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "itemfulu_36"))));
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_01", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    FuluUse.activate(p, p.getInventory().getItem(HOTBAR), 1);
                }),
                new Action(4, p -> {
                    var thunder = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanBeast.class), mob -> true);
                    expectTrue("01 号雷符：召唤雷符实体并携带品阶与召唤者 UUID（数量 " + thunder.size() + "）",
                            thunder.size() == 1 && thunder.get(0).getPersistentData().getString("belongToMasterUUID").equals(p.getStringUUID())
                                    && thunder.get(0).getPersistentData().getDouble("fululevel") == 1);
                }),
                new Action(84, p -> {
                }),
                new Action(4, p -> {
                    expectTrue("雷符 80 刻后落雷并消失",
                            p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanBeast.class), mob -> true).isEmpty());
                })
        );
    }

    private static CookingPotBlockEntity pot(ServerPlayer player, BlockPos pos) {
        return (CookingPotBlockEntity) player.serverLevel().getBlockEntity(pos);
    }

    private CookingPotBlockEntity pot(ServerPlayer player) {
        return pot(player, potPos);
    }

    private DrawingTableBlockEntity table(ServerPlayer player) {
        return (DrawingTableBlockEntity) player.serverLevel().getBlockEntity(tablePos);
    }

    private static BlockState block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)).defaultBlockState();
    }

    private static ItemStack item(String path) {
        return item(path, 1);
    }

    private static ItemStack item(String path, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)), count);
    }

    private static ItemStack material(String path, int level) {
        ItemStack stack = item(path);
        Materials.setInstance(stack, level, 0);
        return stack;
    }

    private static ItemStack talisman(String path, int count, int grade) {
        ItemStack stack = item(path, count);
        CompoundTag data = new CompoundTag();
        data.putInt("fululevel", grade);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    private void useHeld() {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = HOTBAR;
            minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
        });
    }

    private void useBlock(BlockPos pos) {
        minecraft.execute(() -> {
            minecraft.player.getInventory().selected = HOTBAR;
            minecraft.gameMode.useItemOn(minecraft.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos).add(0, .5, 0), Direction.UP, pos, false));
        });
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        if (oreAt != null) {
            player.serverLevel().setBlockAndUpdate(oreAt, oreBefore);
        }
        if (dummy != null) {
            dummy.discard();
            player.removeAllEffects();
            player.serverLevel().getEntities(EntityTypeTest.forClass(TalismanProjectile.class), shot -> true).forEach(shot -> shot.discard());
            player.setGameMode(gameMode);
            Cultivation.set(player, CultivationValue.SOUL, soul);
            Cultivation.editTizhi(player, tizhi -> talismanState.forEach((key, value) -> {
                if (key.startsWith("fuluxiaoguo")) {
                    tizhi.putBoolean(key, value != 0);
                } else {
                    tizhi.putDouble(key, value);
                }
            }));
        }
        pot(player).clearContent();
        table(player).clearContent();
        if (inheritancePos != null) inheritanceBlocks.forEach((pos, state) -> player.serverLevel().setBlockAndUpdate(pos, state));
        if (burnerPos != null) {
            ((AshBurnerBlockEntity) player.serverLevel().getBlockEntity(burnerPos)).clearContent();
            burnerBlocks.forEach((pos, state) -> player.serverLevel().setBlockAndUpdate(pos, state));
        }
        if (clayPos != null) {
            pot(player, clayPos).clearContent();
            for (int below = 0; below < 3; below++) player.serverLevel().setBlockAndUpdate(clayPos.below(below), clayBlocks.get(below));
            player.getData(ProfessionAttachments.PROGRESS).set(Profession.COOKING, cooking);
        }
        for (int below = 0; below < 3; below++) {
            player.serverLevel().setBlockAndUpdate(potPos.below(below), potBlocks.get(below));
            player.serverLevel().setBlockAndUpdate(tablePos.below(below), tableBlocks.get(below));
        }
        player.setData(FuluData.KNOWLEDGE, knowledge);
        player.getData(ProfessionAttachments.PROGRESS).set(Profession.RUNE, rune);
        player.syncData(ProfessionAttachments.PROGRESS);
        player.getInventory().selected = selected;
        minecraft.execute(() -> minecraft.player.getInventory().selected = selected);
        for (int i = 0; i < inventory.size(); i++) player.getInventory().setItem(i, inventory.get(i));
        player.inventoryMenu.broadcastChanges();
    }
}
