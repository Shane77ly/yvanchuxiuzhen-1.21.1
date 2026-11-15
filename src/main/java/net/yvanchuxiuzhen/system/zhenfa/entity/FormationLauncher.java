package net.yvanchuxiuzhen.system.zhenfa.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;

/** 阳一临时发射体：第一刻沿视线发射，十刻后移除。 */
public final class FormationLauncher extends FormationActor {
    public FormationLauncher(EntityType<? extends FormationLauncher> type, Level level) { super(type, level); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypes.DRAGON_BREATH)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        CompoundTag data = getPersistentData();
        if (!data.getString("jinengname").equals("阳1")) return;
        data.putInt("tick", data.getInt("tick") + 1);
        if (data.getInt("tick") >= 10) {
            data.putInt("tick", 0);
            if (!level().isClientSide()) discard();
        }
        if (!data.getBoolean("jinengshifangfou")) {
            if (!level().isClientSide()) {
                FormationArrow arrow = new FormationArrow(ZhenfaSetup.<FormationArrow>entityType("projectile_zhenyang_01").get(), level());
                arrow.setOwner(this);
                arrow.launchedWith(5);
                arrow.setBaseDamage(.1);
                arrow.setSilent(true);
                arrow.setPos(getX(), getEyeY() - .1, getZ());
                arrow.shoot(getLookAngle().x, getLookAngle().y, getLookAngle().z, 1, 0);
                level().addFreshEntity(arrow);
            }
            data.putBoolean("jinengshifangfou", true);
        }
    }
}
