package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 火系的实体技能与施放（来源 {@code SkillFire02/03/04/05/07/08} 与对应的 {@code …shifang}、{@code …DangShiTiGengXinKeShi}）：
 * 大火球、引火、控火术、火焰护盾、火焰漩涡、火龙卷；火鸟术（{@code SkillFire06}）是带 AI 的可驯服实体，见 {@link FireBird}。
 * 来源的自定义粒子（{@code SMALLEXPLOSIONRED}、{@code BROKEN*}）随 particle 迁移。
 */
final class FireEntities {
    private static final String FIRE = "火";

    private FireEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_fire_02", new GreatFireball());
        skills.put("skill_fire_07", new Vortex());
        skills.put("skill_fire_08", new Tornado());
        SkillReleases.register(FIRE, (caster, book) -> greatFireball(caster, book), "大火球");
        SkillReleases.register(FIRE, (caster, book) -> igniteFront(caster), "引火");
        SkillReleases.register(FIRE, (caster, book) -> controlFire(caster, book), "控火术");
        SkillReleases.register(FIRE, (caster, book) -> shield(caster, book), "火焰护盾");
        FireBird.register();
        SkillReleases.register(FIRE, (caster, book) -> summonColumn(caster, book, "skill_fire_07", 6), "火焰漩涡");
        SkillReleases.register(FIRE, (caster, book) -> summonColumn(caster, book, "skill_fire_08", 5), "火龙卷");
    }

    /** 大火球：在施放者上方 1.5 格生成，朝视线水平方向抛出（竖直速度 0.5）。 */
    private static void greatFireball(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity fireball = SkillEntities.summon(caster, book, "skill_fire_02", FIRE);
        SkillEntities.place(level, fireball, caster.position().add(0, 1.5, 0), caster, 0);
        fireball.setDeltaMovement(new Vec3(caster.getLookAngle().x, 0.5, caster.getLookAngle().z));
        SkillEntities.spawn(level, fireball, true);
    }

    /** 引火：点燃视线 4 格处（方块坐标）周围 2 格内的非友方实体 20 秒。 */
    private static void igniteFront(ServerPlayer caster) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos center = ProjectileSupport.lookBlock(caster, 4);
        for (Entity other : ProjectileSupport.around(level, Vec3.atLowerCornerOf(center), 2)) {
            if (!ProjectileSupport.friendly(caster, other)) {
                ProjectileSupport.ignite(other, 20);
            }
        }
    }

    /**
     * 控火术：以视线 4 格处为中心，扫描 9 × 9 × 9 范围内的火焰与灵魂火，每处火焰对周围 2.5 格内的非友方实体造成一次火系伤害并点燃 5 秒，
     * 然后熄灭。攻击者取施放者自己 {@code getPersistentData} 里的 {@code AttackUUID}，玩家没有这个键，命令行里这一项为空，后面的参数前移，
     * 实际按“无攻击者、基础量 = 伤害倍率、倍率 0”结算（Q41）。施放者不是玩家时攻击量为 {@code LEVELnumber ÷ 2 + 1}、倍率 1.2。
     */
    private static void controlFire(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        double[] values = {book.manaAttack(), book.damageMultiplier()};
        BlockPos center = ProjectileSupport.lookBlock(caster, 4);
        String attackUuid = caster.getPersistentData().getString("AttackUUID");
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    boolean fire = level.getBlockState(pos).is(Blocks.FIRE);
                    if (!fire && !level.getBlockState(pos).is(Blocks.SOUL_FIRE)) {
                        continue;
                    }
                    level.sendParticles(fire ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME, pos.getX(), pos.getY(), pos.getZ(), 16, 0, 0, 0, 0.5);
                    for (Entity other : ProjectileSupport.around(level, Vec3.atLowerCornerOf(pos), 2.5)) {
                        if (!ProjectileSupport.friendly(caster, other)) {
                            if (other instanceof LivingEntity living) {
                                SpellDamage.command(level, living, "shanghai " + FIRE + " " + attackUuid + " false " + values[0] + " " + values[1]);
                            }
                            ProjectileSupport.ignite(other, 5);
                        }
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    /** 火焰护盾：自身获得时长 1200 刻、等级为攻击量的抗火，并在身边画一圈火焰粒子（来源按弧度 18 的倍数取点）。 */
    private static void shield(ServerPlayer caster, SkillBook book) {
        caster.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, (int) book.manaAttack()));
        ServerLevel level = (ServerLevel) caster.level();
        double angle = 0;
        for (int index = 0; index < 20; index++) {
            angle += 18;
            level.sendParticles(ParticleTypes.FLAME, caster.getX() + 1.5 * Math.cos(angle), caster.getY() + 1, caster.getZ() + 1.5 * Math.sin(angle), 1, 0, 0, 0, 0);
        }
    }

    /** 火焰漩涡、火龙卷：从视线 {@code reach} 格处向上找一个空位（最多 {@code reach} 格），在那里生成。 */
    private static void summonColumn(ServerPlayer caster, SkillBook book, String id, int reach) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, reach);
        for (int index = 0; index < reach && !level.isEmptyBlock(target); index++) {
            target = target.above();
        }
        SkillEntity column = SkillEntities.summon(caster, book, id, FIRE);
        SkillEntities.place(level, column, Vec3.atLowerCornerOf(target), caster, 0);
        SkillEntities.spawn(level, column, false);
    }

    /** 对中心 {@code inflate} 格内的非友方实体（友方以实体自身继承的团队判定）逐个结算火系伤害（攻击量与倍率取整）并点燃 5 秒。 */
    private static void burn(SkillEntity self, Vec3 center, double inflate, boolean explosionParticles) {
        ServerLevel level = (ServerLevel) self.level();
        for (Entity other : ProjectileSupport.around(level, center, inflate)) {
            if (!ProjectileSupport.friendly(self, other)) {
                if (explosionParticles) {
                    level.sendParticles(ParticleTypes.FLAME, other.getX(), other.getY() + other.getBbHeight() / 2, other.getZ(), 10, 0, 0, 0, 1);
                }
                SkillEntities.strikeRounded(self, other);
                ProjectileSupport.ignite(other, 5);
            }
        }
    }

    /** 大火球：飞行时拖火焰，落地或入水后 5 刻爆炸，对 3 格内的非友方实体造成火系伤害并点燃，然后消失。 */
    private static final class GreatFireball implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 4, 0, 0, 0, 0.05);
            CompoundTag scratch = self.getPersistentData();
            if (self.onGround() || self.isInLava() || self.isInWater()) {
                scratch.putBoolean("kaishi", true);
            }
            if (scratch.getBoolean("kaishi")) {
                scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                if (scratch.getDouble("tick") >= 5) {
                    level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 20, 0, 0, 0, 0.5);
                    burn(self, self.position(), 3, false);
                    self.discard();
                }
            }
        }
    }

    /** 火焰漩涡：400 刻后消失；每 20 刻对 2.5 格内的非友方实体造成火系伤害并点燃，同时把 4 格内的非友方实体（无霸体）拉向自己。 */
    private static final class Vortex implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 10, 3, 3, 3, 0.2);
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
            if (scratch.getDouble("tick2") >= 20) {
                Vec3 center = self.position().add(0, 2, 0);
                burn(self, center, 2.5, true);
                for (Entity other : ProjectileSupport.around(level, center, 4)) {
                    if (!ProjectileSupport.friendly(self, other) && !(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                        other.setDeltaMovement(self.position().subtract(other.position()).scale(0.2));
                    }
                }
                scratch.putDouble("tick2", 0);
            }
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= 400) {
                level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY() + self.getBbHeight() / 3, self.getZ(), 20, 0, 0, 0, 0.8);
                self.discard();
            }
        }
    }

    /** 火龙卷：沿朝向水平前进，把 2 格内的非友方实体（无霸体）拉向自己并抛起；每 20 刻对身周 2 格内（高 2 与高 6 两处）造成火系伤害并点燃，400 刻后消失。 */
    private static final class Tornado implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            double yaw = Math.toRadians(self.getYRot());
            self.setDeltaMovement(new Vec3(-0.2 * Math.sin(yaw), 0, 0.2 * Math.cos(yaw)));
            for (Entity other : ProjectileSupport.around(level, self.position().add(0, 2, 0), 2)) {
                if (!ProjectileSupport.friendly(self, other) && !(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                    other.setDeltaMovement(new Vec3((self.getX() - other.getX()) * 0.4, 0.5, (self.getZ() - other.getZ()) * 0.4));
                }
            }
            level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 5, 0, 6, 0, 0.1);
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
            if (scratch.getDouble("tick2") >= 20) {
                burn(self, self.position().add(0, 2, 0), 2, true);
                burn(self, self.position().add(0, 6, 0), 2, true);
                scratch.putDouble("tick2", 0);
            }
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= 400) {
                level.sendParticles(ParticleTypes.FLAME, self.getX(), self.getY(), self.getZ(), 20, 0, 0, 0, 0.8);
                self.discard();
            }
        }
    }
}
