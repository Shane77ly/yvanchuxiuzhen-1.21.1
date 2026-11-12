package net.yvanchuxiuzhen.system.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/**
 * 技能栏的页与选择（来源 {@code AnjianZ/X/C/V}、{@code Jinengqvxiao}、{@code Tabanjian}、{@code JinengGuiPageUp/Down}、
 * {@code Lianzhaoanniu}）。四页各四槽；当前页的第 k 个键对应技能栏第 (页 − 1) × 4 + k 槽。
 */
public final class SkillSelection {
    private static final int PAGES = 4;
    /** 按键序号 1 的选择不检查禁施与沉默，与来源 {@code AnjianZ} 一致。 */
    private static final int UNGUARDED_KEY = 1;

    private SkillSelection() {
    }

    /**
     * 选择当前页第 {@code key}（1–4）个键对应的技能。同一页再次按同一个键取消选择；选择在禁施（{@code skilleffect_09}）或
     * 沉默（{@code effect_002}）时只提示。槽为空或技能书不属于本人时提示“空”，原选择不变。最后关闭当前界面。
     */
    public static void select(ServerPlayer player, int key) {
        SkillState state = Skills.state(player);
        int selectedPage = (int) Math.ceil(state.selectedSlot() / 4.0);
        if (state.page() != selectedPage || state.selectedKey() != key) {
            state = state.withKey(key);
            player.setData(SkillData.STATE, state);
            if (key == UNGUARDED_KEY || (!StatusEffects.has(player, "skilleffect_09") && !StatusEffects.has(player, "effect_002"))) {
                pick(player, state, (state.page() - 1) * SkillState.PAGE_SIZE + key);
            } else if (!StatusEffects.has(player, "effect_002")) {
                player.displayClientMessage(Component.literal("§4沉默中"), false);
            } else {
                player.displayClientMessage(Component.literal("§4冷却中"), false);
            }
        } else if (!state.selected().isEmpty()) {
            clear(player);
        }
        player.closeContainer();
    }

    private static void pick(ServerPlayer player, SkillState state, int slot) {
        ItemStack stack = state.spell(slot);
        SkillBook book = stack.get(SkillData.SKILL.get());
        if (book != null && book.ownedBy(player.getUUID())) {
            player.setData(SkillData.STATE, state.withSelection(stack, state.selectedKey(), slot));
            player.displayClientMessage(Component.literal("技能" + book.name() + "准备就绪"), false);
        } else {
            player.displayClientMessage(Component.literal("空"), false);
        }
    }

    /** 取消选择（来源 {@code Jinengqvxiao}，按键 B）：清空选择、按键序号与技能栏序号并提示。 */
    public static void clear(ServerPlayer player) {
        player.setData(SkillData.STATE, Skills.state(player).withSelection(ItemStack.EMPTY, 0, 0));
        player.displayClientMessage(Component.literal("取消装备技能"), false);
    }

    /** 按键 Tab：翻到下一页并提示页名；第四页之后回到第一页。 */
    public static void nextPage(ServerPlayer player) {
        SkillState state = Skills.state(player);
        int next = state.page() % PAGES + 1;
        player.setData(SkillData.STATE, state.withPage(next));
        player.displayClientMessage(Component.literal("第" + "一二三四".charAt(next - 1) + "页技能"), false);
    }

    /** 技能界面的上一页、下一页按钮：循环翻页，不提示。 */
    public static void turnPage(ServerPlayer player, int step) {
        SkillState state = Skills.state(player);
        int next = (state.page() - 1 + step + PAGES) % PAGES + 1;
        player.setData(SkillData.STATE, state.withPage(next));
    }

    /** 技能界面的连招按钮：翻转连招开关。 */
    public static void toggleCombo(ServerPlayer player) {
        SkillState state = Skills.state(player);
        player.setData(SkillData.STATE, state.withCombo(!state.combo()));
    }
}
