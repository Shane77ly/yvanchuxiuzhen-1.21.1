package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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

    private double grade;
    private String talismanName = "";
    private String masterUuid = "";
    private double tickCount;
    private double shotTick;

    public TalismanSummon(EntityType<? extends TalismanSummon> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public void configure(double grade, String talismanName, String masterUuid) {
        this.grade = grade;
        this.talismanName = talismanName;
        this.masterUuid = masterUuid;
    }

    public double grade() {
        return grade;
    }

    public String talismanName() {
        return talismanName;
    }

    public String masterUuid() {
        return masterUuid;
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("fululevel", grade);
        tag.putString("fuluname", talismanName);
        tag.putString("belongToMasterUUID", masterUuid);
        tag.putDouble("tick", tickCount);
        tag.putDouble("tick2", shotTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        grade = tag.getDouble("fululevel");
        talismanName = tag.getString("fuluname");
        masterUuid = tag.getString("belongToMasterUUID");
        tickCount = tag.getDouble("tick");
        shotTick = tag.getDouble("tick2");
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        switch (talismanName) {
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
        double ticks = ++tickCount;
        if (ticks >= 40) {
            for (int index = 0; index < 60; index++) {
                spray(level, ParticleTypes.CLOUD, getX() + Mth.nextInt(random, -6, 6), getY() + 1, getZ() + Mth.nextInt(random, -6, 6), 0, 1, 0);
            }
            double lift = 1 + grade;
            for (Entity target : nearby(level, position(), 6)) {
                if (!Relations.isFriendly(this, target)) {
                    if (target instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0));
                    }
                    target.setDeltaMovement(new Vec3(0, lift, 0));
                }
            }
            tickCount = 0;
            discard();
        }
    }

    private void thunderstormTick(ServerLevel level) {
        if (++shotTick >= 5) {
            Lightning.castSaved(level, new Vec3(getX() + Mth.nextInt(random, -10, 10), getY(), getZ() + Mth.nextInt(random, -10, 10)),
                    masterUuid, 20 * grade, 1, Relations.team(this), 4, "紫色", true);
            shotTick = 0;
        }
        if (++tickCount >= 160) {
            tickCount = 0;
            discard();
        }
    }

    private void airburstTick(ServerLevel level) {
        double ticks = tickCount;
        double radius = 7 - ticks * 0.3;
        tickCount = ticks + 1;
        double angle = 0;
        for (int index = 0; index < 30; index++) {
            level.sendParticles(Particles.type("cailiao_233particle"), getX() + radius * Math.cos(Math.toRadians(angle)), getY() + 0.5,
                    getZ() + radius * Math.sin(Math.toRadians(angle)), 1, 0, 0, 0, 0);
            angle += 12;
        }
        if (tickCount >= 20) {
            level.playSound(null, BlockPos.containing(getX(), getY() + 0.5, getZ()), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 2, 1);
            level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.5, getZ(), 40, 0, 0, 0, 1);
            level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 1, 0, 0, 0, 0);
            double damage = grade * 20;
            for (Entity target : nearby(level, position(), 5)) {
                if (!Relations.isFriendly(this, target) && target instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 风 " + masterUuid + " true " + damage + " 1");
                }
            }
            tickCount = 0;
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
