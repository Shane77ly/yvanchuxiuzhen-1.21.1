package net.yvanchuxiuzhen.system.equipment.projectile;

import java.util.Comparator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 拂尘的不可见弹体：十刻寿命，直径四格范围内每个敌方实体只结算法术伤害一次。 */
public final class Fuchen extends AbstractArrow implements ItemSupplier {
    public static final String ID = "projectile_fuchen";
    private byte pierce;

    public Fuchen(EntityType<? extends Fuchen> type, Level level) {
        super(type, level);
    }

    public static Fuchen shoot(ServerPlayer player, float velocity, int pierce, double damage) {
        Fuchen projectile = EquipmentSetup.fuchenType().get().create(player.serverLevel());
        projectile.setOwner(player);
        projectile.setBaseDamage(damage);
        projectile.setSilent(true);
        projectile.pierce = (byte) pierce;
        projectile.setPos(player.getX(), player.getEyeY() - .1, player.getZ());
        Vec3 direction = player.getLookAngle();
        projectile.shoot(direction.x, direction.y, direction.z, velocity, 0);
        player.serverLevel().addFreshEntity(projectile);
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
        return pierce;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pierce = tag.getByte("PierceLevel");
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        target.setArrowCount(target.getArrowCount() - 1);
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null) {
            setNoGravity(true);
            if (level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("lingqi"), getX(), getY(), getZ(), 4, 1, 1, 1, 0);
                Vec3 center = position();
                for (Entity target : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2), entity -> true).stream()
                        .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
                    String key = "uid" + target.getStringUUID();
                    if (!Relations.isFriendly(owner, target) && !getPersistentData().getBoolean(key)) {
                        if (target instanceof LivingEntity living) {
                            SpellDamage.deal(level, living, DamageCategory.NONE, owner, false, 2, 1);
                        }
                        getPersistentData().putBoolean(key, true);
                    }
                }
                double ticks = getPersistentData().getDouble("fuchentick") + 1;
                getPersistentData().putDouble("fuchentick", ticks);
                if (ticks >= 10) {
                    discard();
                    getPersistentData().putDouble("fuchentick", 0);
                }
            }
        }
        if (inGround) discard();
    }
}
