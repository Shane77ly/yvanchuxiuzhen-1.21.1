package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
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

/** 来源实体椅子的无AI、常驻、无重力载具；方块和令牌经拍卖座椅事件请求操作。 */
public final class DecorSeat extends PathfinderMob {
    public DecorSeat(EntityType<? extends DecorSeat> type, Level level) {
        super(type, level);
        setNoAi(true);
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.FLYING_SPEED, 0.3).add(NeoForgeMod.SWIM_SPEED, 0.3);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean canDrownInFluidType(FluidType type) { return type != NeoForgeMod.WATER_TYPE.value() && super.canDrownInFluidType(type); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }
    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }
    @Override public void aiStep() { super.aiStep(); setNoGravity(true); }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getMsgId().equals("trident") || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }
}
