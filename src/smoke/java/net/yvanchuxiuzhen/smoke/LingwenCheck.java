package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;

/**
 * 灵纹附魔的实机检查：主手武器带灵纹附魔命中非友方目标时在攻击者处生成灵纹特效，再命中则增加数量（上限 6）；
 * 灵纹特效每 20 刻朝主人的视线射出灵纹弹并减一枚，每 100 刻自然减一枚，数量归零后消失；
 * 没有灵纹附魔、命中友方、起尸诀附魔（来源的 {@code fumolevel}）要求的灵力受击者没有（非玩家）都不生成。
 */
final class LingwenCheck extends ActionCheck {
    private static final double HEALTH = 100;
    private static final String RUNE = "lingwentexiao_2";

    private IronGolem target;

    LingwenCheck(List<String> report, String levelId) {
        super("灵纹", report, levelId);
        script(
                new Action(12, player -> {
                    Arena.level(player);
                    clearArea(player);
                }),
                new Action(2, player -> {
                    stage(player, 2, 0, false);
                    hit(player);
                    expect("命中后生成一个灵纹特效", 1, runes(player).size());
                    expect("灵纹数量", 1, count(player));
                    expectTrue("灵纹特效属于攻击者", runes(player).get(0).attacker().equals(player.getUUID()));
                }),
                new Action(28, player -> {
                    hit(player);
                    expect("再次命中不再生成新的", 1, runes(player).size());
                    expect("再次命中数量加一", 2, count(player));
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                }),
                new Action(2, player -> expect("20 刻检查射出灵纹弹后减一枚（主人已不持灵纹武器，弹体不再触发命中）", 1, count(player))),
                new Action(28, player -> {
                    stage(player, 2, 0, false);
                    hit(player);
                    target.setHealth((float) HEALTH);
                }),
                new Action(2, player -> {
                    expectTrue("灵纹弹命中目标", target.getHealth() < HEALTH);
                    int count = count(player);
                    expectTrue("射出一枚后按来源命中顺序保留一或两枚灵纹（" + count + ")", count == 1 || count == 2);
                }),
                new Action(2, player -> {
                    stage(player, 0, 0, false);
                    hit(player);
                    expect("没有灵纹附魔不生成", 0, runes(player).size());
                }),
                new Action(2, player -> {
                    stage(player, 2, 0, true);
                    hit(player);
                    expect("命中友方不生成", 0, runes(player).size());
                }),
                new Action(2, player -> {
                    stage(player, 1, 2, false);
                    hit(player);
                    expect("起尸诀附魔要求受击者有灵力，非玩家不生成", 0, runes(player).size());
                }),
                new Action(3, player -> {
                    stage(player, 2, 0, false);
                    hit(player);
                    runes(player).get(0).getPersistentData().putDouble("tick", 99);
                }),
                new Action(2, player -> expect("自然衰减到零后消失", 0, runes(player).size()))
        );
    }

    private static AABB area(ServerPlayer player) {
        return new AABB(player.blockPosition()).inflate(24);
    }

    private static List<SkillEntity> runes(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(SkillEntity.class, area(player), entity -> SkillEntities.is(entity, RUNE));
    }

    private static int count(ServerPlayer player) {
        return runes(player).get(0).getPersistentData().getInt("lingwennum");
    }

    private void clearArea(ServerPlayer player) {
        player.serverLevel().getEntitiesOfClass(SkillEntity.class, area(player)).forEach(SkillEntity::discard);
        if (target != null) {
            target.discard();
            target = null;
        }
    }

    /** 清场；玩家朝东，主手金剑带灵纹 {@code lingwen} 级与起尸诀 {@code qishi} 级附魔；东侧 2 格放一个目标（{@code friend} 为真时与玩家同队）。 */
    private void stage(ServerPlayer player, int lingwen, int qishi, boolean friend) {
        clearArea(player);
        player.setYRot(-90);
        player.setXRot(0);
        Registry<Enchantment> enchantments = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack sword = new ItemStack(Items.GOLDEN_SWORD);
        if (lingwen > 0) {
            sword.enchant(enchantments.getHolderOrThrow(Enchants.key("lingwen")), lingwen);
        }
        if (qishi > 0) {
            sword.enchant(enchantments.getHolderOrThrow(Enchants.key("qishi")), qishi);
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        target = EntityType.IRON_GOLEM.create(player.serverLevel());
        target.moveTo(player.getX() + 2, player.getY(), player.getZ(), 0, 0);
        target.setNoAi(true);
        target.setHealth((float) HEALTH);
        if (friend) {
            Relations.setTeam(target, Relations.team(player));
        }
        player.serverLevel().addFreshEntity(target);
    }

    private void hit(ServerPlayer player) {
        target.invulnerableTime = 0;
        target.hurt(player.damageSources().playerAttack(player), 1);
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        clearArea(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }
}
