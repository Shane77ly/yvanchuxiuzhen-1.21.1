package net.yvanchuxiuzhen.system.skill;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.ItemSwingEvent;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.entity.SwordEntities;

/**
 * 武器技的触发（来源 {@code Swordhuiwu}、{@code Jvjianhuiwu}、{@code Spearhuiwu}、{@code Weaponbroadsword01DangShiTiHuiDongWuPinShi}）：
 * 玩家挥动剑类武器时，若武器技槽中是剑类的武器技，按式数轮换并出手。式数 {@code swordsmanshipnum}、30 刻的连击窗口
 * {@code swordsmanshiptick}、10 刻的出手冷却 {@code smanshipcolddown} 放在玩家的 {@code getPersistentData}（与来源一致），
 * 每刻在 {@link #tick} 推进：窗口结束后式数归零，下一次出手从第 1 式开始。
 *
 * <p>剑、木剑、阔剑的触发要求武器技书已由本人研习（来源比较 {@code belongToMasterUUID}），重剑与枪的触发没有这个要求（来源如此）。
 * 非玩家没有武器技槽，来源里只会改动它们自己的计数而没有任何效果，这里不处理。
 */
final class WeaponArts {
    private static final String COUNTER = "swordsmanshipnum";
    private static final String WINDOW = "swordsmanshiptick";
    private static final String COOLDOWN = "smanshipcolddown";
    private static final String MAX = "smanshipnumMax";

    private WeaponArts() {
    }

    static void onSwing(ItemSwingEvent event) {
        if (!(event.entity() instanceof ServerPlayer player)) {
            return;
        }
        String item = BuiltInRegistries.ITEM.getKey(event.stack().getItem()).getPath();
        if (item.startsWith("weaponsword_") || item.startsWith("weaponwoodsword_") || item.startsWith("weaponbroadsword_")) {
            trigger(player, true);
        } else if (item.startsWith("weaponepee_") || item.startsWith("weaponspear_")) {
            trigger(player, false);
        }
    }

    private static void trigger(ServerPlayer player, boolean mustOwn) {
        SkillBook art = Skills.book(Skills.weaponArt(player)).orElse(null);
        if (art == null || !isSword(art.text("smanshiptype")) || mustOwn && !art.ownedBy(player.getUUID())) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        if (data.getDouble(COOLDOWN) > 0) {
            return;
        }
        data.putDouble(WINDOW, 30);
        data.putDouble(COOLDOWN, 10);
        double step = data.getDouble(COUNTER) + 1;
        if (step > art.number(MAX)) {
            step = 1;
        }
        data.putDouble(COUNTER, step);
        int index = SwordEntities.artIndex(art.name());
        if (index > 0) {
            SwordEntities.perform(player, index, (int) step);
        }
    }

    private static boolean isSword(String type) {
        return type.equals("剑") || type.equals("三尺青锋");
    }

    /** 每刻推进连击窗口与出手冷却（来源 {@code PlayerTick}）。 */
    static void tick(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (data.getDouble(WINDOW) > 0) {
            data.putDouble(WINDOW, data.getDouble(WINDOW) - 1);
            if (data.getDouble(WINDOW) <= 0) {
                data.putDouble(COUNTER, 0);
            }
        }
        if (data.getDouble(COOLDOWN) > 0) {
            data.putDouble(COOLDOWN, data.getDouble(COOLDOWN) - 1);
        }
    }
}
