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
    private int launchTick;
    private boolean fired;

    public FormationLauncher(EntityType<? extends FormationLauncher> type, Level level) { super(type, level); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("launch_tick", launchTick);
        tag.putBoolean("fired", fired);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        launchTick = tag.getInt("launch_tick");
        fired = tag.getBoolean("fired");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypes.DRAGON_BREATH)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!skillName().equals("阳1")) return;
        launchTick += 1;
        if (launchTick >= 10) {
            launchTick = 0;
            if (!level().isClientSide()) discard();
        }
        if (!fired) {
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
            fired = true;
        }
    }
}
