package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 048skill01独立的无拾取箭型弹体，负责水花轨迹及水系命中结算。 */
public final class Yaoshou031WaterProjectile extends AbstractArrow implements ItemSupplier {
    public Yaoshou031WaterProjectile(EntityType<? extends Yaoshou031WaterProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        // 原生箭存档要求非空底层栈；显示与实际拾取入口使用空栈。
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
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        Entity owner = getOwner();
        Entity target = hit.getEntity();
        if (!(level() instanceof ServerLevel level) || owner == null) return;
        level.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 8, 0, 0, 0, 1);
        if (!Relations.isFriendly(owner, target)) {
            level.sendParticles(Particles.type("smallexplosionblue"), target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 1, 0, 0, 0, 0);
            if (target instanceof LivingEntity living) {
                SpellDamage.command(level, living, "shanghai 水 " + owner.getStringUUID() + " false 1 1");
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.SPLASH, getX(), getY(), getZ(), 5, 0.1, 0.1, 0.1, 0);
        if (inGround) discard();
    }
}
