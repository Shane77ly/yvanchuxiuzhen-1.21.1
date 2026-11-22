package net.yvanchuxiuzhen.system.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/** 固定封印结构的放置、全服结束刻延迟及来源物品清理；延迟队列不写入存档。 */
public final class StructureReleases {
    private static final List<Pending> PENDING = new ArrayList<>();

    private StructureReleases() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(StructureReleases::tick);
        NeoForge.EVENT_BUS.addListener(StructureReleases::stop);
    }

    public static void release(ServerLevel level, Vec3 commandPosition, String name) {
        StructureReleaseData.place(level, name);
        int delay = switch (name) {
            case "花繁城", "雪地商店", "平原马车商店", "平原营地商店", "野外营地" -> 60;
            case "冲关河村", "云清派", "佣兵营地" -> 100;
            case "镇妖塔" -> 40;
            default -> 0;
        };
        if (delay != 0) {
            PENDING.add(new Pending(level, delay, () -> NeoForge.EVENT_BUS.post(new StructureReleasedEvent(level, name))));
        }
        // 来源无条件执行原版选择器，清理全服各维度已加载物品，包括未知结构名字。
        PENDING.add(new Pending(level, 10, () -> level.getServer().getCommands().performPrefixedCommand(
                new CommandSourceStack(CommandSource.NULL, commandPosition, Vec2.ZERO, level, 4, "", Component.empty(),
                        level.getServer(), null).withSuppressedOutput(), "kill @e[type=item]")));
    }

    private static void tick(ServerTickEvent.Post event) {
        List<Pending> due = new ArrayList<>();
        PENDING.removeIf(pending -> {
            if (pending.level.getServer() == event.getServer() && --pending.remaining <= 0) {
                due.add(pending);
                return true;
            }
            return false;
        });
        due.forEach(pending -> pending.action.run());
    }

    private static void stop(ServerStoppedEvent event) {
        PENDING.removeIf(pending -> pending.level.getServer() == event.getServer());
    }

    private static final class Pending {
        private final ServerLevel level;
        private int remaining;
        private final Runnable action;

        private Pending(ServerLevel level, int remaining, Runnable action) {
            this.level = level;
            this.remaining = remaining;
            this.action = action;
        }
    }
}
