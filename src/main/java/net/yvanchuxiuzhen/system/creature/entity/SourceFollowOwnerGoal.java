package net.yvanchuxiuzhen.system.creature.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/** 保留1.18.2环境生物的主人跟随门槛、远距传送与随机取样顺序。 */
public final class SourceFollowOwnerGoal extends Goal {
    private final TamableAnimal animal;
    private final PathNavigation navigation;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private final boolean canFly;
    private LivingEntity owner;
    private int recalculateTicks;
    private float oldWaterCost;

    public SourceFollowOwnerGoal(TamableAnimal animal, double speed, float startDistance, float stopDistance, boolean canFly) {
        this.animal = animal;
        this.navigation = animal.getNavigation();
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.canFly = canFly;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(navigation instanceof GroundPathNavigation) && !(navigation instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = animal.getOwner();
        if (candidate == null || candidate.isSpectator() || animal.isOrderedToSit()
                || animal.distanceToSqr(candidate) < startDistance * startDistance) {
            return false;
        }
        owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !navigation.isDone() && !animal.isOrderedToSit() && animal.distanceToSqr(owner) > stopDistance * stopDistance;
    }

    @Override
    public void start() {
        recalculateTicks = 0;
        oldWaterCost = animal.getPathfindingMalus(PathType.WATER);
        animal.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    public void stop() {
        owner = null;
        navigation.stop();
        animal.setPathfindingMalus(PathType.WATER, oldWaterCost);
    }

    @Override
    public void tick() {
        animal.getLookControl().setLookAt(owner, 10, animal.getMaxHeadXRot());
        if (--recalculateTicks <= 0) {
            recalculateTicks = adjustedTickDelay(10);
            if (!animal.isLeashed() && !animal.isPassenger()) {
                if (animal.distanceToSqr(owner) >= 144) {
                    teleportToOwner();
                } else {
                    navigation.moveTo(owner, speed);
                }
            }
        }
    }

    private void teleportToOwner() {
        BlockPos origin = owner.blockPosition();
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = origin.getX() + animal.getRandom().nextInt(7) - 3;
            int y = origin.getY() + animal.getRandom().nextInt(3) - 1;
            int z = origin.getZ() + animal.getRandom().nextInt(7) - 3;
            if (Math.abs(x - owner.getX()) < 2 && Math.abs(z - owner.getZ()) < 2) {
                continue;
            }
            BlockPos target = new BlockPos(x, y, z);
            if (WalkNodeEvaluator.getPathTypeStatic(animal, target) != PathType.WALKABLE
                    || !canFly && animal.level().getBlockState(target.below()).getBlock() instanceof LeavesBlock) {
                continue;
            }
            BlockPos offset = target.subtract(animal.blockPosition());
            if (animal.level().noCollision(animal, animal.getBoundingBox().move(offset))) {
                animal.moveTo(x + 0.5, y, z + 0.5, animal.getYRot(), animal.getXRot());
                navigation.stop();
                return;
            }
        }
    }
}
