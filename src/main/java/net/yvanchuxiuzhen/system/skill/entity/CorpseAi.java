package net.yvanchuxiuzhen.system.skill.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 起尸诀尸体的技能状态机（来源 {@code Skillyin01DangShiTiGengXinKeShi}、{@code Skillyin01DangShiTiShouShangShi} 与各技能过程，
 * {@code Entityjinengjieshu}、{@code Yaoshou001skillzhengtuo}、{@code Skill01siwangyongbao}、{@code Skillyin01konghe}、
 * {@code Skillyin01penshenianye}）。状态沿用来源放在尸体 {@code getPersistentData} 的键：
 * {@code gongjizhe}（目标 UUID）、{@code linshigongjizhe}（最近伤害自己的非友方）、{@code jineng}（当前技能）、{@code jinengjinxing}（阶段）、
 * {@code entitytick}（自上次复位的刻数）、{@code yvdingjineng}/{@code yvdingjinengjishi}（已选定的下一个技能与等待刻数）、{@code cunxvtick}（存续刻数）。
 *
 * <p>每刻：只在存活时推进。没有当前技能时，目标改为最近伤害自己的非友方；有目标且存活时，先按与目标的距离和身上的状态按权重随机选下一个技能，
 * 等待刻数到了再开始；技能按阶段推进，结束时复位。目标丢失（不在维度里）时每 40 刻随机走几步；目标死亡时清空目标并复位。
 * 存续 600 刻且空闲时播放死亡动画并进入“清除自身”，再过 40 刻冒烟消失。
 * 伤害：普攻是以尸体为来源的玩家式攻击伤害，攻击量取 {@code attack_Num}；三个技能是阴系灵力伤害，攻击量取 {@code LEVELnumber}（取整）、倍率 1，攻击者为尸体自己。
 */
final class CorpseAi {
    private static final String CLEAR_SELF = "清除自身";
    private static final String APPROACH = "靠近";
    private static final String BREAK_FREE = "挣脱";
    private static final String STRIKE = "普攻";
    private static final String SPRAY = "喷射粘液";
    private static final String HUG = "死亡拥抱";
    private static final String INTIMIDATE = "恐吓";

    private static final int EXPIRE = 600;
    private static final int DISAPPEAR_DELAY = 40;
    private static final String YIN = "阴";

    /** 一个可选技能：名字、选定后等待的刻数与抽选权重。 */
    private record Option(String name, int delay, int weight) {
    }

    private CorpseAi() {
    }

    /** 受伤（来源 {@code Skillyin01DangShiTiShouShangShi}）：记下伤害自己的非友方为最近攻击者；被打断标记清除过程动画；空闲时播放受击动画。 */
    static void hurt(Corpse self, Entity source) {
        if (self.isAlive() && source != null && !ProjectileSupport.friendly(self, source)) {
            CompoundTag data = self.getPersistentData();
            data.putString("linshigongjizhe", source.getStringUUID());
            if (data.getBoolean("beidaduan")) {
                self.setAnimation("empty");
                data.putBoolean("beidaduan", false);
            }
            if (data.getString("jineng").isEmpty()) {
                self.setAnimation("animation.skillyin01.attacked");
            }
        }
    }

