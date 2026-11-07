package net.yvanchuxiuzhen.system.xiulian.tribulation;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.xiulian.Delayed;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.RawAnimation;

/** 化灵雷龙：徘徊、下坠与范围终结按来源互斥分支推进；死亡粒子独立于终结伤害。 */
public final class ThunderDragon extends TribulationEntity {
    public ThunderDragon(EntityType<ThunderDragon> type, Level level, EntityModelRow model) {
        super(type, level, model, 0, 1);
        refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) { return super.getDefaultDimensions(pose).scale(2); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getMsgId().equals("trident") || source.is(DamageTypes.FALLING_ANVIL)
                || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) Tribulations.particles(this, "brokenyellow", position(), 40, 0, 0, 0, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(movementController().triggerableAnim("thunder", RawAnimation.begin().thenPlay("animation.model.thunder")));
    }

    @Override
    protected void tickTribulation() {
        Entity player = Tribulations.player(this);
        if (player == null) {
            discard();
            return;
        }
        double tick = getPersistentData().getDouble("entitytick") + 1;
        getPersistentData().putDouble("entitytick", tick);
        String stage = getPersistentData().getString("jinengjinxing");
        if (stage.isEmpty()) {
            getPersistentData().putString("jinengjinxing", "徘徊");
            triggerAnim("main", "thunder");
        } else if (stage.equals("徘徊") && tick >= 40) {
            getPersistentData().putString("jinengjinxing", "开始下坠");
        } else if (stage.equals("开始下坠") && tick >= 160) {
            getPersistentData().putString("jinengjinxing", "结束");
        } else if (stage.equals("结束")) {
            Tribulations.particles(this, "brokenyellow", position(), 40, 0, 0, 0, 1);
            Tribulations.damage(this, 12, "5", true);
            for (int delay = 1; delay <= 26; delay += 5) Delayed.run(delay, this::finishParticles);
            discard();
        } else if (stage.equals("徘徊")) {
            teleportTo(player.getX(), player.getY() + 36, player.getZ());
        } else if (stage.equals("开始下坠")) {
            Tribulations.particles(this, "thunderyellow", position(), 1, 2, 0, 2, 0);
            setDeltaMovement(new Vec3((player.getX() - getX()) * 0.5, -1, (player.getZ() - getZ()) * 0.5));
            if (getY() - player.getY() <= 2) {
                getPersistentData().putString("jinengjinxing", "结束");
            } else if (onGround() && level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                level().explode(null, getX(), getY(), getZ(), 4, Level.ExplosionInteraction.BLOCK);
            }
        }
    }

    private void finishParticles() {
        Tribulations.particles(this, "thunderyellow", position(), 10, 6, 0, 6, 0);
        Tribulations.particles(this, "thundergolden", position(), 10, 6, 0, 6, 0);
    }
}
