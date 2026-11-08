package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 一个系统的实机检查：按 tick 推进的步骤序列，提供在服务端线程执行、死亡重生、退出重进与结果记录。
 * 子类在 {@link #step} 中按步骤号执行，返回 true 进入下一步；全部完成时 {@link #done} 为 true。
 */
abstract class SystemCheck {
    protected final Minecraft minecraft = Minecraft.getInstance();
    private final List<String> report;
    private final String levelId;
    private final String name;
    private int step;
    private int stepStart;
    private boolean busy;
    private boolean leaving;
    private int settledTicks;
    private boolean done;

    SystemCheck(String name, List<String> report, String levelId) {
        this.name = name;
        this.report = report;
        this.levelId = levelId;
    }

    /** 执行第 {@code step} 步，{@code local} 为本步已过的 tick 数。 */
    protected abstract boolean step(int step, int local);

    /** 每个客户端 tick 调用一次，全部检查完成时返回 true。退出存档期间客户端会嵌套调用 tick，此时不推进。 */
    final boolean tick(int ticks) {
        if (busy) {
            return false;
        }
        if (leaving) {
            disconnectWhenSettled();
            return false;
        }
        if (!done && step(step, ticks - stepStart)) {
            step++;
            stepStart = ticks;
        }
        return done;
    }

    protected final void finish() {
        done = true;
    }

    protected final void onServer(Consumer<ServerPlayer> action) {
        minecraft.getSingleplayerServer().submit(() -> action.accept(minecraft.getSingleplayerServer().getPlayerList().getPlayers().get(0))).join();
    }

    /** 在服务端杀死玩家；之后用 {@link #respawned} 等待死亡界面并重生。 */
    protected final void kill() {
        onServer(ServerPlayer::kill);
    }

    /** 死亡界面出现后点重生，返回 true；超时记失败并结束。 */
    protected final boolean respawned(int local) {
        if (minecraft.screen instanceof DeathScreen && local >= 20) {
            minecraft.player.respawn();
            return true;
        }
        if (local > 200) {
            fail("没有出现死亡界面");
            finish();
        }
        return false;
    }

    /**
     * 保存并退出到标题界面。集成服务端的区块待办任务连续 40 tick 为 0 后才断开：新建世界后很快退出时，
     * 停止流程偶发停在 {@code Saving worlds} 等待某个区块的保存条件（见 docs/systems/project/logs/2026-11.md）。
     */
    protected final void leaveWorld() {
        leaving = true;
        settledTicks = 0;
    }

    private void disconnectWhenSettled() {
        int[] pending = new int[1];
        onServer(player -> pending[0] = player.server.overworld().getChunkSource().getPendingTasksCount());
        settledTicks = pending[0] == 0 ? settledTicks + 1 : 0;
        if (settledTicks < 40) {
            return;
        }
        leaving = false;
        busy = true;
        minecraft.level.disconnect();
        minecraft.disconnect(new GenericMessageScreen(Component.literal("saving")));
        minecraft.setScreen(new TitleScreen());
        busy = false;
    }

    /** 标题界面出现后重新打开同一存档，返回 true。 */
    protected final boolean reopened(int local) {
        if (minecraft.screen instanceof TitleScreen && local >= 20) {
            busy = true;
            try {
                minecraft.createWorldOpenFlows().openWorld(levelId, () -> fail("无法重新打开存档 " + levelId));
            } finally {
                busy = false;
            }
            return true;
        }
        return false;
    }

    /** 重新进入世界并等待 100 tick 后返回 true；超时记失败并结束。 */
    protected final boolean inWorld(int local) {
        if (minecraft.screen instanceof BackupConfirmScreen screen
                && screen.getTitle().equals(Component.translatable("selectWorld.backupQuestion.experimental"))) {
            report.add("INFO " + name + " 重进遇到原生实验性世界提示，点击不备份并继续");
            Button proceed = screen.children().stream().filter(child -> child instanceof Button button
                            && button.getMessage().equals(Component.translatable("selectWorld.backupJoinSkipButton")))
                    .map(child -> (Button) child).findFirst().orElseThrow();
            busy = true;
            try {
                proceed.onPress();
            } finally {
                busy = false;
            }
        }
        if (minecraft.player != null && minecraft.getSingleplayerServer() != null && local >= 100) {
            return true;
        }
        if (local > 1200) {
            fail("重新打开存档超时，当前界面：" + (minecraft.screen == null ? "无" : minecraft.screen.getClass().getSimpleName()
                    + " / " + minecraft.screen.getTitle().getString()));
            finish();
        }
        return false;
    }

    protected final void expect(String label, double expected, double actual) {
        record(Math.abs(expected - actual) < 1e-6, label + " 期望 " + expected + " 实际 " + actual);
    }

    protected final void expect(String label, Object expected, Object actual) {
        record(expected.equals(actual), label + " 期望 " + expected + " 实际 " + actual);
    }

    protected final void expectTrue(String label, boolean actual) {
        record(actual, label);
    }

    protected final void fail(String message) {
        report.add("FAIL " + name + " " + message);
    }

    private void record(boolean passed, String line) {
        report.add((passed ? "INFO " : "FAIL ") + name + " " + line);
    }
}