    static void tick(Corpse self) {
        if (!self.isAlive()) {
            return;
        }
        ServerLevel level = (ServerLevel) self.level();
        CompoundTag data = self.getPersistentData();
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty() && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = data.getString("gongjizhe").isEmpty() ? null : level.getEntity(UUID.fromString(data.getString("gongjizhe")));
        if (target != null) {
            if (!target.isAlive()) {
                data.putString("gongjizhe", "");
                data.putString("linshigongjizhe", "");
                finish(self);
            } else if (data.getString("jineng").isEmpty()) {
                choose(self, target);
            } else {
                run(self, target);
            }
        } else if (!data.getString("jineng").equals(CLEAR_SELF) && data.getDouble("entitytick") >= 40) {
            self.getNavigation().moveTo(self.getX() + Mth.nextInt(self.getRandom(), -3, 3), self.getY(), self.getZ() + Mth.nextInt(self.getRandom(), -3, 3), 1);
            data.putDouble("entitytick", 0);
        }
        data.putDouble("cunxvtick", data.getDouble("cunxvtick") + 1);
        if (data.getDouble("cunxvtick") >= EXPIRE && data.getString("jineng").isEmpty()) {
            finish(self);
            self.setAnimation("animation.skillyin01.die");
            data.putString("jineng", CLEAR_SELF);
            data.putString("gongjizhe", "");
            data.putDouble("cunxvtick", 0);
            data.putBoolean("qingchuzishen", true);
        } else if (data.getDouble("cunxvtick") >= DISAPPEAR_DELAY && data.getString("jineng").equals(CLEAR_SELF)) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, self.getX(), self.getY() + 1, self.getZ(), 20, 0.3, 1, 0.3, 0);
            self.discard();
        }
    }

    /** 技能结束复位（来源 {@code Entityjinengjieshu}）。 */
    static void finish(Entity self) {
        CompoundTag data = self.getPersistentData();
        data.putString("jineng", "");
        data.putString("jinengjinxing", "");
        data.putDouble("entitytick", 0);
        data.putDouble("entitytick2", 0);
    }

    /**
     * 选择下一个技能：先转向目标，按身上的状态列出可选技能——被骑乘中只有“挣脱”；不被骑乘时没有沉默（{@code effect_002}）与僵直（{@code effect_033}）才有
     * “靠近”（距离大于 3 格）或四个近身技能；没有僵直且距离小于 3 格时再加一个“普攻”（所以沉默中只有普攻，没有沉默时普攻权重翻倍）。
     * 距离是两个包围盒之间的距离。按权重抽出后记下技能与等待刻数，等待刻数到了才开始；没有可选技能（僵直中）则每刻重新判断。
     */
    private static void choose(Corpse self, Entity target) {
        CompoundTag data = self.getPersistentData();
        if (data.getString("yvdingjineng").isEmpty()) {
            double distance = self.position().distanceTo(target.position()) - (self.getBbWidth() / 2 + target.getBbWidth() / 2);
            lookAt(self, target);
            boolean silenced = StatusEffects.has(self, "effect_002");
            boolean stiff = StatusEffects.has(self, "effect_033");
            List<Option> options = new ArrayList<>();
            if (self.isPassenger()) {
                options.add(new Option(BREAK_FREE, 10, 100));
            } else if (!silenced && !stiff) {
                if (distance > 3) {
                    options.add(new Option(APPROACH, 5, 30));
                } else {
                    options.add(new Option(STRIKE, 10, 20));
                    options.add(new Option(SPRAY, 5, 30));
                    options.add(new Option(HUG, 10, 30));
                    options.add(new Option(INTIMIDATE, 10, 30));
                }
            }
            if (!stiff && distance < 3) {
                options.add(new Option(STRIKE, 10, 20));
            }
            int total = options.stream().mapToInt(Option::weight).sum();
            int roll = Mth.nextInt(self.getRandom(), 1, total);
            int sum = 0;
            for (Option option : options) {
                sum += option.weight();
                if (sum >= roll) {
                    data.putString("yvdingjineng", option.name());
                    data.putDouble("yvdingjinengjishi", option.delay());
                    break;
                }
            }
        } else if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) {
            data.putString("jineng", data.getString("yvdingjineng"));
            data.putString("yvdingjineng", "");
            data.putDouble("yvdingjinengjishi", 0);
            data.putDouble("entitytick", 0);
        }
    }

    private static void run(Corpse self, Entity target) {
        switch (self.getPersistentData().getString("jineng")) {
            case APPROACH -> approach(self, target);
            case BREAK_FREE -> breakFree(self);
            case STRIKE -> strike(self, target);
            case HUG -> hug(self, target);
            case INTIMIDATE -> intimidate(self, target);
            case SPRAY -> spray(self, target);
            default -> {
            }
        }
    }

    private static void lookAt(Entity self, Entity target) {
        self.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }

    private static String phase(Entity self) {
        return self.getPersistentData().getString("jinengjinxing");
    }

    private static void setPhase(Entity self, String phase) {
        self.getPersistentData().putString("jinengjinxing", phase);
    }

    private static double ticks(Entity self) {
        return self.getPersistentData().getDouble("entitytick");
    }

    /** 靠近：转向目标并以 1.5 倍速走向目标，40 刻后结束。 */
    private static void approach(Corpse self, Entity target) {
        if (phase(self).isEmpty()) {
            lookAt(self, target);
            self.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            setPhase(self, "移动中");
        } else if (phase(self).equals("移动中") && ticks(self) >= 40) {
            finish(self);
        }
    }

    /** 挣脱（来源 {@code Yaoshou001skillzhengtuo}，妖兽也使用）：准备 20 刻后脱离骑乘，朝视线方向向前跳起（水平 0.5、竖直 0.7），共 30 刻。 */
    static void breakFree(Entity self) {
        if (phase(self).isEmpty()) {
            setPhase(self, "准备");
        } else if (phase(self).equals("准备") && ticks(self) >= 20) {
            self.stopRiding();
            self.setDeltaMovement(new Vec3(self.getLookAngle().x * 0.5, 0.7, self.getLookAngle().z * 0.5));
            setPhase(self, "跳跃");
        } else if (phase(self).equals("跳跃") && ticks(self) >= 30) {
            finish(self);
        }
    }

    /** 普攻：播放攻击动画并转向目标，1 刻后对视线 {@code 0.5 + 宽度/2} 格处周围 1 格内的非友方实体造成攻击伤害，4 刻后结束。 */
    private static void strike(Corpse self, Entity target) {
        ServerLevel level = (ServerLevel) self.level();
        if (phase(self).isEmpty()) {
            setPhase(self, "准备");
            self.setAnimation("animation.skillyin01.attack");
            lookAt(self, target);
        } else if (phase(self).equals("准备") && ticks(self) >= 1) {
            BlockPos front = ProjectileSupport.lookBlock(self, 0.5 + self.getBbWidth() / 2);
            DamageSource source = level.damageSources().source(DamageTypes.PLAYER_ATTACK, self);
            for (Entity other : ProjectileSupport.around(level, Vec3.atLowerCornerOf(front), 1)) {
                if (!ProjectileSupport.friendly(self, other)) {
                    level.sendParticles(ParticleTypes.CRIT, other.getX(), other.getY() + other.getBbHeight() / 2, other.getZ(), 5, 0, 0, 0, 0.5);
                    other.hurt(source, (float) self.getPersistentData().getDouble("attack_Num"));
                }
            }
            setPhase(self, "攻击");
        } else if (phase(self).equals("攻击") && ticks(self) >= 4) {
            finish(self);
        }
    }

    /**
     * 死亡拥抱：转向目标并播放动画，前冲 20 刻（前 14 刻每刻朝目标以 0.4 的水平速度冲出），到点后对前方 3 格（水平）处周围半格内的非友方实体
     * 施加 1 秒的缓慢 20 并造成阴系伤害，之后 40 刻结束。
     */
    private static void hug(Corpse self, Entity target) {
        ServerLevel level = (ServerLevel) self.level();
        if (phase(self).isEmpty()) {
            lookAt(self, target);
            self.setAnimation("animation.skillyin01.skill03");
            setPhase(self, "前冲");
        } else if (phase(self).equals("前冲") && ticks(self) >= 20) {
            for (Entity other : around(self, 0.5)) {
                if (!ProjectileSupport.friendly(self, other)) {
                    ProjectileSupport.burst(other, "smallexplosiondarkgrey", other.getBbHeight() / 2);
                    if (other instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
                    }
                    yinDamage(level, self, other);
                }
            }
            setPhase(self, "结束");
        } else if (phase(self).equals("前冲") && ticks(self) <= 14) {
            lookAt(self, target);
            self.setDeltaMovement(new Vec3(self.getLookAngle().x * 0.4, 0, self.getLookAngle().z * 0.4));
        } else if (phase(self).equals("结束") && ticks(self) >= 40) {
            finish(self);
        }
    }

    /** 恐吓：转向目标并播放动画，20 刻后结束（没有伤害与状态）。 */
    private static void intimidate(Corpse self, Entity target) {
        if (phase(self).isEmpty()) {
            lookAt(self, target);
            self.setAnimation("animation.skillyin01.skill01");
            setPhase(self, "结束");
        } else if (phase(self).equals("结束") && ticks(self) >= 20) {
            finish(self);
        }
    }

    /**
     * 喷射粘液：转向目标并播放动画，自身获得 56 刻霸体；准备 40 刻后对前方 3 格（水平）处周围 1.5 格内的非友方实体施加 {@code 100 × LEVELnumber} 刻的
     * 毒药 20（等级 1）并造成阴系伤害，之后 60 刻结束。
     */
    private static void spray(Corpse self, Entity target) {
        ServerLevel level = (ServerLevel) self.level();
        CompoundTag data = self.getPersistentData();
        if (phase(self).isEmpty()) {
            lookAt(self, target);
            self.setAnimation("animation.skillyin01.skill02");
            self.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 56, 0, false, false));
            setPhase(self, "准备");
        } else if (phase(self).equals("准备") && ticks(self) >= 40) {
            for (Entity other : around(self, 1.5)) {
                if (!ProjectileSupport.friendly(self, other)) {
                    if (other instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_20"), (int) (100 * data.getDouble("LEVELnumber")), 1));
                    }
                    ProjectileSupport.burst(other, "smallexplosionlightblue", other.getBbHeight() / 2);
                    yinDamage(level, self, other);
                }
            }
            setPhase(self, "结束");
        } else if (phase(self).equals("结束") && ticks(self) >= 60) {
            finish(self);
        }
    }

    /** 视线 3 格处的方块水平位置、尸体自身高度为中心，半边长 {@code inflate} 的盒内实体（来源取 {@code clip(...).getBlockPos()} 的 X、Z）。 */
    private static List<Entity> around(Corpse self, double inflate) {
        BlockPos front = ProjectileSupport.lookBlock(self, 3);
        return ProjectileSupport.around((ServerLevel) self.level(), new Vec3(front.getX(), self.getY(), front.getZ()), inflate);
    }

    /** 阴系灵力伤害：攻击者是尸体自己，攻击量取 {@code LEVELnumber}、倍率 1（均取整）。 */
    private static void yinDamage(ServerLevel level, Corpse self, Entity other) {
        if (other instanceof LivingEntity living) {
            SpellDamage.dealRounded(level, living, DamageCategory.byLabel(YIN), self.getUUID(), false, self.getPersistentData().getDouble("LEVELnumber"), 1);
        }
    }
}
