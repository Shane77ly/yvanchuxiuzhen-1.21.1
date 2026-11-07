package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
import net.yvanchuxiuzhen.system.skill.data.SkillState;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 验证满天花雨的施放分派与五枚扇形飞叶，避免遗漏入口或退化为单发。 */
final class SkillRemainderCheck extends ActionCheck {
    private final List<SkillProjectile> leaves = new ArrayList<>();
    private SkillState original;
    private SkillCooldowns cooldowns;
    private ItemStack mainHand;
    private double power;

    SkillRemainderCheck(List<String> report, String levelId) {
        super("技能余项", report, levelId);
        script(
                new Action(10, p -> {
                    original = Skills.state(p);
                    cooldowns = p.getData(SkillData.COOLDOWNS);
                    mainHand = p.getMainHandItem().copy();
                    power = Cultivation.get(p, CultivationValue.POWER);
                    Arena.level(p);
                    p.connection.teleport(p.getX(), p.getY(), p.getZ(), -90, 0);
                    p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
                    p.setData(SkillData.STATE, SkillState.EMPTY);
                    p.setData(SkillData.COOLDOWNS, SkillCooldowns.EMPTY);
                    Cultivation.set(p, CultivationValue.POWER, 1000);
                    String arguments = "yvanchuxiuzhen:book_003 满天花雨 1 1 木 x 无 3 1 0 0 1 0 0 0 0 0 100 0 0 0 0 0";
                    ItemStack stack = Skills.createBook(SkillKind.SPELL, List.of(arguments.split(" ")));
                    SkillBook book = Skills.book(stack).orElseThrow();
                    stack.set(SkillData.SKILL.get(), book.withOwner(p.getUUID(), p.getName().getString()));
                    Skills.setSpell(p, 1, stack);
                    SkillSelection.select(p, 1);
                }),
                new Action(2, p -> {
                    SkillCasting.onRightClick(p);
                    leaves.addAll(p.serverLevel().getEntitiesOfClass(SkillProjectile.class, new AABB(p.blockPosition()).inflate(4)));
                    expect("满天花雨经真实施放入口放出五枚飞叶", 5, leaves.size());
                    expectTrue("扇形覆盖左右三十度且每枚基础伤害为二、射手为本人", leaves.size() == 5
                            && leaves.stream().allMatch(leaf -> leaf.getOwner() == p && leaf.getBaseDamage() == 2)
                            && leaves.stream().anyMatch(leaf -> leaf.getDeltaMovement().z < -.49)
                            && leaves.stream().anyMatch(leaf -> leaf.getDeltaMovement().z > .49));
                })
        );
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        leaves.forEach(SkillProjectile::discard);
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
        player.setData(SkillData.STATE, original);
        player.setData(SkillData.COOLDOWNS, cooldowns);
        Cultivation.set(player, CultivationValue.POWER, power);
    }
}
