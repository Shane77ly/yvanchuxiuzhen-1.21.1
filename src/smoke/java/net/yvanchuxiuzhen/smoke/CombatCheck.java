package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.combat.damage.Shanghai;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 战斗系统的实机检查：以铁傀儡（无 AI，100 点生命，法术防御 3、神识防御 2）为靶，核对灵力与神识伤害的结算数值、
 * 命令 {@code /shanghai}、闪避、手下留情，以及加点机会、灵力恢复和毒药效果在服务端的结果。
 * 期望值按 docs/systems/combat/architecture.md 的公式手算；闪避随机数为 0 时本次伤害无效（约 1/101，Q30），
 * 每次结算最多重试 5 次，取第一次产生的伤害。
 */
final class CombatCheck extends SystemCheck {
    private IronGolem poisoned;

    CombatCheck(List<String> report, String levelId) {
        super("战斗", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::damage);
            case 1 -> {
                if (local < 5) {
                    return false;
                }
                onServer(player -> {
                    expect("瞬间灵力恢复 灵力", 105, Cultivation.get(player, CultivationValue.POWER));
                    expectTrue("瞬间灵力恢复后效果移除", !StatusEffects.has(player, "effect_011"));
                    expect("永久生命 已加点次数", 3, Cultivation.get(player, CultivationValue.SKILL_POINTS_HEALTH_MAX));
                    expectTrue("永久生命后效果移除", !StatusEffects.has(player, "effect_008"));
                });
            }
            case 2 -> {
                if (local < 110) {
                    return false;
                }
                onServer(player -> {
                    near("断肠 100 刻后 生命", 100 - 8, poisoned.getHealth());
                    poisoned.discard();
                    clean(player);
                });
                finish();
            }
            default -> finish();
        }
        return true;
    }

    private void near(String label, double expected, double actual) {
        expectTrue(label + " 期望 " + expected + " 实际 " + actual, Math.abs(expected - actual) < 1e-3);
    }

    private IronGolem golem(ServerPlayer player, double health) {
        ServerLevel level = player.serverLevel();
        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        golem.moveTo(player.getX() + 3, player.getY(), player.getZ(), 0, 0);
        golem.setNoAi(true);
        golem.setHealth((float) health);
        golem.getPersistentData().putDouble("mana_Def", 3);
        golem.getPersistentData().putDouble("soul_Def", 2);
        level.addFreshEntity(golem);
        return golem;
    }

    /** 结算并返回造成的伤害；闪避随机数为 0 时重试。 */
    private double hit(ServerPlayer player, IronGolem golem, Shanghai.Request request) {
        double before = golem.getHealth();
        for (int attempt = 0; attempt < 5 && golem.getHealth() == before; attempt++) {
            golem.invulnerableTime = 0;
            Shanghai.deal(player.serverLevel(), golem, request);
        }
        return before - golem.getHealth();
    }

    private void clean(ServerPlayer player) {
        for (String effect : List.of("effect_013", "effect_008", "effect_011")) {
            StatusEffects.remove(player, effect);
        }
        PlayerAttributes.clearContribution(player, "smoke:combat");
        PlayerAttributes.clearContribution(player, "smoke:combat_gongfa");
    }

    private void damage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        PlayerAttributes.setContribution(player, "smoke:combat", new Contribution(Layer.BASE, Map.of(Stat.MANA_ATT, 5.0, Stat.SOUL_ATT, 2.0, Stat.ROOT_FIRE, 3.0)));
        PlayerAttributes.setContribution(player, "smoke:combat_gongfa", new Contribution(Layer.BONUS, Map.of(Stat.GONGFA_FIRE, 5.0, Stat.GONGFA_LEVEL, 8.0)));

        IronGolem golem = golem(player, 100);
        double damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, null, true, 6, 1));
        near("无来源固定火伤害 6²/(6+3)", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, player.getUUID(), false, 4, 1));
        // 攻击量 = ((4 + 法术攻击 6) × 1 × (1 + 火灵根 0.3) 倍率 + 功法 5×8/4)：10 × 1.3 + 10 = 23，总量 23²/(23+3)
        near("玩家火伤害 23²/(23+3)", 23.0 * 23 / 26, damage);
        expectTrue("重击上僵直", StatusEffects.has(golem, "effect_033"));
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.FIRE, player.getUUID(), true, 6, 1));
        near("有来源固定火伤害 6²/(6+3)", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.SOUL, player.getUUID(), false, 5, 1));
        near("玩家灵魂伤害 (5+神识攻击 3) − 2", 6, damage);
        golem.discard();

        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_013"), 200, 1));
        golem = golem(player, 100);
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.NONE, player.getUUID(), false, 4, 1));
        // 攻击量 (4 + 6) + 法术伤害提高 (1+1)² = 14，总量 14²/(14+3)
        near("法术伤害提高 14²/(14+3)", 14.0 * 14 / 17, damage);
        golem.discard();
        StatusEffects.remove(player, "effect_013");

        golem = golem(player, 5);
        golem.addEffect(new MobEffectInstance(StatusEffects.holder("effect_036"), 200, 0));
        damage = hit(player, golem, new Shanghai.Request(DamageCategory.NONE, null, true, 20, 1));
        near("手下留情 总量不超过当前生命 − 1", 4, damage);
        golem.discard();

        golem = golem(player, 100);
        ItemStack helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchants.key("dodge")), 100);
        golem.setItemSlot(EquipmentSlot.HEAD, helmet);
        expect("四件护甲闪避附魔等级之和", 100, Enchants.armorSum(golem, Enchants.key("dodge")));
        Shanghai.deal(level, golem, new Shanghai.Request(DamageCategory.NONE, null, true, 6, 1));
        near("闪避 100 级 伤害", 0, 100 - golem.getHealth());
        golem.discard();

        golem = golem(player, 100);
        double before = golem.getHealth();
        for (int attempt = 0; attempt < 5 && golem.getHealth() == before; attempt++) {
            golem.invulnerableTime = 0;
            level.getServer().getCommands().performPrefixedCommand(golem.createCommandSourceStack().withSuppressedOutput(), "shanghai 无 无 true 6 1");
        }
        near("命令 /shanghai 无 无 true 6 1", 4, before - golem.getHealth());
        golem.discard();

        Cultivation.set(player, CultivationValue.POWER, 5);
        Cultivation.set(player, CultivationValue.SKILL_POINTS_HEALTH_MAX, 5);
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_011"), 100, 0));
        player.addEffect(new MobEffectInstance(StatusEffects.holder("effect_008"), 100, 1));

        poisoned = golem(player, 100);
        poisoned.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_01"), 400, 1));
    }
}
