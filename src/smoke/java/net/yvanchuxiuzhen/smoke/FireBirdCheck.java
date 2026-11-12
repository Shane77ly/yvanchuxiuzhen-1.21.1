package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.FireBird;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 火鸟术（可驯服飞行实体）的实机检查：施放生成以施放者为主人的火鸟（施放信息、位置、无重力、与主人同队）；非友方进入 1.5 格后 3 刻爆发，
 * 伤害并点燃 2.5 格内的非友方后消失；友方不触发爆发；寿命 1200 刻到期消失；有目标时飞向目标并爆发；免疫仙人掌和跌落结算，被杀死时消失；
 * 实体存档保存施放信息（技能动画实体与火鸟各存取一次）。
 */
final class FireBirdCheck extends ActionCheck {
    private static final double HEALTH = 100;
    private static final String SPELL = "火鸟术";

    private IronGolem golem;

    FireBirdCheck(List<String> report, String levelId) {
        super("火鸟术", report, levelId);
        script(
                new Action(12, player -> {
                    clearArea(player);
                    Arena.level(player);
                    player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
                    Cultivation.set(player, CultivationValue.POWER, 1000);
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                    Skills.setSpell(player, 1, book(player));
                    SkillSelection.clear(player);
                    SkillSelection.select(player, 1);
                    expect("火鸟术已选中", SPELL, Skills.book(Skills.selected(player)).map(SkillBook::name).orElse(""));
                }),
                new Action(4, player -> {
                    cast(player);
                    expect("施放生成一只火鸟", 1, birds(player).size());
                    FireBird bird = birds(player).get(0);
                    SkillBook book = Skills.book(Skills.selected(player)).orElseThrow();
                    expectTrue("主人是施放者", bird.isTame() && bird.getOwnerUUID().equals(player.getUUID()));
                    expect("攻击者", player.getUUID(), bird.cast().attacker());
                    expect("攻击量", book.manaAttack(), bird.cast().attackDamage());
                    expect("伤害倍率", book.damageMultiplier(), bird.cast().multiplier());
                    expect("伤害类别", "火", bird.cast().damageType());
                    expect("技能名", SPELL, bird.cast().skillName());
                    expect("默认纹理", "skillfire06", bird.texture());
                    expectTrue("在施放者上方 1.5 格", Math.abs(bird.getY() - (player.getY() + 1.5)) < 0.01);
                    expectTrue("与主人同队", Relations.isFriendly(bird, player));
                }),
                new Action(6, player -> {
                    expect("主人在身边时不爆发", 1, birds(player).size());
                    expectTrue("无重力（首个实体刻起）", birds(player).get(0).isNoGravity());
                    expectTrue("不下坠 " + birds(player).get(0).getY(), birds(player).get(0).getY() > player.getY() + 1);
                    golem = placeGolem(player, player.getX() + 1, false);
                }),
                new Action(2, player -> {
                    expect("非友方进入 1.5 格后爆发消失", 0, birds(player).size());
                    expectTrue("爆发伤害非友方", golem.getHealth() < HEALTH);
                    expectTrue("爆发点燃非友方", golem.getRemainingFireTicks() > 0);
                }),
                new Action(2, player -> {
                    clearArea(player);
                    cast(player);
                    golem = placeGolem(player, player.getX() + 1, true);
                }),
                new Action(12, player -> {
                    expect("友方不触发爆发", 1, birds(player).size());
                    expect("友方不受伤", HEALTH, (double) golem.getHealth());
                    clearArea(player);
                    cast(player);
                    birds(player).get(0).getPersistentData().putDouble("tick", 1199);
                }),
                new Action(2, player -> expect("寿命到期消失", 0, birds(player).size())),
                new Action(2, player -> {
                    cast(player);
                    golem = placeGolem(player, player.getX() + 5, false);
                    birds(player).get(0).setTarget(golem);
                }),
                new Action(2, player -> expect("追击目标：定为目标后寿命内飞向目标", 1, birds(player).size())),
                new Action(80, player -> {
                }),
                new Action(2, player -> {
                    expect("追击目标：撞上后爆发消失", 0, birds(player).size());
                    expectTrue("追击目标：目标受伤", golem.getHealth() < HEALTH);
                    expectTrue("追击目标：目标被点燃", golem.getRemainingFireTicks() > 0);
                }),
                new Action(2, player -> {
                    clearArea(player);
                    cast(player);
                    FireBird bird = birds(player).get(0);
                    expectTrue("免疫仙人掌", !bird.hurt(player.damageSources().cactus(), 5));
                    float health = bird.getHealth();
                    expectTrue("免疫跌落伤害结算", !bird.causeFallDamage(10, 1, player.damageSources().fall()));
                    expect("跌落结算后生命值不变", (double) health, (double) bird.getHealth());
                    bird.hurt(player.damageSources().generic(), 1000);
                    expect("被杀死时消失", 0, birds(player).size());
                }),
                new Action(2, player -> {
                    SkillEntity animated = SkillSetup.<SkillEntity>entityType("skill_fire_02").get().create(player.serverLevel());
                    animated.configure(player.getUUID(), 12.5, 1.5, "火", "大火球");
                    animated.setHitboxScale(2);
                    SkillEntity restoredAnimated = SkillSetup.<SkillEntity>entityType("skill_fire_02").get().create(player.serverLevel());
                    restoredAnimated.load(animated.saveWithoutId(new CompoundTag()));
                    expect("动画实体存档：攻击者", player.getUUID(), restoredAnimated.attacker());
                    expect("动画实体存档：攻击量", 12.5, restoredAnimated.attackDamage());
                    expect("动画实体存档：倍率", 1.5, restoredAnimated.multiplier());
                    expect("动画实体存档：类别", "火", restoredAnimated.damageType());
                    expect("动画实体存档：技能名", "大火球", restoredAnimated.skillName());
                    expect("动画实体存档：碰撞箱比例", animated.getBbWidth(), restoredAnimated.getBbWidth());
                    FireBird bird = SkillSetup.<FireBird>entityType(FireBird.ID).get().create(player.serverLevel());
                    bird.cast().configure(player.getUUID(), 7.5, 2.5, "火", SPELL);
                    bird.setTexture("skillfire06");
                    FireBird restored = SkillSetup.<FireBird>entityType(FireBird.ID).get().create(player.serverLevel());
                    restored.load(bird.saveWithoutId(new CompoundTag()));
                    expect("火鸟存档：攻击者", player.getUUID(), restored.cast().attacker());
                    expect("火鸟存档：攻击量", 7.5, restored.cast().attackDamage());
                    expect("火鸟存档：倍率", 2.5, restored.cast().multiplier());
                    expect("火鸟存档：类别", "火", restored.cast().damageType());
                    expect("火鸟存档：技能名", SPELL, restored.cast().skillName());
                })
        );
    }

