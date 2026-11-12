package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** 016毒液箭保留来源穿透级别、击退与命中回调。 */
public final class Yaoshou016duyeEntity extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<Byte> SOURCE_PIERCE = SynchedEntityData.defineId(
            Yaoshou016duyeEntity.class, EntityDataSerializers.BYTE);
    private int sourceKnockback;

    public Yaoshou016duyeEntity(EntityType<? extends Yaoshou016duyeEntity> type, Level level) {
        super(type, level);
    }

    public void configure(double damage, int knockback, byte piercing) {
        setBaseDamage(damage);
        sourceKnockback = knockback;
        entityData.set(SOURCE_PIERCE, piercing);
        setSilent(true);
    }

    public void setSourceKnockback(int knockback) { sourceKnockback = knockback; }
    public void setSourcePierceLevel(byte piercing) { entityData.set(SOURCE_PIERCE, piercing); }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SOURCE_PIERCE, (byte) 0);
    }

    @Override public byte getPierceLevel() { return entityData.get(SOURCE_PIERCE); }
    @Override public ItemStack getItem() { return ItemStack.EMPTY; }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SOURCE_PIERCE, tag.getByte("PierceLevel"));
    }

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
        Yaoshou016Behavior.Yaoshou016duyeDangTouZhiWuJiZhongShiTiShi(hit.getEntity(), getOwner());
    }

    @Override protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        Yaoshou016Behavior.Yaoshou016duyeDangTouSheWuSheZhongCiFangKuai(level(), this);
    }

    @Override public void tick() {
        super.tick();
        Yaoshou016Behavior.Yaoshou016duyeTouZhiWuFeiXingTick(level(), this);
        if (inGround) discard();
    }
}
