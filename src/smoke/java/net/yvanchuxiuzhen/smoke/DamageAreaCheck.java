package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.entity.DamageArea;

/** 伤害范围的悬浮、免疫与计时存档边界；控雷阵落雷由已有落雷场景验证。 */
final class DamageAreaCheck extends ActionCheck {
    private DamageArea area;
    private Vec3 at;

    DamageAreaCheck(List<String> report, String levelId) {
        super("伤害范围", report, levelId);
        script(
                new Action(12, player -> {
                    at = player.position().add(0, 4, 5);
                    Skills.showDamageArea(player, at, "animation.shanghaifanwei.shiliuge3", "", 60, 0);
                    area = nearby(player).get(0);
                }),
                new Action(4, player -> {
                    expectTrue("范围提示保持位置、首个 aiStep 后关闭重力并继承施放者阵营", area.position().equals(at) && area.isNoGravity()
                            && Relations.team(area).equals(Relations.team(player)) && Relations.isFriendly(area, player));
                    float health = area.getHealth();
                    expectTrue("拒绝玩家与环境伤害，普通伤害仍可生效", !area.hurt(player.damageSources().playerAttack(player), 1)
                            && !area.hurt(player.damageSources().lightningBolt(), 1) && !area.hurt(player.damageSources().cactus(), 1)
                            && area.hurt(player.damageSources().generic(), 1) && area.getHealth() < health);
                    area.getPersistentData().putDouble("daojishi", 2);
                    DamageArea restored = create(player);
                    restored.load(area.saveWithoutId(new CompoundTag()));
                    restored.baseTick();
                    expectTrue("保存重读保留倒计时和阵营，最后一刻仍存续", !restored.isRemoved()
                            && restored.getPersistentData().getDouble("daojishi") == 1 && Relations.isFriendly(restored, player));
                    restored.baseTick();
                    expectTrue("倒计时归零移除", restored.isRemoved() && restored.getPersistentData().getDouble("daojishi") == 0);
                    DamageArea thunder = create(player);
                    thunder.getPersistentData().putString("jinengname", "惊蛰闻雷");
                    thunder.getPersistentData().putDouble("jishiqi", 38);
                    thunder.baseTick();
                    boolean aliveAt39 = !thunder.isRemoved();
                    thunder.baseTick();
                    expectTrue("惊蛰闻雷忽略普通倒计时，在第 40 刻移除并清计时", aliveAt39 && thunder.isRemoved()
                            && thunder.getPersistentData().getDouble("jishiqi") == 0);
                    DamageArea empty = create(player);
                    empty.baseTick();
                    expectTrue("未提供倒计时的普通提示首刻移除", empty.isRemoved());
                })
        );
    }

    private List<DamageArea> nearby(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(DamageArea.class, new AABB(at, at).inflate(2));
    }

    private static DamageArea create(ServerPlayer player) {
        return SkillSetup.<DamageArea>entityType(DamageArea.ID).get().create(player.serverLevel());
    }

    @Override
    protected void cleanup(ServerPlayer player) {
        area.discard();
    }
}