    private static AABB area(ServerPlayer player) {
        return new AABB(player.blockPosition()).inflate(24);
    }

    private static List<FireBird> birds(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(FireBird.class, area(player));
    }

    /** 攻击倍率 3、消耗 0、火组冷却 1 秒且上限 100 秒的火鸟术书（来源要求至少一组冷却数值大于 0 才能施放）。 */
    private static ItemStack book(ServerPlayer player) {
        String arguments = "yvanchuxiuzhen:book_003 " + SPELL + " 1 1 火 x 无 3 1 0 0 0 0 0 1 0 0 0 0 0 100 0 0";
        ItemStack stack = Skills.createBook(SkillKind.SPELL, List.of(arguments.split(" ")));
        stack.set(SkillData.SKILL.get(), Skills.book(stack).orElseThrow().withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    private static void cast(ServerPlayer player) {
        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
        SkillCasting.onRightClick(player);
    }

    /** 在玩家东侧 {@code x} 处放一个不动的铁傀儡；{@code friend} 为真时与玩家同队。 */
    private IronGolem placeGolem(ServerPlayer player, double x, boolean friend) {
        IronGolem placed = EntityType.IRON_GOLEM.create(player.serverLevel());
        placed.moveTo(x, player.getY(), player.getZ(), 0, 0);
        placed.setNoAi(true);
        placed.setHealth((float) HEALTH);
        if (friend) {
            Relations.setTeam(placed, Relations.team(player));
        }
        player.serverLevel().addFreshEntity(placed);
        return placed;
    }

    private void clearArea(ServerPlayer player) {
        birds(player).forEach(FireBird::discard);
        if (golem != null) {
            golem.discard();
            golem = null;
        }
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clearArea(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        Skills.setSpell(player, 1, ItemStack.EMPTY);
        SkillSelection.clear(player);
        player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
    }
}
