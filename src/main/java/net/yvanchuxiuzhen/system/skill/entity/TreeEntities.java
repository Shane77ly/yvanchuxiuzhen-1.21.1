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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillElement;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 木系的实体技能与施放（来源 {@code SkillTree04/05/06} 及对应的 {@code …shifang}、{@code …ShiTiChuShiShengChengShi}、{@code …DangShiTiGengXinKeShi}）：
 * 乱枝、灰木剑、乱木之种、灰木树梯共用 {@code skill_tree_04}（以技能名区分模式，攻击量与倍率截成整数记下），木篱术是 {@code skill_tree_05}，
 * 毒蔓鞭、回身鞭、地蔓鞭是 {@code skill_tree_06}。自定义粒子（{@code BROKENDEEPORANGE}、{@code SMALLEXPLOSIONDEEPORANGE}、
 * {@code SMALLEXPLOSIONDARKGREEN}、{@code CAILIAO_233PARTICLE}）随 particle 迁移。
 */
final class TreeEntities {
    private static final String TREE = "木";
    private static final String STARTING = "开始";
    private static final String RUNNING = "进行";
    private static final String ENDING = "结束";
    private static final String LASH = "抽击";

    private TreeEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put("skill_tree_04", new Branches());
        skills.put("skill_tree_05", new Hedge());
        skills.put("skill_tree_06", new Vine());
        SkillReleases.register(TREE, (caster, book) -> tangle(caster, book), "乱枝");
        SkillReleases.register(TREE, (caster, book) -> sword(caster, book), "灰木剑");
        SkillReleases.register(TREE, (caster, book) -> seed(caster, book), "乱木之种");
        SkillReleases.register(TREE, (caster, book) -> ladder(caster, book), "灰木树梯");
        SkillReleases.register(TREE, (caster, book) -> hedge(caster, book), "木篱术");
        SkillReleases.register(TREE, (caster, book) -> lash(caster, book), "毒蔓鞭", "回身鞭");
        SkillReleases.register(TREE, (caster, book) -> groundVine(caster, book), "地蔓鞭");
    }

    /** 以施放者所选技能的攻击量与倍率召唤木系枝条实体；模式名写入技能名，攻击量与倍率截成整数。 */
    private static SkillEntity branch(ServerPlayer caster, SkillBook book, String mode) {
        return SkillEntities.summon((ServerLevel) caster.level(), caster, "skill_tree_04", TREE, (int) book.manaAttack(), (int) book.damageMultiplier(), mode);
    }

    /** 视线 {@code distance} 格处的方块在水平面上的位置，高度取 {@code self} 自己的（来源取剪裁结果的 X、Z）。 */
    private static Vec3 ahead(SkillEntity self, double distance) {
        BlockPos block = ProjectileSupport.lookBlock(self, distance);
        return new Vec3(block.getX(), self.getY(), block.getZ());
    }

    /** 乱枝：在视线 4 格处 4.5 格内每个非友方玩家或生物脚下生成一丛枯枝；潜行时来源只给友方枝条打一个没有读取者的标记，没有效果。 */
    private static void tangle(ServerPlayer caster, SkillBook book) {
        if (caster.isShiftKeyDown()) {
            return;
        }
        ServerLevel level = (ServerLevel) caster.level();
        Vec3 center = Vec3.atLowerCornerOf(ProjectileSupport.lookBlock(caster, 4));
        for (Entity other : ProjectileSupport.around(level, center, 4.5)) {
            if ((other instanceof Mob || other instanceof Player) && !ProjectileSupport.friendly(caster, other)) {
                SkillEntity tangle = branch(caster, book, "乱枝正常");
                tangle.moveTo(other.getX(), other.getY(), other.getZ(), level.getRandom().nextFloat() * 360F, 0);
                tangle.setAnimation("animation.skillTree04.skill01");
                SkillEntities.turn(tangle, caster.getYRot(), 0);
                SkillEntities.spawn(level, tangle, false);
            }
        }
    }

    /** 灰木剑：在施放者处生成，按潜行、Ctrl、Alt、正常四式选动画与模式，朝向同施放者。 */
    private static void sword(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        String mode;
        String animation;
        if (caster.isShiftKeyDown()) {
            mode = "灰木剑潜行";
            animation = "animation.skillTree04.skill02";
        } else if (ModifierKeys.ctrl(caster)) {
            mode = "灰木剑ctrl";
            animation = "animation.skillTree04.skill08";
        } else if (ModifierKeys.alt(caster)) {
            mode = "灰木剑alt";
            animation = "animation.skillTree04.skill09";
        } else {
            mode = "灰木剑正常";
            animation = "animation.skillTree04.skill07";
        }
        SkillEntity sword = branch(caster, book, mode);
        sword.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        sword.setAnimation(animation);
        SkillEntities.turn(sword, caster.getYRot(), 0);
        SkillEntities.spawn(level, sword, false);
    }

    /**
     * 乱木之种：在视线 4 格处（先向上、再向下贴地，最多 5 次）空位上播种；随后数施放者 16 格内的木系枝条实体（含刚播下的），
     * 数量 n 大于 1 时给木系冷却加 2 的 n−1 次方刻（直接加刻数，不乘冷却缩减）。
     */
    private static void seed(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 4);
        for (int index = 0; index < 5; index++) {
            if (!level.isEmptyBlock(target)) {
                target = target.above();
            } else if (level.isEmptyBlock(target.below())) {
                target = target.below();
            } else {
                break;
            }
        }
        if (level.isEmptyBlock(target)) {
            SkillEntity seed = branch(caster, book, "乱木之种");
            seed.moveTo(target.getX(), target.getY(), target.getZ(), level.getRandom().nextFloat() * 360F, 0);
            SkillEntities.turn(seed, caster.getYRot(), 0);
            seed.setAnimation("animation.skillTree04.skill04");
            SkillEntities.spawn(level, seed, false);
        }
        int count = 0;
        for (Entity other : ProjectileSupport.around(level, caster.position(), 16)) {
            if (SkillEntities.is(other, "skill_tree_04")) {
                count++;
            }
        }
        if (count > 1) {
            SkillCooldowns cooldowns = caster.getData(SkillData.COOLDOWNS);
            caster.setData(SkillData.COOLDOWNS, cooldowns.with(SkillElement.TREE, cooldowns.get(SkillElement.TREE) + Math.pow(2, count - 1)));
        }
    }

    /** 灰木树梯：在施放者处生成树梯，并把 1.5 格内（含施放者自己、树梯与掉落物，不分敌友）的无霸体实体抛起：潜行垂直抛起，否则沿视线抛出；生物获得缓降 20 刻。 */
    private static void ladder(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity ladder = branch(caster, book, "灰木树梯");
        ladder.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(ladder, caster.getYRot(), 0);
        ladder.setAnimation(caster.isShiftKeyDown() ? "animation.skillTree04.skill05" : "animation.skillTree04.skill10");
        SkillEntities.spawn(level, ladder, false);
        for (Entity other : ProjectileSupport.around(level, caster.position(), 1.5)) {
            if (SkillEntities.movable(other)) {
                other.setDeltaMovement(caster.isShiftKeyDown() ? new Vec3(0, 3, 0) : new Vec3(caster.getLookAngle().x, 1.5, caster.getLookAngle().z));
                if (other instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20, 0));
                }
            }
        }
    }

    /**
     * 木篱术：在施放者周围半径 3 格的弧上（从朝向再偏 30°起每隔 15° 一个，共 9 个）各生成一道木篱，位置先向上再向下贴地（最多 3 次）。
     * 来源放出时没有走初始生成，木篱停在初始状态，不会收起也不会消失（Q44）；伤害类别记作“水”，木篱本身不造成伤害。
     */
    private static void hedge(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        double angle = caster.getYRot() + 30;
        for (int index = 0; index < 9; index++) {
            double x = caster.getX() + 3 * Math.cos(Math.toRadians(angle));
            double y = caster.getY();
            double z = caster.getZ() + 3 * Math.sin(Math.toRadians(angle));
            for (int step = 0; step < 3; step++) {
                if (!level.isEmptyBlock(BlockPos.containing(x, y, z))) {
                    y++;
                } else if (level.isEmptyBlock(BlockPos.containing(x, y - 1, z))) {
                    y--;
                } else {
                    break;
                }
            }
            SkillEntity hedge = SkillEntities.summon(level, caster, "skill_tree_05", "水", book.manaAttack(), book.damageMultiplier(), book.name());
            hedge.moveTo(x, y, z, level.getRandom().nextFloat() * 360F, 0);
            SkillEntities.spawn(level, hedge, false);
            angle += 15;
        }
    }

    private static SkillEntity vine(ServerPlayer caster, SkillBook book) {
        return SkillEntities.summon((ServerLevel) caster.level(), caster, "skill_tree_06", TREE, book.manaAttack(), book.damageMultiplier(), book.name());
    }

    /** 毒蔓鞭、回身鞭：在施放者处生成藤鞭，朝向同施放者，动画分别为 skill01、skill03。 */
    private static void lash(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity vine = vine(caster, book);
        SkillEntities.place(level, vine, caster.position(), caster, 0);
        vine.setAnimation(book.name().equals("毒蔓鞭") ? "animation.skillTree06.skill01" : "animation.skillTree06.skill03");
        SkillEntities.spawn(level, vine, false);
    }

    /** 地蔓鞭：在视线 4 格处（先向上再向下贴地，最多 4 次）生成地蔓，状态直接记为“开始”，播放展开动画。 */
    private static void groundVine(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        BlockPos target = ProjectileSupport.lookBlock(caster, 4);
        for (int index = 0; index < 4; index++) {
            if (!level.isEmptyBlock(target)) {
                target = target.above();
            } else if (level.isEmptyBlock(target.below())) {
                target = target.below();
            } else {
                break;
            }
        }
        SkillEntity vine = vine(caster, book);
        SkillEntities.place(level, vine, Vec3.atLowerCornerOf(target), caster, 0);
        vine.setAnimation("animation.skillTree06.start");
        vine.getPersistentData().putString("zhuangtai", STARTING);
        SkillEntities.spawn(level, vine, false);
    }

    private static void slow(Entity target, int duration, int amplifier) {
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        }
    }

    private static void bleed(Entity target, SkillEntity self) {
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(StatusEffects.holder("effect_004"), 300, (int) self.attackDamage()));
        }
    }

    /** 对 {@code center} 为中心 {@code inflate} 格内的非友方实体造成一次伤害，并按 {@code bleedChance} 的几率让生物流血 300 刻、等级为攻击量。 */
    private static void strikeAround(SkillEntity self, Vec3 center, double inflate, double bleedChance) {
        for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), center, inflate)) {
            if (!ProjectileSupport.friendly(self, other)) {
                SkillEntities.strike(self, other);
                if (Math.random() < bleedChance) {
                    bleed(other, self);
                }
            }
        }
    }

    /** 对 {@code center} 为中心 {@code inflate} 格内的全部实体：非友方者受伤害，无霸体者（不分敌友）获得新的速度：水平分量是到自己的偏移乘 {@code horizontal}，垂直分量是 {@code vertical}。 */
    private static void strikeAndLift(SkillEntity self, Vec3 center, double inflate, double horizontal, double vertical) {
        for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), center, inflate)) {
            if (!ProjectileSupport.friendly(self, other)) {
                SkillEntities.strike(self, other);
            }
            if (SkillEntities.movable(other)) {
                other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * horizontal, vertical, (other.getZ() - self.getZ()) * horizontal));
            }
        }
    }

    /**
     * 木系枝条实体，按技能名分模式。乱枝正常：每 10 刻（首刻起）使身边 1 格内的非友方实体缓慢 10 刻、等级 10，140 刻消失；
     * 灰木剑正常：第 6、12 刻朝前 4.5 格处出手（9 格范围，20% 流血），20 刻消失；灰木剑潜行：第 10 刻朝前 3 格出手（7.5 格范围）并把范围内无霸体实体推开，25 刻消失；
     * 灰木剑 Ctrl：第 8 刻朝前 2.5 与 7.5 格各出手一次并抛起，20 刻消失；灰木剑 Alt：第 4 刻朝前 1.5、4.5、7.5 格各出手一次（3.5 格范围，必定流血），20 刻消失；
     * 乱木之种：1 格内出现非友方玩家或生物时在原处生成“爆裂”并消失，600 刻后自行消失；爆裂：20 刻内每刻对 5 格内的非友方玩家或生物造成攻击量加 1、倍率除以 4 的伤害，
     * 已缓慢者重新缓慢 100 刻、等级 2；灰木树梯：计数每刻加 2（来源在模式分支里又加了一次），52 以上消失。
     */
    private static final class Branches implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            double tick = scratch.getDouble("tick");
            switch (self.skillName()) {
                case "乱枝正常" -> {
                    scratch.putDouble("tick2", scratch.getDouble("tick2") - 1);
                    if (tick >= 140) {
                        self.discard();
                    }
                    if (scratch.getDouble("tick2") <= 0) {
                        for (Entity other : ProjectileSupport.around(level, self.position(), 1)) {
                            if (!ProjectileSupport.friendly(self, other)) {
                                slow(other, 10, 10);
                            }
                        }
                        scratch.putDouble("tick2", 10);
                    }
                }
                case "灰木剑正常" -> {
                    String stage = scratch.getString("jieduan");
                    if (tick >= 20) {
                        self.discard();
                    } else if (tick >= 12 && stage.equals("第二下")) {
                        strikeAround(self, ahead(self, 4.5), 4.5, 0.2);
                        scratch.putString("jieduan", ENDING);
                    } else if (tick >= 6 && stage.isEmpty()) {
                        strikeAround(self, ahead(self, 4.5), 4.5, 0.2);
                        scratch.putString("jieduan", "第二下");
                    }
                }
                case "灰木剑潜行" -> {
                    if (tick >= 25) {
                        self.discard();
                    } else if (tick >= 10 && !scratch.getBoolean("kaishi")) {
                        strikeAndLift(self, ahead(self, 3), 3.75, 1, 0.5);
                        scratch.putBoolean("kaishi", true);
                    }
                }
                case "灰木剑ctrl" -> {
                    if (tick >= 20) {
                        self.discard();
                    } else if (tick >= 8 && !scratch.getBoolean("kaishi")) {
                        strikeAndLift(self, ahead(self, 2.5), 2.5, 0, 1);
                        strikeAndLift(self, ahead(self, 7.5), 2.5, 0, 1.3);
                        scratch.putBoolean("kaishi", true);
                    }
                }
                case "灰木剑alt" -> {
                    if (tick >= 20) {
                        self.discard();
                    } else if (tick >= 4 && !scratch.getBoolean("kaishi")) {
                        strikeAround(self, ahead(self, 1.5), 1.75, 1);
                        strikeAround(self, ahead(self, 4.5), 1.75, 1);
                        strikeAround(self, ahead(self, 7.5), 1.75, 1);
                        scratch.putBoolean("kaishi", true);
                    }
                }
                case "乱木之种" -> {
                    for (Entity other : ProjectileSupport.around(level, self.position(), 1)) {
                        if (!ProjectileSupport.friendly(self, other) && (other instanceof Player || other instanceof Mob)) {
                            SkillEntity burst = SkillEntities.chain(self, "skill_tree_04", self.attackDamage());
                            burst.configure(self.attacker(), self.attackDamage(), self.multiplier(), self.damageType(), "爆裂");
                            burst.moveTo(self.getX(), self.getY(), self.getZ(), level.getRandom().nextFloat() * 360F, 0);
                            burst.setAnimation("animation.skillTree04.skill06");
                            SkillEntities.spawn(level, burst, false);
                            self.discard();
                            break;
                        }
                    }
                    if (tick >= 600) {
                        self.discard();
                    }
                }
                case "爆裂" -> {
                    if (tick >= 20) {
                        self.discard();
                    }
                    for (Entity other : ProjectileSupport.around(level, self.position(), 2.5)) {
                        if (other instanceof LivingEntity living && (other instanceof Player || other instanceof Mob) && !ProjectileSupport.friendly(self, other)) {
                            SpellDamage.deal(level, living, DamageCategory.byLabel(self.damageType()), self.attacker(), false, self.attackDamage() + 1, self.multiplier() / 4);
                            if (living.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                                slow(living, 100, 2);
                            }
                        }
                    }
                }
                case "灰木树梯" -> {
                    scratch.putDouble("tick", scratch.getDouble("tick") + 1);
                    if (scratch.getDouble("tick") >= 52) {
                        self.discard();
                    }
                }
                default -> {
                }
            }
        }
    }

    /** 木篱：初始生成时播放展开动画；展开 14 刻后持续 160 刻，收起动画 20 刻后消失。（经 {@code /summon} 之类走初始生成的才会进入这些阶段，见 Q44。） */
    private static final class Hedge implements EntitySkill {
        @Override
        public void spawned(SkillEntity self) {
            CompoundTag scratch = self.getPersistentData();
            if (scratch.getString("zhuangtai").isEmpty()) {
                self.setAnimation("animation.skillTree05.start");
                scratch.putString("zhuangtai", STARTING);
            }
        }

        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            String state = scratch.getString("zhuangtai");
            if (state.equals(STARTING)) {
                level.sendParticles(ParticleTypes.END_ROD, self.getX(), self.getY(), self.getZ(), 10, 1, 1, 1, 0.1);
                if (scratch.getDouble("tick") >= 14) {
                    scratch.putDouble("tick", 0);
                    scratch.putString("zhuangtai", RUNNING);
                }
            } else if (state.equals(RUNNING)) {
                if (scratch.getDouble("tick") >= 160) {
                    self.setAnimation("animation.skillTree05.close");
                    scratch.putDouble("tick", 0);
                    scratch.putString("zhuangtai", ENDING);
                }
            } else if (state.equals(ENDING)) {
                level.sendParticles(ParticleTypes.END_ROD, self.getX(), self.getY(), self.getZ(), 10, 1, 1, 1, 0.1);
                if (scratch.getDouble("tick") >= 20) {
                    scratch.putDouble("tick", 0);
                    level.sendParticles(ParticleTypes.END_ROD, self.getX(), self.getY(), self.getZ(), 20, 0, 0, 0, 0.5);
                    self.discard();
                }
            }
        }
    }

    private static void sting(SkillEntity self, Entity other) {
        ProjectileSupport.particles((ServerLevel) self.level(), ParticleTypes.CRIT, other.position(), 10, 0, 0, 0, 1);
        SkillEntities.strikeRounded(self, other);
    }

    /**
     * 藤鞭。毒蔓鞭：第 3 刻对朝前 1.5 格处（水平 4 格、垂直 1 格范围）的非友方实体出手，30% 中毒 600 刻（等级为攻击量），第 16 刻消失；
     * 回身鞭：第 4 刻对朝前 3 格处 2 格内的非友方实体出手，只把带霸体的目标沿相反视线方向抛起（来源如此，Q44），第 16 刻消失；
     * 地蔓鞭：展开 17 刻后持续 160 刻，按“一、二、三、四、五”的节拍（各 4、8、9、9、9 刻）向前、后、右、左各出手一次（2.5 格外 2.5 格范围，一轮后回到“一”），
     * 收起 12 刻后消失。
     */
    private static final class Vine implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            double tick = scratch.getDouble("tick");
            String state = scratch.getString("zhuangtai");
            switch (self.skillName()) {
                case "毒蔓鞭" -> {
                    if (state.isEmpty() && tick >= 3) {
                        for (Entity other : ProjectileSupport.around(level, ahead(self, 1.5), 4, 1, 4)) {
                            if (!ProjectileSupport.friendly(self, other)) {
                                sting(self, other);
                                if (Math.random() < 0.3 && other instanceof LivingEntity living) {
                                    living.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_09"), 600, (int) self.attackDamage()));
                                }
                            }
                        }
                        scratch.putString("zhuangtai", LASH);
                    } else if (state.equals(LASH) && tick >= 16) {
                        self.discard();
                    }
                }
                case "回身鞭" -> {
                    if (state.isEmpty() && tick >= 4) {
                        for (Entity other : ProjectileSupport.around(level, ahead(self, 3), 2)) {
                            if (!ProjectileSupport.friendly(self, other)) {
                                sting(self, other);
                                if (other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08")) {
                                    other.setDeltaMovement(new Vec3(-self.getLookAngle().x, 0.5, -self.getLookAngle().z));
                                }
                            }
                        }
                        scratch.putString("zhuangtai", LASH);
                    } else if (state.equals(LASH) && tick >= 16) {
                        self.discard();
                    }
                }
                case "地蔓鞭" -> {
                    if (state.equals(STARTING)) {
                        if (tick >= 17) {
                            scratch.putString("zhuangtai", RUNNING);
                            scratch.putString("shunxv", "一");
                            scratch.putDouble("tick", 0);
                        }
                    } else if (state.equals(RUNNING)) {
                        scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                        if (scratch.getDouble("tick2") >= 160) {
                            self.setAnimation("animation.skillTree06.close");
                            scratch.putDouble("tick", 0);
                            scratch.putDouble("tick2", 0);
                            scratch.putString("zhuangtai", ENDING);
                        }
                        tickBeat(self, level, scratch);
                    } else if (state.equals(ENDING) && tick >= 12) {
                        scratch.putDouble("tick", 0);
                        self.discard();
                    }
                }
                default -> {
                }
            }
        }

        private static void tickBeat(SkillEntity self, ServerLevel level, CompoundTag scratch) {
            String beat = scratch.getString("shunxv");
            double tick = scratch.getDouble("tick");
            double angle;
            if (beat.equals("一") && tick >= 4) {
                angle = self.getYRot();
                scratch.putString("shunxv", "二");
            } else if (beat.equals("二") && tick >= 8) {
                angle = self.getYRot() + 180;
                scratch.putString("shunxv", "三");
            } else if (beat.equals("三") && tick >= 9) {
                angle = self.getYRot() + 90;
                scratch.putString("shunxv", "四");
            } else if (beat.equals("四") && tick >= 9) {
                angle = self.getYRot() - 90;
                scratch.putString("shunxv", "五");
            } else {
                if (beat.equals("五") && tick >= 9) {
                    scratch.putString("shunxv", "一");
                    scratch.putDouble("tick", 0);
                }
                return;
            }
            scratch.putDouble("tick", 0);
            Vec3 center = new Vec3(self.getX() + 2.5 * Math.cos(Math.toRadians(angle)), self.getY(), self.getZ() + 2.5 * Math.sin(Math.toRadians(angle)));
            for (Entity other : ProjectileSupport.around(level, center, 2.5)) {
                if (!ProjectileSupport.friendly(self, other)) {
                    sting(self, other);
                }
            }
        }
    }
}
