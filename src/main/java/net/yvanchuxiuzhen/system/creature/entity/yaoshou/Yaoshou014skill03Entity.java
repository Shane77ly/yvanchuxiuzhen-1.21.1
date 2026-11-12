package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.minecraft.world.level.Level;

/** 014号飞羽箭体：保留各发射入口的暴击参数、击退、无重力和三十刻寿命。 */
public final class Yaoshou014skill03Entity extends AbstractArrow implements ItemSupplier {
    private int sourceKnockback;
    private int sourceTick;

    public Yaoshou014skill03Entity(EntityType<? extends Yaoshou014skill03Entity> type, Level level) {
        super(type, level);
    }

    public void configure(double damage, int knockback) {
        setBaseDamage(damage);
        sourceKnockback = knockback;
        setCritArrow(false);
        setSilent(true);
        pickup = Pickup.DISALLOWED;
    }

    @Override public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou_014skill_03")));
    }
    @Override protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou_014skill_03")));
    }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback > 0) {
            var push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6
                    * Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
            target.push(push.x, 0.1, push.z);
        }
    }

    @Override protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override public void tick() {
        super.tick();
        if (!isNoGravity()) setNoGravity(true);
        sourceTick++;
        if (sourceTick >= 30 && !level().isClientSide()) discard();
        if (inGround) discard();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("sourceKnockback", sourceKnockback);
        tag.putInt("sourceTick", sourceTick);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        sourceKnockback = tag.getInt("sourceKnockback");
        sourceTick = tag.getInt("sourceTick");
    }
}
