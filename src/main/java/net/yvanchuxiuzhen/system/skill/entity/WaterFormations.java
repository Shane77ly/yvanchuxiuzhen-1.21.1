package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 水系的空浮球、陨河术、水灵阵、玲珑阵（来源 {@code SkillWater04/06/07} 及对应的 {@code …shifang}、{@code …ShiTiChuShiShengChengShi}、
 * {@code …DangShiTiGengXinKeShi}、{@code SkillWater04PengZhuangXiangChiDu}）。实体把攻击量与倍率截成整数记下；
 * 空浮球与陨河术的碰撞箱按模式缩放，命中与到期分别发送水系爆发和破碎粒子。
 */
final class WaterFormations {
    private static final String WATER = "水";

    private WaterFormations() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_water_04", new FloatingOrb());
        skills.put("skill_water_06", new SpiritFormation());
        skills.put("skill_water_07", new HealingFormation());
        SkillReleases.register(WATER, (caster, book) -> orb(caster, book), "空浮球", "陨河术");
        SkillReleases.register(WATER, (caster, book) -> formation(caster, book, "skill_water_06", true), "水灵阵");
        SkillReleases.register(WATER, (caster, book) -> formation(caster, book, "skill_water_07", false), "玲珑阵");
    }

    private static SkillEntity newOrb(ServerPlayer caster, SkillBook book, String mode, float scale) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity orb = SkillEntities.summon(level, caster, "skill_water_04", WATER, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        orb.getPersistentData().putString("mode", mode);
        orb.setHitboxScale(scale);
        return orb;
    }

    /**
     * 空浮球：潜行、按住 Ctrl 或 Alt 时操作 16 格内自己放出的空浮球——潜行引爆、Ctrl 转向施放者朝向、Alt 转向面对施放者；
     * 没有这些按键时在视线 3 格处（向上找空位，最多 3 格）放出一个无重力的空浮球（碰撞箱放大 2 倍）。
     * 陨河术：潜行放一个贴地平推的（碰撞箱 1 倍，动画 skill2），否则在头顶 2 格处抛出一个坠落的（碰撞箱 3 倍，动画 skill1）。
     */
    private static void orb(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        if (book.name().equals("空浮球")) {
            boolean ctrl = ModifierKeys.ctrl(caster);
            boolean alt = ModifierKeys.alt(caster);
            if (caster.isShiftKeyDown() || ctrl || alt) {
                for (Entity other : ProjectileSupport.around(level, caster.position(), 16)) {
                    if (SkillEntities.is(other, "skill_water_04") && other instanceof SkillEntity orb && caster.getUUID().equals(orb.attacker())) {
                        if (caster.isShiftKeyDown()) {
                            orb.getPersistentData().putBoolean("baolie", true);
                        } else if (ctrl) {
                            SkillEntities.turn(orb, caster.getYRot(), 0);
                        } else {
                            orb.lookAt(EntityAnchorArgument.Anchor.EYES, caster.position());
                        }
                    }
                }
            } else {
                BlockPos target = ProjectileSupport.lookBlock(caster, 3);
                for (int index = 0; index < 3 && !level.isEmptyBlock(target); index++) {
                    target = target.above();
                }
                SkillEntity orb = newOrb(caster, book, "空浮球正常", 2);
                orb.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
                orb.setNoGravity(true);
                SkillEntities.turn(orb, caster.getYRot(), 0);
                SkillEntities.spawn(level, orb, false);
            }
        } else if (caster.isShiftKeyDown()) {
            SkillEntity orb = newOrb(caster, book, "陨河术潜行", 1);
            orb.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
            orb.setAnimation("animation.skillWater04.skill2");
            SkillEntities.turn(orb, caster.getYRot(), 0);
            SkillEntities.spawn(level, orb, false);
        } else {
            SkillEntity orb = newOrb(caster, book, "陨河术正常", 3);
            orb.moveTo(caster.getX(), caster.getY() + 2, caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
            orb.setDeltaMovement(new Vec3(caster.getLookAngle().x * 2, 0.5, caster.getLookAngle().z * 2));
            orb.setAnimation("animation.skillWater04.skill1");
            SkillEntities.turn(orb, caster.getYRot(), 0);
            SkillEntities.spawn(level, orb, false);
        }
    }

    /** 水灵阵、玲珑阵：水灵阵在视线 4 格处（向下贴地，最多 2 格）、玲珑阵在施放者处生成，初始生成时播放展开动画。 */
    private static void formation(ServerPlayer caster, SkillBook book, String id, boolean spirit) {
        ServerLevel level = (ServerLevel) caster.level();
        Vec3 at = caster.position();
        if (spirit) {
            BlockPos front = ProjectileSupport.lookBlock(caster, 4);
            for (int index = 0; index < 2 && level.isEmptyBlock(front.below()); index++) {
                front = front.below();
            }
            at = Vec3.atLowerCornerOf(front);
        }
        SkillEntity formation = SkillEntities.summon(level, caster, id, WATER, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        formation.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.spawn(level, formation, true);
    }

    /** 已有乘客就把对方向外抛开，否则让对方骑上来。 */
    private static void carry(SkillEntity self, Entity other, double horizontal, double vertical) {
        if (!self.isVehicle()) {
            other.startRiding(self);
        } else {
            other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * horizontal, vertical, (other.getZ() - self.getZ()) * horizontal));
        }
    }

    /** 对 3 格内的非友方实体造成伤害，并把 2 格内的非友方玩家或生物（无霸体）收为乘客。 */
    private static void sweep(SkillEntity self, double horizontal, double vertical) {
        ServerLevel level = (ServerLevel) self.level();
        Vec3 center = self.position().add(0, 1, 0);
        for (Entity other : ProjectileSupport.around(level, center, 3)) {
            if (!ProjectileSupport.friendly(self, other)) {
                ProjectileSupport.burst(other, "smallexplosionblue", other.getBbHeight() / 2);
                SkillEntities.strike(self, other);
            }
        }
        for (Entity other : ProjectileSupport.around(level, center, 2)) {
            if ((other instanceof Player || other instanceof Mob) && !ProjectileSupport.friendly(self, other) && SkillEntities.movable(other)) {
                carry(self, other, horizontal, vertical);
            }
        }
    }

    /**
     * 空浮球与陨河术。被引爆时对 5 格内的非友方实体造成水系伤害（无霸体者被推开）后消失。
     * 空浮球：沿朝向以 0.2 的速度水平飘行，120 刻消失；每 10 刻伤害并收乘客。陨河术正常：坠落，落地或入水后 5 刻对 6 格内的非友方实体造成伤害并推开，
     * 然后消失；陨河术潜行：沿朝向以 0.5 的速度水平前进，40 刻消失，每 5 刻伤害并收乘客。
     */
    private static final class FloatingOrb implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String mode = scratch.getString("mode");
            if (scratch.getBoolean("baolie")) {
                ProjectileSupport.particles(level, Particles.type("brokenblue"), self.position().add(0, self.getBbHeight() / 2, 0), 30, 0, 0, 0, 1);
                for (Entity other : ProjectileSupport.around(level, self.position(), 5)) {
                    if (!ProjectileSupport.friendly(self, other)) {
                        ProjectileSupport.burst(other, "smallexplosionblue", other.getBbHeight() / 2);
                        SkillEntities.strike(self, other);
                        if (SkillEntities.movable(other)) {
                            other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 0.4, 0.5, (other.getZ() - self.getZ()) * 0.4));
                        }
                    }
                }
                self.discard();
            } else if (mode.equals("空浮球正常")) {
                self.setDeltaMovement(new Vec3(self.getLookAngle().x * 0.2, 0, self.getLookAngle().z * 0.2));
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 120) {
                    ProjectileSupport.particles(level, Particles.type("brokenblue"), self.position().add(0, self.getBbHeight() / 2, 0), 20, 1, 1, 1, 0.2);
                    self.discard();
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 10) {
                    sweep(self, 0.4, 0.5);
                    scratch.putDouble("tick2", 0);
                }
            } else if (mode.equals("陨河术正常")) {
                self.setAnimation("animation.skillWater04.skill1");
                if (self.onGround() || self.isInWater() || self.isInLava()) {
                    scratch.putBoolean("kaishi", true);
                }
                if (scratch.getBoolean("kaishi")) {
                    scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                    if (scratch.getDouble("tick") >= 5) {
                        ProjectileSupport.particles(level, ParticleTypes.SPLASH, self.position().add(0, 0.6, 0), 40, 3, 3, 3, 1);
                        ProjectileSupport.particles(level, Particles.type("brokenblue"), self.position().add(0, self.getBbHeight() / 2, 0), 30, 0.5, 0.5, 0.5, 1);
                        for (Entity other : ProjectileSupport.around(level, self.position().add(0, 0.6, 0), 6)) {
                            if (!ProjectileSupport.friendly(self, other)) {
                                ProjectileSupport.burst(other, "smallexplosionblue", other.getBbHeight() / 2);
                                SkillEntities.strike(self, other);
                                other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 0.6, 0.5, (other.getZ() - self.getZ()) * 0.6));
                            }
                        }
                        scratch.putDouble("tick", 0);
                        self.discard();
                    }
                }
            } else if (mode.equals("陨河术潜行")) {
                self.setAnimation("animation.skillWater04.skill2");
                self.setDeltaMovement(new Vec3(self.getLookAngle().x * 0.5, 0, self.getLookAngle().z * 0.5));
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 40) {
                    scratch.putDouble("tick", 0);
                    ProjectileSupport.particles(level, Particles.type("brokenblue"), self.position().add(0, self.getBbHeight() / 2, 0), 20, 0, 0, 0, 1);
                    self.discard();
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 5) {
                    sweep(self, 0.3, 0.3);
                    scratch.putDouble("tick2", 0);
                }
            }
        }
    }

    /** 水灵阵：展开 40 刻后进入释放；释放 160 刻内每 7 刻在身边随机位置放出一枚由施放者发射的水灵弹，朝阵中偏下处飞；收尾 40 刻后消失。 */
    private static final class SpiritFormation implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            self.setAnimation("animation.skillWater06.start");
            self.getPersistentData().putString("shunxv", "进行");
        }

        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String stage = scratch.getString("shunxv");
            if (stage.equals("进行")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 40) {
                    scratch.putString("shunxv", "释放");
                    scratch.putDouble("tick", 0);
                }
            } else if (stage.equals("释放")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 160) {
                    scratch.putString("shunxv", "收尾");
                    self.setAnimation("animation.skillWater06.close");
                    scratch.putDouble("tick", 0);
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 7) {
                    Entity caster = level.getEntity(self.attacker());
                    double x = Mth.nextDouble(level.random, -3, 3);
                    double y = Mth.nextDouble(level.random, 1, 4);
                    double z = Mth.nextDouble(level.random, -3, 3);
                    ProjectileSupport.shoot(level, caster, "projectile_skill_water_05", self.position().add(x, y, z), new Vec3(-x, -(y - 2), -z), 0.1F, 0, 0);
                    scratch.putDouble("tick2", 0);
                }
            } else if (stage.equals("收尾")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 40) {
                    self.discard();
                }
            }
        }
    }

    /** 玲珑阵：展开 40 刻后持续 600 刻，每 20 刻给 3 格内的友方实体（含自己）回复相当于攻击量的生命；收起 20 刻后消失。 */
    private static final class HealingFormation implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            if (scratch.getString("zhuangtai").isEmpty()) {
                self.setAnimation("animation.skillWater07.start");
                scratch.putString("zhuangtai", "开始");
            }
        }

        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            String state = scratch.getString("zhuangtai");
            if (state.equals("开始")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 40) {
                    scratch.putString("zhuangtai", "进行");
                    scratch.putDouble("tick", 0);
                }
            } else if (state.equals("进行")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 600) {
                    scratch.putString("zhuangtai", "结束");
                    self.setAnimation("animation.skillWater07.close");
                    scratch.putDouble("tick", 0);
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 20) {
                    for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position(), 3)) {
                        if (ProjectileSupport.friendly(self, other) && other instanceof LivingEntity living) {
                            living.setHealth((float) (living.getHealth() + self.attackDamage()));
                        }
                    }
                    scratch.putDouble("tick2", 0);
                }
            } else if (state.equals("结束")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 20) {
                    self.discard();
                    scratch.putDouble("tick", 0);
                }
            }
        }
    }
}
