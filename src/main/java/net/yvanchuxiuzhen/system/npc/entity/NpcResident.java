package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

/** 21至30号居民的共同生命周期；实例任务和属性沿用结构持久数据。 */
public class NpcResident extends PathfinderMob {
    public NpcResident(EntityType<? extends NpcResident> type, Level level) {
        super(type, level);
        xpReward = 0;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 1).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        goalSelector.addGoal(3, new FloatGoal(this));
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)) return false;
        return super.hurt(source, amount);
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        setYRot(player.getYRot() - 180);
        setXRot(0);
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
        yRotO = getYRot();
        xRotO = getXRot();
        yBodyRotO = getYRot();
        yHeadRotO = getYRot();
        return InteractionResult.sidedSuccess(level().isClientSide());
    }
}
