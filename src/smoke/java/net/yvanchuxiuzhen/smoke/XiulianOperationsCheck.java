package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPoint;

/**
 * 修炼的其余公开操作：分配属性点（生命上限成长 + 4 并回复 4 点生命、灵力上限 + 40 并加当前灵力 40、超过层数 × 1.5 时不分配、
 * 没有属性点时不分配），管理命令的灵根重置与提升至最大，升仙令的体质键与漂浮。
 */
final class XiulianOperationsCheck extends SystemCheck {
    XiulianOperationsCheck(List<String> report, String levelId) {
        super("修炼操作", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(player -> {
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 2);
                Cultivation.set(player, CultivationValue.SKILL_POINTS, 2);
                Cultivation.set(player, CultivationValue.SKILL_POINTS_HEALTH_MAX, 0);
                Cultivation.setTizhiDouble(player, "ShouyvanAddSkillCorrect", 0);
                Cultivation.setTizhiDouble(player, "ShouyvanAddSkillCorrectPercentage", 0);
                double healthBase = PlayerAttributes.baseValue(player, Stat.HEALTH_MAX);
                player.setHealth(5);
                Cultivation.spendSkillPoint(player, SkillPoint.HEALTH_MAX);
                expect("加点 生命上限成长 +4", healthBase + 4, PlayerAttributes.baseValue(player, Stat.HEALTH_MAX));
                expect("加点 回复 4 点生命", 9, player.getHealth());
                expect("加点 属性点 −1", 1, Cultivation.get(player, CultivationValue.SKILL_POINTS));
                expect("加点 该项点数 +1", 1, Cultivation.get(player, CultivationValue.SKILL_POINTS_HEALTH_MAX));
                double power = Cultivation.get(player, CultivationValue.POWER);
                Cultivation.spendSkillPoint(player, SkillPoint.POWER_MAX);
                expect("加点 灵力 +40", power + 40, Cultivation.get(player, CultivationValue.POWER));
                Cultivation.spendSkillPoint(player, SkillPoint.ATTACK);
                expect("没有属性点时不分配", 0, Cultivation.get(player, CultivationValue.SKILL_POINTS_ATTACK));
                Cultivation.set(player, CultivationValue.SKILL_POINTS, 1);
                Cultivation.set(player, CultivationValue.SKILL_POINTS_ATTACK, 4);
                Cultivation.spendSkillPoint(player, SkillPoint.ATTACK);
                expect("超过层数 × 1.5 时不分配", 1, Cultivation.get(player, CultivationValue.SKILL_POINTS));

                Cultivation.maximizeRoots(player);
                expect("灵根提升至最大 金", 9, Cultivation.get(player, CultivationValue.ROOT_GOLD));
                expect("灵根提升至最大 阴阳", 0, Cultivation.get(player, CultivationValue.YINYANG));
                Cultivation.resetRoots(player);
                double gold = Cultivation.get(player, CultivationValue.ROOT_GOLD);
                double yinyang = gold + Cultivation.get(player, CultivationValue.ROOT_FIRE)
                        - Cultivation.get(player, CultivationValue.ROOT_TREE) - Cultivation.get(player, CultivationValue.ROOT_WATER);
                expectTrue("重置灵根在 0–9 " + gold, gold >= 0 && gold <= 9);
                expect("重置灵根 阴阳 = 金 + 火 − 木 − 水", yinyang, Cultivation.get(player, CultivationValue.YINYANG));

                Item token = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ling_09"));
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(token));
                token.use(player.level(), player, InteractionHand.MAIN_HAND);
                expectTrue("升仙令 记下 shengxianling", Cultivation.tizhiBoolean(player, "shengxianling"));
                expectTrue("升仙令 漂浮", player.hasEffect(MobEffects.LEVITATION));
                player.removeAllEffects();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                Cultivation.set(player, CultivationValue.LEVEL_NUMBER, 0);
            });
        }
        finish();
        return true;
    }
}
