package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.BookQuizQuestions;
import net.yvanchuxiuzhen.system.book.client.BookQuizScreen;
import net.yvanchuxiuzhen.system.book.content.BookMenus;
import net.yvanchuxiuzhen.system.book.content.BookQuizMenu;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;
import net.yvanchuxiuzhen.system.book.network.BookQuizButtonPayload;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;

/** 答题页仅由夹具显式打开，实际按钮经客户端消息提交；覆盖失败、答满和真实保存重进。 */
final class BookQuizCheck extends SystemCheck {
    private static final int[] QUESTIONS = {4, 1, 5, 2};
    private final Screenshots screenshots;
    private final List<ItemStack> inventory = new ArrayList<>();
    private int selected;
    private int originalQuestion;
    private int retainedQuestion;
    private double originalProgress;
    private boolean regeneration;
    private float originalHealth;
    private double failedHealth;

    BookQuizCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("书籍答题", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step >= 1 && step <= 8) {
            int button = (step - 1) / 2;
            if (step % 2 == 1) {
                return action(local, 8, player -> stage(player, QUESTIONS[button], 0));
            }
            if (local < 8) return false;
            if (!opened()) return true;
            if (button == 0) screenshots.take("book_quiz_question.png");
            click(button);
            return true;
        }
        if (step >= 18 && step <= 29) {
            SkillKind kind = SkillKind.values()[(step - 18) / 3];
            if (step == 18 && local >= 8) {
                if (!opened()) return true;
                screenshots.take("book_quiz_long_question.png");
            }
            return switch ((step - 18) % 3) {
                case 0 -> action(local, 8, player -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, skillBook(kind));
                    stage(player, 1, 10);
                });
                case 1 -> {
                    if (local < 8) yield false;
                    if (!opened()) yield true;
                    click(1);
                    yield true;
                }
                default -> action(local, 8, player -> {
                    SkillBook book = Skills.book(player.getMainHandItem()).orElseThrow();
                    expectTrue(kind + "答满覆盖既有研习者并保留技能参数", book.ownedBy(player.getUUID())
                            && book.level() == 40 && book.body().equals("五") && book.number("jinengmanaatt") == 3);
                    expectTrue(kind + "答满关闭且计数清零", !(player.containerMenu instanceof BookQuizMenu)
                            && BookQuizData.answerCount(player) == 0);
                    expectTrue(kind + "答满书组件双端一致", ItemStack.matches(player.getMainHandItem(), minecraft.player.getMainHandItem()));
                });
            };
        }
        return switch (step) {
            case 0 -> action(local, 8, this::prepare);
            case 9 -> action(local, 8, player -> {
                checkCorrect(player, "第四选项");
                retainedQuestion = player.getData(BookQuizData.QUESTION);
                BookQuizData.setAnswerCount(player, 3.5);
                BookQuizData.publish(player);
                player.closeContainer();
            });
            case 10 -> action(local, 8, player -> {
                expect("手动关闭保留题目", retainedQuestion, player.getData(BookQuizData.QUESTION).intValue());
                expect("手动关闭保留非整数题数", 3.5, BookQuizData.answerCount(player));
                BookMenus.openQuiz(player);
            });
            case 11 -> {
                if (local < 8) yield false;
                if (!opened()) yield true;
                onServer(player -> {
                    expect("重开沿用题目", retainedQuestion, BookQuizData.state(minecraft.player).questionId());
                    expect("重开同步题数", 3.5, BookQuizData.state(minecraft.player).progress());
                });
                int id = minecraft.player.containerMenu.containerId;
                PacketDistributor.sendToServer(new BookQuizButtonPayload(id, -1));
                PacketDistributor.sendToServer(new BookQuizButtonPayload(id, 4));
                PacketDistributor.sendToServer(new BookQuizButtonPayload(id + 100, 0));
                yield true;
            }
            case 12 -> action(local, 8, player -> {
                expect("非法选项和旧容器请求不改计数", 3.5, BookQuizData.answerCount(player));
                stage(player, 4, 6);
                failedHealth = player.getHealth() - player.getMaxHealth() * 0.1;
            });
            case 13 -> {
                if (local < 8) yield false;
                if (!opened()) yield true;
                click(1);
                yield true;
            }
            case 14 -> action(local, 8, player -> {
                expectTrue("答错关闭答题页", !(player.containerMenu instanceof BookQuizMenu));
                expect("答错直接减最大生命十分之一", failedHealth, player.getHealth());
                expect("答错计数清零", 0, BookQuizData.answerCount(player));
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                stage(player, 4, 10);
            });
            case 15 -> {
                if (local < 8) yield false;
                if (!opened()) yield true;
                onServer(player -> {
                    player.setItemInHand(InteractionHand.MAIN_HAND, techniqueBook());
                    player.inventoryMenu.broadcastChanges();
                });
                screenshots.take("book_quiz_tenth.png");
                click(0);
                yield true;
            }
            case 16 -> action(local, 8, player -> {
                TechniqueBook book = Techniques.book(player.getMainHandItem()).orElseThrow();
                expectTrue("第十一答读取开窗后更换的主手并覆盖研习者且不受境界灵根限制", book.ownedBy(player.getUUID()));
                expect("功法层数重置为一", 1, book.level());
                expect("功法原经验保留", 13, book.exp());
                expect("来源中档初始生命加成", 64, book.value(TechniqueStat.HEALTH_MAX));
                expect("来源中档初始移速加成", 0.04, book.value(TechniqueStat.MOVE_SPEED));
                expectTrue("答满不扣整栈也不触发普通研习附赠技能", player.getMainHandItem().getCount() == 2
                        && player.getInventory().items.stream().filter(stack -> !stack.isEmpty()).count() == 1);
                expectTrue("答满归属与层数双端一致", ItemStack.matches(player.getMainHandItem(), minecraft.player.getMainHandItem()));
                expectTrue("答满关闭菜单并同步清零", !(player.containerMenu instanceof BookQuizMenu)
                        && BookQuizData.state(minecraft.player).progress() == 0);
            });
            case 17 -> action(local, 5, player -> {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                stage(player, 26, 2);
            });
            case 30 -> action(local, 8, player -> stage(player, 1, 7));
            case 31 -> { leaveWorld(); yield true; }
            case 32 -> reopened(local);
            case 33 -> inWorld(local);
            case 34 -> action(local, 8, player -> {
                expect("真实重进保留题号", 1, player.getData(BookQuizData.QUESTION).intValue());
                expect("真实重进保留实体答题数", 7, BookQuizData.answerCount(player));
                expectTrue("真实重进保留答满后技能书归属", Skills.book(player.getMainHandItem()).orElseThrow().ownedBy(player.getUUID()));
                BookMenus.openQuiz(player);
            });
            case 35 -> {
                if (local < 8) yield false;
                if (!opened()) yield true;
                screenshots.take("book_quiz_reopened.png");
                click(1);
                yield true;
            }
            case 36 -> action(local, 8, player -> {
                expect("重进后实际按钮继续累计", 8, BookQuizData.answerCount(player));
                cleanup(player);
                finish();
            });
            default -> throw new IllegalStateException("未知书籍答题检查步骤 " + step);
        };
    }

    private boolean action(int local, int wait, Consumer<ServerPlayer> run) {
        if (local < wait) return false;
        onServer(run);
        return true;
    }

    private void prepare(ServerPlayer player) {
        player.closeContainer();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        selected = player.getInventory().selected;
        originalQuestion = player.getData(BookQuizData.QUESTION);
        originalProgress = BookQuizData.answerCount(player);
        originalHealth = player.getHealth();
        regeneration = player.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, player.server);
        player.setHealth(player.getMaxHealth());
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
    }

    private void stage(ServerPlayer player, int question, double progress) {
        if (progress == 0 && player.containerMenu instanceof BookQuizMenu) checkCorrect(player, "前一选项");
        player.closeContainer();
        player.setData(BookQuizData.QUESTION, question);
        BookQuizData.setAnswerCount(player, progress);
        player.inventoryMenu.broadcastChanges();
        BookMenus.openQuiz(player);
    }

    private void checkCorrect(ServerPlayer player, String label) {
        expect(label + "经真实按钮累计一次", 1, BookQuizData.answerCount(player));
        int question = player.getData(BookQuizData.QUESTION);
        expectTrue(label + "继续均匀题库范围", question >= 1 && question <= 40);
        expectTrue(label + "题号和计数双端一致", BookQuizData.state(player).equals(BookQuizData.state(minecraft.player)));
    }

    private boolean opened() {
        boolean opened = minecraft.screen instanceof BookQuizScreen && minecraft.player.containerMenu instanceof BookQuizMenu;
        expectTrue("显式打开答题菜单并同步原生界面", opened);
        if (!opened) {
            onServer(this::cleanup);
            finish();
            return false;
        }
        expect("中文菜单标题", "书籍答题", minecraft.screen.getTitle().getString());
        expectTrue("来源答题菜单无物品槽", minecraft.player.containerMenu.slots.isEmpty());
        int question = BookQuizData.state(minecraft.player).questionId();
        List<String> labels = minecraft.screen.children().stream().filter(child -> child instanceof Button)
                .map(child -> ((Button) child).getMessage().getString()).toList();
        expect("四按钮旁白与当前四选项一致", BookQuizQuestions.get(question).options(), labels);
        return true;
    }

    private void click(int button) {
        List<Button> buttons = minecraft.screen.children().stream().filter(child -> child instanceof Button)
                .map(child -> (Button) child).toList();
        buttons.get(button).onPress();
    }

    private static ItemStack techniqueBook() {
        ItemStack stack = Techniques.createBook(new TechniqueDefinition(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_002"),
                "行尸御火", "黄阶一级", 1, "大乘十阶", 40, "五", "", Map.of(),
                Collections.nCopies(TechniqueStat.values().length, "中档"))).copyWithCount(2);
        stack.set(GongfaData.TECHNIQUE.get(), Techniques.book(stack).orElseThrow()
                .withOwner(UUID.randomUUID(), "原研习者").withProgress(7, 13));
        return stack;
    }

    private static ItemStack skillBook(SkillKind kind) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "book_006")));
        stack.set(SkillData.SKILL.get(), new SkillBook(kind, "答题检查技能", 1, 40, "五", "", Optional.of(UUID.randomUUID()),
                "原研习者", Map.of("jinengmanaatt", 3.0), Map.of()));
        return stack;
    }

    private void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        player.getInventory().selected = selected;
        player.setHealth(originalHealth);
        player.serverLevel().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(regeneration, player.server);
        player.setData(BookQuizData.QUESTION, originalQuestion);
        BookQuizData.setAnswerCount(player, originalProgress);
        BookQuizData.publish(player);
        player.inventoryMenu.broadcastChanges();
    }
}
