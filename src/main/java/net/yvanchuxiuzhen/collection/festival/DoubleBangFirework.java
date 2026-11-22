package net.yvanchuxiuzhen.collection.festival;

import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 二踢脚实体：右键引燃，第一响传燃近邻，第二阶段上升后二响移除。 */
public final class DoubleBangFirework extends PathfinderMob {
    public DoubleBangFirework(EntityType<? extends DoubleBangFirework> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        getPersistentData().putBoolean("begin", true);
        return InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        CompoundTag data = getPersistentData();
        if (data.getBoolean("begin")) {
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 3, 0, 0, 0, .1);
            }
            double tick = data.getDouble("tick") + 1;
            data.putDouble("tick", tick);
            if (tick >= 20) {
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                    server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                }
                sound("pao0902", 1);
                Vec3 center = position();
                for (DoubleBangFirework neighbor : level().getEntitiesOfClass(DoubleBangFirework.class, new AABB(center, center).inflate(1))
                        .stream().sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                    CompoundTag other = neighbor.getPersistentData();
                    if (!other.getBoolean("begin") && !other.getBoolean("begin2")) other.putBoolean("begin", true);
                }
                data.putBoolean("begin", false);
                data.putBoolean("begin2", true);
                data.putDouble("tick", 0);
            }
        } else if (data.getBoolean("begin2")) {
            double tick = data.getDouble("tick") + 1;
            data.putDouble("tick", tick);
            setDeltaMovement(new Vec3(0, .8, 0));
            if (tick >= 20) {
                if (level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.FLASH, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                    server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
                    server.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 20, 0, 0, 0, .1);
                }
                sound("pao0903", 2);
                data.putBoolean("begin2", false);
                data.putDouble("tick", 0);
                if (!level().isClientSide()) discard();
            }
        }
    }

    private void sound(String id, float volume) {
        SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
        float pitch = (float) Mth.nextDouble(RandomSource.create(), 1, 1.5);
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), sound, SoundSource.NEUTRAL, volume, pitch);
        } else {
            level().playLocalSound(getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, volume, pitch, false);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Player || source.getDirectEntity() instanceof ThrownPotion
                || source.getDirectEntity() instanceof AreaEffectCloud) return false;
        if (source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.WITHER)
                || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }
}
