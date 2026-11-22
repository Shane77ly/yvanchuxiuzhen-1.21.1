package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.yvanchuxiuzhen.platform.ModId;

/** 炮竹（projectile_pao_01）与窜天猴（projectile_pao_08）的飞行与爆裂弹体。 */
public final class SimpleFireworkProjectileEntity extends AbstractArrow implements ItemSupplier {
    private final SimpleFireworkProjectileItem.Kind kind;

    public SimpleFireworkProjectileEntity(EntityType<? extends SimpleFireworkProjectileEntity> type, Level level,
            SimpleFireworkProjectileItem.Kind kind) {
        super(type, level);
        this.kind = kind;
    }

    private SimpleFireworkProjectileEntity(EntityType<? extends SimpleFireworkProjectileEntity> type, LivingEntity owner,
            Level level, SimpleFireworkProjectileItem.Kind kind) {
        super(type, owner, level, ItemStack.EMPTY, ItemStack.EMPTY);
        this.kind = kind;
    }

    @Override
    public ItemStack getItem() {
        return kind == SimpleFireworkProjectileItem.Kind.PAO_01
                ? new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao_01")))
                : ItemStack.EMPTY;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        String id = kind == SimpleFireworkProjectileItem.Kind.PAO_01 ? "pao_01" : "pao_08";
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)));
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        explode();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        if (kind == SimpleFireworkProjectileItem.Kind.PAO_08) {
            if (!isNoGravity()) setNoGravity(true);
            double ticks = getPersistentData().getDouble("tick") + 1;
            getPersistentData().putDouble("tick", ticks);
            if (ticks >= 11) {
                explode();
                discard();
                return;
            }
        }
        if (inGround) discard();
    }

    private void explode() {
        if (!(level() instanceof ServerLevel server)) return;
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao0801"));
        if (kind == SimpleFireworkProjectileItem.Kind.PAO_01) {
            server.playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, 1F, 1F);
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 5, 0, 0, 0, 0.2);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        } else {
            float volume = (float) (1 + Mth.nextDouble(server.random, 0, 1));
            float pitch = (float) Mth.nextDouble(server.random, 1, 1.5);
            server.playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, volume, pitch);
            server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        }
    }

    public static SimpleFireworkProjectileEntity shoot(ServerLevel level, LivingEntity owner,
            SimpleFireworkProjectileItem.Kind kind) {
        EntityType<SimpleFireworkProjectileEntity> type = kind == SimpleFireworkProjectileItem.Kind.PAO_01
                ? FestivalSetup.pao01Type().get()
                : FestivalSetup.pao08Type().get();
        SimpleFireworkProjectileEntity projectile = new SimpleFireworkProjectileEntity(type, owner, level, kind);
        projectile.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, 2F, 0F);
        projectile.setSilent(true);
        projectile.setBaseDamage(0.1);
        projectile.setCritArrow(kind == SimpleFireworkProjectileItem.Kind.PAO_08);
        level.addFreshEntity(projectile);
        if (kind == SimpleFireworkProjectileItem.Kind.PAO_08) {
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pao0802"));
            level.playSound(null, owner.blockPosition(), sound, SoundSource.PLAYERS, 1F,
                    1F / (level.random.nextFloat() * 0.5F + 1F) + 0.5F);
        }
        return projectile;
    }
}
