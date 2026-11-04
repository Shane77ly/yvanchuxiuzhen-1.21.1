package net.yvanchuxiuzhen.system.skill.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.skill.SkillLibrary;
import net.yvanchuxiuzhen.system.skill.SkillStudy;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;

/**
 * 技能系统的公开操作。技能书实例（{@link SkillBook}）在书的物品组件上；技能栏、身法槽、轻功槽、武器技槽、当前选择与连招开关在玩家附件
 * {@link SkillState}，六组冷却在 {@link SkillCooldowns}。写操作只在服务端调用，写入后同步给本人。
 */
public final class Skills {
    private Skills() {
    }

    /** 物品上的技能书实例；不是技能书时为空。 */
    public static Optional<SkillBook> book(ItemStack stack) {
        return Optional.ofNullable(stack.get(SkillData.SKILL.get()));
    }

    public static SkillState state(Player player) {
        return player.getData(SkillData.STATE);
    }

    /** 技能栏第 {@code slot}（1–16）槽中的书（来源 {@code jineng01}–{@code jineng16}）。 */
    public static ItemStack spell(Player player, int slot) {
        return state(player).spell(slot);
    }

    /** 放入或取出技能栏的一槽（面板的技能页、符箓的重置）。 */
    public static void setSpell(ServerPlayer player, int slot, ItemStack stack) {
        player.setData(SkillData.STATE, state(player).withSpell(slot, stack));
    }

    /** 当前选择的技能书（来源 {@code jinengxvanze}）；没有选择为空。 */
    public static ItemStack selected(Player player) {
        return state(player).selected();
    }

    /** 身法槽（来源 {@code qinggong}）。 */
    public static ItemStack movement(Player player) {
        return state(player).movement();
    }

    public static void setMovement(ServerPlayer player, ItemStack stack) {
        player.setData(SkillData.STATE, state(player).withMovement(stack));
    }

    /** 轻功槽（来源 {@code flyjineng}）。 */
    public static ItemStack flight(Player player) {
        return state(player).flight();
    }

    public static void setFlight(ServerPlayer player, ItemStack stack) {
        player.setData(SkillData.STATE, state(player).withFlight(stack));
    }

    /** 武器技槽（来源 {@code wuqiskill}）。 */
    public static ItemStack weaponArt(Player player) {
        return state(player).weaponArt();
    }

    public static void setWeaponArt(ServerPlayer player, ItemStack stack) {
        player.setData(SkillData.STATE, state(player).withWeaponArt(stack));
    }

    /** 某一冷却分组的剩余刻数（来源 {@code colddownGold} 等）。 */
    public static double cooldown(Player player, SkillElement element) {
        return player.getData(SkillData.COOLDOWNS).get(element);
    }

    /** 研习手中的技能书（书籍右键的研习分支）；已被研习、境界或灵根不足时只提示。 */
    public static void study(ServerPlayer player, ItemStack stack) {
        SkillStudy.study(player, stack);
    }

    /** 由命令参数串生成一本未研习的技能书（管理命令与玩法发书）；参数格式见 {@link SkillLibrary#fromArguments}。 */
    public static ItemStack createBook(SkillKind kind, List<String> arguments) {
        return SkillLibrary.fromArguments(kind, arguments);
    }

    /** 书架藏宝里的技能书（来源 {@code Jinenglistzong}）：在 13 本中随机给一本。 */
    public static void giveTreasureBook(ServerPlayer player) {
        ItemHandlerHelper.giveItemToPlayer(player, SkillLibrary.treasure(player.getRandom()));
    }
}
