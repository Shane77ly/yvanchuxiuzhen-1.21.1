package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 雷系的实体技能与施放（来源 {@code SkillThunder08}、{@code SkillThunder10} 及对应的 {@code …shifang}、{@code …DangShiTiGengXinKeShi}）：
 * 掌心雷、地滚雷、惊雷阵。天雷术与五雷轰顶经 {@code /lightninglanding} 命令落雷，随彩色闪电实体（雷劫）一起迁移。
 * 自定义粒子（{@code SMALLEXPLOSIONPURPLE}、{@code SMALLEXPLOSIONYELLOW}）随 particle 迁移。
 */
final class ThunderEntities {
    private static final String THUNDER = "雷";

    private ThunderEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_thunder_08", new PalmThunder());
        skills.put("skill_thunder_10", new ThunderField());
        SkillReleases.register("火", (caster, book) -> palmThunder(caster, book), "掌心雷", "地滚雷");
        SkillReleases.register("火", (caster, book) -> thunderField(caster, book), "惊雷阵");
    }

    /** 掌心雷、地滚雷：在视线 3 格处（向上找空位，最多 3 格）生成；地滚雷无重力。生成时不转向，地滚雷的前进方向就是随机朝向。 */
    private static void palmThunder(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 3);
        for (int index = 0; index < 3 && !level.isEmptyBlock(target); index++) {
            target = target.above();
        }
        SkillEntity thunder = SkillEntities.summon(caster, book, "skill_thunder_08", THUNDER);
        thunder.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        if (book.name().equals("地滚雷")) {
            thunder.setNoGravity(true);
        }
        SkillEntities.spawn(level, thunder, false);
    }

    /** 惊雷阵：在视线 8 格处生成，位置先向上找空位、再向下贴地（最多 6 次），初始生成时播放展开动画。 */
    private static void thunderField(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 8);
        for (int index = 0; index < 6; index++) {
            if (!level.isEmptyBlock(target)) {
                target = target.above();
            } else if (level.isEmptyBlock(target.below())) {
                target = target.below();
            } else {
                break;
            }
        }
        SkillEntity field = SkillEntities.summon(caster, book, "skill_thunder_10", THUNDER);
        field.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.spawn(level, field, true);
    }

    /**
     * 掌心雷：第 4 刻对 1.5 格内的非友方实体造成一次雷系伤害（取整）并沉默 100 刻，第 8 刻后消失。
     * 地滚雷：沿朝向以 0.1 的速度滚动，200 刻后对 5 格内的非友方实体造成雷系伤害并施加重伤 300 刻后消失；
     * 滚动中 1.5 格内出现队伍名与自己不同的实体时提前爆发（来源比较 {@code getPersistentData} 里的队伍名，玩家的值由登录与每秒的恢复过程与其团队保持一致，施放者本人与同队实体不触发），伤害取整。
     */
    private static final class PalmThunder implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            Vec3 center = self.position().add(0, 0.5, 0);
            if (self.skillName().equals("地滚雷")) {
                double yaw = Math.toRadians(self.getYRot());
                double pitch = Math.toRadians(self.getXRot());
                self.setDeltaMovement(new Vec3(-0.1 * Math.sin(yaw), -0.1 * Math.sin(pitch), 0.1 * Math.cos(yaw)));
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 200) {
                    for (Entity other : ProjectileSupport.around(level, center, 5)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            SkillEntities.strike(self, other);
                            wound(other);
                        }
                    }
                    scratch.putDouble("tick", 0);
                    self.discard();
                }
                for (Entity other : ProjectileSupport.around(level, center, 1.5)) {
                    if (!Relations.team(other).equals(Relations.team(self))) {
                        scratch.putBoolean("kaishi", true);
                    }
                }
                if (scratch.getBoolean("kaishi")) {
                    for (Entity other : ProjectileSupport.around(level, center, 5)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            SkillEntities.strikeRounded(self, other);
                            wound(other);
                        }
                    }
                    self.discard();
                }
            } else {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") > 8) {
                    scratch.putDouble("tick", 0);
                    self.discard();
                } else if (scratch.getDouble("tick") >= 4 && !scratch.getBoolean("kaishi")) {
                    for (Entity other : ProjectileSupport.around(level, center, 1.5)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            SkillEntities.strikeRounded(self, other);
                            if (other instanceof LivingEntity living) {
                                living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_002"), 100, 0));
                            }
                        }
                        scratch.putBoolean("kaishi", true);
                    }
                }
            }
        }

        private static void wound(Entity other) {
            if (other instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 300, 0));
            }
        }
    }

    /** 惊雷阵：同冰天雪地——展开 20 刻后持续 600 刻，每 20 刻对 3 格内的非友方实体造成雷系伤害（取整）；收起 16 刻后消失。 */
    private static final class ThunderField implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            if (scratch.getString("zhuangtai").isEmpty()) {
                self.setAnimation("animation.skillThunder10.start");
                scratch.putString("zhuangtai", "开始");
            }
        }

        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            String state = scratch.getString("zhuangtai");
            if (state.equals("开始")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 20) {
                    scratch.putString("zhuangtai", "进行");
                    scratch.putDouble("tick", 0);
                }
            } else if (state.equals("进行")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 600) {
                    scratch.putString("zhuangtai", "结束");
                    self.setAnimation("animation.skillThunder10.close");
                    scratch.putDouble("tick", 0);
                }
                scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                if (scratch.getDouble("tick2") >= 20) {
                    for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position(), 3)) {
                        if (!ProjectileSupport.friendly(self, other)) {
                            SkillEntities.strikeRounded(self, other);
                        }
                    }
                    scratch.putDouble("tick2", 0);
                }
            } else if (state.equals("结束")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 16) {
                    self.discard();
                    scratch.putDouble("tick", 0);
                }
            }
        }
    }
}
