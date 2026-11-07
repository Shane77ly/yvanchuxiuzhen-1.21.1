package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 冰系的实体技能与施放（来源 {@code SkillIce04}、{@code SkillIce05} 及对应的 {@code …shifang}、{@code …ShiTiChuShiShengChengShi}、
 * {@code …DangShiTiGengXinKeShi}）：冰天雪地、裂冰斩。两种实体把攻击量与倍率截成整数记下（来源写进 {@code friendlist} 的 {@code putInt}）。
 * 冰天雪地每次命中逐目标随机选择天蓝色或白色爆发粒子。
 */
final class IceEntities {
    private static final String ICE = "冰";
    private static final String STARTING = "开始";
    private static final String RUNNING = "进行";
    private static final String ENDING = "结束";

    private IceEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_ice_04", new FrozenField());
        skills.put("skill_ice_05", new IceSpike());
        SkillReleases.register("水", (caster, book) -> frozenField(caster, book), "冰天雪地");
        SkillReleases.register("水", (caster, book) -> iceSpike(caster, book), "裂冰斩");
    }

    /** 冰天雪地：在视线 4 格处（向上找空位，最多 7 格）生成，朝向同施放者。 */
    private static void frozenField(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 4);
        for (int index = 0; index < 7 && !level.isEmptyBlock(target); index++) {
            target = target.above();
        }
        SkillEntity field = SkillEntities.summon(level, caster, "skill_ice_04", ICE, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        SkillEntities.place(level, field, Vec3.atLowerCornerOf(target), caster, 0);
        SkillEntities.spawn(level, field, true);
    }

    /** 裂冰斩：在视线 1 格处、施放者所在高度生成第一根冰刺，随机朝向，记下施放者朝向作为推进方向；攻击量同时是后续冰刺的剩余根数。 */
    private static void iceSpike(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 1);
        SkillEntity spike = SkillEntities.summon(level, caster, "skill_ice_05", ICE, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        spike.moveTo(target.getX(), caster.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(spike, Mth.nextInt(level.getRandom(), 1, 360), 0);
        spike.getPersistentData().putInt("jiaodu", (int) caster.getYRot());
        SkillEntities.spawn(level, spike, true);
    }

    /** 冰天雪地：初始生成时播放展开动画；展开 20 刻后持续 600 刻，每 20 刻对 3 格内的非友方实体造成冰系伤害；收起动画 20 刻后消失。 */
    private static final class FrozenField implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            if (scratch.getString("zhuangtai").isEmpty()) {
                self.setAnimation("animation.skillIce04.start");
                scratch.putString("zhuangtai", STARTING);
            }
        }

        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String state = scratch.getString("zhuangtai");
            if (state.equals(STARTING)) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 20) {
                    scratch.putString("zhuangtai", RUNNING);
                    scratch.putDouble("tick", 0);
                }
            } else if (state.equals(RUNNING)) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 600) {
                    scratch.putString("zhuangtai", ENDING);
                    self.setAnimation("animation.skillIce04.close");
                    scratch.putDouble("tick", 0);
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 20) {
                    for (Entity other : ProjectileSupport.around(level, self.position(), 3)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            ProjectileSupport.burst(other, Math.random() < 0.5 ? "smallexplosionskyblue" : "smallexplosionwhite", other.getBbHeight() / 2);
                            SkillEntities.strike(self, other);
                        }
                    }
                    scratch.putDouble("tick2", 0);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, self.getX(), self.getY(), self.getZ(), 10, 6, 6, 6, 0.5);
            } else if (state.equals(ENDING)) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 20) {
                    self.discard();
                    scratch.putDouble("tick", 0);
                }
            }
        }
    }

    /**
     * 冰刺：初始生成时播放出土动画；4 刻后进入进行，再 20 刻时对 1 格内的非友方实体造成一次冰系伤害（攻击量固定为 2、倍率取记下的整数）
     * 并冷冻 40 刻，破冰并消失。进行中 3 刻后若攻击量（剩余根数）大于 0，在沿推进方向（施放者朝向 + 90 度）1 格外生成下一根，剩余根数减 1。
     */
    private static final class IceSpike implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            if (scratch.getString("zhuangtai").isEmpty()) {
                self.setAnimation("animation.skillIce05.start2");
                scratch.putString("zhuangtai", STARTING);
            }
        }

        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String state = scratch.getString("zhuangtai");
            if (state.equals(STARTING)) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 4) {
                    scratch.putString("zhuangtai", RUNNING);
                    scratch.putDouble("tick", 0);
                }
            } else if (state.equals(RUNNING)) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 20) {
                    for (Entity other : ProjectileSupport.around(level, self.position(), 1)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            if (other instanceof LivingEntity living) {
                                SpellDamage.deal(level, living, DamageCategory.byLabel(ICE), self.attacker(), false, 2, self.multiplier());
                            }
                            other.setTicksFrozen(other.getTicksFrozen() + 40);
                        }
                    }
                    scratch.putString("zhuangtai", ENDING);
                    ProjectileSupport.breakEffect(level, self.position(), Blocks.ICE.defaultBlockState());
                    scratch.putDouble("tick", 0);
                    self.discard();
                }
            }
            if (!scratch.getBoolean("shifang")) {
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 3) {
                    if (self.attackDamage() > 0) {
                        double angle = Math.toRadians(scratch.getInt("jiaodu") + 90);
                        SkillEntity next = SkillEntities.chain(self, "skill_ice_05", (int) (self.attackDamage() - 1));
                        next.moveTo(self.getX() + Math.cos(angle), self.getY(), self.getZ() + Math.sin(angle), level.getRandom().nextFloat() * 360F, 0);
                        SkillEntities.turn(next, Mth.nextInt(level.getRandom(), 0, 360), 0);
                        next.getPersistentData().putInt("jiaodu", scratch.getInt("jiaodu"));
                        SkillEntities.spawn(level, next, true);
                    }
                    scratch.putBoolean("shifang", true);
                }
            }
        }
    }
}
