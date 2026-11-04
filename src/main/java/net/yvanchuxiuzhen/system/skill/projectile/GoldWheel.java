package net.yvanchuxiuzhen.system.skill.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.entity.GoldEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;

/**
 * 金轮弹体（来源 {@code SkillGold03}）：捆妖轮、金弧轮、天堑金轮三个技能放出的无重力弹体，第一刻生成一个跟随自己的金轮实体
 * （{@code SkillGold0201}）。技能名不是金弧轮或天堑金轮时都按捆妖轮处理。
 * 金弧轮绕发射者视线两侧的圆弧飞行（潜行向左，否则向右），每刻转 10 度，20 刻消失，每刻对 2 格内的非友方实体造成金系伤害；
 * 天堑金轮直线飞行 40 刻，每刻对 2 格内的非友方、非投射物实体造成金系伤害；捆妖轮 20 刻消失，命中的敌方目标受 60 刻 20 级缓慢，
 * 并在目标处生成一个“捆妖轮锁住”金轮。伤害读第一刻记下的数值。
 */
final class GoldWheel implements ProjectileSkill {
    private static final String ENTITY = GoldEntities.ID;
    private static final String GOLD = "金";

    @Override
    public void tick(SkillProjectile projectile, Entity owner) {
        ServerLevel level = (ServerLevel) projectile.level();
        if (projectile.begin()) {
            projectile.snapshot(owner, 1, 1, 1);
            String name = projectile.skillName();
            String mode;
            String animation = null;
            if (owner instanceof Player && name.equals("金弧轮")) {
                projectile.setReversed(owner.isShiftKeyDown());
                projectile.setAngle(owner.getYRot() + (owner.isShiftKeyDown() ? 90 : -90));
                mode = owner.isShiftKeyDown() ? "金弧轮左旋" : "金弧轮右旋";
                animation = owner.isShiftKeyDown() ? "animation.skillGold02.skill09" : "animation.skillGold02.skill08";
            } else if (owner instanceof Player && name.equals("天堑金轮")) {
                mode = "天堑金轮";
                animation = "animation.skillGold02.skill10";
            } else {
                projectile.setSkillName("捆妖轮");
                mode = "捆妖轮";
            }
            SkillEntity wheel = SkillEntities.summon(level, owner, ENTITY, GOLD, 0, 0, mode);
            SkillEntities.place(level, wheel, projectile.position().add(0, -1, 0), owner, owner.getXRot());
            wheel.setNoGravity(true);
            if (animation != null) {
                wheel.setAnimation(animation);
            }
            GoldEntities.setMode(wheel, mode, projectile);
            SkillEntities.spawn(level, wheel, false);
        }
        switch (projectile.skillName()) {
            case "金弧轮" -> {
                projectile.advance();
                projectile.setAngle(projectile.angle() + (projectile.reversed() ? -10 : 10));
                double radians = Math.toRadians(projectile.angle());
                projectile.setDeltaMovement(new Vec3(-Math.sin(radians), -Math.sin(Math.toRadians(owner.getXRot())), Math.cos(radians)));
                if (projectile.ticks() >= 20) {
                    projectile.discard();
                }
                sweep(projectile, owner, false);
            }
            case "天堑金轮" -> {
                if (projectile.advance() >= 40) {
                    projectile.discard();
                }
                sweep(projectile, owner, true);
            }
            default -> {
                if (projectile.advance() >= 20) {
                    projectile.discard();
                }
            }
        }
    }

    /** 对 2 格内的非友方实体结算一次金系伤害；{@code skipProjectiles} 为真时跳过投射物。 */
    private static void sweep(SkillProjectile projectile, Entity owner, boolean skipProjectiles) {
        ServerLevel level = (ServerLevel) projectile.level();
        for (Entity other : ProjectileSupport.around(level, projectile.position(), 2)) {
            if (!ProjectileSupport.friendly(owner, other) && !(skipProjectiles && other instanceof ThrowableProjectile)) {
                ProjectileSupport.deal(level, other, owner, GOLD, projectile.mana(), projectile.multiplier());
            }
        }
    }

    @Override
    public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
        if (ProjectileSupport.friendly(owner, target)) {
            return;
        }
        ServerLevel level = (ServerLevel) projectile.level();
        ProjectileSupport.deal(level, target, owner, GOLD, projectile.mana(), projectile.multiplier());
        if (projectile.skillName().equals("捆妖轮")) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20, true, false));
            }
            SkillEntity lock = SkillEntities.summon(level, owner, ENTITY, GOLD, 0, 0, "捆妖轮锁住");
            lock.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
            lock.setAnimation("animation.skillGold02.skill05");
            GoldEntities.setMode(lock, "捆妖轮锁住", target);
            SkillEntities.spawn(level, lock, false);
        }
    }
}
