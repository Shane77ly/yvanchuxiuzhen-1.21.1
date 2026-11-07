package net.yvanchuxiuzhen.system.equipment.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.api.Equipment;

/** 覆火葫芦的普通火焰弹体；保留来源的十刻寿命、无重力、命中后置火与命中字段写入。 */
public final class Fabao010huoyan extends AbstractArrow implements ItemSupplier {
    public static final String ID = "projectile_fabao_010huoyan";
    private int ticks;
    private byte pierceLevel = 2;

    public Fabao010huoyan(EntityType<? extends Fabao010huoyan> type, Level level) {
        super(type, level);
        setBaseDamage(.1);
        setCritArrow(false);
        setSilent(true);
    }

    public static Fabao010huoyan shoot(ServerLevel level, LivingEntity owner, float velocity, float spread) {
        return shoot(level, owner, velocity, spread, 2, false);
    }

    public static Fabao010huoyan shootHidden(ServerLevel level, LivingEntity owner, float velocity, float spread) {
        return shoot(level, owner, velocity, spread, 0, true);
    }

    private static Fabao010huoyan shoot(ServerLevel level, LivingEntity owner, float velocity, float spread, int pierce, boolean playSound) {
        Fabao010huoyan projectile = EquipmentSetup.fabao010FireType().get().create(level);
        projectile.pierceLevel = (byte) pierce;
        projectile.setOwner(owner);
        projectile.setPos(owner.getX(), owner.getEyeY() - .1, owner.getZ());
        projectile.shoot(owner.getViewVector(1).x, owner.getViewVector(1).y, owner.getViewVector(1).z, velocity, spread);
        level.addFreshEntity(projectile);
        if (playSound) {
            level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fireshoot")),
                    SoundSource.PLAYERS, 1, 1 / (level.random.nextFloat() * .5f + 1) + velocity / 2);
        }
        return projectile;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.AIR);
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
        if (level() instanceof ServerLevel level && getOwner() != null) {
            writePendingDamage(result.getEntity(), getOwner());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (level() instanceof ServerLevel level) {
            BlockPos above = result.getBlockPos().above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, getX(), getY() + .3, getZ(),
                    2, .2, .2, .2, 0);
            if (++ticks >= 10) {
                discard();
            }
        }
        if (inGround) {
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("fabaoFireTicks", ticks);
        tag.putByte("fabaoFirePierce", pierceLevel);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ticks = tag.getInt("fabaoFireTicks");
        pierceLevel = tag.getByte("fabaoFirePierce");
    }

    @Override
    public byte getPierceLevel() {
        return pierceLevel;
    }

    private static void writePendingDamage(Entity target, Entity owner) {
        String targetTeam = target.getPersistentData().getString("team");
        String ownerTeam = owner.getPersistentData().getString("team");
        if (targetTeam.equals(ownerTeam)) {
            return;
        }
        double damage = target instanceof Player player ? PlayerAttributes.value(player, Stat.MANA_ATT) : 0;
        if (target instanceof LivingEntity living) {
            damage += Equipment.properties(living.getMainHandItem()).number("mana_Att");
        }
        target.getPersistentData().putDouble("ManaAttackDamage", damage);
        target.getPersistentData().putString("ManaAttackUUID", owner.getStringUUID());
        target.getPersistentData().putString("damagetype", "火");
        target.getPersistentData().putDouble("Damage_multiplier", .5);
    }
}
