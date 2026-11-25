package net.yvanchuxiuzhen.system.xiulian.tribulation;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 劫云跟随同维度渡劫玩家，逐组分派天雷；死亡与跳过分支按来源分别修改玩家状态。 */
public final class TribulationCloud extends TribulationEntity {
    private double trackingTicks;
    private double patternIndex;
    private double windupTicks;
    private double remainingCount;
    private double boltTicks;
    private double boltSecondaryTicks;
    private boolean dragonPhase;
    private final Map<Integer, String> patterns = new LinkedHashMap<>();

    public TribulationCloud(EntityType<TribulationCloud> type, Level level, EntityModelRow model) {
        super(type, level, model, 4, 20);
        moveControl = new FlyingMoveControl(this, 10, true);
        setNoGravity(true);
        refreshDimensions();
    }

    public double patternIndex() { return patternIndex; }
    public void setPatternIndex(double patternIndex) { this.patternIndex = patternIndex; }
    public double windupTicks() { return windupTicks; }
    public void setWindupTicks(double windupTicks) { this.windupTicks = windupTicks; }
    public double remainingCount() { return remainingCount; }
    public void setRemainingCount(double remainingCount) { this.remainingCount = remainingCount; }
    public double boltTicks() { return boltTicks; }
    public void setBoltTicks(double boltTicks) { this.boltTicks = boltTicks; }
    public double boltSecondaryTicks() { return boltSecondaryTicks; }
    public void setBoltSecondaryTicks(double boltSecondaryTicks) { this.boltSecondaryTicks = boltSecondaryTicks; }
    public boolean dragonPhase() { return dragonPhase; }
    public void setDragonPhase(boolean dragonPhase) { this.dragonPhase = dragonPhase; }

    public String pattern(int index) {
        return patterns.getOrDefault(index, "");
    }

    public void setPattern(int index, String type) {
        if (type == null || type.isEmpty()) patterns.remove(index);
        else patterns.put(index, type);
    }

    public void removePattern(int index) {
        patterns.remove(index);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("zhuizongtick", trackingTicks);
        tag.putDouble("thundernum", patternIndex);
        tag.putDouble("qianyao", windupTicks);
        tag.putDouble("number", remainingCount);
        tag.putDouble("leipitick", boltTicks);
        tag.putDouble("leipitick2", boltSecondaryTicks);
        tag.putBoolean("jieduan", dragonPhase);
        patterns.forEach((index, type) -> tag.putString("thunder" + index, type));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        trackingTicks = tag.getDouble("zhuizongtick");
        patternIndex = tag.getDouble("thundernum");
        windupTicks = tag.getDouble("qianyao");
        remainingCount = tag.getDouble("number");
        boltTicks = tag.getDouble("leipitick");
        boltSecondaryTicks = tag.getDouble("leipitick2");
        dragonPhase = tag.getBoolean("jieduan");
        patterns.clear();
        for (String key : tag.getAllKeys()) {
            if (key.startsWith("thunder") && !key.equals("thunder_Att") && !key.equals("thundernum")) {
                try {
                    patterns.put(Integer.parseInt(key.substring("thunder".length())), tag.getString(key));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) { return super.getDefaultDimensions(pose).scale(3); }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return super.getPassengerAttachmentPoint(passenger, dimensions, partialTick).add(0, 10, 0);
    }

    @Override
    public boolean isPushedByFluid(FluidType type) { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    protected void pushEntities() { }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos position) { }

    @Override
    public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypes.LIGHTNING_BOLT) && super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) {
            ServerPlayer player = Tribulations.player(this);
            if (player != null) {
                Cultivation.setThunderOpen(player, false);
                Cultivation.set(player, CultivationValue.THUNDER_MAX, 0);
                Cultivation.add(player, CultivationValue.THUNDER, 1);
            }
        }
    }

    static void success(ServerPlayer player) {
        player.displayClientMessage(Component.literal("§6渡劫成功！"), false);
        Cultivation.setTizhiBoolean(player, "jinji", true);
        Cultivation.set(player, CultivationValue.THUNDER_MAX, 0);
        Cultivation.setThunderOpen(player, false);
    }

    @Override
    protected void tickTribulation() {
        ServerLevel level = (ServerLevel) level();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 100, 30, 5, 30, 0);
        ServerPlayer player = Tribulations.player(this);
        if (++trackingTicks >= 5) {
            if (player == null) {
                discard();
            } else if (!player.isAlive()) {
                // 来源的 max 与 thunder 写入了云的玩家能力对象，实际只清玩家开关。
                Cultivation.setThunderOpen(player, false);
                discard();
            } else if (Cultivation.tizhiBoolean(player, "tiaoguo")) {
                Cultivation.setTizhiBoolean(player, "tiaoguo", false);
                success(player);
            }
            trackingTicks = 0;
        }
        if (player != null) teleportTo(player.getX(), player.getY() + 64, player.getZ());
        if (!ThunderPatterns.current(this).isEmpty()) {
            ThunderPatterns.tick(this);
        } else {
            if (player != null) success(player);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY(), getZ(), 100, 3, 3, 3, 1);
            discard();
        }
    }
}
