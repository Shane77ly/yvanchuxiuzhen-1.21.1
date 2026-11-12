package net.yvanchuxiuzhen.system.equipment.projectile;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 明光镜发出的光线弹体；首刻复制潜行标签，持续发出材料粒子并在二十刻后移除。 */
public final class Fabao014 extends AbstractArrow implements ItemSupplier {
    public static final String ID = "projectile_fabao_014";

    public Fabao014(EntityType<? extends Fabao014> type, Level level) {
        super(type, level);
        setBaseDamage(.1);
        setCritArrow(false);
        setSilent(true);
    }

    public static Fabao014 shoot(ServerLevel level, LivingEntity owner) {
        Fabao014 projectile = EquipmentSetup.fabao014Type().get().create(level);
        projectile.setOwner(owner);
        projectile.setPos(owner.getX(), owner.getEyeY() - .1, owner.getZ());
        projectile.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, 2, 0);
        level.addFreshEntity(projectile);
        return projectile;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao_100")));
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
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(level() instanceof ServerLevel level) || getOwner() == null || Relations.isFriendly(getOwner(), result.getEntity())) {
            return;
        }
        Entity target = result.getEntity();
        if (getPersistentData().getBoolean("sneak") && target instanceof LivingEntity living) {
            level.sendParticles(Particles.type("fulutexiao_33"), target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 20));
        }
        if (target instanceof LivingEntity living) {
            SpellDamage.deal(level, living, DamageCategory.LIGHT, getOwner(), false, 10, 1.1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null) {
            if (level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("cailiao_229particle"), getX(), getY() + .3, getZ(), 2, 0, 0, 0, 0);
            }
            if (!isNoGravity()) {
                if (owner instanceof LivingEntity living
                        && living.getMainHandItem().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("sneak")) {
                    getPersistentData().putBoolean("sneak", true);
                }
                setNoGravity(true);
            }
            int ticks = getPersistentData().getInt("tick") + 1;
            getPersistentData().putInt("tick", ticks);
            if (ticks >= 20 && !level().isClientSide()) {
                discard();
            }
        }
        if (inGround) {
            discard();
        }
    }
}
