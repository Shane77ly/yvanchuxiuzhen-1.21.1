package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 017蜕壳后的独立Animal实体，保存自身动画、属性与长壳生命周期。 */
public final class Yaoshou01701Entity extends Animal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Yaoshou01701Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Yaoshou01701Entity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private boolean swinging;
    private long lastSwing;

    public Yaoshou01701Entity(EntityType<? extends Yaoshou01701Entity> type, Level level) { super(type, level); xpReward = 0; }
    public static AttributeSupplier.Builder attributes() { return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 100).add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16).add(YaoshouAttributes.SIZE); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder); builder.define(ANIMATION, "undefined"); builder.define(TEXTURE, "yaoshou017"); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    @Override protected EntityDimensions getDefaultDimensions(Pose pose) { return super.getDefaultDimensions(pose).scale((float) getAttributeValue(YaoshouAttributes.SIZE)).withEyeHeight(0.4F); }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override protected SoundEvent getAmbientSound() { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou017idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou017attacked"); }
    @Override protected SoundEvent getDeathSound() { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou017die"); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou017walk"), 0.15F, 1F); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override protected void tickDeath() { if (++deathTime == 600) { remove(RemovalReason.KILLED); dropExperience(this); } }
    @Override public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (isAlive() && attacker != null && !Relations.isFriendly(this, attacker)) {
            getPersistentData().putString("linshigongjizhe", attacker.getStringUUID());
            if (getPersistentData().getBoolean("beidaduan")) { setAnimation("empty"); getPersistentData().putBoolean("beidaduan", false); }
            if (getPersistentData().getString("jineng").isEmpty() && !level().isClientSide()) setAnimation("animation.yaoshou01701.attacked");
        }
        return !source.is(DamageTypes.DROWN) && super.hurt(source, amount);
    }
    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        CompoundTag scratch = getPersistentData();
        if (scratch.getString("LEVELname").isEmpty()) {
            if (scratch.getDouble("LEVELnumber") < 1) scratch.putDouble("LEVELnumber", 1);
            double number = scratch.getDouble("LEVELnumber");
            getAttribute(YaoshouAttributes.SIZE).setBaseValue((new Random().nextInt(31) + 70) * 0.01 + (number / 4) * 0.1);
            String[] keys = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
            String[] ratings = {"勉强", "贫弱", "贫弱", "勉强", "贫弱", "贫弱", "平庸", "贫弱", "勉强", "贫弱", "强劲", "贫弱", "贫弱"};
            for (int index = 0; index < keys.length; index++) scratch.putString(keys[index] + "_LEVEL", ratings[index]);
            YaoshouShared.updateLevelName(scratch, number);
            if (number <= 40 && scratch.getDouble("experience_Max") < 1) { double maximum = 100; for (int index = 1; index <= (int) number; index++) maximum += index * index * 10 + 10; scratch.putDouble("experience_Max", maximum); }
            YaoshouShared.calculateAttributes(this, scratch, number);
            YaoshouShared.initializeCharacter(this, scratch);
            String team = getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
            Relations.setTeam(this, team); Relations.addFriendlyTeam(this, team);
        }
        return result;
    }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { Yaoshou01701Entity result = (Yaoshou01701Entity) getType().create(level); result.finalizeSpawn(level, level.getCurrentDifficultyAt(result.blockPosition()), MobSpawnType.BREEDING, null); return result; }
    @Override public void baseTick() { super.baseTick(); Yaoshou01701Behavior.tick(level(), this); refreshDimensions(); String request = entityData.get(ANIMATION); if (!request.equals("undefined")) { entityData.set(ANIMATION, "undefined"); procedureAnimation = request; } }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Yaoshou01701Entity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou01701.walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou01701.die", Animation.LoopType.PLAY_ONCE));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou01701.swim"));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou01701.sneak"));
            if (isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou01701.attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou01701.idle"));
        }));
        controllers.add(new AnimationController<Yaoshou01701Entity>(this, "attacking", 4, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) { swinging = true; lastSwing = level().getGameTime(); }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<Yaoshou01701Entity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) { controller.forceAnimationReset(); controller.setAnimation(RawAnimation.begin().then("animation.yaoshou01701.attack", Animation.LoopType.PLAY_ONCE)); }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<Yaoshou01701Entity>(this, "procedure", 4, state -> {
            AnimationController<Yaoshou01701Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) { controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE)); if (controller.getAnimationState() == AnimationController.State.STOPPED) { procedureAnimation = "empty"; controller.forceAnimationReset(); } }
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
