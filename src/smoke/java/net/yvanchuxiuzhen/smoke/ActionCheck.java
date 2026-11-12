package net.yvanchuxiuzhen.smoke;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 由若干“服务端动作”组成的检查：每个动作在其步骤的第一次调用时于服务端线程执行，之后等待 {@link Action#ticks} 刻再进入下一个；
 * 全部完成后调用 {@link #cleanup} 收尾。{@link SystemCheck} 的步内计数不从 0 起，所以用执行标记而不是 {@code local == 0} 判断首次调用。
 */
abstract class ActionCheck extends SystemCheck {
    protected record Action(int ticks, Consumer<ServerPlayer> run) {
    }

    private List<Action> actions = List.of();
    private int executed = -1;

    ActionCheck(String name, List<String> report, String levelId) {
        super(name, report, levelId);
    }

    protected final void script(Action... steps) {
        actions = Arrays.asList(steps);
    }

    /** 全部动作完成后在服务端执行：清场并还原玩家。 */
    protected abstract void cleanup(ServerPlayer player);

    @Override
    protected final boolean step(int step, int local) {
        if (step >= actions.size()) {
            onServer(this::cleanup);
            finish();
            return true;
        }
        Action action = actions.get(step);
        if (executed != step) {
            executed = step;
            onServer(action.run());
        }
        return local >= action.ticks();
    }
}
