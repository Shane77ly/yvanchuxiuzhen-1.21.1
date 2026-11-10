package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.system.craft.client.CarpentryScreen;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.CarpentryMenu;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotMenu;
import net.yvanchuxiuzhen.system.cooking.client.CookingPotScreen;
import net.yvanchuxiuzhen.system.craft.client.StoneMillScreen;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.StoneMillMenu;
import net.yvanchuxiuzhen.system.craft.content.LoomBlockEntity;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;
import net.yvanchuxiuzhen.system.fulu.FuluRanged;
import net.yvanchuxiuzhen.system.fulu.FuluUse;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluKnowledge;
import net.yvanchuxiuzhen.system.fulu.client.DrawingTableScreen;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableMenu;
import net.yvanchuxiuzhen.system.fulu.content.CloudSpirit;
import net.yvanchuxiuzhen.system.fulu.content.ExplosiveTalismanBlock;
import net.yvanchuxiuzhen.system.fulu.content.RepellingTalismanBlockEntity;
import net.yvanchuxiuzhen.system.fulu.content.TalismanBeast;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanSummon;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.relation.data.RelationAttachments;
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
    private Difficulty difficulty;
    private boolean naturalRegeneration;
    private double soul;
    private float health;
    private float clusterPitch;
    private final Map<BlockPos, BlockState> clusterAir = new LinkedHashMap<>();
    private final Map<String, Double> talismanState = new LinkedHashMap<>();
    private Mob dummy;
    private final int[] volleySpawns = new int[2];
    private Consumer<EntityJoinLevelEvent> volleyListener;
    private BlockPos oreAt;
    private BlockState oreBefore;
    private BlockPos repellingBase;
    private BlockPos explosiveBase;
    private BlockPos stoneMillPos;
    private BlockPos stoneRollerPos;
    private BlockPos loomPos;
    private BlockPos carpentryPos;
    private BlockPos silkReelPos;
    private BlockPos silkInputPos;
    private final List<ItemEntity> silkDrops = new ArrayList<>();
    private Consumer<EntityJoinLevelEvent> silkDropListener;
    private boolean stoneMillShift;
    private final Map<BlockPos, BlockState> stoneMillBlocks = new LinkedHashMap<>();
    private final Map<BlockPos, CompoundTag> stoneMillEntities = new LinkedHashMap<>();
    private final List<ItemEntity> stoneMillDrops = new ArrayList<>();
    private Consumer<EntityJoinLevelEvent> stoneMillDropListener;
    private final Map<BlockPos, BlockState> repellingBlocks = new LinkedHashMap<>();

    FuluCookingCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("fulucooking", report, levelId);
        script(
                new Action(4, p -> {
                    naturalRegeneration = p.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
                    p.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, p.server);
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
                    difficulty = p.serverLevel().getDifficulty();
                    soul = Cultivation.get(p, CultivationValue.SOUL);
                    health = p.getHealth();
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
                    volleyListener = event -> {
                        if (!event.getLevel().isClientSide() && event.getEntity() instanceof TalismanProjectile shot && shot.getOwner() == p) {
                            if (shot.getType().toString().endsWith("46")) volleySpawns[0]++;
                            if (shot.getType().toString().endsWith("17")) volleySpawns[1]++;
                        }
                    };
                    NeoForge.EVENT_BUS.addListener(volleyListener);
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
                    NeoForge.EVENT_BUS.unregister(volleyListener);
                    expectTrue("13/17 号符施用后逐刻发射两种弹体（实际生成火球 " + volleySpawns[0] + "，陨石 " + volleySpawns[1] + "）",
                            Cultivation.tizhiBoolean(p, "fuluxiaoguo13") && Cultivation.tizhiBoolean(p, "fuluxiaoguo17")
                                    && volleySpawns[0] >= 2 && volleySpawns[1] >= 2);
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
                    for (BlockPos pos : BlockPos.betweenClosed(p.blockPosition().offset(-4, 2, -4), p.blockPosition().offset(4, 38, 4))) {
                        BlockPos saved = pos.immutable();
                        clusterAir.put(saved, p.serverLevel().getBlockState(saved));
                        p.serverLevel().setBlockAndUpdate(saved, Blocks.AIR.defaultBlockState());
                    }
                    minecraft.execute(() -> {
                        clusterPitch = minecraft.player.getXRot();
                        minecraft.player.setXRot(-90);
                    });
                    useHeld();
                }),
                new Action(24, p -> {
                }),
                new Action(4, p -> {
                    long pieces = p.serverLevel().getEntities(EntityTypeTest.forClass(TalismanProjectile.class), shot -> shot.getType().toString().endsWith("32")).size();
                    expectTrue("32 号符：主弹 16 刻或命中时炸出子弹，使用者的分裂标记被主弹取走（子弹 " + pieces + "）",
                            !p.getPersistentData().getBoolean("fulu32shoufa") && pieces >= 2);
                    minecraft.execute(() -> minecraft.player.setXRot(clusterPitch));
                
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
                
                    p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.IRON_PICKAXE));
                    p.getInventory().selected = HOTBAR;
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_47", 1, 2));
                    p.inventoryMenu.broadcastChanges();
                    FuluUse.activate(p, p.getInventory().getItem(HOTBAR), 47);
                }),
                new Action(4, p -> {
                    expectTrue("47 号符二阶：不付精力，副手镐获得连锁 2 级，符被消耗（连锁 "
                                    + Enchants.level(p, p.getOffhandItem(), Enchants.key("chain")) + "）",
                            Enchants.level(p, p.getOffhandItem(), Enchants.key("chain")) == 2 && p.getInventory().getItem(HOTBAR).isEmpty());
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_40", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    FuluUse.activate(p, p.getInventory().getItem(HOTBAR), 40);
                }),
                new Action(4, p -> {
                    expectTrue("40 号符一阶：防御提升 1200 刻（等级 1）并获得效果 022",
                            p.hasEffect(MobEffects.DAMAGE_RESISTANCE) && p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 1
                                    && p.getActiveEffects().stream().anyMatch(effect -> effect.getEffect().getRegisteredName().endsWith("effect_022")));
                
                    Cultivation.set(p, CultivationValue.SOUL, 20);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_44", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    FuluUse.activate(p, p.getInventory().getItem(HOTBAR), 44);
                }),
                new Action(6, p -> {
                    var spirits = p.serverLevel().getEntities(EntityTypeTest.forClass(CloudSpirit.class), mob -> true);
                    expectTrue("44 号符：召唤被使用者驯服的云灵，携带品阶、符名与召唤者 UUID（数量 " + spirits.size() + "）",
                            spirits.size() == 1 && spirits.get(0).isTame() && spirits.get(0).isOwnedBy(p)
                                    && spirits.get(0).getPersistentData().getString("fuluname").equals("云灵符")
                                    && spirits.get(0).getPersistentData().getDouble("fululevel") == 1);
                    minecraft.execute(() -> screenshots.take("fulu-cloud-spirit.png"));
                    spirits.forEach(Entity::discard);
                    p.removeAllEffects();
                    p.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(
                            ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "effect_007")).orElseThrow(), 200));
                    p.setHealth(1);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_22", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(2, p -> {
                    double expectedHealth = 1 + p.getMaxHealth() * 0.1;
                    expectTrue("22 号符背包被动：危急时回血、加速并消耗（生命 " + p.getHealth() + "/" + p.getMaxHealth()
                                    + "，预期 " + expectedHealth + "，剩余 " + p.getInventory().getItem(HOTBAR).getCount()
                                    + "，移速效果 " + p.getEffect(MobEffects.MOVEMENT_SPEED) + "）",
                            p.getInventory().getItem(HOTBAR).isEmpty() && Math.abs(p.getHealth() - expectedHealth) < 0.01
                                    && p.hasEffect(MobEffects.MOVEMENT_SPEED) && p.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 5);
                    p.removeAllEffects();
                    p.setHealth(1);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_23", 1, 1));
                    p.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(
                            ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "effect_007")).orElseThrow(), 200));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(2, p -> {
                    double expectedHealth = p.getMaxHealth() * 0.15;
                    expectTrue("23 号符背包被动：危急时保命、加效果并消耗（生命 " + p.getHealth() + "/" + p.getMaxHealth()
                                    + "，预期 " + expectedHealth + "，剩余 " + p.getInventory().getItem(HOTBAR).getCount()
                                    + "，移速 " + p.getEffect(MobEffects.MOVEMENT_SPEED) + "，力量 " + p.getEffect(MobEffects.DAMAGE_BOOST)
                                    + "，抗性 " + p.getEffect(MobEffects.DAMAGE_RESISTANCE) + "，吸收 " + p.getEffect(MobEffects.ABSORPTION) + "）",
                            p.getInventory().getItem(HOTBAR).isEmpty() && Math.abs(p.getHealth() - expectedHealth) < 0.01
                                    && p.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 1
                                    && p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() == 1
                                    && p.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() == 0
                                    && p.getEffect(MobEffects.ABSORPTION).getAmplifier() == 1);
                    dummy = (Mob) EntityType.PIG.create(p.serverLevel());
                    dummy.moveTo(p.getX() + 2, p.getY(), p.getZ(), 0, 0);
                    dummy.setNoAi(true);
                    dummy.setPersistenceRequired();
                    p.serverLevel().addFreshEntity(dummy);
                    p.removeAllEffects();
                    p.setHealth(1);
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_16", 1, 1));
                    p.inventoryMenu.broadcastChanges();
                    p.getServer().setDifficulty(Difficulty.NORMAL, true);
                    p.invulnerableTime = 0;
                    p.hurt(p.damageSources().indirectMagic(dummy, dummy), 100);
                }),
                new Action(20, p -> {
                    expectTrue("16 号替死符：致命伤害中保护并消耗（生命 " + p.getHealth() + "/" + p.getMaxHealth()
                                    + "，剩余 " + p.getInventory().getItem(HOTBAR).getCount() + "，发光 " + p.hasEffect(MobEffects.GLOWING) + "）",
                            p.getHealth() == p.getMaxHealth() && p.getInventory().getItem(HOTBAR).isEmpty()
                                    && p.hasEffect(MobEffects.GLOWING));
                    p.getServer().setDifficulty(difficulty, true);
                    repellingBase = p.blockPosition().offset(1, -1, 1);
                    repellingBlocks.put(repellingBase, p.serverLevel().getBlockState(repellingBase));
                    repellingBlocks.put(repellingBase.above(), p.serverLevel().getBlockState(repellingBase.above()));
                    p.serverLevel().setBlockAndUpdate(repellingBase, Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(repellingBase.above(), Blocks.AIR.defaultBlockState());
                    p.getInventory().setItem(HOTBAR, item("itemfulu_15"));
                    p.inventoryMenu.broadcastChanges();
                    useBlock(repellingBase);
                }),
                new Action(8, p -> {
                    BlockPos wardPos = repellingBase.above();
                    dummy.discard();
                    dummy = (Mob) EntityType.PIG.create(p.serverLevel());
                    dummy.setNoAi(true);
                    if (!(p.serverLevel().getBlockEntity(wardPos) instanceof RepellingTalismanBlockEntity ward)) {
                        expectTrue("15 号镇宅符已放置方块与方块实体（目标方块 " + p.serverLevel().getBlockState(wardPos)
                                        + "，底座 " + p.serverLevel().getBlockState(repellingBase) + "，手持数量 "
                                        + p.getInventory().getItem(HOTBAR).getCount() + "）", false);
                        return;
                    }
                    dummy.moveTo(wardPos.getX() + 6, wardPos.getY(), wardPos.getZ() + 0.5, 0, 0);
                    p.serverLevel().addFreshEntity(dummy);
                    String hostileTeam = "fulu_smoke_hostile";
                    while (Relations.friendlyTeams(p).contains(hostileTeam)) hostileTeam += "_";
                    Relations.setTeam(dummy, hostileTeam);
                    ward.repel(p.serverLevel());
                    CompoundTag friendlist = ward.getPersistentData().getCompound("friendlist");
                    boolean capturedFriends = Relations.friendlyTeams(p).stream().allMatch(friendlist::getBoolean);
                    boolean blockPlaced = p.serverLevel().getBlockState(wardPos).is(block("itemfulu_15").getBlock());
                    boolean teamCaptured = ward.getPersistentData().getString("team").equals(Relations.team(p));
                    boolean velocityApplied = Math.abs(dummy.getDeltaMovement().y() - 0.1) < 0.001;
                    expectTrue("15 号镇宅符：方块=" + blockPlaced + "、阵营=" + teamCaptured + "、初始友方集合=" + capturedFriends
                                    + "、弹开速度=" + dummy.getDeltaMovement() + "、手持=" + p.getInventory().getItem(HOTBAR).getCount(),
                            blockPlaced && teamCaptured && capturedFriends && velocityApplied && p.getInventory().getItem(HOTBAR).isEmpty());
                    var allegiance = p.getData(RelationAttachments.PLAYER).allegiance();
                    allegiance.setFriendly(hostileTeam, true);
                    dummy.setDeltaMovement(Vec3.ZERO);
                    ward.repel(p.serverLevel());
                    boolean followsNewFriend = dummy.getDeltaMovement().equals(Vec3.ZERO);
                    RepellingTalismanBlockEntity restoredWard = (RepellingTalismanBlockEntity) BlockEntity.loadStatic(wardPos,
                            p.serverLevel().getBlockState(wardPos), ward.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    allegiance.setFriendly(hostileTeam, false);
                    ward.repel(p.serverLevel());
                    boolean followsRemovedFriend = Math.abs(dummy.getDeltaMovement().y() - 0.1) < 0.001;
                    dummy.setDeltaMovement(Vec3.ZERO);
                    restoredWard.repel(p.serverLevel());
                    expectTrue("15 号友方集合：放置后随玩家增删变化，保存重读后保持独立集合",
                            followsNewFriend && followsRemovedFriend && dummy.getDeltaMovement().equals(Vec3.ZERO));
                    explosiveBase = repellingBase.offset(-1, 0, -1);
                    repellingBlocks.put(explosiveBase, p.serverLevel().getBlockState(explosiveBase));
                    repellingBlocks.put(explosiveBase.above(), p.serverLevel().getBlockState(explosiveBase.above()));
                    p.serverLevel().setBlockAndUpdate(explosiveBase, Blocks.STONE.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(explosiveBase.above(), Blocks.AIR.defaultBlockState());
                    p.getInventory().setItem(HOTBAR, talisman("itemfulu_42", 1, 2));
                    p.inventoryMenu.broadcastChanges();
                    useBlock(explosiveBase);
                }),
                new Action(4, p -> {
                    BlockPos at = explosiveBase.above();
                    expectTrue("42 号二阶符真实右键放置二阶爆裂方块并消耗一张",
                            p.serverLevel().getBlockState(at).is(block("fulutexiao_4202").getBlock())
                                    && p.getInventory().getItem(HOTBAR).isEmpty());
                    List<Float> powers = new ArrayList<>();
                    List<Explosion.BlockInteraction> interactions = new ArrayList<>();
                    Consumer<ExplosionEvent.Start> capture = event -> {
                        powers.add(event.getExplosion().radius());
                        interactions.add(event.getExplosion().getBlockInteraction());
                        event.setCanceled(true);
                    };
                    NeoForge.EVENT_BUS.addListener(capture);
                    try {
                        Snowball shot = new Snowball(p.serverLevel(), p);
                        for (String id : List.of("fulutexiao_42", "fulutexiao_4202", "fulutexiao_4203", "fulutexiao_4204")) {
                            BlockState state = block(id);
                            ExplosiveTalismanBlock talismanBlock = (ExplosiveTalismanBlock) state.getBlock();
                            talismanBlock.onProjectileHit(p.serverLevel(), state,
                                    new BlockHitResult(Vec3.atCenterOf(at), Direction.UP, at, false), shot);
                            talismanBlock.wasExploded(p.serverLevel(), at,
                                    new Explosion(p.serverLevel(), null, at.getX(), at.getY(), at.getZ(), 1, false, Explosion.BlockInteraction.DESTROY));
                        }
                    } finally {
                        NeoForge.EVENT_BUS.unregister(capture);
                    }
                    expectTrue("42 号四档投射物与爆炸回调：半径 4/8/12/16，固定 DESTROY_WITH_DECAY（受控取消爆炸）",
                            powers.equals(List.of(4f, 4f, 8f, 8f, 12f, 12f, 16f, 16f))
                                    && interactions.stream().allMatch(mode -> mode == Explosion.BlockInteraction.DESTROY_WITH_DECAY));
                }),
                new Action(8, p -> {
                    p.closeContainer();
                    stoneMillShift = minecraft.options.keyShift.isDown();
                    stoneMillPos = prepareStoneMill(p, p.blockPosition().offset(2, 0, 0));
                    stoneRollerPos = prepareStoneMill(p, p.blockPosition().offset(-2, 0, 0));
                    p.serverLevel().setBlockAndUpdate(stoneMillPos, block("stonemill"));
                    p.serverLevel().setBlockAndUpdate(stoneRollerPos, block("stoneroller"));
                    p.getInventory().setItem(HOTBAR, ItemStack.EMPTY);
                    p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    p.inventoryMenu.broadcastChanges();
                    stoneMillDropListener = event -> {
                        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity drop
                                && drop.position().distanceToSqr(Vec3.atCenterOf(stoneMillPos)) < 16) stoneMillDrops.add(drop);
                    };
                    NeoForge.EVENT_BUS.addListener(stoneMillDropListener);
                    stoneMillSneak(true);
                }),
                new Action(8, p -> {
                    expectTrue("石磨开窗前客户端潜行输入已同步到服务端", p.isShiftKeyDown());
                    useBlock(stoneMillPos);
                }),
                new Action(8, p -> {
                    expectTrue("真实潜行右键石磨打开双端三槽菜单与中文原生界面",
                            p.containerMenu instanceof StoneMillMenu && p.containerMenu.slots.size() == 39
                                    && minecraft.player.containerMenu instanceof StoneMillMenu
                                    && minecraft.screen instanceof StoneMillScreen
                                    && !Component.translatable("gui.yvanchuxiuzhen.stonemill_gui.label_yan_mo")
                                            .getString().equals("gui.yvanchuxiuzhen.stonemill_gui.label_yan_mo"));
                    minecraft.execute(() -> screenshots.take("craft-stonemill-menu.png"));
                }),
                new Action(8, p -> {
                    p.closeContainer();
                    stoneMillSneak(false);
                    StoneMillBlockEntity mill = stoneMill(p, stoneMillPos);
                    mill.setItem(0, item("fuluzhushablock"));
                    mill.setItem(1, ItemStack.EMPTY);
                    mill.setItem(2, ItemStack.EMPTY);
                    mill.changed();
                }),
                new Action(4, p -> useBlock(stoneMillPos)),
                new Action(4, p -> {
                    StoneMillBlockEntity mill = stoneMill(p, stoneMillPos);
                    expectTrue("石磨普通右键：1份辰砂耗尽仍产生4份朱砂，保留来源主料初始数量缓存",
                            !p.isShiftKeyDown() && mill.getItem(0).isEmpty()
                                    && mill.getItem(1).is(item("fuluzhushaitem").getItem()) && mill.getItem(1).getCount() == 4);
                    mill.setItem(1, item("fuluzhushaitem", 3));
                    mill.changed();
                }),
                new Action(4, p -> useBlock(stoneMillPos)),
                new Action(4, p -> {
                    int dropped = stoneMillDrops.stream().filter(drop -> drop.getItem().is(item("fuluzhushaitem").getItem()))
                            .mapToInt(drop -> drop.getItem().getCount()).sum();
                    expectTrue("石磨旧成品3件被复制成三份完整栈，共掉出9件且清空成品槽（实际 " + dropped + "）",
                            dropped == 9 && stoneMill(p, stoneMillPos).getItem(1).isEmpty());
                    stoneMillDrops.forEach(Entity::discard);
                    stoneMillDrops.clear();
                    StoneMillBlockEntity roller = stoneMill(p, stoneRollerPos);
                    roller.setItem(0, item("shicai_046", 4));
                    roller.setItem(1, ItemStack.EMPTY);
                    roller.setItem(2, item("foodwan"));
                    roller.changed();
                }),
                new Action(4, p -> useBlock(stoneRollerPos)),
                new Action(4, p -> {
                    StoneMillBlockEntity roller = stoneMill(p, stoneRollerPos);
                    expectTrue("石碾普通右键：4主料与1碗只产1份，后续轮次重读辅料阻止继续产出",
                            roller.getItem(0).isEmpty() && roller.getItem(2).isEmpty()
                                    && roller.getItem(1).is(item("food_039").getItem()) && roller.getItem(1).getCount() == 1);
                    roller.setItem(0, item("shicai_046", 5));
                    roller.setItem(1, item("food_039"));
                    roller.setItem(2, item("foodwan", 2));
                    roller.changed();
                    StoneMillBlockEntity restored = (StoneMillBlockEntity) BlockEntity.loadStatic(stoneRollerPos,
                            roller.getBlockState(), roller.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    boolean allSlots = restored != null;
                    for (int slot = 0; slot < 3 && allSlots; slot++) allSlots &= ItemStack.matches(restored.getItem(slot), roller.getItem(slot));
                    boolean directions = true;
                    for (Direction direction : Direction.values()) {
                        IItemHandler sided = p.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, stoneRollerPos, direction);
                        directions &= sided != null && sided.getSlots() == 3
                                && sided.insertItem(0, item("shicai_046"), true).isEmpty()
                                && sided.insertItem(2, item("foodwan"), true).isEmpty()
                                && sided.insertItem(1, item("food_039"), true).getCount() == 1
                                && sided.extractItem(0, 1, true).isEmpty() && sided.extractItem(2, 1, true).isEmpty()
                                && sided.extractItem(1, 1, true).getCount() == 1;
                    }
                    expectTrue("石碾三槽保存重读一致，六方向能力只插0/2并只取成品1", allSlots && directions);
                }),
                new Action(4, p -> {
                    loomPos = prepareStoneMill(p, p.blockPosition().offset(0, 0, 2));
                    p.serverLevel().setBlockAndUpdate(loomPos, block("zhibuji"));
                    p.getInventory().clearContent();
                    p.getInventory().selected = HOTBAR;
                    p.setItemInHand(InteractionHand.MAIN_HAND, item("mianitem", 2));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(4, p -> useBlock(loomPos)),
                new Action(4, p -> {
                    ItemStack cloth = inventoryItem(p, "cailiao_171");
                    expectTrue("织布机真实右键：两份棉花只扣一份、只产一份4级灵布，且不打开菜单",
                            p.getMainHandItem().getCount() == 1 && cloth.getCount() == 1
                                    && Materials.instance(cloth).level() == 4
                                    && p.containerMenu == p.inventoryMenu);
                    p.getInventory().clearContent();
                    p.setItemInHand(InteractionHand.MAIN_HAND, material("cailiao_209", 40));
                    p.getInventory().setItem(1, material("cailiao_219", 40));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(4, p -> useBlock(loomPos)),
                new Action(4, p -> {
                    ItemStack cloth = inventoryItem(p, "cailiao_179");
                    expectTrue("织布机按ID匹配带实例组件的两种金属，各扣一件并给同品阶41级千丝锦两件",
                            inventoryItem(p, "cailiao_209").isEmpty() && inventoryItem(p, "cailiao_219").isEmpty()
                                    && cloth.getCount() == 2 && Materials.instance(cloth).level() == 41);
                    p.getInventory().clearContent();
                    p.setItemInHand(InteractionHand.MAIN_HAND, item("thundergrass"));
                    p.getInventory().setItem(1, material("cailiao_178", 99));
                    p.getInventory().setItem(2, material("cailiao_179", 1));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(4, p -> useBlock(loomPos)),
                new Action(4, p -> {
                    ItemStack cloth = inventoryItem(p, "cailiao_243");
                    expectTrue("云雷锦按布ID优先取千丝锦，忽略实例等级，保留99级浣花绫并产41级云雷锦",
                            p.getMainHandItem().isEmpty() && inventoryItem(p, "cailiao_179").isEmpty()
                                    && inventoryItem(p, "cailiao_178").getCount() == 1
                                    && cloth.getCount() == 1 && Materials.instance(cloth).level() == 41);
                    p.getInventory().clearContent();
                    p.setItemInHand(InteractionHand.OFF_HAND, item("mianitem", 2));
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(4, p -> minecraft.execute(() -> minecraft.gameMode.useItemOn(minecraft.player,
                        InteractionHand.OFF_HAND, new BlockHitResult(Vec3.atCenterOf(loomPos), Direction.UP, loomPos, false)))),
                new Action(4, p -> {
                    expectTrue("织布机副手点击仍读取空主手，副手棉花不消耗且不产布",
                            p.getOffhandItem().getCount() == 2 && inventoryItem(p, "cailiao_171").isEmpty());
                    p.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                    LoomBlockEntity loom = (LoomBlockEntity) p.serverLevel().getBlockEntity(loomPos);
                    loom.setItem(0, item("silkthread", 3));
                    LoomBlockEntity restored = (LoomBlockEntity) BlockEntity.loadStatic(loomPos, loom.getBlockState(),
                            loom.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    boolean directions = true;
                    for (Direction direction : Direction.values()) {
                        IItemHandler handler = p.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, loomPos, direction);
                        directions &= handler != null && handler.getSlots() == 1
                                && handler.extractItem(0, 1, true).getCount() == 1
                                && handler.insertItem(0, item("silkthread"), true).isEmpty();
                    }
                    expectTrue("织布机独立单槽保存重读一致，六方向均可插取，未被主手加工使用",
                            restored != null && ItemStack.matches(restored.getItem(0), loom.getItem(0)) && directions);
                    loom.setItem(0, new ItemStack(Items.IRON_SWORD, 3));
                    StoneMillBlockEntity mill = stoneMill(p, stoneMillPos);
                    mill.setItem(0, new ItemStack(Items.IRON_SWORD, 3));
                    expectTrue("石磨与织布机强制赋槽保留来源容器64件上限，不按单件物品上限截断",
                            loom.getItem(0).getCount() == 3 && mill.getItem(0).getCount() == 3);
                }),
                new Action(8, p -> {
                    carpentryPos = prepareStoneMill(p, p.blockPosition().offset(0, 0, -2));
                    p.serverLevel().setBlockAndUpdate(carpentryPos, block("mugongtai"));
                    p.getInventory().clearContent();
                    p.getInventory().setItem(0, item("beechplanks", 8));
                    p.getInventory().selected = HOTBAR;
                    p.getInventory().setItem(HOTBAR, new ItemStack(Items.IRON_SWORD));
                    p.inventoryMenu.broadcastChanges();
                    stoneMillSneak(true);
                }),
                new Action(8, p -> useBlock(carpentryPos)),
                new Action(8, p -> {
                    expectTrue("木匠台持物潜行真实右键打开双端78槽菜单与43按钮",
                            p.containerMenu instanceof CarpentryMenu && p.containerMenu.slots.size() == 78
                                    && minecraft.player.containerMenu instanceof CarpentryMenu
                                    && minecraft.screen instanceof CarpentryScreen screen
                                    && screen.children().stream().filter(Button.class::isInstance).count() == 43);
                    stoneMillSneak(false);
                    carpentryButton(1);
                }),
                new Action(8, p -> {
                    CarpentryBlockEntity table = carpentry(p);
                    expectTrue("木匠选择首项同步预览与8榉木成本，不消耗材料",
                            table.getItem(40).is(item("beechfurniture_01").getItem())
                                    && table.requirementCount(0) == 8 && inventoryItem(p, "beechplanks").getCount() == 8
                                    && minecraft.player.containerMenu.getSlot(40).getItem().is(item("beechfurniture_01").getItem()));
                    minecraft.execute(() -> screenshots.take("craft-carpentry-menu.png"));
                    carpentryButton(0);
                }),
                new Action(8, p -> {
                    expectTrue("木匠制作扣8木板、共享结果1件并同步两端",
                            inventoryItem(p, "beechplanks").isEmpty()
                                    && carpentry(p).getItem(41).is(item("beechfurniture_01").getItem())
                                    && carpentry(p).getItem(41).getCount() == 1
                                    && minecraft.player.containerMenu.getSlot(41).getItem().getCount() == 1);
                    minecraft.execute(() -> minecraft.gameMode.handleInventoryMouseClick(
                            minecraft.player.containerMenu.containerId, 41, 0, ClickType.QUICK_MOVE, minecraft.player));
                }),
                new Action(8, p -> {
                    expectTrue("木匠成品经原生shift点击领取，机器结果清空",
                            carpentry(p).getItem(41).isEmpty() && inventoryItem(p, "beechfurniture_01").getCount() == 1);
                    p.getInventory().clearContent();
                    p.getInventory().setItem(0, item("beechplanks", 8));
                    p.getInventory().setItem(1, item("book_005"));
                    p.containerMenu.broadcastChanges();
                    carpentryButton(19);
                }),
                new Action(6, p -> carpentryButton(0)),
                new Action(8, p -> {
                    expectTrue("木匠五行数量跨材料累计：8木板加1书可制作要求2书的书柜",
                            carpentry(p).getItem(41).is(item("beechfurniture_19").getItem())
                                    && inventoryItem(p, "beechplanks").isEmpty() && inventoryItem(p, "book_005").isEmpty());
                    p.getInventory().setItem(0, item("beechplanks", 8));
                    p.containerMenu.broadcastChanges();
                    carpentryButton(0);
                }),
                new Action(8, p -> {
                    expectTrue("木匠累计数量不能代替材料存在性：完全缺书不扣木板、不增加结果",
                            inventoryItem(p, "beechplanks").getCount() == 8 && carpentry(p).getItem(41).getCount() == 1);
                    carpentryButton(41);
                }),
                new Action(6, p -> {
                    expectTrue("木匠切类别保留旧预览与成本，同时刷新两项杂物候选",
                            carpentry(p).getItem(40).is(item("beechfurniture_19").getItem())
                                    && carpentry(p).requirementId(1).endsWith(":book_005")
                                    && carpentry(p).getItem(0).is(item("itemdanyaohulu_04").getItem())
                                    && carpentry(p).getItem(2).isEmpty());
                    carpentryButton(1);
                }),
                new Action(6, p -> {
                    p.getInventory().clearContent();
                    p.getInventory().setItem(0, new ItemStack(Items.OAK_PLANKS, 6));
                    p.containerMenu.broadcastChanges();
                    carpentryButton(0);
                }),
                new Action(8, p -> {
                    expectTrue("木匠换配方覆盖旧成品物种并沿用数量，单件小盒子两端均保留2件",
                            carpentry(p).getItem(41).is(item("itemdanyaohulu_04").getItem())
                                    && carpentry(p).getItem(41).getCount() == 2
                                    && minecraft.player.containerMenu.getSlot(41).getItem().getCount() == 2
                                    && p.getInventory().getItem(0).getCount() == 3);
                    CarpentryBlockEntity table = carpentry(p);
                    table.setItem(41, item("itemdanyaohulu_04", 64));
                    table.changed();
                    carpentryButton(0);
                }),
                new Action(8, p -> {
                    CarpentryBlockEntity table = carpentry(p);
                    CarpentryBlockEntity restored = (CarpentryBlockEntity) BlockEntity.loadStatic(carpentryPos,
                            table.getBlockState(), table.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    boolean directions = true;
                    for (Direction direction : Direction.values()) {
                        IItemHandler handler = p.serverLevel().getCapability(Capabilities.ItemHandler.BLOCK, carpentryPos, direction);
                        directions &= handler != null && handler.insertItem(41, item("itemdanyaohulu_04"), true).getCount() == 1
                                && handler.extractItem(40, 1, true).isEmpty() && handler.extractItem(41, 1, true).getCount() == 1;
                    }
                    expectTrue("木匠结果满64仍扣料，42槽与选择保存读回、六面只抽结果",
                            p.getInventory().getItem(0).isEmpty() && table.getItem(41).getCount() == 64
                                    && minecraft.player.containerMenu.getSlot(41).getItem().getCount() == 64
                                    && restored != null && restored.getItem(41).getCount() == 64
                                    && restored.requirementCount(0) == 3 && directions && table.handler(null).getSlots() == 42);
                    p.closeContainer();
                    silkReelPos = prepareStoneMill(p, p.blockPosition().offset(1, 0, -3));
                    silkInputPos = prepareStoneMill(p, silkReelPos.north().below());
                    p.serverLevel().setBlockAndUpdate(silkReelPos, block("saosilun")
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
                    p.serverLevel().setBlockAndUpdate(silkInputPos, Blocks.CHEST.defaultBlockState());
                    ((Container) p.serverLevel().getBlockEntity(silkInputPos)).setItem(0, item("silkwormchrysalisgold", 2));
                    silkDropListener = event -> {
                        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ItemEntity drop
                                && drop.position().distanceToSqr(Vec3.atCenterOf(silkReelPos)) < 4) silkDrops.add(drop);
                    };
                    NeoForge.EVENT_BUS.addListener(silkDropListener);
                    p.getInventory().clearContent();
                    p.inventoryMenu.broadcastChanges();
                }),
                new Action(30, p -> useBlock(silkReelPos)),
                new Action(80, p -> {
                    SilkReelBlockEntity reel = silkReel(p);
                    double before = reel.getPersistentData().getDouble("tick");
                    useBlock(silkReelPos);
                    expectTrue("缫丝启动后使用既有周期且忙碌不重置进度，未提前产出",
                            reel.getPersistentData().getBoolean("saosi") && before >= 1 && silkDrops.isEmpty());
                    p.serverLevel().setBlockAndUpdate(silkReelPos, reel.getBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST));
                }),
                new Action(4, p -> {
                    SilkReelBlockEntity reel = silkReel(p);
                    expectTrue("缫丝第5周期沿启动方向取槽0金蛹，无热源仍扣1并只出1金丝，完成复位",
                            ((Container) p.serverLevel().getBlockEntity(silkInputPos)).getItem(0).getCount() == 1
                                    && silkDrops.stream().filter(drop -> drop.getItem().is(item("silkthreadgold").getItem()))
                                            .mapToInt(drop -> drop.getItem().getCount()).sum() == 1
                                    && !reel.getPersistentData().getBoolean("saosi") && reel.getPersistentData().getDouble("tick") == 0);
                    silkDrops.forEach(Entity::discard);
                    silkDrops.clear();
                    ((Container) p.serverLevel().getBlockEntity(silkInputPos)).clearContent();
                    p.serverLevel().setBlockAndUpdate(silkInputPos, Blocks.WATER_CAULDRON.defaultBlockState());
                    p.serverLevel().setBlockAndUpdate(silkInputPos.below(), Blocks.CAMPFIRE.defaultBlockState()
                            .setValue(BlockStateProperties.LIT, false));
                    p.serverLevel().setBlockAndUpdate(silkReelPos, reel.getBlockState()
                            .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
                    ItemEntity drop = new ItemEntity(p.serverLevel(), silkInputPos.getX() + .5,
                            silkInputPos.getY() + .5, silkInputPos.getZ() + .5, item("silkwormchrysalisred", 3));
                    drop.setDeltaMovement(Vec3.ZERO);
                    p.serverLevel().addFreshEntity(drop);
                    silkDrops.add(drop);
                }),
                new Action(108, p -> useBlock(silkReelPos)),
                new Action(4, p -> {
                    SilkReelBlockEntity reel = silkReel(p);
                    expectTrue("缫丝水釜路径认可未点燃营火，整栈3红蛹移除只产1红丝且不耗水",
                            silkDrops.stream().filter(drop -> drop.getItem().is(item("silkwormchrysalisred").getItem())).noneMatch(Entity::isAlive)
                                    && silkDrops.stream().filter(drop -> drop.getItem().is(item("silkthreadred").getItem()))
                                            .mapToInt(drop -> drop.getItem().getCount()).sum() == 1
                                    && p.serverLevel().getBlockState(silkInputPos).getValue(BlockStateProperties.LEVEL_CAULDRON) == 1);
                    reel.setItem(8, new ItemStack(Items.IRON_SWORD, 3));
                    reel.getPersistentData().putBoolean("saosi", true);
                    reel.getPersistentData().putDouble("tick", 3);
                    reel.getPersistentData().putString("fangxiang", "south");
                    SilkReelBlockEntity restored = (SilkReelBlockEntity) BlockEntity.loadStatic(silkReelPos,
                            reel.getBlockState(), reel.saveWithFullMetadata(p.registryAccess()), p.registryAccess());
                    expectTrue("缫丝九槽与忙碌进度/方向完整保存读回，单件物品仍保留3件",
                            restored != null && restored.getItem(8).getCount() == 3
                                    && restored.getPersistentData().getBoolean("saosi")
                                    && restored.getPersistentData().getDouble("tick") == 3
                                    && restored.getPersistentData().getString("fangxiang").equals("south"));
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

    private static ItemStack inventoryItem(ServerPlayer player, String path) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty() && stack.is(item(path).getItem())) return stack;
        }
        return ItemStack.EMPTY;
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

    private BlockPos prepareStoneMill(ServerPlayer player, BlockPos pos) {
        for (int offset = -1; offset <= 1; offset++) {
            BlockPos at = pos.above(offset);
            stoneMillBlocks.put(at, player.serverLevel().getBlockState(at));
            BlockEntity original = player.serverLevel().getBlockEntity(at);
            if (original != null) {
                stoneMillEntities.put(at, original.saveWithFullMetadata(player.registryAccess()));
                if (original instanceof Container container) container.clearContent();
            }
            player.serverLevel().setBlockAndUpdate(at, offset == -1 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        return pos;
    }

    private CarpentryBlockEntity carpentry(ServerPlayer player) {
        return (CarpentryBlockEntity) player.serverLevel().getBlockEntity(carpentryPos);
    }

    private SilkReelBlockEntity silkReel(ServerPlayer player) {
        return (SilkReelBlockEntity) player.serverLevel().getBlockEntity(silkReelPos);
    }

    private void carpentryButton(int operation) {
        minecraft.execute(() -> {
            if (minecraft.screen instanceof CarpentryScreen screen) {
                screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .skip(operation).findFirst().orElseThrow().onPress();
            }
        });
    }

    private static StoneMillBlockEntity stoneMill(ServerPlayer player, BlockPos pos) {
        return (StoneMillBlockEntity) player.serverLevel().getBlockEntity(pos);
    }

    private void stoneMillSneak(boolean down) {
        minecraft.execute(() -> {
            if (minecraft.options.keyShift.isDown() != down) minecraft.options.keyShift.setDown(down || minecraft.options.toggleCrouch().get());
        });
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        player.closeContainer();
        if (stoneMillDropListener != null) NeoForge.EVENT_BUS.unregister(stoneMillDropListener);
        if (silkDropListener != null) NeoForge.EVENT_BUS.unregister(silkDropListener);
        silkDrops.forEach(Entity::discard);
        if (stoneMillPos != null) {
            stoneMillSneak(stoneMillShift);
            stoneMillDrops.forEach(Entity::discard);
            for (Map.Entry<BlockPos, BlockState> entry : stoneMillBlocks.entrySet()) {
                BlockEntity current = player.serverLevel().getBlockEntity(entry.getKey());
                if (current instanceof Container container) container.clearContent();
                player.serverLevel().setBlockAndUpdate(entry.getKey(), entry.getValue());
                CompoundTag original = stoneMillEntities.get(entry.getKey());
                if (original != null) {
                    BlockEntity restored = BlockEntity.loadStatic(entry.getKey(), entry.getValue(), original, player.registryAccess());
                    player.serverLevel().setBlockEntity(restored);
                    restored.setChanged();
                    player.serverLevel().sendBlockUpdated(entry.getKey(), entry.getValue(), entry.getValue(), 3);
                }
            }
        }
        player.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(naturalRegeneration, player.server);
        if (volleyListener != null) NeoForge.EVENT_BUS.unregister(volleyListener);
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (oreAt != null) {
            player.serverLevel().setBlockAndUpdate(oreAt, oreBefore);
        }
        if (dummy != null) {
            dummy.discard();
            player.serverLevel().getEntities(EntityTypeTest.forClass(CloudSpirit.class), mob -> true).forEach(mob -> mob.discard());
            player.removeAllEffects();
            player.serverLevel().getEntities(EntityTypeTest.forClass(TalismanProjectile.class), shot -> true).forEach(shot -> shot.discard());
            player.setGameMode(gameMode);
            player.getServer().setDifficulty(difficulty, true);
            player.setHealth(health);
            Cultivation.set(player, CultivationValue.SOUL, soul);
            Cultivation.editTizhi(player, tizhi -> talismanState.forEach((key, value) -> {
                if (key.startsWith("fuluxiaoguo")) {
                    tizhi.putBoolean(key, value != 0);
                } else {
                    tizhi.putDouble(key, value);
                }
            }));
        }
        clusterAir.forEach((pos, state) -> player.serverLevel().setBlockAndUpdate(pos, state));
        if (repellingBase != null) repellingBlocks.forEach((pos, state) -> player.serverLevel().setBlockAndUpdate(pos, state));
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
