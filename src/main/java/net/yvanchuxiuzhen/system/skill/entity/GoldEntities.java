package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.input.ModifierKeys;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 金系的金轮实体与金刚轮施放（来源 {@code SkillGold0201}、{@code SkillGold02}）。金轮实体按模式（来源放在 {@code friendlist.jinengName}）
 * 分八种：金刚轮的正常、潜行、Ctrl、Alt 四式，金弧轮的左旋与右旋、天堑金轮、捆妖轮、捆妖轮锁住；后四种由金轮弹体
 * （{@code SkillGold03}，见 {@link net.yvanchuxiuzhen.system.skill.projectile.GoldWheel}）生成并跟随它。模式、跟随对象（{@code belongToMasterUUID}）
 * 与角度记在实体的 {@code getPersistentData} 里；各模式到期时发出黄色破碎粒子。
 */
public final class GoldEntities {
    public static final String ID = "skill_gold_0201";
    private static final String GOLD = "金";

    private GoldEntities() {
    }

    static void register(Map<String, EntitySkill> skills) {
        skills.put(ID, new Wheel());
        SkillReleases.register(GOLD, (caster, book) -> vajraWheel(caster, book), "金刚轮");
    }

    /** 记下模式与跟随对象。 */
    public static void setMode(SkillEntity wheel, String mode, Entity master) {
        wheel.getPersistentData().putString("mode", mode);
        wheel.getPersistentData().putString("master", master.getStringUUID());
    }

    /**
     * 金刚轮：在施放者处生成。潜行为“潜行”式（动画 skill02）；按住 Ctrl 为“Ctrl”式（记下俯仰角、朝向同施放者）；按住 Alt 为
     * “Alt”式（动画 skill04）；否则为“正常”式（动画 skill01），并立即把 4 格内的非友方实体（无霸体）弹开、造成金系伤害。
     */
    private static void vajraWheel(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        SkillEntity wheel = SkillEntities.summon(caster, book, ID, GOLD);
        wheel.moveTo(caster.getX(), caster.getY(), caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        wheel.setNoGravity(true);
        String mode;
        if (caster.isShiftKeyDown()) {
            wheel.setAnimation("animation.skillGold02.skill02");
            mode = "金刚轮潜行";
        } else if (ModifierKeys.ctrl(caster)) {
            mode = "金刚轮ctrl";
            wheel.getPersistentData().putInt("jiaodu", (int) caster.getXRot());
            SkillEntities.turn(wheel, caster.getYRot(), caster.getXRot());
        } else if (ModifierKeys.alt(caster)) {
            wheel.setAnimation("animation.skillGold02.skill04");
            mode = "金刚轮alt";
        } else {
            wheel.setAnimation("animation.skillGold02.skill01");
            mode = "金刚轮正常";
            for (Entity other : ProjectileSupport.around(level, caster.position(), 4)) {
                if (!ProjectileSupport.friendly(caster, other)) {
                    if (!(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                        other.setDeltaMovement(new Vec3((other.getX() - caster.getX()) * 0.7, 0.5, (other.getZ() - caster.getZ()) * 0.7));
                    }
                    ProjectileSupport.deal(level, other, caster, GOLD, book.manaAttack(), book.damageMultiplier());
                }
            }
        }
        setMode(wheel, mode, caster);
        SkillEntities.spawn(level, wheel, false);
    }

    /** 金轮：各模式的存在时间、对施放者的跟随与对敌人的牵引。 */
    private static final class Wheel implements EntitySkill {
        @Override
        public void tick(SkillEntity self) {
            ServerLevel level = (ServerLevel) self.level();
            CompoundTag scratch = self.getPersistentData();
            String mode = scratch.getString("mode");
            switch (mode) {
                case "金刚轮正常" -> expire(self, 16);
                case "金刚轮潜行" -> {
                    self.setAnimation("animation.skillGold02.skill02");
                    expire(self, 100);
                    follow(self, 8, 0);
                }
                case "金刚轮ctrl" -> {
                    double yaw = Math.toRadians(self.getYRot());
                    self.setDeltaMovement(new Vec3(-0.8 * Math.sin(yaw), -0.8 * Math.sin(Math.toRadians(scratch.getInt("jiaodu"))), 0.8 * Math.cos(yaw)));
                    for (Entity other : ProjectileSupport.around(level, self.position(), 2.5)) {
                        if (!ProjectileSupport.friendly(self, other) && !(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                            other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 0.7, 0.5, (other.getZ() - self.getZ()) * 0.7));
                        }
                    }
                    expire(self, 100);
                }
                case "金刚轮alt" -> {
                    self.setAnimation("animation.skillGold02.skill04");
                    scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
                    if (scratch.getDouble("tick2") >= 5) {
                        for (Entity other : ProjectileSupport.around(level, self.position(), scratch.getDouble("fanwei") / 2)) {
                            if (!ProjectileSupport.friendly(self, other) && !(other instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08"))) {
                                other.setDeltaMovement(new Vec3((other.getX() - self.getX()) * 0.4, 0.3, (other.getZ() - self.getZ()) * 0.4));
                            }
                        }
                        if (scratch.getDouble("fanwei") <= 13) {
                            if (scratch.getDouble("suojian") == 0) {
                                scratch.putDouble("suojian", 1);
                            }
                            scratch.putDouble("suojian", Math.round(scratch.getDouble("suojian") + Math.pow(scratch.getDouble("suojian"), 0.2)));
                            scratch.putDouble("fanwei", scratch.getDouble("fanwei") + 4 - scratch.getDouble("suojian"));
                        }
                        scratch.putDouble("tick2", 0);
                    }
                    expire(self, 56);
                }
                case "金弧轮左旋", "金弧轮右旋" -> {
                    expire(self, 20);
                    follow(self, 2.5, -1);
                }
                case "天堑金轮" -> {
                    expire(self, 40);
                    follow(self, 2.5, -1);
                }
                case "捆妖轮" -> {
                    expire(self, 20);
                    follow(self, 2.5, -1);
                }
                case "捆妖轮锁住" -> {
                    self.setAnimation("animation.skillGold02.skill05");
                    expire(self, 60);
                    follow(self, 2.5, 0);
                }
                default -> {
                }
            }
        }

        /** 计时加一，到达寿命时散出金色粒子并消失。 */
        private static void expire(SkillEntity self, int lifetime) {
            CompoundTag scratch = self.getPersistentData();
            scratch.putDouble("tick", scratch.getDouble("tick") + 1);
            if (scratch.getDouble("tick") >= lifetime) {
                ProjectileSupport.particles((ServerLevel) self.level(), Particles.type("brokenyellow"), self.position().add(0, 1, 0), 30, 1, 0, 1, 0.2);
                self.discard();
                scratch.putDouble("tick", 0);
            }
        }

        /** 把自己传送到跟随对象（盒内 {@code inflate} 格内找到的那个实体）的位置，纵向偏移 {@code lift}。 */
        private static void follow(SkillEntity self, double inflate, double lift) {
            String master = self.getPersistentData().getString("master");
            for (Entity other : ProjectileSupport.around((ServerLevel) self.level(), self.position(), inflate)) {
                if (other.getStringUUID().equals(master)) {
                    self.teleportTo(other.getX(), other.getY() + lift, other.getZ());
                }
            }
        }
    }
}
