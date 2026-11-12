package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;

/** 013号投桃弹体：保留箭体伤害、击退、拾取外观与着地移除。 */
public final class Yaoshou013taoziwuqiEntity extends AbstractArrow implements ItemSupplier {
    private int sourceKnockback;

    public Yaoshou013taoziwuqiEntity(EntityType<? extends Yaoshou013taoziwuqiEntity> type, Level level) {
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
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "peachpeach")));
    }

    @Override protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou_013taoziwuqi")));
    }

    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback > 0) {
            double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            var push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6 * resistance);
            if (push.lengthSqr() > 0) target.push(push.x, 0.1, push.z);
        }
    }

    @Override protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override public void tick() {
        super.tick();
        if (inGround) discard();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("sourceKnockback", sourceKnockback);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        sourceKnockback = tag.getInt("sourceKnockback");
    }
}
