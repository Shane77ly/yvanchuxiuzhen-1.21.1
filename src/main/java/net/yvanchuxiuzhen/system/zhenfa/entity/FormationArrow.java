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
    private int arrowTick;
    private int targetMode;
    private int manaAttackDamage;
    private String manaAttackUuid = "";
    private double damageMultiplier;

    public FormationArrow(EntityType<? extends FormationArrow> type, Level level) { super(type, level); }
    public FormationArrow(EntityType<? extends FormationArrow> type, LivingEntity owner, Level level) {
        super(type, owner, level, new ItemStack(Items.ARROW), ItemStack.EMPTY);
    }

    public void launchedWith(int piercing) { knockback = 2; pierce = (byte) piercing; }
    public int targetMode() { return targetMode; }
    public int manaAttackDamage() { return manaAttackDamage; }
    public String manaAttackUuid() { return manaAttackUuid; }
    public double damageMultiplier() { return damageMultiplier; }
    public void setArrowTick(int tick) { arrowTick = tick; }
    @Override public ItemStack getItem() { return ItemStack.EMPTY; }
    @Override protected ItemStack getDefaultPickupItem() { return new ItemStack(Items.ARROW); }
    @Override protected ItemStack getPickupItem() { return ItemStack.EMPTY; }
    @Override public byte getPierceLevel() { return pierce; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("arrow_tick", arrowTick);
        tag.putInt("target_mode", targetMode);
        tag.putInt("mana_attack_damage", manaAttackDamage);
        tag.putString("mana_attack_uuid", manaAttackUuid);
        tag.putDouble("damage_multiplier", damageMultiplier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        pierce = tag.getByte("PierceLevel");
        arrowTick = tag.getInt("arrow_tick");
        targetMode = tag.getInt("target_mode");
        manaAttackDamage = tag.getInt("mana_attack_damage");
        manaAttackUuid = tag.getString("mana_attack_uuid");
        damageMultiplier = tag.getDouble("damage_multiplier");
    }

    @Override
    protected void doKnockback(LivingEntity entity, DamageSource damageSource) {
        if (knockback <= 0) return;
        Vec3 push = getDeltaMovement().multiply(1, 0, 1).normalize().scale(knockback * .6);
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
        boolean sameTeam = Relations.entityTeam(this).equals(Relations.entityTeam(target));
        if (targetMode == 1 ? sameTeam : targetMode != 2 || !sameTeam) {
            SpellDamage.markFormationManaAttack(target, manaAttackDamage, manaAttackUuid, damageMultiplier);
        }
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = getOwner();
        if (owner != null && level() instanceof ServerLevel server) {
            arrowTick += 1;
            if (arrowTick >= 20) {
                arrowTick = 0;
                server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("应该清除"), false);
                discard();
            }
            if (!isNoGravity()) {
                Relations.setTeam(this, Relations.entityTeam(owner));
                int rank = owner instanceof FormationActor actor ? actor.zhenLevel() : 0;
                manaAttackDamage = rank;
                damageMultiplier = .5 + rank * .5;
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
