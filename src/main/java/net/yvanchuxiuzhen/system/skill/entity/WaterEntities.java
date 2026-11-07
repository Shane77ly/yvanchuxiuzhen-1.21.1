package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillDelays;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 水系的实体技能与施放（来源 {@code SkillWater01}、{@code SkillWater02} 及对应的 {@code …shifang}、{@code …DangShiTiGengXinKeShi}）：
 * 水蛇鞭、水连鞭、激流波、水龙吟。水蛇鞭与水连鞭按潜行、Ctrl、Alt 分式，实体跟随施放者并分阶段出手；
 * 两种实体把攻击量与倍率截成整数记下，粒子沿各模式的命中、收尾与持续计时触发。
 */
final class WaterEntities {
    private static final String WATER = "水";
    private static final String STAGE_TWO = "阶段二";

    private WaterEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_water_01", new Whip());
        skills.put("skill_water_02", new Dragon());
        SkillReleases.register(WATER, (caster, book) -> whip(caster, book), "水蛇鞭", "水连鞭", "激流波");
        SkillReleases.register(WATER, (caster, book) -> dragon(caster, book), "水龙吟");
    }

    /**
     * 水蛇鞭、水连鞭、激流波：在施放者处生成鞭影实体，朝向同施放者；模式与动画：水蛇鞭按潜行、Ctrl、Alt、正常四式，水连鞭按潜行、正常两式，
     * 激流波一式。激流波另在施放时把视线 2 格与 5 格处 2.5 格内的非友方实体（无霸体）沿视线抛出并造成水系伤害，其中视线 5 格处的非物品实体记为施放者体质里的
     * 追踪目标（来源在视线 2 格处那一次写到了技能书上，没有读取者，不迁移），再放出 9 枚水系小水弹（第 0 到第 8 刻各一枚，向随机方向）。
     */
    private static void whip(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity whip = SkillEntities.summon(level, caster, "skill_water_01", WATER, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        whip.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(whip, caster.getYRot(), caster.getXRot());
        String mode;
        String animation;
        boolean sneaking = caster.isShiftKeyDown();
        switch (book.name()) {
            case "水蛇鞭" -> {
                if (sneaking) {
                    mode = "水蛇鞭潜行";
                    animation = "animation.skillWater01.skill4";
                } else if (ModifierKeys.ctrl(caster)) {
                    mode = "水蛇鞭ctrl";
                    animation = "animation.skillWater01.skill5";
                } else if (ModifierKeys.alt(caster)) {
                    mode = "水蛇鞭alt";
                    animation = "animation.skillWater01.skill6";
                } else {
                    mode = "水蛇鞭正常";
                    animation = "animation.skillWater01.skill1";
                }
            }
            case "水连鞭" -> {
                mode = sneaking ? "水连鞭潜行" : "水连鞭正常";
                animation = sneaking ? "animation.skillWater01.skill6" : "animation.skillWater01.skill2";
            }
            default -> {
                mode = "激流波";
                animation = "animation.skillWater01.skill3";
            }
        }
        whip.setAnimation(animation);
        whip.getPersistentData().putString("mode", mode);
        SkillEntities.spawn(level, whip, false);
        if (mode.equals("激流波")) {
            for (int reach : new int[] {2, 5}) {
                Vec3 center = Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(caster, reach));
                for (Entity other : ProjectileSupport.around(level, center, 2.5)) {
                    if (!ProjectileSupport.friendly(caster, other)) {
                        if (!(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                            other.setDeltaMovement(new Vec3(caster.getLookAngle().x * 2, 0.7, caster.getLookAngle().z * 2));
                            if (reach == 5 && !(other instanceof ItemEntity)) {
                                Cultivation.setTizhiString(caster, "mubiaouid", other.getStringUUID());
                            }
                        }
                        ProjectileSupport.deal(level, other, caster, WATER, book.manaAttack(), book.damageMultiplier());
                    }
                }
            }
            for (int delay = 0; delay <= 8; delay++) {
                if (delay == 0) {
                    droplet(level, caster);
                } else {
                    SkillDelays.after(delay, () -> droplet(level, caster));
                }
            }
        }
    }

    /** 从施放者所在位置向随机方向放出一枚小水弹。 */
    private static void droplet(ServerLevel level, ServerPlayer caster) {
        Vec3 direction = new Vec3(Mth.nextDouble(level.random, -1, 1), Mth.nextDouble(level.random, 0, 1), Mth.nextDouble(level.random, -1, 1));
        ProjectileSupport.shoot(level, caster, "projectile_skill_water_0101", caster.position(), direction, 0.1, 0, 0);
    }

    /** 水龙吟：在视线 7 格处（向上以半格为步找空位，最多 7 次）生成水龙，朝向同施放者。 */
    private static void dragon(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos block = ProjectileSupport.lookBlock(caster, 7);
        double y = block.getY();
        for (int index = 0; index < 7 && !level.isEmptyBlock(BlockPos.containing(block.getX(), y, block.getZ())); index++) {
            y += 0.5;
        }
        SkillEntity dragon = SkillEntities.summon(level, caster, "skill_water_02", WATER, (int) book.manaAttack(), (int) book.damageMultiplier(), book.name());
        SkillEntities.place(level, dragon, new Vec3(block.getX(), y, block.getZ()), caster, 0);
        SkillEntities.spawn(level, dragon, false);
    }

    private static void hit(SkillEntity self, Vec3 center, double inflate, boolean knock, double horizontal, double vertical, boolean stun) {
        for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), center, inflate)) {
            if (!ProjectileSupport.friendly(self, other)) {
                if (!self.getPersistentData().getString("mode").equals("水连鞭正常")) {
                    ProjectileSupport.particles((ServerLevel) self.level(), Particles.type("brokenlightblue"), other.position().add(0, other.getBbHeight() / 2, 0), 10, 0, 0, 0, 1);
                    ProjectileSupport.burst(other, "smallexplosionblue", other.getBbHeight() / 2);
                }
                if (stun && other instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_001"), 100, 0));
                }
                SkillEntities.strike(self, other);
                if (knock && !(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                    other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * horizontal, vertical, (other.getZ() - self.getZ()) * horizontal));
                }
            }
        }
    }

    /**
     * 鞭影。水蛇鞭正常、潜行、Ctrl 三式在第 3、8、8 刻向自己视线 3.5、3.5、2 格处（半径 3.5、3.5、2.5）出手一次，潜行式另使目标眩晕 100 刻，
     * Ctrl 式把目标抛起，之后到第 10、16、16 刻消失；水蛇鞭 Alt 式与水连鞭潜行式每 6 刻对身边 6.5 格内出手并推开，共 4 次后消失；
     * 水连鞭正常式在第 4、14、25 刻（自己的第二计数）向视线 3.5 格处出手三次，第三次推开，第 36 刻消失；激流波 16 刻消失，其余 9 刻消失。
     * 前六式每刻传送到施放者的位置。
     */
    private static final class Whip implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String mode = scratch.getString("mode");
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            double tick = scratch.getDouble("tick");
            if (follows(mode)) {
                Entity master = level.getEntity(self.attacker());
                if (master != null) {
                    self.teleportTo(master.getX(), master.getY(), master.getZ());
                }
            }
            String stage = scratch.getString("jieduan");
            switch (mode) {
                case "水蛇鞭正常" -> {
                    if (stage.isEmpty()) {
                        if (tick >= 3) {
                            hit(self, Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(self, 3.5)), 3.5, false, 0, 0, false);
                            scratch.putString("jieduan", STAGE_TWO);
                        }
                    } else if (stage.equals(STAGE_TWO) && tick >= 10) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 0, 0, 0, 0.4);
                        scratch.putDouble("tick", 0);
                        self.discard();
                    }
                }
                case "水蛇鞭潜行" -> {
                    if (stage.isEmpty()) {
                        if (tick >= 8) {
                            hit(self, Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(self, 3.5)), 3.5, false, 0, 0, true);
                            scratch.putString("jieduan", STAGE_TWO);
                        }
                    } else if (stage.equals(STAGE_TWO) && tick >= 16) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 0, 0, 0, 0.4);
                        scratch.putDouble("tick", 0);
                        self.discard();
                    }
                }
                case "水蛇鞭ctrl" -> {
                    if (stage.isEmpty()) {
                        if (tick >= 8) {
                            hit(self, Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(self, 2)), 2.5, true, 0.1, 1, false);
                            scratch.putString("jieduan", STAGE_TWO);
                        }
                    } else if (stage.equals(STAGE_TWO) && tick >= 16) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 0, 0, 0, 0.4);
                        scratch.putDouble("tick", 0);
                        self.discard();
                    }
                }
                case "水蛇鞭alt", "水连鞭潜行" -> {
                    if (tick >= 6) {
                        hit(self, self.position(), 6.5, true, 0.4, 0.5, false);
                        scratch.putString("jieduan", STAGE_TWO);
                        scratch.putDouble("tick", 0);
                        scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                        if (scratch.getDouble("tick2") >= 4) {
                            self.discard();
                            scratch.putDouble("tick2", 0);
                        }
                    }
                    if (scratch.getDouble("tick2") >= 3) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 2, 0, 2, 0);
                    }
                }
                case "水连鞭正常" -> {
                    if (tick >= 36) {
                        self.discard();
                    } else if (tick >= 32) {
                        BlockPos front = ProjectileSupport.lookBlock(self, 2);
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), new Vec3(front.getX(), self.getY(), front.getZ()), 8, 0, 0, 0, 1);
                    }
                    scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                    double second = scratch.getDouble("tick2");
                    Vec3 center = Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(self, 3.5));
                    if (second >= 25 && stage.equals("第二")) {
                        scratch.putString("jieduan", "第三");
                        hit(self, center, 3.5, true, 0.2, 0.5, false);
                    } else if (second >= 14 && stage.equals("第一")) {
                        scratch.putString("jieduan", "第二");
                        hit(self, center, 3.5, false, 0, 0, false);
                    } else if (second >= 4 && stage.isEmpty()) {
                        scratch.putString("jieduan", "第一");
                        hit(self, center, 3.5, false, 0, 0, false);
                    }
                }
                case "激流波" -> {
                    if (tick >= 16) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 0, 0, 0, 1);
                        self.discard();
                    }
                }
                default -> {
                    if (tick >= 9) {
                        ProjectileSupport.particles(level, Particles.type("brokenlightblue"), self.position(), 10, 0, 0, 0, 1);
                        self.discard();
                    }
                }
            }
        }

        private static boolean follows(String mode) {
            return mode.equals("水蛇鞭正常") || mode.equals("水蛇鞭潜行") || mode.equals("水蛇鞭ctrl") || mode.equals("水蛇鞭alt")
                    || mode.equals("水连鞭潜行") || mode.equals("水连鞭正常");
        }
    }

    /** 水龙：90 刻后消失；每 10 刻对 3.5 格内的非友方实体造成水系伤害，并把无霸体者向上抛起。 */
    private static final class Dragon implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            ProjectileSupport.particles((ServerLevel) self.level(), Particles.type("brokenblue"), self.position(), 10, 2, 0, 2, 0);
            if (scratch.getDouble("tick") >= 90) {
                scratch.putDouble("tick", 0);
                self.discard();
            }
            scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
            if (scratch.getDouble("tick2") >= 10) {
                for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position().add(0, self.getBbHeight() / 2, 0), 3.5)) {
                    if (!ProjectileSupport.friendly(self, other)) {
                        SkillEntities.strike(self, other);
                        if (!(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                            other.setDeltaMovement(new Vec3(0, 0.5, 0));
                        }
                    }
                }
                scratch.putDouble("tick2", 0);
            }
        }
    }
}
