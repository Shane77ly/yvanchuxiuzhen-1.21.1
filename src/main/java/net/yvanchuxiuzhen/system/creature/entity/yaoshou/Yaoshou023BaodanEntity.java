package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 023号彩羽轰炸独立箭体：命中后执行来源木属性范围伤害，落地后移除。 */
public final class Yaoshou023BaodanEntity extends AbstractArrow implements ItemSupplier {
    private int sourceKnockback;

    public Yaoshou023BaodanEntity(EntityType<? extends Yaoshou023BaodanEntity> type, Level level) {
        super(type, level);
    }

    public void configure(double damage, int knockback) {
        setBaseDamage(damage);
        sourceKnockback = knockback;
        setCritArrow(false);
        setSilent(true);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_200")));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou_023baodan")));
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        if (sourceKnockback > 0) {
            double resistance = Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(sourceKnockback * 0.6 * resistance);
            if (push.lengthSqr() > 0) target.push(push.x, 0.1, push.z);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    public void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        detonate(getOwner());
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        detonate(getOwner());
    }

    @Override
    public void tick() {
        super.tick();
        if (inGround) discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("sourceKnockback", sourceKnockback);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        sourceKnockback = tag.getInt("sourceKnockback");
    }

    private void detonate(Entity owner) {
        if (!(level() instanceof ServerLevel level) || owner == null) return;
        level.sendParticles(ParticleTypes.ITEM_SLIME, getX(), getY(), getZ(), 10, 0, 0, 0, 0);
        Vec3 center = position();
        for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
            if (Relations.isFriendly(owner, target)) continue;
            level.sendParticles(Particles.type("smallexplosiondarkgreen"), target.getX(), target.getY() + target.getBbHeight() / 2,
                    target.getZ(), 1, 0, 0, 0, 0);
            if (target instanceof LivingEntity living) {
                SpellDamage.command(level, living, "shanghai 木 " + owner.getStringUUID() + " false 2 0.5");
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            }
        }
    }
}
