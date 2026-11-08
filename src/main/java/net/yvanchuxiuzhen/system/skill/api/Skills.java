package net.yvanchuxiuzhen.system.skill.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.skill.SkillLibrary;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.ArrowSelection;
import net.yvanchuxiuzhen.system.skill.SkillStudy;
import net.yvanchuxiuzhen.system.skill.SkillFlight;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.content.SkillMenus;
import net.yvanchuxiuzhen.system.skill.entity.DamageArea;
import net.yvanchuxiuzhen.system.skill.entity.Yuanying;
import net.yvanchuxiuzhen.system.skill.entity.VoidRift;
import net.yvanchuxiuzhen.system.skill.entity.WindEntities;
import net.yvanchuxiuzhen.system.skill.projectile.SkillArrows;

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

    /** 来源 flybegin，供界面、飞行装备与重置入口查询或写入；原生飞行能力由具体操作负责。 */
    public static boolean flying(Player player) {
        return player.getData(SkillData.FLYING);
    }

    public static void setFlying(ServerPlayer player, boolean flying) {
        player.setData(SkillData.FLYING, flying);
    }

    public static void toggleFlight(ServerPlayer player) {
        SkillFlight.toggle(player);
    }

    /** C 键的剩余格挡显示刻数，只有来源计时与 HUD 语义。 */
    public static double guardRemaining(Player player) {
        return player.getData(SkillData.GUARD_REMAINING);
    }

    /** G 键或其他系统的导航入口，打开本人的无槽技能选择菜单。 */
    public static void openMenu(ServerPlayer player) {
        SkillMenus.open(player);
    }

    /** 玩家面板的功法与技能八槽装配页，物品真值在关闭或技能翻页时交回所属系统。 */
    public static void openLoadout(ServerPlayer player) {
        SkillMenus.openLoadout(player);
    }

    /** 在指定的最终打开坐标创建装配页；跨页导航先将上一菜单坐标向上移一格。 */
    public static void openLoadout(ServerPlayer player, BlockPos pos) {
        SkillMenus.openLoadout(player, pos);
    }

    /** 四页技能循环翻页，不改当前已选技能、连招、箭种和冷却；装配菜单先保存旧页四槽。 */
    public static void turnPage(ServerPlayer player, int step) {
        SkillSelection.turnPage(player, step);
    }

    /** 武器技槽（来源 {@code wuqiskill}）。 */
    public static ItemStack weaponArt(Player player) {
        return state(player).weaponArt();
    }

    public static void setWeaponArt(ServerPlayer player, ItemStack stack) {
        player.setData(SkillData.STATE, state(player).withWeaponArt(stack));
    }

    /** 当前箭种的物品表示与轮盘序号 1–11；没有选择时物品为空、序号为 0。 */
    public static ItemStack arrow(Player player) {
        return state(player).arrow();
    }

    public static int arrowChoice(Player player) {
        return state(player).arrowChoice();
    }

    /** 重置或写入箭种选择，不扣资源。 */
    public static void setArrow(ServerPlayer player, ItemStack stack, int choice) {
        player.setData(SkillData.STATE, state(player).withArrow(stack.copy(), choice));
    }

    public static boolean canSelectArrow(Player player, int choice) {
        return ArrowSelection.available(player, choice);
    }

    public static boolean selectArrow(ServerPlayer player, int choice) {
        return ArrowSelection.select(player, choice);
    }

    /**
     * 创建尚未加入世界的技能箭，类型为 1–10。调用者设置基础伤害、位置和速度后加入世界，并负责弓的蓄力与消耗。
     * 修饰键由调用者提供；箭在首次飞行刻消费箭技能与满蓄力标记，按来源顺序选择模式。
     */
    public static AbstractArrow createArrow(ServerPlayer owner, int kind, int knockback, int pierce, boolean control, boolean alt,
                                             boolean fullCharge, boolean infinity) {
        return SkillArrows.create(owner, kind, knockback, pierce, control, alt, fullCharge, infinity);
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

    /** 技能界面的虚空按钮：境界含修正达到 31 时，在视线五格处开启虚空裂隙。 */
    public static void openVoidRift(ServerPlayer player) {
        VoidRift.open(player);
    }

    /** 显示伤害范围；普通提示的技能名为空，控雷阵与惊蛰闻雷使用各自的计时分支。 */
    public static void showDamageArea(Entity owner, Vec3 at, String animation, String skillName, double duration, double attackDamage) {
        DamageArea.show(owner, at, animation, skillName, duration, attackDamage);
    }

    /** 技能界面的元婴按钮：达到元婴期时出窍，再次调用归窍。 */
    public static void toggleYuanying(ServerPlayer player) {
        Yuanying.toggle(player);
    }

    /** 妖兽爆气使用的八刻风波；只生成技能实体，爆气的击退、声音与粒子由调用者处理。 */
    public static void showWindBurst(Entity caster) {
        WindEntities.showBurst(caster);
    }

    /** 妖兽黑风使用的追踪风刃；调用者传入攻击量，技能系统记录攻击者、目标及二百刻上限。 */
    public static void launchWindBlade(Entity caster, Entity target, double attackDamage) {
        WindEntities.launchBlade(caster, target, attackDamage);
    }
}
