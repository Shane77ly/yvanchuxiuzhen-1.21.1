package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 风系的实体技能与施放（来源 {@code Skillwind02}、{@code Skillwind02shifang}、{@code Skillwind05}、{@code Skillwind06}）：
 * 聚风术、聚风化气、风飘术、造云术。{@code Skillwind05}（按 {@code endtick} 到时消失）与 {@code Skillwind06}（追踪 {@code mubiaoUUID} 的风刃）
 * 由妖兽技能通过公开操作放出；本系统保存攻击量、目标和存在刻数，并负责实体行为。
 */
public final class WindEntities {
    private static final String WIND = "风";

    private WindEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skillwind_02", new Gale());
        skills.put("skillwind_05", new Timed());
        skills.put("skillwind_06", new Blade());
        SkillReleases.register("木", (caster, book) -> gather(caster, book, false), "聚风术");
        SkillReleases.register("木", (caster, book) -> gather(caster, book, true), "聚风化气");
        SkillReleases.register("木", (caster, book) -> drift(caster, book), "风飘术");
        SkillReleases.register("木", (caster, book) -> cloud(caster), "造云术");
    }

    /** 妖兽爆气的风波：在施放者脚下随机朝向生成，继承敌友关系，播放波动动画并存在八刻。 */
    public static void showBurst(Entity caster) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity wave = SkillSetup.<SkillEntity>entityType("skillwind_05").get().create(level);
        wave.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        Relations.copyAllegiance(caster, wave);
        wave.setAnimation("animation.skillwind05.bodong04");
        wave.getPersistentData().putDouble("endtick", 8);
        SkillEntities.spawn(level, wave, true);
    }

    /** 妖兽黑风的追踪风刃：攻击量由调用者计算，倍率为一，初始朝向目标脚下，最长累计追踪二百刻。 */
    public static void launchBlade(Entity caster, Entity target, double attackDamage) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity blade = SkillEntities.summon(level, caster, "skillwind_06", WIND, attackDamage, 1, "");
        blade.moveTo(caster.getX(), caster.getY() + 1, caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        blade.getPersistentData().putString("mubiaoUUID", target.getStringUUID());
        blade.getPersistentData().putDouble("endtick", 200);
        blade.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        SkillEntities.spawn(level, blade, true);
    }

    /** 聚风术、聚风化气：在视线 6 格处（向上找空位，最多 6 格）生成风团；聚风化气另记下施放者的朝向与俯仰角，风团沿它飞行。 */
    private static void gather(ServerPlayer caster, SkillBook book, boolean moving) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 6);
        for (int index = 0; index < 6 && !level.isEmptyBlock(target); index++) {
            target = target.above();
        }
        SkillEntity gale = SkillEntities.summon(caster, book, "skillwind_02", WIND);
        gale.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(gale, caster.getYRot(), moving ? caster.getXRot() : 0);
        if (moving) {
            gale.getPersistentData().putDouble("fangxiang", caster.getYRot());
            gale.getPersistentData().putDouble("jiaodu", caster.getXRot());
        }
        SkillEntities.spawn(level, gale, false);
    }

    /** 风飘术：自身获得时长 1200 刻、等级为攻击量的速度与风飘状态。 */
    private static void drift(ServerPlayer caster, SkillBook book) {
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, (int) book.manaAttack()));
        caster.addEffect(new MobEffectInstance(StatusEffects.holder("skillwind_04"), 1200, (int) book.manaAttack()));
        ((ServerLevel) caster.level()).sendParticles(ParticleTypes.CLOUD, caster.getX(), caster.getY(), caster.getZ(), 10, 0, 0, 0, 0.1);
    }

    /** 造云术：身边画十层云环，随后隐身 40 刻。 */
    private static void cloud(ServerPlayer caster) {
        ServerLevel level = (ServerLevel) caster.level();
        double y = caster.getY();
        double angle = 0;
        for (int layer = 0; layer < 10; layer++) {
            for (int index = 0; index < 20; index++) {
                angle += 18;
                level.sendParticles(ParticleTypes.CLOUD, caster.getX() + 2 * Math.cos(angle), y, caster.getZ() + 2 * Math.sin(angle), 1, 0, 0, 0, 0);
            }
            y += 0.2;
        }
        caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0));
    }

    /**
     * 风团。聚风化气：沿记下的朝向与俯仰角以 0.1 的速度飞行，把 4 格内的非友方实体（无霸体）拉向自己；200 刻后爆发，对 4 格内的非友方实体造成风系伤害并抛起，
     * 再过 14 刻消失。聚风术：原地把 4 格内的全部实体（不分敌友，无霸体）拉向自己，200 刻后消失。
     */
    private static final class Gale implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            if (self.skillName().equals("聚风化气")) {
                double yaw = Math.toRadians(scratch.getDouble("fangxiang"));
                double pitch = Math.toRadians(scratch.getDouble("jiaodu"));
                self.setDeltaMovement(new Vec3(-0.1 * Math.sin(yaw), -0.1 * Math.sin(pitch), 0.1 * Math.cos(yaw)));
                level.sendParticles(ParticleTypes.CLOUD, self.getX(), self.getY(), self.getZ(), 4, 0, 0, 0, 0.2);
                for (Entity other : ProjectileSupport.around(level, self.position(), 4)) {
                    if (!ProjectileSupport.friendly(self, other) && SkillEntities.movable(other)) {
                        other.setDeltaMovement(self.position().subtract(other.position()).scale(0.3));
                    }
                }
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (!scratch.getBoolean("kaishi") && scratch.getDouble("tick") >= 200) {
                    self.setAnimation("animation.skillwind02.skill02");
                    for (Entity other : ProjectileSupport.around(level, self.position(), 4)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            SkillEntities.strike(self, other);
                            if (SkillEntities.movable(other)) {
                                other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 0.6, 1, (other.getZ() - self.getZ()) * 0.6));
                            }
                        }
                    }
                    scratch.putDouble("tick", 0);
                    scratch.putBoolean("kaishi", true);
                } else if (scratch.getBoolean("kaishi") && scratch.getDouble("tick") >= 14) {
                    level.sendParticles(ParticleTypes.CLOUD, self.getX(), self.getY(), self.getZ(), 30, 0, 0, 0, 1);
                    self.discard();
                }
            } else {
                level.sendParticles(ParticleTypes.CLOUD, self.getX(), self.getY(), self.getZ(), 4, 0, 0, 0, 0.2);
                for (Entity other : ProjectileSupport.around(level, self.position(), 4)) {
                    if (SkillEntities.movable(other)) {
                        other.setDeltaMovement(self.position().subtract(other.position()).scale(0.4));
                    }
                }
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 200) {
                    scratch.putDouble("tick", 0);
                    level.sendParticles(ParticleTypes.CLOUD, self.getX(), self.getY(), self.getZ(), 20, 2, 2, 2, 0.1);
                    self.discard();
                }
            }
        }
    }

    /** 到时消失的风实体：存在刻数由放出者写入的 {@code endtick} 决定。 */
    private static final class Timed implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= scratch.getDouble("endtick")) {
                self.discard();
                scratch.putDouble("tick", 0);
            }
        }
    }

    /**
     * 风刃：追踪放出者写入的目标（{@code mubiaoUUID}），找不到目标时什么也不做。首刻进入追踪，之后每刻转向目标、以 0.8 的速度前进；
     * 与目标的间距（扣去两者宽度的一半）不超过 1 时转入自旋并播放动画，14 刻后散出烟雾消失；追踪途中每 10 刻对身边（宽度加 2 的一半）的非友方实体造成风系伤害。
     */
    private static final class Blade implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String targetId = scratch.getString("mubiaoUUID");
            if (targetId.isEmpty()) {
                return;
            }
            Entity target = level.getEntity(UUID.fromString(targetId));
            if (target == null) {
                return;
            }
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= scratch.getDouble("endtick")) {
                self.discard();
                scratch.putDouble("tick", 0);
            }
            String stage = scratch.getString("jinengjieduan");
            if (stage.isEmpty()) {
                scratch.putString("jinengjieduan", "追踪");
            } else if (stage.equals("追踪")) {
                self.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
                double yaw = Math.toRadians(self.getYRot());
                double pitch = Math.toRadians(self.getXRot());
                self.setDeltaMovement(new Vec3(-0.8 * Math.sin(yaw), -0.8 * Math.sin(pitch), 0.8 * Math.cos(yaw)));
                double gap = self.position().distanceTo(target.position()) - (self.getBbWidth() / 2 + target.getBbWidth() / 2);
                if (gap <= 1) {
                    self.setAnimation("animation.skillwind06.skill03");
                    scratch.putDouble("tick02", 0);
                    scratch.putString("jinengjieduan", "自旋");
                } else {
                    scratch.putDouble("tick02", scratch.getDouble("tick02") + 1);
                    if (scratch.getDouble("tick02") >= 10) {
                        for (Entity other : ProjectileSupport.around(level, self.position(), (self.getBbWidth() + 2) / 2)) {
                            if (!ProjectileSupport.friendly(self, other)) {
                                ProjectileSupport.burst(other, "smallexplosionblack", other.getBbHeight() / 2);
                                SkillEntities.strike(self, other);
                            }
                        }
                        scratch.putDouble("tick02", 0);
                    }
                }
            } else if (stage.equals("自旋")) {
                scratch.putDouble("tick02", scratch.getDouble("tick02") + 1);
                if (scratch.getDouble("tick02") >= 14) {
                    scratch.putDouble("tick02", 0);
                    level.sendParticles(ParticleTypes.LARGE_SMOKE, self.getX(), self.getY(), self.getZ(), 12, 0, 0, 0, 0.3);
                    ProjectileSupport.particles(level, Particles.type("smokeblack"), self.position(), 12, 0, 0, 0, 0.3);
                    self.discard();
                }
            }
        }
    }
}
