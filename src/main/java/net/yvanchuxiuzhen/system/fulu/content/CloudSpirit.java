package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 44 号符召唤的云灵（来源 {@code Fulutexiao44Entity} 与更新过程）：召唤者驯服的飞行随从，跟随主人、协助主人攻击与反击，
 * 存在 12000 刻。有攻击目标时按“蓄势 100 刻—动画 100 刻—放雷 30 刻”循环：放雷期间每刻沿自身视线 1、3、5、7、9、11、11 格处
 * 对 1 格范围内非友方造成雷属性固定伤害 {@code 5×品阶}（11 格处来源写了两次，各结算一次）。
 * 近战距离：来源覆写 {@code getAttackReachSqr}，1.21.1 版本该方法不存在，改用该版本的近战判定。
 * 来源的右键过程把消息发给自身而不是玩家，且云灵没有可喂食物，所以右键没有额外效果。
 */
public final class CloudSpirit extends TamableAnimal implements GeoEntity, AnimatedLook {
    public static final String ID = "fulutexiao_44";
    private static final String CHARGING = "";
    private static final String ANIMATING = "动画播放";
    private static final String STRIKING = "开启伤害";
    private static final int[] REACH = {1, 3, 5, 7, 9, 11, 11};

    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;
    private double grade;
    private String talismanName = "";
    private String masterUuid = "";
    private double lifeTicks;
    private double cycleTicks;
    private String phase = CHARGING;

    public CloudSpirit(EntityType<? extends CloudSpirit> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        navigation = new FlyingPathNavigation(this, level);
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

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        goalSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.addGoal(4, new FollowOwnerGoal(this, 1, 10, 2));
    }

    @Override
    public String texture() {
        return model.texture();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.FALLING_ANVIL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean canDrownInFluidType(FluidType type) {
        return type != NeoForgeMod.WATER_TYPE.value() && super.canDrownInFluidType(type);
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        CloudSpirit child = (CloudSpirit) getType().create(level);
        child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.BREEDING, null);
        return child;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        refreshDimensions();
        if (level() instanceof ServerLevel level) {
            tickSpirit(level);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("fululevel", grade);
        tag.putString("fuluname", talismanName);
        tag.putString("belongToMasterUUID", masterUuid);
        tag.putDouble("tick", lifeTicks);
        tag.putDouble("tick2", cycleTicks);
        tag.putString("attacking", phase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        grade = tag.getDouble("fululevel");
        talismanName = tag.getString("fuluname");
        masterUuid = tag.getString("belongToMasterUUID");
        lifeTicks = tag.getDouble("tick");
        cycleTicks = tag.getDouble("tick2");
        phase = tag.contains("attacking") ? tag.getString("attacking") : CHARGING;
    }

    private void tickSpirit(ServerLevel level) {
        double life = ++lifeTicks;
        if (life >= 12000) {
            lifeTicks = 0;
            discard();
        }
        if (getTarget() == null) {
            if (cycleTicks != 0) {
                cycleTicks = 0;
            }
            if (phase.equals(STRIKING)) {
                phase = CHARGING;
            }
            return;
        }
        double ticks = ++cycleTicks;
        if (phase.equals(CHARGING)) {
            if (ticks >= 100) {
                phase = ANIMATING;
                triggerAnim("attack", "attack");
                cycleTicks = 0;
            }
        } else if (phase.equals(ANIMATING)) {
            if (ticks >= 100) {
                phase = STRIKING;
                cycleTicks = 0;
            }
        } else if (phase.equals(STRIKING)) {
            strike(level);
            if (ticks >= 30) {
                phase = CHARGING;
                cycleTicks = 0;
            }
        }
    }

    private void strike(ServerLevel level) {
        Vec3 eye = getEyePosition(1f);
        Vec3 view = getViewVector(1f);
        double damage = grade * 5;
        for (int reach : REACH) {
            BlockPos spot = level.clip(new ClipContext(eye, eye.add(view.scale(reach)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this)).getBlockPos();
            Vec3 center = new Vec3(spot.getX(), spot.getY(), spot.getZ());
            List<Entity> targets = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1)).stream()
                    .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
            for (Entity target : targets) {
                if (!Relations.isFriendly(this, target) && target instanceof LivingEntity living) {
                    SpellDamage.dealSaved(level, living, DamageCategory.THUNDER, masterUuid, true, damage, 1);
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", model.lifecycle().animationTransitionTicks(),
                state -> state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()))));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.fulutexiao44.attack")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
