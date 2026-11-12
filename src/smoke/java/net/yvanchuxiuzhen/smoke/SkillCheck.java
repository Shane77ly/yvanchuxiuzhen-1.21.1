package net.yvanchuxiuzhen.smoke;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStudiedEvent;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillLibrary;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.SkillText;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 技能框架的实机检查：技能书生成与品阶文字、研习条件、技能栏的选择与翻页、右键施放（武器类别、沉默、灵力、冷却、禁施、连招）、
 * 冷却累加与递减、命令生成、藏宝技能书与研习附赠、霸体与禁施的属性成对加减，以及死亡与重进后的保留。
 */
final class SkillCheck extends SystemCheck {
    private static final String GOLD_BOOK = "yvanchuxiuzhen:book_003 金芒术 1 1 金 发射一枚有穿透的金属性飞弹。 无 1 1.1 1 0 5 -4 0 0 0 0 20 0 0 0 0 0";
    private static final String WATER_BOOK = "yvanchuxiuzhen:book_005 水蛇鞭 2 2 水 前方召唤一个水鞭。 无 1 1.2 1.2 0 0 3 0 0 0 0 0 10 0 0 0 0";
    private static final String SEAL_BOOK = "yvanchuxiuzhen:book_012 乱枝 3 3 木 生长出一堆枯枝。 无 1 1 1 20 0 0 5 0 0 0 0 0 30 0 0 0";
    private static final String SWORD_BOOK = "yvanchuxiuzhen:book_007 剑诀 1 1 无 需要剑。 剑 1 1 1 0 0 0 0 5 0 0 0 0 0 20 0";

    private double speedBefore;
    private double resistanceBefore;

    SkillCheck(List<String> report, String levelId) {
        super("技能", report, levelId);
    }

    private static ItemStack book(SkillKind kind, String arguments) {
        return Skills.createBook(kind, List.of(arguments.split(" ")));
    }

