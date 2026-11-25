package net.yvanchuxiuzhen.system.xiulian.tribulation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.xiulian.Delayed;

/** 地滚雷：朝云写下的固定落点飞行，接近、受雨水或触地后两刻爆开，留下魂火并结算范围雷伤。 */
public final class RollingThunder extends TribulationEntity {
    private double targetX;
    private double targetY;
    private double targetZ;
    private boolean triggered;
    private double fuseTicks;

    public RollingThunder(EntityType<RollingThunder> type, Level level, EntityModelRow model) {
        super(type, level, model, 0, 2);
    }

    public void setTarget(Vec3 target) {
        targetX = target.x;
        targetY = target.y;
        targetZ = target.z;
    }

    public void arm(boolean triggered, double fuseTicks) {
        this.triggered = triggered;
        this.fuseTicks = fuseTicks;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("mubiaoX", targetX);
        tag.putDouble("mubiaoY", targetY);
        tag.putDouble("mubiaoZ", targetZ);
        tag.putBoolean("keyi", triggered);
        tag.putDouble("tick", fuseTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetX = tag.getDouble("mubiaoX");
        targetY = tag.getDouble("mubiaoY");
        targetZ = tag.getDouble("mubiaoZ");
        triggered = tag.getBoolean("keyi");
        fuseTicks = tag.getDouble("tick");
    }

    private void burstParticles() {
        Vec3 at = position().add(0, getBbHeight() / 2, 0);
        Tribulations.particles(this, "thunderpurple", at, 2, 2.5, 1, 2.5, 0);
        ((ServerLevel) level()).sendParticles(ParticleTypes.FLASH, at.x, at.y, at.z, 1, 0, 0, 0, 0);
    }

    @Override
    protected void tickTribulation() {
        Vec3 target = new Vec3(targetX, targetY, targetZ);
        lookAt(EntityAnchorArgument.Anchor.EYES, target);
        setDeltaMovement(new Vec3(-0.6 * Math.sin(Math.toRadians(getYRot())), -0.6 * Math.sin(Math.toRadians(getXRot())), 0.6 * Math.cos(Math.toRadians(getYRot()))));
        Tribulations.particles(this, "thunderpurple", position().add(0, getBbHeight() / 2, 0), 1, 0, 0, 0, 0);
        if (position().distanceTo(target) <= 2 || isInWaterRainOrBubble() || onGround()) triggered = true;
        if (!triggered) return;
        double tick = ++fuseTicks;
        if (tick < 2) return;
        if (level().isEmptyBlock(blockPosition())) level().setBlock(blockPosition(), Blocks.SOUL_FIRE.defaultBlockState(), 3);
        burstParticles();
        for (int delay = 1; delay <= 21; delay += 10) Delayed.run(delay, this::burstParticles);
        Tribulations.damage(this, 5, "2", false);
        fuseTicks = 0;
        if (level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            level().explode(null, getX(), getY(), getZ(), 4, Level.ExplosionInteraction.BLOCK);
        }
        discard();
    }
}
