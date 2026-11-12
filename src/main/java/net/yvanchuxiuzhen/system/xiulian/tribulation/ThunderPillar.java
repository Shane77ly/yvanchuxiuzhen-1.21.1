package net.yvanchuxiuzhen.system.xiulian.tribulation;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 九霄雷柱：追随同维度的存活目标，每十刻结算近身雷伤，三百二十刻后消散；保留来源水生导航。 */
public final class ThunderPillar extends TribulationEntity {
    public ThunderPillar(EntityType<ThunderPillar> type, Level level, EntityModelRow model) {
        super(type, level, model, 4, 2);
        setPathfindingMalus(PathType.WATER, 0);
        moveControl = new PillarMoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }

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
    protected void tickTribulation() {
        Entity player = Tribulations.player(this);
        if (player == null || !player.isAlive()) {
            discard();
            return;
        }
        lookAt(EntityAnchorArgument.Anchor.EYES, player.position());
        setDeltaMovement(new Vec3(-0.5 * Math.sin(Math.toRadians(getYRot())), player.getY() > getY() ? 0.5 : -0.5 * Math.sin(Math.toRadians(getXRot())),
                0.5 * Math.cos(Math.toRadians(getYRot()))));
        Tribulations.particles(this, "thunderlightyellow", position(), 1, 1, 0, 1, 0);
        double tick = getPersistentData().getDouble("tick") + 1;
        getPersistentData().putDouble("tick", tick);
        if (tick >= 10) {
            Tribulations.damage(this, 3, "0.7", false);
            getPersistentData().putDouble("tick", 0);
        }
        double life = getPersistentData().getDouble("tick2") + 1;
        getPersistentData().putDouble("tick2", life);
        if (life >= 320) {
            Vec3 at = position().add(0, getBbHeight() / 2, 0);
            Tribulations.particles(this, "disappearlightyellow", at, 30, 0, 8, 0, 0);
            Tribulations.particles(this, "disappearyellow", at, 30, 0, 8, 0, 0);
            getPersistentData().putDouble("tick2", 0);
            discard();
        }
    }

    private static final class PillarMoveControl extends MoveControl {
        private final ThunderPillar pillar;

        private PillarMoveControl(ThunderPillar pillar) {
            super(pillar);
            this.pillar = pillar;
        }

        @Override
        public void tick() {
            if (pillar.isInWater()) pillar.setDeltaMovement(pillar.getDeltaMovement().add(0, 0.005, 0));
            if (operation == Operation.MOVE_TO && !pillar.getNavigation().isDone()) {
                double dx = wantedX - pillar.getX();
                double dy = wantedY - pillar.getY();
                double dz = wantedZ - pillar.getZ();
                float yaw = (float) Math.toDegrees(Mth.atan2(dz, dx)) - 90;
                float speed = (float) (speedModifier * pillar.getAttributeValue(Attributes.MOVEMENT_SPEED));
                pillar.setYRot(rotlerp(pillar.getYRot(), yaw, 10));
                pillar.yBodyRot = pillar.getYRot();
                pillar.yHeadRot = pillar.getYRot();
                if (pillar.isInWater()) {
                    pillar.setSpeed((float) pillar.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    float pitch = -(float) Math.toDegrees(Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)));
                    pillar.setXRot(rotlerp(pillar.getXRot(), Mth.clamp(Mth.wrapDegrees(pitch), -85, 85), 5));
                    pillar.setZza(Mth.cos((float) Math.toRadians(pillar.getXRot())) * speed);
                    pillar.setYya((float) (speed * dy));
                } else {
                    pillar.setSpeed(speed * 0.05F);
                }
            } else {
                pillar.setSpeed(0);
                pillar.setYya(0);
                pillar.setZza(0);
            }
        }
    }
}
