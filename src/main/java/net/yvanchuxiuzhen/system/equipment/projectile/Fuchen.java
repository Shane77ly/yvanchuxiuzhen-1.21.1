package net.yvanchuxiuzhen.system.equipment.projectile;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
    private double ticks;
    private final Set<String> hitTargets = new LinkedHashSet<>();

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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("fuchentick", ticks);
        ListTag hitList = new ListTag();
        for (String uuid : hitTargets) hitList.add(StringTag.valueOf(uuid));
        tag.put("hitTargets", hitList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pierce = tag.getByte("PierceLevel");
        ticks = tag.getDouble("fuchentick");
        hitTargets.clear();
        ListTag hitList = tag.getList("hitTargets", Tag.TAG_STRING);
        for (int index = 0; index < hitList.size(); index++) hitTargets.add(hitList.getString(index));
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
                    String uuid = target.getStringUUID();
                    if (!Relations.isFriendly(owner, target) && hitTargets.add(uuid)) {
                        if (target instanceof LivingEntity living) {
                            SpellDamage.deal(level, living, DamageCategory.NONE, owner, false, 2, 1);
                        }
                    }
                }
                if (++ticks >= 10) {
                    discard();
                    ticks = 0;
                }
            }
        }
        if (inGround) discard();
    }
}
