package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.DamageElement;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;

/**
 * 属性系统的实机检查：提交贡献后核对服务端的最终值、基础值与原版属性，客户端收到的本人数据与原版属性同步；
 * 撤销一项贡献后当前生命压到新上限；死亡重生与退出重进后贡献与原版属性保持。
 * 数值取 docs/systems/attribute/architecture.md 的 Q17、Q18 算例。
 */
final class AttributeCheck extends SystemCheck {
    AttributeCheck(List<String> report, String levelId) {
        super("属性", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::submitContributions);
            case 1 -> {
                if (local < 20) {
                    return false;
                }
                checkClient("提交后", 35, 13);
                onServer(player -> {
                    player.setHealth(35);
                    PlayerAttributes.clearContribution(player, "smoke:ornament");
                });
            }
            case 2 -> {
                if (local < 20) {
                    return false;
                }
                onServer(player -> {
                    expect("撤销饰品后 最终生命上限", 30, PlayerAttributes.value(player, Stat.HEALTH_MAX));
                    expect("撤销饰品后 原版最大生命", 30, player.getMaxHealth());
                    expect("撤销饰品后 当前生命压到上限", 30, player.getHealth());
                });
                kill();
            }
            case 3 -> {
                return respawned(local);
            }
            case 4 -> {
                if (local < 40) {
                    return false;
                }
                onServer(player -> {
                    expect("重生后 最终生命上限", 30, PlayerAttributes.value(player, Stat.HEALTH_MAX));
                    expect("重生后 原版最大生命", 30, player.getMaxHealth());
                    expect("重生后 当前生命", 30, player.getHealth());
                    expect("重生后 原版攻击伤害基础值", 13, player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
                    // 存档前把原版基础值改成过时的值，重进后应由登录时的重新写入恢复。
                    player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);
                });
                checkClient("重生后", 30, 13);
                leaveWorld();
            }
            case 5 -> {
                return reopened(local);
            }
            case 6 -> {
                if (!inWorld(local)) {
                    return false;
                }
                onServer(player -> {
                    expect("重进后 最终生命上限", 30, PlayerAttributes.value(player, Stat.HEALTH_MAX));
                    expect("重进后 原版最大生命", 30, player.getMaxHealth());
                    expect("重进后 攻击基础值", 5, PlayerAttributes.baseValue(player, Stat.ATTACK));
                    expect("重进后 原版攻击伤害基础值", 13, player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
                });
                checkClient("重进后", 30, 13);
                finish();
            }
            default -> finish();
        }
        return true;
    }

    /** Q17：防具与武器的炼器数值计入原版最大生命与攻击伤害；Q18：受击方同属性功法加到伤害上。 */
    private void submitContributions(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        expect("初始 最终生命上限", 20, PlayerAttributes.value(player, Stat.HEALTH_MAX));
        expect("初始 原版最大生命", 20, player.getMaxHealth());
        expect("初始 原版攻击伤害基础值", 1, player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
        expect("初始 原版移速基础值", 0.1, player.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue());
        PlayerAttributes.setContribution(player, "smoke:growth", new Contribution(Layer.BASE, Map.of(Stat.ATTACK, 4.0, Stat.ROOT_FIRE, 3.0)));
        PlayerAttributes.setContribution(player, "smoke:equipment", new Contribution(Layer.BONUS, Map.of(Stat.HEALTH_MAX, 10.0, Stat.ATTACK, 8.0)));
        PlayerAttributes.setContribution(player, "smoke:ornament", new Contribution(Layer.BONUS, Map.of(Stat.HEALTH_MAX, 5.0)));
        PlayerAttributes.setContribution(player, "smoke:gongfa", new Contribution(Layer.BONUS, Map.of(Stat.GONGFA_FIRE, 5.0, Stat.GONGFA_LEVEL, 8.0)));
        expect("提交后 生命基础值", 20, PlayerAttributes.baseValue(player, Stat.HEALTH_MAX));
        expect("提交后 最终生命上限", 35, PlayerAttributes.value(player, Stat.HEALTH_MAX));
        expect("提交后 原版最大生命", 35, player.getMaxHealth());
        expect("提交后 攻击基础值", 5, PlayerAttributes.baseValue(player, Stat.ATTACK));
        expect("提交后 最终攻击", 13, PlayerAttributes.value(player, Stat.ATTACK));
        expect("提交后 原版攻击伤害基础值", 13, player.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
        expect("提交后 火五行系数", 0.3, PlayerAttributes.elementCoefficient(player, DamageElement.FIRE));
        expect("提交后 光借用火灵根", 0.3, PlayerAttributes.elementCoefficient(player, DamageElement.LIGHT));
        expect("提交后 受击方火功法项", 10, PlayerAttributes.receivedElementBonus(player, DamageElement.FIRE));
        expect("提交后 阴没有功法项", 0, PlayerAttributes.receivedElementBonus(player, DamageElement.YIN));
    }

    private void checkClient(String label, double health, double attack) {
        expect(label + " 客户端 最终生命上限", health, PlayerAttributes.value(minecraft.player, Stat.HEALTH_MAX));
        expect(label + " 客户端 原版最大生命", health, minecraft.player.getMaxHealth());
        expect(label + " 客户端 最终攻击", attack, PlayerAttributes.value(minecraft.player, Stat.ATTACK));
    }
}
