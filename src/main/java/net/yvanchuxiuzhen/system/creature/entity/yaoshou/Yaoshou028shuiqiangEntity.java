package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 028号妖兽的来源水枪箭：保留水中改向、粒子、穿透和36刻寿命。 */
public final class Yaoshou028shuiqiangEntity extends AbstractArrow implements ItemSupplier {
    private static final EntityDataAccessor<Byte> SOURCE_PIERCE = SynchedEntityData.defineId(
            Yaoshou028shuiqiangEntity.class, EntityDataSerializers.BYTE);
    private int flightTicks;
    private int sourceKnockback;

    public Yaoshou028shuiqiangEntity(EntityType<? extends Yaoshou028shuiqiangEntity> type, Level level) {
        super(type, level);
        setBaseDamage(0.1);
        setCritArrow(false);
        setSilent(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SOURCE_PIERCE, (byte) 1);
    }

    public void configure(Entity owner) {
        configure(owner, 1, getPierceLevel());
    }

    /** 玩家物品发射保留来源独立的击退和穿透参数，妖兽技能继续使用既有默认值。 */
    public void configure(Entity owner, int knockback, byte piercing) {
        setOwner(owner);
        sourceKnockback = knockback;
        entityData.set(SOURCE_PIERCE, piercing);
    }

    public static Yaoshou028shuiqiangEntity shoot(EntityType<Yaoshou028shuiqiangEntity> type, ServerLevel level,
            Entity owner, Vec3 position, Vec3 direction, float speed, float inaccuracy) {
        Yaoshou028shuiqiangEntity projectile = type.create(level);
        projectile.configure(owner);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, speed, inaccuracy);
        level.addFreshEntity(projectile);
        return projectile;
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FlightTicks", flightTicks);
        tag.putInt("SourceKnockback", sourceKnockback);
        tag.putByte("SourcePierce", getPierceLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        flightTicks = tag.getInt("FlightTicks");
        sourceKnockback = tag.getInt("SourceKnockback");
        entityData.set(SOURCE_PIERCE, tag.getByte("SourcePierce"));
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback <= 0) return;
        double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6 * resistance);
        if (push.lengthSqr() > 0) target.push(push.x, 0.1, push.z);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = getOwner();
        if (owner != null && result.getEntity() instanceof LivingEntity target && level() instanceof ServerLevel server
                && !Relations.isFriendly(owner, target)) {
            SpellDamage.deal(server, target, DamageCategory.WATER, owner, false, 1, 0.3);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        entityData.set(SOURCE_PIERCE, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null) {
            if (!isNoGravity()) setNoGravity(true);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.BUBBLE, getX(), getY() + getBbHeight() / 2, getZ(), 5,
                        getBbWidth(), getBbHeight(), getBbWidth(), 0);
                server.sendParticles(ParticleTypes.SPLASH, getX(), getY() + getBbHeight() / 2, getZ(), 5, 0, 0, 0, 0);
            }
            flightTicks++;
            if (flightTicks >= 36) {
                flightTicks = 0;
                if (!level().isClientSide) discard();
            }
            if (isInWaterRainOrBubble()) setDeltaMovement(owner.getLookAngle());
        }
        if (inGround) discard();
    }
}
