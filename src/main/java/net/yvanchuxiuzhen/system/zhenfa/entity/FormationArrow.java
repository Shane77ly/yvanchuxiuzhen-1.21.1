package net.yvanchuxiuzhen.system.zhenfa.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;

/** 阳一弹体保留原版箭命中、二级击退与来源标记；施放快照只复制来源实际写入的字段。 */
public final class FormationArrow extends AbstractArrow implements ItemSupplier {
    private int knockback;
    private byte pierce;

    public FormationArrow(EntityType<? extends FormationArrow> type, Level level) { super(type, level); }
    public FormationArrow(EntityType<? extends FormationArrow> type, LivingEntity owner, Level level) {
        super(type, owner, level, new ItemStack(Items.ARROW), ItemStack.EMPTY);
    }

    public void launchedWith(int piercing) { knockback = 2; pierce = (byte) piercing; }
    @Override public ItemStack getItem() { return ItemStack.EMPTY; }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }
    @Override public byte getPierceLevel() { return pierce; }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pierce = tag.getByte("PierceLevel");
    }

    @Override
    protected void doKnockback(LivingEntity entity, DamageSource damageSource) {
        if (knockback <= 0) return;
        double resistance = Math.max(0, 1 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(knockback * .6 * resistance);
        if (push.lengthSqr() > 0) entity.push(push.x, .1, push.z);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide()) return;
        Entity target = result.getEntity();
        int mode = getPersistentData().getInt("zhenmubiao");
        boolean sameTeam = Relations.entityTeam(this).equals(Relations.entityTeam(target));
        if (mode == 1 ? sameTeam : mode != 2 || !sameTeam) {
            SpellDamage.markFormationManaAttack(target, getPersistentData().getInt("ManaAttackDamage"),
                    getPersistentData().getString("ManaAttackUUID"), getPersistentData().getDouble("Damage_multiplier"));
        }
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel server) {
            CompoundTag data = getPersistentData();
            data.putInt("tick", data.getInt("tick") + 1);
            if (data.getInt("tick") >= 20) {
                data.putInt("tick", 0);
                server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("应该清除"), false);
                discard();
            }
            if (!isNoGravity()) {
                Relations.setTeam(this, Relations.entityTeam(owner));
                data.putInt("ManaAttackDamage", owner.getPersistentData().getInt("zhenLEVEL"));
                data.putDouble("Damage_multiplier", .5 + owner.getPersistentData().getInt("zhenLEVEL") * .5);
                setNoGravity(true);
            }
        }
        if (inGround) discard();
    }

    public static FormationArrow shoot(ServerLevel level, LivingEntity owner) {
        FormationArrow arrow = new FormationArrow(ZhenfaSetup.<FormationArrow>entityType("projectile_zhenyang_01").get(), owner, level);
        Vec3 look = owner.getViewVector(1);
        arrow.shoot(look.x, look.y, look.z, 2, 0);
        arrow.setSilent(true);
        arrow.setCritArrow(false);
        arrow.setBaseDamage(.1);
        arrow.launchedWith(0);
        level.addFreshEntity(arrow);
        return arrow;
    }
}
