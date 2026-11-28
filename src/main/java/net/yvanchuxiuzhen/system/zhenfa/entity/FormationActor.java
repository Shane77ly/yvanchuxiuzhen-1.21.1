package net.yvanchuxiuzhen.system.zhenfa.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;

/** 阳一发射体和阳三落实体的共有来源生命周期：飞行导航、无重力、无推挤与基础免伤。 */
public abstract class FormationActor extends PathfinderMob {
    private String skillName = "";
    private int zhenLevel;
    private int targetMode;
    private String manaAttackUuid = "";

    protected FormationActor(EntityType<? extends FormationActor> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public String skillName() { return skillName; }
    public int zhenLevel() { return zhenLevel; }
    public int targetMode() { return targetMode; }
    public String manaAttackUuid() { return manaAttackUuid; }

    public void configure(String skillName, int zhenLevel, int targetMode, String manaAttackUuid) {
        this.skillName = skillName == null ? "" : skillName;
        this.zhenLevel = zhenLevel;
        this.targetMode = targetMode;
        this.manaAttackUuid = manaAttackUuid == null ? "" : manaAttackUuid;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("skill_name", skillName);
        tag.putInt("zhen_level", zhenLevel);
        tag.putInt("target_mode", targetMode);
        tag.putString("mana_attack_uuid", manaAttackUuid);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        skillName = tag.getString("skill_name");
        zhenLevel = tag.getInt("zhen_level");
        targetMode = tag.getInt("target_mode");
        manaAttackUuid = tag.getString("mana_attack_uuid");
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.FLYING_SPEED, .3).add(NeoForgeMod.SWIM_SPEED, .3);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) { }
    @Override public boolean canDrownInFluidType(FluidType type) {
        return type != NeoForgeMod.WATER_TYPE.value() && super.canDrownInFluidType(type);
    }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(Entity entity) { }
    @Override protected void pushEntities() { }
    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }
    @Override public void aiStep() { super.aiStep(); setNoGravity(true); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof AbstractArrow || direct instanceof Player || direct instanceof ThrownPotion || direct instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)) return false;
        return super.hurt(source, amount);
    }
}
