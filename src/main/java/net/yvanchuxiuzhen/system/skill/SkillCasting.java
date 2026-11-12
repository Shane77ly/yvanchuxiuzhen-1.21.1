package net.yvanchuxiuzhen.system.skill;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.FightingState;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.api.WeaponStatEvent;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 技能的施放与冷却（来源 {@code PlayerRightClick} 的施放分支、{@code Jiancewuqi}、{@code Jinenglengqvejiance}、
 * {@code Colddownjisuan}、{@code Colddownplayertick}、{@code Lianzhaoxitong}）。
 * 手持武器（标签 {@code wuqi}，不含 {@code yibow}）右键时，所选技能满足武器类别、本人研习、没有沉默、灵力足够、
 * 有一个冷却分组在可施放范围、没有禁施，才会施放：禁施、分派、冷却累加、扣灵力、连招换下一个、进入战斗状态、挥手。
 */
public final class SkillCasting {
    private static final TagKey<Item> WEAPON = tag("wuqi");
    private static final TagKey<Item> BOW = tag("yibow");
    /** 技能书“所需武器”文字与武器类别标签。 */
    private static final Map<String, TagKey<Item>> NEEDED = Map.of("剑", tag("yisword"), "刀", tag("yibroadsword"), "长兵", tag("yispear"),
            "弓弩", BOW, "拂尘", tag("yihossu"), "奇门", tag("yiqimen"), "法宝", tag("yifabao"));

