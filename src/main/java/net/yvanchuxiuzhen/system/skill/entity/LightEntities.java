package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillDelays;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 光系的实体技能与施放（来源 {@code SkillLight03}、{@code SkillLight03shifang}）：闪光术、万丈光芒。
 * 万丈光芒立即发出一组光爆，再于 2、4、6、8 刻各补一组；回调保留原实体，执行时现读位置。
 */
final class LightEntities {
    private static final String LIGHT = "光";

    private LightEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_light_03", new Flash());
        SkillReleases.register("火", (caster, book) -> flash(caster, book), "闪光术", "万丈光芒");
    }

    /** 在施放者处生成，随机朝向。 */
    private static void flash(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity flash = SkillEntities.summon(caster, book, "skill_light_03", LIGHT);
        flash.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.spawn(level, flash, false);
    }

    /**
     * 第 3 刻爆发并消失。闪光术：5 格内的非友方实体失明 400 刻；万丈光芒：8 格内的非友方实体，生命低于最大生命 10% 的直接清零，
     * 其余失明 100 刻并受光系伤害（攻击量与倍率取整）。
     */
    private static final class Flash implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") < 3) {
                return;
            }
            ServerLevel level = (ServerLevel) self.level();
            if (self.skillName().equals("闪光术")) {
                for (Entity other : ProjectileSupport.around(level, self.position(), 5)) {
                    if (!ProjectileSupport.friendly(self, other) && other instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0));
                    }
                }
            } else if (self.skillName().equals("万丈光芒")) {
                for (Entity other : ProjectileSupport.around(level, self.position(), 8)) {
                    if (!ProjectileSupport.friendly(self, other)) {
                        ProjectileSupport.burst(other, "smallexplosionlightyellow", other.getBbHeight() / 2);
                        if (other instanceof LivingEntity living && living.getHealth() < living.getMaxHealth() * 0.1) {
                            living.setHealth(0);
                        } else {
                            if (other instanceof LivingEntity living) {
                                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                            }
                            SkillEntities.strikeRounded(self, other);
                        }
                    }
                }
                flashParticles(level, self);
                for (int delay : new int[]{2, 4, 6, 8}) {
                    SkillDelays.after(delay, () -> flashParticles(level, self));
                }
            }
            scratch.putDouble("tick", 0);
            self.discard();
        }

        private static void flashParticles(ServerLevel level, SkillEntity self) {
            ProjectileSupport.particles(level, Particles.type("bigbanglightyellow"), self.position().add(0, self.getBbHeight() / 2, 0), 2, 6, 6, 6, 0);
            ProjectileSupport.particles(level, Particles.type("smallexplosionlightyellow"), self.position().add(0, self.getBbHeight() / 2, 0), 4, 6, 6, 6, 0);
        }
    }
}
