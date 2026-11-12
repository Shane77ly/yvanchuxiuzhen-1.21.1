package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 026号妖兽的来源火焰箭：保留元素命中、粒子、寿命和箭体碰撞。 */
public final class Yaoshou026huoyanEntity extends AbstractArrow implements ItemSupplier {
    private int sourceKnockback;
    private int flightTicks;

    public Yaoshou026huoyanEntity(EntityType<? extends Yaoshou026huoyanEntity> type, Level level) {
        super(type, level);
        setBaseDamage(0.1);
        setCritArrow(false);
        setSilent(true);
    }

    public void configure(Entity owner, int knockback) {
        setOwner(owner);
        sourceKnockback = knockback;
    }

    public static Yaoshou026huoyanEntity shoot(EntityType<Yaoshou026huoyanEntity> type, ServerLevel level,
            Entity owner, Vec3 position, Vec3 direction, float speed) {
        Yaoshou026huoyanEntity projectile = type.create(level);
        projectile.configure(owner, 1);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, speed, 0);
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
            SpellDamage.command(server, target, "shanghai 火 " + owner.getStringUUID() + " false 5 1");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) return;
        if (!isNoGravity()) setNoGravity(true);
        if (level() instanceof ServerLevel server) {
            SimpleParticleType particle = Particles.type("cailiao_238particle");
            server.sendParticles(particle, getX(), getY() + getBbHeight() / 2, getZ(), 5,
                    getBbWidth(), getBbHeight(), getBbWidth(), 0);
        }
        if (level().isClientSide) return;
        flightTicks++;
        if (flightTicks >= 40 || inGround) discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SourceKnockback", sourceKnockback);
        tag.putInt("FlightTicks", flightTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        sourceKnockback = tag.getInt("SourceKnockback");
        flightTicks = tag.getInt("FlightTicks");
    }
}
