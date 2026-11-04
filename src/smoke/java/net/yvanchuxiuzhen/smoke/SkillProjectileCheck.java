package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.system.skill.SkillCasting;
import net.yvanchuxiuzhen.system.skill.SkillSelection;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 技能弹体的实机检查：十五个弹体技能各自施放一次，发射出弹体，命中面前的铁傀儡并使其掉血，施放者不受伤，
 * 冰系命中后目标冷冻、定魄神光命中后目标缓慢。飞羽冰花沿侧向圆弧飞行，打不到正前方的目标，只检查它到时消失、不伤施放者。
 */
final class SkillProjectileCheck extends SystemCheck {
    private enum Extra { NONE, FROZEN, SLOWED, ARC }

    private record Case(String body, String name, Extra extra) {
    }

    private static final List<Case> CASES = List.of(
            new Case("金", "金芒术", Extra.NONE), new Case("金", "碎裂金芒", Extra.NONE), new Case("水", "水球术", Extra.NONE),
            new Case("水", "水灵弹", Extra.NONE), new Case("水", "冰锥术", Extra.FROZEN), new Case("水", "破碎冰矛", Extra.FROZEN),
            new Case("水", "飞羽冰花", Extra.ARC), new Case("木", "拈花飞叶", Extra.NONE), new Case("木", "风弹术", Extra.NONE),
            new Case("火", "火球术", Extra.NONE), new Case("火", "连环火球术", Extra.NONE), new Case("火", "光弹术", Extra.NONE),
            new Case("火", "定魄神光", Extra.SLOWED), new Case("火", "幻光术", Extra.NONE), new Case("土", "飞石术", Extra.NONE));
    private static final double HEALTH = 100;

    private IronGolem golem;
    private double playerHealth;

    SkillProjectileCheck(List<String> report, String levelId) {
        super("技能弹体", report, levelId);
    }

    /** 攻击倍率 3、消耗 0、金组冷却 1 秒且上限 100 秒的技能书（来源要求至少一组冷却数值大于 0 才能施放）。 */
    private static ItemStack book(ServerPlayer player, Case spell) {
        String arguments = "yvanchuxiuzhen:book_003 " + spell.name() + " 1 1 " + spell.body() + " x 无 3 1 0 0 1 0 0 0 0 0 100 0 0 0 0 0";
        ItemStack stack = Skills.createBook(SkillKind.SPELL, List.of(arguments.split(" ")));
        SkillBook parsed = Skills.book(stack).orElseThrow();
        stack.set(SkillData.SKILL.get(), parsed.withOwner(player.getUUID(), player.getName().getString()));
        return stack;
    }

    private void clearArea(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB area = new AABB(player.blockPosition()).inflate(16);
        level.getEntitiesOfClass(SkillProjectile.class, area).forEach(SkillProjectile::discard);
        if (golem != null) {
            golem.discard();
            golem = null;
        }
    }

    @Override
    protected boolean step(int step, int local) {
        int index = step / 2;
        if (index >= CASES.size()) {
            onServer(player -> {
                clearArea(player);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                Skills.setSpell(player, 1, ItemStack.EMPTY);
                SkillSelection.clear(player);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
            });
            finish();
            return true;
        }
        Case spell = CASES.get(index);
        if (step % 2 == 0) {
            onServer(player -> {
                clearArea(player);
                ServerLevel level = player.serverLevel();
                player.connection.teleport(player.getX(), player.getY(), player.getZ(), -90, 0);
                golem = EntityType.IRON_GOLEM.create(level);
                golem.moveTo(player.getX() + 4, player.getY(), player.getZ(), 0, 0);
                golem.setNoAi(true);
                golem.setHealth((float) HEALTH);
                level.addFreshEntity(golem);
                playerHealth = player.getHealth();
                Cultivation.set(player, CultivationValue.POWER, 1000);
                player.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                Skills.setSpell(player, 1, book(player, spell));
                SkillSelection.clear(player);
                SkillSelection.select(player, 1);
                expect(spell.name() + " 已选中", spell.name(), Skills.book(Skills.selected(player)).map(SkillBook::name).orElse(""));
                SkillCasting.onRightClick(player);
                expectTrue(spell.name() + " 发射出弹体", !player.serverLevel().getEntitiesOfClass(SkillProjectile.class, new AABB(player.blockPosition()).inflate(16)).isEmpty());
            });
            return true;
        }
        if (local < 14) {
            return false;
        }
        onServer(player -> {
            expect(spell.name() + " 施放者不受伤", playerHealth, player.getHealth());
            if (spell.extra() == Extra.ARC) {
                expectTrue(spell.name() + " 到时消失", player.serverLevel().getEntitiesOfClass(SkillProjectile.class, new AABB(player.blockPosition()).inflate(32)).isEmpty());
                clearArea(player);
                return;
            }
            expectTrue(spell.name() + " 命中使目标掉血 " + golem.getHealth(), golem.getHealth() < HEALTH);
            if (spell.extra() == Extra.FROZEN) {
                expectTrue(spell.name() + " 目标被冷冻 " + golem.getTicksFrozen(), golem.getTicksFrozen() > 0);
            } else if (spell.extra() == Extra.SLOWED) {
                expectTrue(spell.name() + " 目标缓慢", golem.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
            }
            clearArea(player);
        });
        return true;
    }
}
