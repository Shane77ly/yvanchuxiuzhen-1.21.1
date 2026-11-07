package net.yvanchuxiuzhen.system.skill.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

/** 来源九种水中导航技能实体共用的移动控制：水中微升，按目标俯仰推进，离水后使用二十分之一的移动速度。 */
final class SkillWaterMoveControl extends MoveControl {
    SkillWaterMoveControl(SkillEntity entity) {
        super(entity);
    }

    @Override
    public void tick() {
        if (mob.isInWater()) {
            mob.setDeltaMovement(mob.getDeltaMovement().add(0, 0.005, 0));
        }
        if (operation == Operation.MOVE_TO && !mob.getNavigation().isDone()) {
            double dx = wantedX - mob.getX();
            double dy = wantedY - mob.getY();
            double dz = wantedZ - mob.getZ();
            float yaw = (float) (Mth.atan2(dz, dx) * (180 / Math.PI)) - 90;
            float speed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            mob.setYRot(rotlerp(mob.getYRot(), yaw, 10));
            mob.yBodyRot = mob.getYRot();
            mob.yHeadRot = mob.getYRot();
            if (mob.isInWater()) {
                mob.setSpeed((float) mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float pitch = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
                pitch = Mth.clamp(Mth.wrapDegrees(pitch), -85, 85);
                mob.setXRot(rotlerp(mob.getXRot(), pitch, 5));
                float horizontal = Mth.cos(mob.getXRot() * (float) (Math.PI / 180));
                mob.setZza(horizontal * speed);
                mob.setYya((float) (speed * dy));
            } else {
                mob.setSpeed(speed * 0.05F);
            }
        } else {
            mob.setSpeed(0);
            mob.setYya(0);
            mob.setZza(0);
        }
    }
}
