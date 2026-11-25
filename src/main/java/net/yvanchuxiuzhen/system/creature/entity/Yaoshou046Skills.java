package net.yvanchuxiuzhen.system.creature.entity;

import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 飞箭豪猪的飞箭和爆裂飞箭阶段；调用者保有目标选择、技能选择和逐刻计时。 */
public final class Yaoshou046Skills {
    private Yaoshou046Skills() { }

    /** 飞箭：蓄力8刻、攻击值一半、击退1、穿透5、22刻清理当前技能。 */
    public static void flyingArrow(Entity owner, Entity target, Consumer<String> animation) {
        CreatureState state = CreatureData.state(owner);
        switch (state.phase()) {
            case "" -> {
                lookAt(owner, target);
                state.setPhase("蓄力");
                if (!owner.level().isClientSide) {
                    animation.accept("animation.yaoshou046.skill02");
                }
            }
            case "蓄力" -> {
                if (state.tick() >= 8) {
                    lookAt(owner, target);
                    if (owner.level() instanceof ServerLevel level) {
                        Yaoshou046skill01Entity arrow = arrow(level, owner, (float) (EntityAttributes.stat(owner, Stat.ATTACK) / 2));
                        Vec3 look = owner.getLookAngle();
                        arrow.shoot(look.x, look.y + 0.1, look.z, 1, 10);
                        level.addFreshEntity(arrow);
                    }
                    state.setPhase("结束");
                }
            }
            case "结束" -> {
                if (state.tick() >= 22) {
                    finish(state);
                }
            }
        }
    }

    /** 爆裂飞箭：20刻朝随机上半球发24支三倍攻击骨刺，30刻清理技能。 */
    public static void burstingArrows(Entity owner, Entity target, Consumer<String> animation) {
        CreatureState state = CreatureData.state(owner);
        switch (state.phase()) {
            case "" -> {
                lookAt(owner, target);
                state.setPhase("蓄力");
                if (!owner.level().isClientSide) {
                    animation.accept("animation.yaoshou046.skill03");
                }
            }
            case "蓄力" -> {
                if (state.tick() >= 20) {
                    if (owner.level() instanceof ServerLevel level) {
                        for (int index = 0; index < 24; index++) {
                            Yaoshou046skill01Entity arrow = arrow(level, owner, (float) (EntityAttributes.stat(owner, Stat.ATTACK) * 3));
                            arrow.shoot(level.random.nextDouble() * 2 - 1, 0.1 + level.random.nextDouble() * 0.9,
                                    level.random.nextDouble() * 2 - 1, (float) (0.5 + level.random.nextDouble() * 0.5), 0);
                            level.addFreshEntity(arrow);
                        }
                    }
                    state.setPhase("结束");
                }
            }
            case "结束" -> {
                if (state.tick() >= 30) {
                    finish(state);
                }
            }
        }
    }

    private static Yaoshou046skill01Entity arrow(ServerLevel level, Entity owner, float damage) {
        Yaoshou046skill01Entity arrow = CreatureSetup.<Yaoshou046skill01Entity>entityType("projectile_yaoshou_046skill_01").get().create(level);
        arrow.setOwner(owner);
        arrow.configure(damage, 1, (byte) 5);
        arrow.setPos(owner.getX(), owner.getY() + owner.getBbHeight() / 2, owner.getZ());
        return arrow;
    }

    private static void lookAt(Entity owner, Entity target) {
        owner.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }

    private static void finish(CreatureState state) {
        state.setSkill("");
        state.setPhase("");
        state.setTick(0);
        state.setTick2(0);
    }
}
