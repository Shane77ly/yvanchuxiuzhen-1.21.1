package net.yvanchuxiuzhen.system.creature.entity;

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
import net.minecraft.world.phys.Vec3;

/** 飞箭豪猪的原生箭型骨刺：保留穿透、固定击退、命中箭数抵消及落地移除。 */
public final class Yaoshou046skill01Entity extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<Byte> SOURCE_PIERCE = SynchedEntityData.defineId(
            Yaoshou046skill01Entity.class, EntityDataSerializers.BYTE);
    private int sourceKnockback;

    public Yaoshou046skill01Entity(EntityType<? extends Yaoshou046skill01Entity> type, Level level) {
        super(type, level);
    }

    /** 来源施放入口指定的箭属性；出生位置和方向由具体技能阶段控制。 */
    public void configure(double damage, int knockback, byte piercing) {
        setBaseDamage(damage);
        sourceKnockback = knockback;
        entityData.set(SOURCE_PIERCE, piercing);
        setSilent(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SOURCE_PIERCE, (byte) 0);
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public byte getPierceLevel() {
        return entityData.get(SOURCE_PIERCE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SOURCE_PIERCE, tag.getByte("PierceLevel"));
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback > 0) {
            double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6 * resistance);
            if (push.lengthSqr() > 0) {
                target.push(push.x, 0.1, push.z);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (inGround) {
            discard();
        }
    }
}
