package net.yvanchuxiuzhen.system.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.system.economy.api.BankAccounts;

/** 银行打开和存入都在两个服务端刻结束后刷新；打开回调按来源现读当时的槽映射。 */
public final class BankUpdates {
    private static final List<Pending> PENDING = new ArrayList<>();

    private BankUpdates() {
    }

    public static void opened(ServerPlayer player) {
        PENDING.add(new Pending(player, true));
    }

    public static void refreshLater(ServerPlayer player) {
        PENDING.add(new Pending(player, false));
    }

    public static void tick(ServerTickEvent.Post event) {
        List<Pending> due = new ArrayList<>();
        PENDING.removeIf(pending -> {
            if (--pending.ticks > 0) return false;
            due.add(pending);
            return true;
        });
        for (Pending pending : due) {
            ServerPlayer player = pending.player;
            if (pending.initialize && player.containerMenu instanceof Supplier<?> supplier && supplier.get() instanceof Map<?, ?> slots) {
                for (int tier = 1; tier <= 9; tier++) {
                    ((Slot) slots.get(tier)).set(BankAccounts.spiritStone(tier));
                    player.containerMenu.broadcastChanges();
                }
            }
            BankAccounts.refresh(player);
        }
    }

    public static void stopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    private static final class Pending {
        private final ServerPlayer player;
        private final boolean initialize;
        private int ticks = 2;

        private Pending(ServerPlayer player, boolean initialize) {
            this.player = player;
            this.initialize = initialize;
        }
    }
}
