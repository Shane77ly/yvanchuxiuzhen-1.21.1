package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.quest.DialogueRuntime;
import net.yvanchuxiuzhen.system.quest.api.DialogueActions;
import net.yvanchuxiuzhen.system.quest.api.DialogueNodeDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestDialogueSession;
import net.yvanchuxiuzhen.system.quest.api.QuestJournalState;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.client.DialogueScreen;
import net.yvanchuxiuzhen.system.quest.client.QuestJournalScreen;
import net.yvanchuxiuzhen.system.quest.content.DialogueMenu;
import net.yvanchuxiuzhen.system.quest.content.QuestJournalMenu;
import net.yvanchuxiuzhen.system.quest.data.QuestData;
import org.lwjgl.glfw.GLFW;

/** 受控任务目录经真实U键、原生13按钮、共享启动tick与真实保存重进验证。 */
final class QuestJournalCheck extends SystemCheck {
    private static final String UNFINISHED = "未完成任务";
    private static final String FINISHED = "已完成任务";
    private static final String DESCRIPTION = "任务说明每行十八个字符逐项切分并同步给玩家用于检查来源文字转换和选择结果";
    private final Screenshots screenshots;
    private QuestDialogueSession originalSession;
    private QuestJournalState originalJournal;
    private QuestJournalState fixture;
    private boolean prepared;

    QuestJournalCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("任务目录", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            if (step >= 3 && step <= 18) {
                int row = (step - 3) / 2 + 1;
                if (step % 2 == 1) row(row).onPress();
                else {
                    if (!settled(local)) return false;
                    onServer(player -> checkSelection("服务端第" + row + "行", player, row));
                    checkSelection("客户端第" + row + "行", minecraft.player, row);
                }
                return true;
            }
            return runStep(step, local);
        } catch (RuntimeException exception) {
            fail("检查异常：" + exception);
            if (prepared && minecraft.getSingleplayerServer() != null && minecraft.player != null) onServer(this::restore);
            finish();
            return true;
        }
    }

    private boolean runStep(int step, int local) {
        switch (step) {
            case 0 -> {
                expect("启动中文选项", "zh_cn", minecraft.getLanguageManager().getSelected());
                expect("任务按键中文", "任务菜单按键", I18n.get("key.yvanchuxiuzhen.renwuanjian"));
                expectTrue("来源默认U键", key().getDefaultKey().getValue() == GLFW.GLFW_KEY_U);
                onServer(this::prepare);
            }
            case 1 -> {
                if (!settled(local)) return false;
                pressKey();
            }
            case 2 -> {
                if (!journalOpen(local)) return false;
                expect("原生中文菜单标题", "任务列表", minecraft.screen.getTitle().getString());
                expect("原生13个按钮", 13, buttons().size());
                expect("未完成八行同步", 8, IntStream.rangeClosed(1, 8)
                        .filter(row -> DialogueActions.snapshot(minecraft.player).row(row).uuid().equals(id(row))).count());
                onServer(player -> expectTrue("服务端任务菜单身份", player.containerMenu instanceof QuestJournalMenu));
                screenshots.take("quest-journal-eight-rows.png");
            }
            case 19 -> {
                screenshots.take("quest-journal-selected-detail.png");
                button("下一页").onPress();
            }
            case 20 -> {
                if (!journalOpen(local)) return false;
                checkPage(1, UNFINISHED, id(11));
                button("下一页").onPress();
            }
            case 21 -> {
                if (!journalOpen(local)) return false;
                checkPage(2, UNFINISHED, "");
                button("下一页").onPress();
            }
            case 22 -> {
                if (!journalOpen(local)) return false;
                checkPage(0, UNFINISHED, id(1));
                button("上一页").onPress();
            }
            case 23 -> {
                if (!journalOpen(local)) return false;
                checkPage(2, UNFINISHED, "");
                category(0).onPress();
            }
            case 24 -> {
                if (!journalOpen(local)) return false;
                checkPage(2, FINISHED, id(17));
                button("下一页").onPress();
            }
            case 25 -> {
                if (!journalOpen(local)) return false;
                checkPage(0, FINISHED, id(9));
                expect("失败任务也在已完成类", id(10), DialogueActions.snapshot(minecraft.player).row(2).uuid());
                category(1).onPress();
            }
            case 26 -> {
                if (!journalOpen(local)) return false;
                checkPage(0, UNFINISHED, id(1));
                row(1).onPress();
            }
            case 27 -> {
                if (!settled(local)) return false;
                button("继续任务").onPress();
            }
            case 28 -> {
                if (!dialogueOpen(local, 1)) return false;
                minecraft.player.closeContainer();
            }
            case 29 -> {
                if (!settled(local)) return false;
                pressKey();
            }
            case 30 -> {
                if (!journalOpen(local)) return false;
                row(2).onPress();
            }
            case 31 -> {
                if (!settled(local)) return false;
                button("继续任务").onPress();
            }
            case 32 -> {
                if (!dialogueOpen(local, 2)) return false;
                minecraft.player.closeContainer();
                onServer(player -> player.setData(QuestData.SESSION, QuestDialogueSession.EMPTY));
            }
            case 33 -> leaveWorld();
            case 34 -> { return reopened(local); }
            case 35 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    expectTrue("真实保存重进保留全部18项任务目录", fixture.equals(DialogueActions.journal(player)));
                    expectTrue("登录按来源清除页面会话", DialogueActions.snapshot(player).isEmpty());
                });
                pressKey();
            }
            case 36 -> {
                if (!journalOpen(local)) return false;
                checkPage(0, UNFINISHED, id(1));
                expect("重进八行目录及中文保持", "烟测任务8（进行中）", DialogueActions.snapshot(minecraft.player).row(8).name());
                onServer(player -> {
                    verifyDimensionCheck(player, "minecraft:overworld", "匹配当前维度");
                    verifyDimensionCheck(player, "minecraft:the_nether", "不匹配其他维度");
                    restore(player);
                });
                finish();
            }
            default -> throw new IllegalStateException("未知任务目录步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        originalSession = DialogueActions.snapshot(player);
        originalJournal = DialogueActions.journal(player);
        prepared = true;
        player.closeContainer();
        fixture = QuestJournalState.EMPTY;
        for (int row = 1; row <= 18; row++) {
            String id = id(row);
            String status = row <= 8 || row == 11 ? "未完成" : row == 9 ? "失败" : "完成";
            QuestTemplateDefinition template = new QuestTemplateDefinition(id, id, "烟测任务" + row, "说明",
                    0, status, "需求" + row, "奖励" + row, List.of(
                    new DialogueNodeDefinition.TextNode(false, "错误初始位置"),
                    new DialogueNodeDefinition.TextNode(false, "继续任务" + row + "成功"),
                    DialogueNodeDefinition.EndNode.INSTANCE));
            fixture = fixture.withEntry(id, new QuestJournalState.QuestJournalEntry(
                    "烟测任务" + row, id, DESCRIPTION, 2, status, "需求" + row, "奖励" + row, id,
                    Optional.of(template), row % 2 == 1));
        }
        player.setData(QuestData.JOURNAL, fixture);
        player.setData(QuestData.SESSION, QuestDialogueSession.EMPTY);
    }

    private void checkSelection(String label, Player player, int row) {
        QuestDialogueSession state = DialogueActions.snapshot(player);
        expectTrue(label + " 任务/需求/奖励/高级标志对应", state.journal().selectedUuid().equals(id(row))
                && state.journal().selectedTitle().equals("烟测任务" + row + "（进行中）")
                && state.journal().selectedRequirement().equals("需求" + row) && state.journal().selectedReward().equals("奖励" + row)
                && state.journal().selectedHigh() == (row % 2 == 1));
        expectTrue(label + " 原说明及18字符分行对应", state.displayText().equals(DESCRIPTION)
                && state.detailLine(1).equals(DESCRIPTION.substring(0, 18)) && state.detailLine(2).equals(DESCRIPTION.substring(18, 36)));
    }

    private void checkPage(int page, String category, String first) {
        QuestDialogueSession state = DialogueActions.snapshot(minecraft.player);
        expectTrue("分类与原始目录页码同步 " + category + page, state.journal().category().equals(category)
                && state.journal().page() == page && state.journal().totalPages() == 2
                && state.row(1).uuid().equals(first) && state.journal().selectedUuid().isEmpty());
        onServer(player -> expectTrue("服务端同页码 " + page, DialogueActions.snapshot(player).equals(state)));
    }

    private boolean dialogueOpen(int local, int row) {
        if (!(minecraft.screen instanceof DialogueScreen) || !DialogueActions.snapshot(minecraft.player).displayText().equals("继续任务" + row + "成功")) {
            if (local > 200) throw new IllegalStateException("继续任务没有进入已保存进度 " + row);
            return false;
        }
        onServer(player -> {
            QuestDialogueSession state = DialogueActions.snapshot(player);
            expectTrue((row == 1 ? "高级renwubegin" : "普通taskbegin") + " 经共享启动tick消费并恢复进度", player.containerMenu instanceof DialogueMenu
                    && state.cursor() == 2 && !state.dialogueBegin() && !state.taskBegin()
                    && state.displayText().equals("继续任务" + row + "成功"));
        });
        return true;
    }

    private boolean journalOpen(int local) {
        if (local < 10) return false;
        if (!(minecraft.screen instanceof QuestJournalScreen)) {
            if (local > 200) throw new IllegalStateException("任务列表没有打开");
            return false;
        }
        return true;
    }

    private static boolean settled(int local) {
        return local >= 10;
    }

    private KeyMapping key() {
        return Arrays.stream(minecraft.options.keyMappings).filter(mapping -> mapping.getName().equals("key.yvanchuxiuzhen.renwuanjian")).findFirst().orElseThrow();
    }

    private void pressKey() {
        key().setDown(false);
        key().setDown(true);
        key().setDown(false);
    }

    private List<Button> buttons() {
        return minecraft.screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }

    private Button button(String label) {
        return buttons().stream().filter(button -> button.getMessage().getString().equals(label)).findFirst().orElseThrow();
    }

    private Button category(int direction) {
        return buttons().stream().filter(button -> button.getMessage().getString().equals("切换任务分类")).toList().get(direction);
    }

    private Button row(int row) {
        return button("烟测任务" + row + "（进行中）");
    }

    private static String id(int row) {
        return "smoke_quest_journal_" + row;
    }

    private void verifyDimensionCheck(ServerPlayer player, String dimensionId, String expected) {
        QuestTemplateDefinition template = new QuestTemplateDefinition("smoke_dim", "smoke_dim", "维度校验", "",
                0, "未完成", "", "", List.of(
                new DialogueNodeDefinition.CheckDimensionNode(dimensionId, 2, 3),
                new DialogueNodeDefinition.TextNode(false, "匹配当前维度"),
                new DialogueNodeDefinition.TextNode(false, "不匹配其他维度")));
        QuestDialogueSession state = QuestDialogueSession.EMPTY.toBuilder().template(Optional.of(template)).cursor(1).build();
        player.setData(QuestData.SESSION, state);
        DialogueRuntime.run(player, player.blockPosition());
        expect("Q84 维度条件分支 " + dimensionId, expected, DialogueActions.snapshot(player).displayText());
    }

    private void restore(ServerPlayer player) {
        player.closeContainer();
        player.setData(QuestData.JOURNAL, originalJournal);
        player.setData(QuestData.SESSION, originalSession);
        prepared = false;
    }
}
