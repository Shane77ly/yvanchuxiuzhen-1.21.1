package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 土系的实体技能与施放（来源 {@code SkillRock02–05} 及 {@code SkillRock02shifang}、{@code SkillRock05shifang}、
 * {@code …DangShiTiGengXinKeShi}）：滚石术、天外陨石、化石手、巨石锤。
 */
final class RockEntities {
    private static final String EARTH = "土";
    private static final String TYPE_ONE = "类型一";
    private static final String TYPE_TWO = "类型二";

    private RockEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_rock_02", new RollingStone());
        skills.put("skill_rock_03", new Meteor());
        skills.put("skill_rock_04", new FossilHand());
        skills.put("skill_rock_05", new BoulderHammer());
        SkillReleases.register(EARTH, (caster, book) -> rollingStone(caster, book), "滚石术");
        SkillReleases.register(EARTH, (caster, book) -> meteor(caster, book), "天外陨石");
        SkillReleases.register(EARTH, (caster, book) -> hand(caster, book, "skill_rock_04", "animation.skillRock04.skill2"), "化石手");
        SkillReleases.register(EARTH, (caster, book) -> hand(caster, book, "skill_rock_05", "animation.skillRock05.skill1"), "巨石锤");
    }

    /** 滚石术：在视线 2 格处上方 16 格生成，朝视线方向缓慢滚落。 */
    private static void rollingStone(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 2);
        SkillEntity stone = SkillEntities.summon(caster, book, "skill_rock_02", EARTH);
        SkillEntities.place(level, stone, new Vec3(target.getX(), caster.getY() + 16, target.getZ()), caster, 0);
        stone.setDeltaMovement(new Vec3(caster.getLookAngle().x * 0.3, 0.1, caster.getLookAngle().z * 0.3));
        SkillEntities.spawn(level, stone, false);
    }

    /** 天外陨石：在视线 6 格处上方 16 格生成，自由下落（不转向）。 */
    private static void meteor(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 6);
        SkillEntity meteor = SkillEntities.summon(caster, book, "skill_rock_03", EARTH);
        meteor.moveTo(target.getX(), caster.getY() + 16, target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.spawn(level, meteor, false);
    }

    /** 化石手、巨石锤：在视线 3 格处、施放者所在高度生成；潜行时换成类型二并播放对应动画。 */
    private static void hand(ServerPlayer caster, SkillBook book, String id, String sneakAnimation) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 3);
        SkillEntity hand = SkillEntities.summon(caster, book, id, EARTH);
        hand.moveTo(target.getX(), caster.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        if (caster.isShiftKeyDown()) {
            hand.setAnimation(sneakAnimation);
            hand.getPersistentData().putString("dangqianleixing", TYPE_TWO);
        } else {
            hand.getPersistentData().putString("dangqianleixing", TYPE_ONE);
        }
        SkillEntities.spawn(level, hand, false);
    }

    private static void smoke(SkillEntity self, Vec3 at, int count, double spread, double speed) {
        ((ServerLevel) self.level()).sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** 对中心附近的非友方实体各结算一次土系伤害（不取整），可选地再按几率施加重伤（{@code effect_007}，1200 刻 1 级）。 */
    private static void crush(SkillEntity self, Vec3 center, double inflate, double woundChance) {
        for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), center, inflate)) {
            if (!ProjectileSupport.friendly(self, other)) {
                if (woundChance > 0) {
                    ProjectileSupport.burst(other, "smallexplosiondeeporange", other.getBbHeight() / 2);
                }
                if (woundChance > 0 && Math.random() < woundChance && other instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 1200, 1));
                }
                SkillEntities.strike(self, other);
            }
        }
    }

    /** 滚石术：每刻按朝向缓慢前进并下沉，200 刻后消失；每 12 刻对 2 格内的非友方实体造成土系伤害并施加重伤。 */
    private static final class RollingStone implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            double yaw = Math.toRadians(self.getYRot());
            self.setDeltaMovement(new Vec3(-0.2 * Math.sin(yaw), -0.2, 0.2 * Math.cos(yaw)));
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= 200) {
                scratch.putDouble("tick", 0);
                self.discard();
            }
            scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
            if (scratch.getDouble("tick2") >= 12) {
                for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position(), 2)) {
                    if (!ProjectileSupport.friendly(self, other)) {
                        ProjectileSupport.burst(other, "smallexplosiondeeporange", other.getBbHeight() / 2);
                        SkillEntities.strike(self, other);
                        if (other instanceof LivingEntity living) {
                            living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 1200, 1));
                        }
                    }
                }
                scratch.putDouble("tick2", 0);
            }
        }
    }

    /** 天外陨石：下落时拖火焰，落地或入水后 5 刻爆炸，对 3 格内的非友方实体造成土系伤害，然后消失。 */
    private static final class Meteor implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 4, 0.5, 0.5, 0.5, 0.05);
            CompoundTag scratch = self.getPersistentData();
            if (self.onGround() || self.isInLava() || self.isInWater()) {
                scratch.putBoolean("kaishi", true);
            }
            if (scratch.getBoolean("kaishi")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 5) {
                    level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 20, 0, 0, 0, 0.5);
                    smoke(self, self.position(), 20, 0, 0.5);
                    crush(self, self.position(), 3, 0);
                    scratch.putDouble("tick", 0);
                    self.discard();
                }
            }
        }
    }

    /**
     * 化石手：类型一在第 21 刻攻击视线 3.5 格处（以实体自己的朝向取，朝向在生成时是随机的）2.5 格内的非友方实体，30% 重伤，第 39 刻消失；
     * 类型二在第 2 刻攻击视线 1 格处 2 格内的非友方实体，30% 重伤，把无霸体者抛向外侧，第 19 刻消失。
     */
    private static final class FossilHand implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            String type = scratch.getString("dangqianleixing");
            String progress = scratch.getString("jindu");
            if (type.equals(TYPE_ONE)) {
                if (progress.isEmpty() && scratch.getDouble("tick") >= 21) {
                    BlockPos block = ProjectileSupport.lookBlock(self, 3.5);
                    smoke(self, new Vec3(block.getX(), self.getY(), block.getZ()), 20, 2, 0.2);
                    crush(self, new Vec3(block.getX(), self.getY(), block.getZ()), 2.5, 0.3);
                    scratch.putString("jindu", "攻击");
                } else if (progress.equals("攻击") && scratch.getDouble("tick") >= 39) {
                    scratch.putString("jindu", "清除");
                    scratch.putDouble("tick", 0);
                    self.discard();
                }
            } else if (type.equals(TYPE_TWO)) {
                if (progress.isEmpty() && scratch.getDouble("tick") >= 2) {
                    BlockPos block = ProjectileSupport.lookBlock(self, 1);
                    Vec3 center = Vec3.atLowerCornerOf(block);
                    smoke(self, center, 20, 0.5, 0.4);
                    for (Entity other : ProjectileSupport.around(level, center, 2)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            ProjectileSupport.burst(other, "smallexplosiondeeporange", other.getBbHeight() / 2);
                            if (Math.random() < 0.3 && other instanceof LivingEntity living) {
                                living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 1200, 1));
                            }
                            SkillEntities.strike(self, other);
                            if (!(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                                other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 1.2, 0.7, (other.getZ() - self.getZ()) * 1.2));
                            }
                        }
                    }
                    scratch.putString("jindu", "收尾");
                } else if (progress.equals("收尾") && scratch.getDouble("tick") >= 19) {
                    scratch.putString("jindu", "清除");
                    scratch.putDouble("tick", 0);
                    self.discard();
                }
            }
        }
    }

    /**
     * 巨石锤：类型一在第 11、33 刻对 2 格内的非友方实体各造成一次土系伤害，第 46 刻消失；类型二落地后 5 刻（或生成 20 刻）造成一次伤害并消失。
     * 来源的 20% 眩晕（{@code effect_001}）加给了实体自己而不是受击者（Q42），保持。
     */
    private static final class BoulderHammer implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            String type = scratch.getString("dangqianleixing");
            String state = scratch.getString("zhuangtai");
            if (type.equals(TYPE_ONE)) {
                if (state.isEmpty() && scratch.getDouble("tick") >= 11) {
                    smoke(self, self.position(), 20, 0.5, 0.5);
                    hammer(self, true);
                    scratch.putString("zhuangtai", "攻击");
                } else if (state.equals("攻击") && scratch.getDouble("tick") >= 33) {
                    smoke(self, self.position(), 20, 0.5, 0.5);
                    hammer(self, true);
                    scratch.putString("zhuangtai", "收尾");
                } else if (state.equals("收尾") && scratch.getDouble("tick") >= 46) {
                    self.discard();
                }
            } else if (type.equals(TYPE_TWO)) {
                if (self.onGround() || self.isInLava() || self.isInWater()) {
                    scratch.putBoolean("kaishi", true);
                }
                if (scratch.getBoolean("kaishi")) {
                    scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                    if (scratch.getDouble("tick2") >= 5) {
                        smoke(self, self.position(), 20, 0.5, 0.5);
                        hammer(self, true);
                        scratch.putDouble("tick2", 0);
                        self.discard();
                    }
                }
                if (scratch.getDouble("tick") >= 20) {
                    smoke(self, self.position(), 20, 0.5, 0.5);
                    hammer(self, false);
                    self.discard();
                }
            }
        }

        /** 对 2 格内的非友方实体造成一次土系伤害；{@code stun} 为真时有 20% 几率给实体自己加 600 刻眩晕。 */
        private static void hammer(SkillEntity self, boolean stun) {
            for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position(), 2)) {
                if (!ProjectileSupport.friendly(self, other)) {
                    ProjectileSupport.burst(other, "smallexplosiondeeporange", other.getBbHeight() / 2);
                    SkillEntities.strike(self, other);
                    if (stun && Math.random() < 0.2) {
                        self.addEffect(new MobEffectInstance(StatusEffects.holder("effect_001"), 600, 0));
                    }
                }
            }
        }
    }
}
