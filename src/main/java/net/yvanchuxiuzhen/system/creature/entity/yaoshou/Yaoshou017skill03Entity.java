package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** 017魂灵泡沫箭保留来源无拾取、击退和飞行回调。 */
public final class Yaoshou017skill03Entity extends AbstractArrow implements ItemSupplier {
    private int sourceKnockback;

    public Yaoshou017skill03Entity(EntityType<? extends Yaoshou017skill03Entity> type, Level level) {
        super(type, level);
    }

    public void configure(double damage, int knockback) {
        setBaseDamage(damage);
        sourceKnockback = knockback;
        setSilent(true);
    }

    public void setSourceKnockback(int knockback) { sourceKnockback = knockback; }
    @Override public ItemStack getItem() { return ItemStack.EMPTY; }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback > 0) {
            double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6 * resistance);
            if (push.lengthSqr() > 0) target.push(push.x, 0.1, push.z);
        }
    }

    @Override protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Yaoshou017Behavior.Yaoshou017skill03DangTouZhiWuJiZhongShiTiShi(level(), hit.getEntity(), getOwner());
    }

    @Override public void tick() {
        super.tick();
        Yaoshou017Behavior.Yaoshou017skill03TouZhiWuFeiXingTick(level(), this);
        if (inGround) discard();
    }
}