    private SkillCasting() {
    }

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 右键物品时尝试施放所选技能。 */
    public static void onRightClick(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!held.is(WEAPON) || held.is(BOW)) {
            return;
        }
        Optional<SkillBook> selected = Skills.book(Skills.selected(player));
        if (selected.isEmpty() || !weaponFits(held, selected.get()) || !selected.get().ownedBy(player.getUUID())) {
            return;
        }
        SkillBook book = selected.get();
        if (StatusEffects.has(player, "effect_002")) {
            bar(player, "§4沉默");
            return;
        }
        double cost = cost(player, held, book);
        if (Cultivation.get(player, CultivationValue.POWER) < cost) {
            bar(player, "§4灵力不足");
            return;
        }
        if (!anyGroupOpen(player, book)) {
            bar(player, "§4冷却中...");
            return;
        }
        if (StatusEffects.has(player, "skilleffect_09")) {
            bar(player, "§4施法中...");
            return;
        }
        if (book.seal() > 0) {
            StatusEffects.addHidden(player, "skilleffect_09", (int) book.seal(), 0);
        }
        SkillReleases.dispatch(player, book);
        accumulateCooldowns(player, book);
        Cultivation.add(player, CultivationValue.POWER, -cost);
        if (Skills.state(player).combo()) {
            nextReady(player);
        }
        FightingState.mark(player);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static void bar(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), true);
    }

    /** 武器是否符合技能的类别要求：空或“无”接受任意武器，其余要求武器带对应类别标签；未识别的类别要求不接受任何武器。 */
    private static boolean weaponFits(ItemStack held, SkillBook book) {
        String needed = book.neededWeapon();
        if (needed.isEmpty() || needed.equals("无")) {
            return true;
        }
        TagKey<Item> required = NEEDED.get(needed);
        return required != null && held.is(required);
    }

    /** 灵力消耗 = (法术攻击基础值 + 武器法术攻击) × 消耗系数，再加体质固定值、乘体质百分比。 */
    private static double cost(Player player, ItemStack held, SkillBook book) {
        WeaponStatEvent weapon = NeoForge.EVENT_BUS.post(new WeaponStatEvent(held, "mana_Att"));
        double cost = (PlayerAttributes.baseValue(player, Stat.MANA_ATT) + weapon.total()) * book.consumption();
        double fixed = Cultivation.tizhiDouble(player, "SkillConsumePower");
        if (fixed != 0) {
            cost += fixed;
        }
        double percentage = Cultivation.tizhiDouble(player, "SkillConsumePowerPercentage");
        if (percentage != 0) {
            cost *= percentage + 1;
        }
        return cost;
    }

    /** 任一有冷却的分组当前冷却不超过该组的可施放上限（秒 × 20）即可施放。 */
    private static boolean anyGroupOpen(Player player, SkillBook book) {
        for (SkillElement element : SkillElement.values()) {
            if (book.cooldown(element) > 0 && Skills.cooldown(player, element) <= book.cooldownIgnore(element) * 20) {
                return true;
            }
        }
        return false;
    }

    /**
     * 连招换技能时判定所选技能能否施放：各分组依次判定，有冷却的分组按“当前冷却不超过上限”取值，后面的分组覆盖前面的结果。
     */
    static boolean ready(Player player, SkillBook book) {
        boolean ready = false;
        for (SkillElement element : SkillElement.values()) {
            if (book.cooldown(element) > 0) {
                ready = book.cooldownIgnore(element) * 20 >= Skills.cooldown(player, element);
            }
        }
        return ready;
    }

    /**
     * 施放后累加冷却（来源 {@code Colddownjisuan}）。每组有冷却数值时：若该组正在冷却且数值为负，按负值缩短该组冷却，
     * 加体质固定值、乘体质百分比，并标记可恢复；否则该组冷却不超过上限时，增加 数值 × 20 × (1 − 冷却缩减)，再乘体质技能冷却百分比。
     * 增量变量在各组之间不重置，某组两个条件都不满足时沿用上一组的增量；阴阳组的上限比较读取的是土组冷却（Q36）。
     * 标记可恢复后按体质恢复生命、灵力与神识。
     */
    private static void accumulateCooldowns(ServerPlayer player, SkillBook book) {
        double reduction = PlayerAttributes.value(player, Stat.COOLDOWN_REDUCTION);
        double fixed = Cultivation.tizhiDouble(player, "ColddownCorrect");
        double percentage = Cultivation.tizhiDouble(player, "ColddownCorrectPercentage");
        double skillPercentage = Cultivation.tizhiDouble(player, "SkillColdCorrectPercentage");
        SkillCooldowns cooldowns = player.getData(SkillData.COOLDOWNS);
        double delta = 0;
        boolean recover = false;
        for (SkillElement element : SkillElement.values()) {
            double seconds = book.cooldown(element);
            if (seconds == 0) {
                continue;
            }
            double current = cooldowns.get(element);
            SkillElement compared = element == SkillElement.YINYANG ? SkillElement.SOIL : element;
            if (current > 0 && seconds < 0) {
                delta = seconds * 20;
                if (fixed != 0) {
                    delta += fixed;
                }
                if (percentage != 0) {
                    delta *= percentage + 1;
                }
                recover = true;
            } else if (cooldowns.get(compared) <= book.cooldownIgnore(element) * 20) {
                delta = seconds * 20 * (1 - reduction);
                if (skillPercentage != 0) {
                    delta *= skillPercentage + 1;
                }
            }
            cooldowns = cooldowns.with(element, current + delta);
        }
        player.setData(SkillData.COOLDOWNS, cooldowns);
        if (recover) {
            recover(player);
        }
    }

    /** 缩短冷却后的体质恢复：生命、灵力、神识的固定值与占上限百分比。 */
    private static void recover(ServerPlayer player) {
        double health = Cultivation.tizhiDouble(player, "SkillRestoreHealth");
        if (health != 0) {
            player.setHealth((float) (player.getHealth() + health));
        }
        double healthPercentage = Cultivation.tizhiDouble(player, "SkillRestoreHealthPercentage");
        if (healthPercentage != 0) {
            player.setHealth((float) (player.getHealth() + player.getMaxHealth() * healthPercentage));
        }
        double power = Cultivation.tizhiDouble(player, "SkillRestorePower");
        if (power != 0) {
            Cultivation.add(player, CultivationValue.POWER, power);
        }
        double powerPercentage = Cultivation.tizhiDouble(player, "SkillRestorePowerPercentage");
        if (powerPercentage != 0) {
            Cultivation.add(player, CultivationValue.POWER, PlayerAttributes.value(player, Stat.POWER_MAX) * powerPercentage);
        }
        double soul = Cultivation.tizhiDouble(player, "SkillRestoreJingli");
        if (soul != 0) {
            Cultivation.add(player, CultivationValue.SOUL, soul);
        }
        double soulPercentage = Cultivation.tizhiDouble(player, "SkillRestoreJingliPercentage");
        if (soulPercentage != 0) {
            Cultivation.add(player, CultivationValue.SOUL, PlayerAttributes.value(player, Stat.SOUL_MAX) * soulPercentage);
        }
    }

    /**
     * 连招：从当前按键序号的下一个键起依次检查当前页的四个键（一圈为止），遇到非空槽就选中它，
     * 能施放则停在这个技能上；选择序号每检查一个键就前进一格。
     */
    private static void nextReady(ServerPlayer player) {
        SkillState state = Skills.state(player);
        for (int step = 0; step < SkillState.PAGE_SIZE; step++) {
            int key = state.selectedKey() % SkillState.PAGE_SIZE + 1;
            state = state.withKey(key);
            int slot = (state.page() - 1) * SkillState.PAGE_SIZE + key;
            ItemStack stack = state.spell(slot);
            if (!stack.isEmpty()) {
                state = state.withSelection(stack, key, slot);
                Optional<SkillBook> book = Skills.book(stack);
                if (book.isPresent() && ready(player, book.get())) {
                    break;
                }
            }
        }
        player.setData(SkillData.STATE, state);
    }

    /** 每刻把六个冷却分组减 1，负数归零（来源 {@code Colddownplayertick}）。 */
    public static void tick(ServerPlayer player) {
        SkillCooldowns cooldowns = player.getData(SkillData.COOLDOWNS);
        SkillCooldowns next = cooldowns;
        for (SkillElement element : SkillElement.values()) {
            double value = cooldowns.get(element);
            if (value > 0) {
                next = next.with(element, value - 1);
            } else if (value < 0) {
                next = next.with(element, 0);
            }
        }
        if (next != cooldowns) {
            player.setData(SkillData.COOLDOWNS, next);
        }
    }
}
