package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.system.combat.api.Lightning;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/**
 * 09、12、14 号符召唤出的不可见飞行体（来源 {@code Fulutexiao09Entity}）：行为由实体持久数据 {@code fuluname} 决定，
 * 浮空符（旋转并在 40 刻后把 6 格内非友方抛向空中）、雷暴符（每 5 刻在 ±10 格内落一道紫色独立落雷，持续 160 刻）、
 * 空爆符（20 刻内收缩的粒子环，随后 5 格内非友方受风属性固定伤害 {@code 20×品阶}）。
 * 阵营取自召唤者，品阶与召唤者 UUID 由召唤方写入持久数据。1.21.1 版本的水下呼吸改由实体类型标签 {@code can_breathe_under_water} 承担。
 */
public final class TalismanSummon extends PathfinderMob {
    public static final String ID = "fulutexiao_09";
    public static final String FLOAT = "浮空符";
    public static final String THUNDERSTORM = "雷暴符";
    public static final String AIRBURST = "空爆符";

    public TalismanSummon(EntityType<? extends TalismanSummon> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 10).add(Attributes.ARMOR, 0)
                .add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16).add(Attributes.FLYING_SPEED, 0.3).add(NeoForgeMod.SWIM_SPEED, 0.3);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof AbstractArrow || direct instanceof Player || direct instanceof ThrownPotion || direct instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.TRIDENT)
                || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        switch (getPersistentData().getString("fuluname")) {
            case FLOAT -> floatTick(level);
            case THUNDERSTORM -> thunderstormTick(level);
            case AIRBURST -> airburstTick(level);
            default -> {
            }
        }
    }

    private void floatTick(ServerLevel level) {
        setYRot(getYRot() + 9);
        setXRot(0);
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
        yRotO = getYRot();
        xRotO = getXRot();
        yBodyRotO = getYRot();
        yHeadRotO = getYRot();
        Vec3 eye = getEyePosition(1f);
        for (int distance : new int[] {4, 6}) {
            BlockPos spot = level.clip(new ClipContext(eye, eye.add(getViewVector(1f).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getBlockPos();
            level.sendParticles(ParticleTypes.CLOUD, spot.getX(), spot.getY(), spot.getZ(), 2, 0, 0, 0, 0);
        }
        double ticks = getPersistentData().getDouble("tick") + 1;
        getPersistentData().putDouble("tick", ticks);
        if (ticks >= 40) {
            for (int index = 0; index < 60; index++) {
                spray(level, ParticleTypes.CLOUD, getX() + Mth.nextInt(random, -6, 6), getY() + 1, getZ() + Mth.nextInt(random, -6, 6), 0, 1, 0);
            }
            double lift = 1 + getPersistentData().getDouble("fululevel");
            for (Entity target : nearby(level, position(), 6)) {
                if (!Relations.isFriendly(this, target)) {
                    if (target instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
                    }
                    target.setDeltaMovement(new Vec3(0, lift, 0));
                }
            }
            getPersistentData().putDouble("tick", 0);
            discard();
        }
    }

    private void thunderstormTick(ServerLevel level) {
        double shot = getPersistentData().getDouble("tick2") + 1;
        if (shot >= 5) {
            Lightning.command(level, new Vec3(getX() + Mth.nextInt(random, -10, 10), getY(), getZ() + Mth.nextInt(random, -10, 10)),
                    "lightninglanding " + 20 * getPersistentData().getDouble("fululevel") + " " + getPersistentData().getString("belongToMasterUUID")
                            + " 1 " + Relations.team(this) + " 4 紫色 独立");
            shot = 0;
        }
        getPersistentData().putDouble("tick2", shot);
        double ticks = getPersistentData().getDouble("tick") + 1;
        getPersistentData().putDouble("tick", ticks);
        if (ticks >= 160) {
            getPersistentData().putDouble("tick", 0);
            discard();
        }
    }

    private void airburstTick(ServerLevel level) {
        double ticks = getPersistentData().getDouble("tick");
        double radius = 7 - ticks * 0.3;
        getPersistentData().putDouble("jvli", radius);
        ticks += 1;
        getPersistentData().putDouble("tick", ticks);
        double angle = 0;
        for (int index = 0; index < 30; index++) {
            level.sendParticles(Particles.type("cailiao_233particle"), getX() + radius * Math.cos(Math.toRadians(angle)), getY() + 0.5,
                    getZ() + radius * Math.sin(Math.toRadians(angle)), 1, 0, 0, 0, 0);
            angle += 12;
        }
        if (ticks >= 20) {
            level.playSound(null, BlockPos.containing(getX(), getY() + 0.5, getZ()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 2, 1);
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 40, 0, 0, 0, 1);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
            double damage = getPersistentData().getDouble("fululevel") * 20;
            for (Entity target : nearby(level, position(), 5)) {
                if (!Relations.isFriendly(this, target) && target instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 风 " + getPersistentData().getString("belongToMasterUUID") + " true " + damage + " 1");
                }
            }
            getPersistentData().putDouble("tick", 0);
            discard();
        }
    }

    private static List<Entity> nearby(ServerLevel level, Vec3 center, double halfSize) {
        return level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(halfSize)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static void spray(ServerLevel level, ParticleOptions type, double x, double y, double z, double vx, double vy, double vz) {
        level.sendParticles(type, x, y, z, 0, vx, vy, vz, 1);
    }
}