    private static ItemStack studied(ServerPlayer player, ItemStack stack) {
        SkillBook book = Skills.book(stack).orElseThrow();
        stack.set(SkillData.SKILL.get(), book.withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    private static String selectedName(ServerPlayer player) {
        return Skills.book(Skills.selected(player)).map(SkillBook::name).orElse("");
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                ItemStack gold = book(SkillKind.SPELL, GOLD_BOOK);
                SkillBook parsed = Skills.book(gold).orElseThrow();
                expect("技能书的物品", "yvanchuxiuzhen:book_003", BuiltInRegistries.ITEM.getKey(gold.getItem()).toString());
                expect("技能名", "金芒术", parsed.name());
                expect("伤害倍率", 1.1, parsed.damageMultiplier());
                expect("金组冷却秒数", 5.0, parsed.cooldown(SkillElement.GOLD));
                expect("水组冷却秒数（负数缩短）", -4.0, parsed.cooldown(SkillElement.WATER));
                expect("金组冷却上限秒数", 20.0, parsed.cooldownIgnore(SkillElement.GOLD));
                expect("品阶文字 1", "黄阶一级", SkillText.grade(1));
                expect("品阶文字 35", "天阶五级", SkillText.grade(35));
                expect("品阶文字 41", "超品", SkillText.grade(41));
                expect("境界文字 10", "练气圆满", SkillText.level(10));
                expect("境界文字 22", "结丹二转", SkillText.level(22));
                expect("境界文字 41", "化神", SkillText.level(41));
                expectTrue("无法识别的承载物品得到空栈", Skills.createBook(SkillKind.SPELL, List.of("minecraft:air", "x")).isEmpty());
                ItemStack movement = book(SkillKind.MOVEMENT,
                        "yvanchuxiuzhen:book_012 分形捉影 21 11 无 双击W时快速弹射。 20 60 20 60 1.5 0 0 0 0");
                expect("身法参数 shenfaWjump", 1.5, Skills.book(movement).orElseThrow().number("shenfaWjump"));
                ItemStack flight = book(SkillKind.FLIGHT, "yvanchuxiuzhen:book_012 御风 5 5 无 飞行。 2 1.5 云雾");
                expect("轻功特效", "云雾", Skills.book(flight).orElseThrow().text("flyskilltexiao"));
                ItemStack weapon = book(SkillKind.WEAPON_ART, "yvanchuxiuzhen:book_012 劈砍 5 5 无 挥剑。 剑 sword01 10");
                expect("武器技次数上限", 10.0, Skills.book(weapon).orElseThrow().number("smanshipnumMax"));

                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                Cultivation.set(player, CultivationValue.ROOT_GOLD, 0);
                Skills.study(player, gold);
                expectTrue("境界不足不能研习", Skills.book(gold).orElseThrow().owner().isEmpty());
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 5);
                Skills.study(player, gold);
                expectTrue("没有金灵根不能研习", Skills.book(gold).orElseThrow().owner().isEmpty());
                Cultivation.set(player, CultivationValue.ROOT_GOLD, 1);
                Skills.study(player, gold);
                expectTrue("有金灵根后研习成功", Skills.book(gold).orElseThrow().ownedBy(player.getUUID()));
                ItemStack others = book(SkillKind.SPELL, GOLD_BOOK);
                UUID stranger = UUID.randomUUID();
                others.set(SkillData.SKILL.get(), Skills.book(others).orElseThrow().withOwner(stranger, "别人"));
                Skills.study(player, others);
                expect("他人已研习的书不变", stranger, Skills.book(others).orElseThrow().owner().orElseThrow());
            });
            case 1 -> onServer(player -> {
                ItemStack gold = studied(player, book(SkillKind.SPELL, GOLD_BOOK));
                ItemStack water = studied(player, book(SkillKind.SPELL, WATER_BOOK));
                ItemStack unowned = book(SkillKind.SPELL, SEAL_BOOK);
                Skills.setSpell(player, 1, gold);
                Skills.setSpell(player, 2, water);
                Skills.setSpell(player, 3, unowned);
                SkillSelection.select(player, 1);
                expect("选择第 1 键", "金芒术", selectedName(player));
                expect("选择序号", 1, Skills.state(player).selectedSlot());
                SkillSelection.select(player, 1);
                expect("同键再按取消", "", selectedName(player));
                expect("取消后序号清零", 0, Skills.state(player).selectedSlot());
                SkillSelection.select(player, 3);
                expect("未研习的书不能选择", "", selectedName(player));
                SkillSelection.select(player, 2);
                expect("选择第 2 键", "水蛇鞭", selectedName(player));
                SkillSelection.nextPage(player);
                expect("Tab 翻到第二页", 2, Skills.state(player).page());
                SkillSelection.select(player, 1);
                expect("第二页第 1 键是空槽，原选择不变", "水蛇鞭", selectedName(player));
                SkillSelection.turnPage(player, -1);
                expect("下一页按钮反向回到第一页", 1, Skills.state(player).page());
                SkillSelection.turnPage(player, -1);
                expect("第一页再上翻到第四页", 4, Skills.state(player).page());
                SkillSelection.turnPage(player, 1);
                SkillSelection.clear(player);
                expect("B 取消选择", "", selectedName(player));
                StatusEffects.add(player, "effect_002", 100, 0);
                SkillSelection.select(player, 2);
                expect("沉默时选择第 2 键无效", "", selectedName(player));
                StatusEffects.remove(player, "effect_002");
                SkillSelection.select(player, 1);
                expect("沉默解除后重选第 1 键", "金芒术", selectedName(player));
                SkillSelection.toggleCombo(player);
                expectTrue("连招开关打开", Skills.state(player).combo());
                SkillSelection.toggleCombo(player);
                expectTrue("连招开关关闭", !Skills.state(player).combo());
            });
            case 2 -> onServer(player -> {
                Cultivation.set(player, CultivationValue.POWER, 100);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 1, studied(player, book(SkillKind.SPELL, GOLD_BOOK)));
                SkillSelection.clear(player);
                SkillSelection.select(player, 1);
                expect("选中金芒术", "金芒术", selectedName(player));
                double cost = PlayerAttributes.baseValue(player, Stat.MANA_ATT) * 1;
                SkillCasting.onRightClick(player);
                expect("施放扣灵力 法术攻击基础值 × 消耗系数", 100 - cost, Cultivation.get(player, CultivationValue.POWER));
                SkillCooldowns cooldowns = player.getData(SkillData.COOLDOWNS);
                expect("金组冷却 5 秒", 100.0, cooldowns.get(SkillElement.GOLD));
                expect("水组冷却被负值缩短 −4 秒", -80.0, cooldowns.get(SkillElement.WATER));
                SkillCasting.onRightClick(player);
                expect("冷却不超过上限时可再次施放", 100 - 2 * cost, Cultivation.get(player, CultivationValue.POWER));
                expect("金组冷却累加", 200.0, player.getData(SkillData.COOLDOWNS).get(SkillElement.GOLD));
                player.setData(SkillData.COOLDOWNS, player.getData(SkillData.COOLDOWNS).with(SkillElement.GOLD, 500));
                SkillCasting.onRightClick(player);
                expect("冷却超过上限不能施放", 100 - 2 * cost, Cultivation.get(player, CultivationValue.POWER));
                SkillCasting.tick(player);
                expect("每刻冷却 −1", 499.0, player.getData(SkillData.COOLDOWNS).get(SkillElement.GOLD));
                expect("负冷却归零", 0.0, player.getData(SkillData.COOLDOWNS).get(SkillElement.WATER));
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                StatusEffects.add(player, "effect_002", 100, 0);
                SkillCasting.onRightClick(player);
                expect("沉默时不能施放", 100 - 2 * cost, Cultivation.get(player, CultivationValue.POWER));
                StatusEffects.remove(player, "effect_002");
                Cultivation.set(player, CultivationValue.POWER, cost / 2);
                SkillCasting.onRightClick(player);
                expect("灵力不足不能施放", cost / 2, Cultivation.get(player, CultivationValue.POWER));
                Cultivation.set(player, CultivationValue.POWER, 100);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
                SkillCasting.onRightClick(player);
                expect("手持非武器不能施放", 100.0, Cultivation.get(player, CultivationValue.POWER));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 4, studied(player, book(SkillKind.SPELL, SWORD_BOOK)));
                SkillSelection.select(player, 4);
                expect("选中剑诀", "剑诀", selectedName(player));
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                SkillCasting.onRightClick(player);
                expect("剑类技能手持剑可施放", 100 - cost, Cultivation.get(player, CultivationValue.POWER));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                SkillCasting.onRightClick(player);
                expect("剑类技能手持斧不能施放", 100 - cost, Cultivation.get(player, CultivationValue.POWER));
            });
            case 3 -> onServer(player -> {
                Cultivation.set(player, CultivationValue.POWER, 100);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 3, studied(player, book(SkillKind.SPELL, SEAL_BOOK)));
                SkillSelection.select(player, 3);
                expect("选中乱枝", "乱枝", selectedName(player));
                speedBefore = player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
                double cost = PlayerAttributes.baseValue(player, Stat.MANA_ATT) * 1;
                SkillCasting.onRightClick(player);
                expectTrue("施放后进入施法中", StatusEffects.has(player, "skilleffect_09"));
                expect("施法中移动速度基础值 −10", speedBefore - 10, player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
                expect("禁施技能扣灵力", 100 - cost, Cultivation.get(player, CultivationValue.POWER));
                expect("木组冷却 5 秒", 100.0, player.getData(SkillData.COOLDOWNS).get(SkillElement.TREE));
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                SkillCasting.onRightClick(player);
                expect("施法中不能再次施放", 100 - cost, Cultivation.get(player, CultivationValue.POWER));
                SkillSelection.select(player, 2);
                expect("施法中选择第 2 键无效", "乱枝", selectedName(player));
                player.removeEffect(StatusEffects.holder("skilleffect_09"));
                expect("施法中结束，移动速度还原", speedBefore, player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
                resistanceBefore = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue();
                StatusEffects.add(player, "skilleffect_08", 200, 0);
                expect("霸体击退抗性基础值 +10", resistanceBefore + 10, player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
                expectTrue("霸体附加发光", player.hasEffect(MobEffects.GLOWING));
                StatusEffects.add(player, "skilleffect_08", 300, 1);
                expect("霸体刷新先还原再应用，仍是 +10", resistanceBefore + 10, player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
                StatusEffects.remove(player, "skilleffect_08");
                expect("霸体移除后还原", resistanceBefore, player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
                player.removeEffect(MobEffects.GLOWING);
                StatusEffects.add(player, "skilleffect_08", 3, 0);
            });
            case 4 -> {
                if (local < 15) {
                    return false;
                }
                onServer(player -> {
                    expect("霸体到期后击退抗性还原", resistanceBefore, player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue());
                    player.removeEffect(MobEffects.GLOWING);
                    Cultivation.set(player, CultivationValue.POWER, 100);
                    player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                    Skills.setSpell(player, 1, studied(player, book(SkillKind.SPELL, GOLD_BOOK)));
                    Skills.setSpell(player, 2, studied(player, book(SkillKind.SPELL, WATER_BOOK)));
                    SkillSelection.clear(player);
                    SkillSelection.select(player, 1);
                    expect("连招前选中金芒术", "金芒术", selectedName(player));
                    SkillSelection.toggleCombo(player);
                    SkillCasting.onRightClick(player);
                    expect("连招：施放后换到当前页下一个键", "水蛇鞭", selectedName(player));
                    expect("连招：选择序号", 2, Skills.state(player).selectedSlot());
                    SkillSelection.toggleCombo(player);
                });
            }
            case 5 -> onServer(player -> {
                Set<String> names = new HashSet<>();
                boolean unowned = true;
                for (int index = 0; index < 600; index++) {
                    SkillBook treasureBook = Skills.book(SkillLibrary.treasure(player.getRandom())).orElseThrow();
                    unowned &= treasureBook.owner().isEmpty();
                    names.add(treasureBook.name());
                }
                expectTrue("藏宝技能书都未研习", unowned);
                expect("藏宝技能书 13 本都能取到", 13, names.size());
                expectTrue("藏宝含金芒术", names.contains("金芒术"));
                expectTrue("藏宝含飞石术", names.contains("飞石术"));
                int before = countBooks(player.getInventory().items);
                Skills.giveTreasureBook(player);
                expect("书架藏宝给一本技能书", before + 1, countBooks(player.getInventory().items));
                player.getInventory().clearContent();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                CommandSourceStack source = player.createCommandSourceStack().withSuppressedOutput().withPermission(4);
                player.server.getCommands().performPrefixedCommand(source, "jineng " + GOLD_BOOK);
                List<ItemEntity> dropped = droppedBooks(player);
                expect("/jineng 掉落一本技能书", 1, dropped.size());
                expect("掉落的技能书名", "金芒术", Skills.book(dropped.get(0).getItem()).orElseThrow().name());
                dropped.forEach(ItemEntity::discard);
                player.server.getCommands().performPrefixedCommand(source, "shenfa yvanchuxiuzhen:book_012 分形捉影 21 11 无 双击W时快速弹射。 20 60 20 60 1.5 0 0 0 0");
                expect("/shenfa 书放进背包", 1, countBooks(player.getInventory().items));
                player.getInventory().clearContent();
                NeoForge.EVENT_BUS.post(new TechniqueStudiedEvent(player, "行尸御火"));
                List<ItemEntity> bonus = droppedBooks(player);
                expect("研习行尸御火附赠起尸诀", "起尸诀", Skills.book(bonus.get(0).getItem()).orElseThrow().name());
                bonus.forEach(ItemEntity::discard);
                NeoForge.EVENT_BUS.post(new TechniqueStudiedEvent(player, "五行基础"));
                expect("其他功法没有附赠", 0, droppedBooks(player).size());
                Skills.setSpell(player, 1, studied(player, book(SkillKind.SPELL, GOLD_BOOK)));
                Skills.setSpell(player, 5, studied(player, book(SkillKind.SPELL, WATER_BOOK)));
                Skills.setMovement(player, studied(player, book(SkillKind.MOVEMENT, "yvanchuxiuzhen:book_012 分形捉影 21 11 无 x 20 60 20 60 1.5 0 0 0 0")));
                SkillSelection.nextPage(player);
                player.setData(SkillData.COOLDOWNS, player.getData(SkillData.COOLDOWNS).with(SkillElement.FIRE, 300));
            });
            case 6 -> {
                kill();
            }
            case 7 -> {
                if (!respawned(local)) {
                    return false;
                }
            }
            case 8 -> {
                if (local < 20) {
                    return false;
                }
                onServer(player -> {
                    expect("死亡后技能栏第 1 槽保留", "金芒术", Skills.book(Skills.spell(player, 1)).map(SkillBook::name).orElse(""));
                    expect("死亡后技能栏第 5 槽保留", "水蛇鞭", Skills.book(Skills.spell(player, 5)).map(SkillBook::name).orElse(""));
                    expect("死亡后身法槽保留", "分形捉影", Skills.book(Skills.movement(player)).map(SkillBook::name).orElse(""));
                    expect("死亡后页数保留", 2, Skills.state(player).page());
                    expect("死亡后冷却清零", 0.0, player.getData(SkillData.COOLDOWNS).get(SkillElement.FIRE));
                    player.setData(SkillData.COOLDOWNS, player.getData(SkillData.COOLDOWNS).with(SkillElement.WATER, 5000));
                });
                leaveWorld();
            }
            case 9 -> {
                if (!reopened(local)) {
                    return false;
                }
            }
            case 10 -> {
                if (!inWorld(local)) {
                    return false;
                }
                onServer(player -> {
                    expect("重进后技能栏第 1 槽", "金芒术", Skills.book(Skills.spell(player, 1)).map(SkillBook::name).orElse(""));
                    expect("重进后页数", 2, Skills.state(player).page());
                    double kept = player.getData(SkillData.COOLDOWNS).get(SkillElement.WATER);
                    expectTrue("重进后冷却保留（期间每刻递减） " + kept, kept > 4000 && kept < 5000);
                    Skills.setSpell(player, 1, ItemStack.EMPTY);
                    Skills.setSpell(player, 5, ItemStack.EMPTY);
                    Skills.setMovement(player, ItemStack.EMPTY);
                    player.setData(SkillData.STATE, player.getData(SkillData.STATE).withPage(1).withSelection(ItemStack.EMPTY, 0, 0));
                    player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
                    Cultivation.set(player, CultivationValue.ROOT_GOLD, 0);
                    player.getInventory().clearContent();
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private static int countBooks(List<ItemStack> items) {
        return items.stream().filter(stack -> Skills.book(stack).isPresent()).mapToInt(ItemStack::getCount).sum();
    }

    /** 玩家附近还存在的技能书掉落物。 */
    private static List<ItemEntity> droppedBooks(ServerPlayer player) {
        return player.level().getEntitiesOfClass(ItemEntity.class, new AABB(player.blockPosition()).inflate(4),
                entity -> !entity.isRemoved() && Skills.book(entity.getItem()).isPresent());
    }
}
