package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** Pao12 发射及 Pao13 物品发射共用的烟花弹体；八档燃放保持来源的时序和随机范围。 */
public final class Pao13Entity extends AbstractArrow implements ItemSupplier {
    private double tick;
    private int fireType;

    public Pao13Entity(EntityType<? extends Pao13Entity> type, Level level) { super(type, level); }
    public void setFireType(int value) { fireType = value; }
    public Pao13Entity(EntityType<? extends Pao13Entity> type, LivingEntity owner, Level level) {
        super(type, owner, level, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    @Override public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao_13")));
    }

    @Override protected ItemStack getDefaultPickupItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao_13")));
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (!isNoGravity()) {
            fireType = 1 + server.random.nextInt(8);
            setNoGravity(true);
        }
        tick += 1;
        server.sendParticles(Particles.type("pao_0201"), getX(), getY(), getZ(), 5, 0, 0, 0, 0);
        if (tick >= 16) {
            Pao13Fireworks.emit(server, this, fireType);
            discard();
        }
        if (inGround) discard();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("tick", tick);
        tag.putInt("firetype", fireType);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        tick = tag.getDouble("tick");
        fireType = tag.getInt("firetype");
    }

    @Override protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    public static Pao13Entity shoot(ServerLevel level, LivingEntity owner) {
        Pao13Entity projectile = new Pao13Entity(FestivalSetup.pao13Type().get(), owner, level);
        projectile.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, 2, 0);
        projectile.setSilent(true);
        projectile.setBaseDamage(.1);
        projectile.setCritArrow(false);
        level.addFreshEntity(projectile);
        level.playSound(null, owner.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1,
                1 / (level.random.nextFloat() * .5f + 1) + .5f);
        return projectile;
    }

    public static Pao13Entity battery(ServerLevel level, double x, double y, double z) {
        Pao13Entity projectile = new Pao13Entity(FestivalSetup.pao13Type().get(), level);
        projectile.setPos(x, y, z);
        projectile.setBaseDamage(.1);
        projectile.setSilent(true);
        RandomSource random = RandomSource.create();
        projectile.shoot(random.nextDouble() - .5, 4, random.nextDouble() - .5, 1, 0);
        level.addFreshEntity(projectile);
        level.playSound(null, projectile.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1, 1);
        return projectile;
    }
}
