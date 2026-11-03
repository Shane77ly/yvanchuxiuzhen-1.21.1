package net.yvanchuxiuzhen.system.xiulian;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.content.BirthMenu;

/**
 * 首次进入游戏的灵根、体质与出身（来源 {@code PlayerJoinGame} 前段、出身界面 {@code PlayerJoinGUI} 的按钮
 * {@code Lock1}–{@code 5}、{@code Suijikaijv}、{@code BirthUp}、{@code Birthdown}，以及关闭界面后 {@code PlayerJoin02} 的体质部分）。
 * 关闭界面后按出身发放物品与传送的部分涉及多个系统的物品，由 flow 在对应系统实现后接入。
 */
public final class BirthSelection {
    /** 按钮编号与来源 {@code PlayerJoinGUIButtonMessage} 一致：0–4 锁定金水木火土，5 随机，6 上一个出身，7 下一个出身。 */
    public static final int BUTTONS = 8;
    /** 金、水、木、火、土灵根与对应的锁定键（来源 {@code tizhiitem} 的 {@code Lock*}）。 */
    public static final List<CultivationValue> ROOTS = List.of(CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL);
    public static final List<String> LOCK_KEYS = List.of("LockGold", "LockWater", "LockTree", "LockFire", "LockSoil");
    private static final String ROLL_OBJECTIVE = "suijishu";

    private BirthSelection() {
    }

    /** 登录时五行灵根之和小于 1 且游戏规则开启：随机灵根与体质并打开出身界面。 */
    public static void onLogin(ServerPlayer player) {
        double sum = 0;
        for (CultivationValue root : ROOTS) {
            sum += Cultivation.get(player, root);
        }
        if (sum >= 1 || !player.serverLevel().getGameRules().getBoolean(XiulianRules.BIRTH_SELECTION)) {
            return;
        }
        for (CultivationValue root : ROOTS) {
            rollRoot(player, root);
        }
        updateYinyang(player);
        Cultivation.set(player, CultivationText.CONSTITUTION, Constitution.rolled(Mth.nextInt(player.getRandom(), 1, 10)).title());
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new BirthMenu(id, inventory), Component.literal("PlayerJoinGUI")));
    }

    public static void press(ServerPlayer player, int button) {
        if (button < 5) {
            toggleLock(player, LOCK_KEYS.get(button));
            return;
        }
        switch (button) {
            case 5 -> reroll(player);
            case 6, 7 -> {
                Birth birth = Birth.of(Cultivation.get(player, CultivationText.BIRTH));
                if (birth != null) {
                    Cultivation.set(player, CultivationText.BIRTH, (button == 6 ? birth.previous() : birth.next()).title());
                }
            }
            default -> throw new IllegalArgumentException("出身界面没有按钮 " + button);
        }
    }

    /** 已锁定时解锁并返还一次锁定次数；未锁定且有剩余次数时锁定（来源 {@code liftlock}）。 */
    private static void toggleLock(ServerPlayer player, String key) {
        if (Cultivation.tizhiBoolean(player, key)) {
            Cultivation.setTizhiBoolean(player, key, false);
            Cultivation.add(player, CultivationValue.LIFTLOCK, 1);
        } else if (Cultivation.get(player, CultivationValue.LIFTLOCK) > 0) {
            Cultivation.setTizhiBoolean(player, key, true);
            Cultivation.add(player, CultivationValue.LIFTLOCK, -1);
        }
    }

    /**
     * 消耗一次随机次数（来源 {@code randomlift}）：未锁定的灵根重随，重算阴阳；体质未锁定（{@code tizhilock}，来源中没有写入者）时
     * 把 1–10 的随机数写入记分项 {@code suijishu} 再读出作为体质。
     */
    private static void reroll(ServerPlayer player) {
        if (Cultivation.get(player, CultivationValue.RANDOMLIFT) <= 0) {
            player.displayClientMessage(Component.literal("§4已无刷新次数"), false);
            return;
        }
        for (int i = 0; i < ROOTS.size(); i++) {
            if (!Cultivation.tizhiBoolean(player, LOCK_KEYS.get(i))) {
                rollRoot(player, ROOTS.get(i));
            }
        }
        updateYinyang(player);
        if (!Cultivation.tizhiBoolean(player, "tizhilock")) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective(ROLL_OBJECTIVE);
            if (objective == null) {
                objective = scoreboard.addObjective(ROLL_OBJECTIVE, ObjectiveCriteria.DUMMY, Component.literal(ROLL_OBJECTIVE),
                        ObjectiveCriteria.RenderType.INTEGER, false, null);
            }
            scoreboard.getOrCreatePlayerScore(player, objective).set(Mth.nextInt(player.getRandom(), 1, 10));
            Cultivation.set(player, CultivationText.CONSTITUTION, Constitution.rolled(scoreboard.getOrCreatePlayerScore(player, objective).get()).title());
        }
        Cultivation.add(player, CultivationValue.RANDOMLIFT, -1);
    }

    /** 关闭出身界面：按体质打开标记、加修正值，体质计数 {@code tizhinumber} 加 1。 */
    public static void close(ServerPlayer player) {
        Constitution constitution = Constitution.applied(Cultivation.get(player, CultivationText.CONSTITUTION));
        Cultivation.setTizhiBoolean(player, constitution.openKey(), true);
        for (Constitution.Bonus bonus : constitution.bonuses()) {
            Cultivation.setTizhiDouble(player, bonus.key(), Cultivation.tizhiDouble(player, bonus.key()) + bonus.amount());
        }
        if (constitution == Constitution.YINYANG) {
            Cultivation.add(player, CultivationValue.EXPERIENCE_RATE, Constitution.YINYANG_EXPERIENCE_RATE);
        }
        Cultivation.setTizhiDouble(player, "tizhinumber", Cultivation.tizhiDouble(player, "tizhinumber") + 1);
    }

    private static void rollRoot(ServerPlayer player, CultivationValue root) {
        RandomSource random = player.getRandom();
        Cultivation.set(player, root, Mth.nextInt(random, 0, player.serverLevel().getGameRules().getInt(XiulianRules.ROOT_LEVEL)));
    }

    /** 阴阳 = 金 + 火 − 木 − 水。 */
    private static void updateYinyang(ServerPlayer player) {
        Cultivation.set(player, CultivationValue.YINYANG, Cultivation.get(player, CultivationValue.ROOT_GOLD) + Cultivation.get(player, CultivationValue.ROOT_FIRE)
                - Cultivation.get(player, CultivationValue.ROOT_TREE) - Cultivation.get(player, CultivationValue.ROOT_WATER));
    }
}
