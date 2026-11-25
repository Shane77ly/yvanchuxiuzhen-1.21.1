package net.yvanchuxiuzhen.system.skill;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.platform.content.ItemSwingEvent;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.WeaponArtState;
import net.yvanchuxiuzhen.system.skill.entity.SwordEntities;

/**
 * 武器技的触发（来源 {@code Swordhuiwu}、{@code Jvjianhuiwu}、{@code Spearhuiwu}、{@code Weaponbroadsword01DangShiTiHuiDongWuPinShi}）：
 * 玩家挥动剑类武器时，若武器技槽中是剑类的武器技，按式数轮换并出手。式数、30 刻的连击窗口与 10 刻的出手冷却保存在实体的
 * {@link SkillData#WEAPON_ART} 附件中，每刻在 {@link #tick} 推进玩家状态：窗口结束后式数归零，下一次出手从第 1 式开始。
 *
 * <p>剑、木剑、阔剑的触发要求武器技书已由本人研习（来源比较 {@code belongToMasterUUID}），重剑与枪的触发没有这个要求（来源如此）。
 * 重剑与枪的非玩家挥动仍写入实体计数；来源玩家刻不会推进这些非玩家计数。
 */
final class WeaponArts {
    private static final String MAX = "smanshipnumMax";

    private WeaponArts() {
    }

    static void onSwing(ItemSwingEvent event) {
        LivingEntity entity = event.entity();
        if (entity.level().isClientSide) {
            return;
        }
        String item = BuiltInRegistries.ITEM.getKey(event.stack().getItem()).getPath();
        if (entity instanceof ServerPlayer player) {
            if (item.startsWith("weaponsword_") || item.startsWith("weaponwoodsword_") || item.startsWith("weaponbroadsword_")) {
                trigger(player, true);
            } else if (item.startsWith("weaponepee_") || item.startsWith("weaponspear_")) {
                trigger(player, false);
            }
        } else if (!(entity instanceof Player) && (item.startsWith("weaponepee_") || item.startsWith("weaponspear_"))) {
            WeaponArtState state = entity.getData(SkillData.WEAPON_ART);
            if (state.cooldownTicks() <= 0) {
                double step = state.step() + 1;
                entity.setData(SkillData.WEAPON_ART, new WeaponArtState(step > 0 ? 1 : step, 30, 10));
            }
        }
    }

    private static void trigger(ServerPlayer player, boolean mustOwn) {
        SkillBook art = Skills.book(Skills.weaponArt(player)).orElse(null);
        if (art == null || !isSword(art.text("smanshiptype")) || mustOwn && !art.ownedBy(player.getUUID())) {
            return;
        }
        WeaponArtState state = player.getData(SkillData.WEAPON_ART);
        if (state.cooldownTicks() > 0) {
            return;
        }
        double step = state.step() + 1;
        if (step > art.number(MAX)) {
            step = 1;
        }
        player.setData(SkillData.WEAPON_ART, new WeaponArtState(step, 30, 10));
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
        WeaponArtState state = player.getData(SkillData.WEAPON_ART);
        double window = state.windowTicks();
        double step = state.step();
        if (window > 0) {
            window -= 1;
            if (window <= 0) {
                step = 0;
            }
        }
        double cooldown = state.cooldownTicks() > 0 ? state.cooldownTicks() - 1 : state.cooldownTicks();
        if (step != state.step() || window != state.windowTicks() || cooldown != state.cooldownTicks()) {
            player.setData(SkillData.WEAPON_ART, new WeaponArtState(step, window, cooldown));
        }
    }
}
