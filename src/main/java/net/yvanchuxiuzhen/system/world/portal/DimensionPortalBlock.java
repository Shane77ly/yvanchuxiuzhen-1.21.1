package net.yvanchuxiuzhen.system.world.portal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 天道六组门户共用的方块入口；每个实例仅由内容表 ID选择目标维度与对应 POI。 */
public final class DimensionPortalBlock extends NetherPortalBlock {
    private final DimensionPortalSpec spec;

    public DimensionPortalBlock(BlockTraits traits) {
        super(traits.properties());
        this.spec = DimensionPortalSpec.fromPortalId(traits.row().id());
    }

    public static boolean portalSpawn(Level level, BlockPos pos, DimensionPortalSpec spec) {
        return DimensionPortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X, spec)
                .map(shape -> {
                    shape.createPortalBlocks();
                    return true;
                }).orElse(false);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState facingState,
            LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis neighborAxis = direction.getAxis();
        Direction.Axis portalAxis = state.getValue(AXIS);
        boolean perpendicular = portalAxis != neighborAxis && neighborAxis.isHorizontal();
        return !perpendicular && !facingState.is(this)
                && !new DimensionPortalShape(level, currentPos, portalAxis, spec).isComplete()
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, facingState, level, currentPos, facingPos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 来源门户禁用随机刻，不生成僵尸猪人。
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 4; i++) {
            double px = pos.getX() + random.nextFloat();
            double py = pos.getY() + random.nextFloat();
            double pz = pos.getZ() + random.nextFloat();
            double vx = (random.nextFloat() - 0.5) / 2.0;
            double vy = (random.nextFloat() - 0.5) / 2.0;
            double vz = (random.nextFloat() - 0.5) / 2.0;
            int offset = random.nextInt(4) - 1;
            if (!level.getBlockState(pos.west()).is(this) && !level.getBlockState(pos.east()).is(this)) {
                px = pos.getX() + 0.5 + 0.25 * offset;
                vx = random.nextFloat() * 2 * offset;
            } else {
                pz = pos.getZ() + 0.5 + 0.25 * offset;
                vz = random.nextFloat() * 2 * offset;
            }
            level.addParticle(ParticleTypes.CLOUD, px, py, pz, vx, vy, vz);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.isVehicle() && entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 0;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        return DimensionPortalRuntime.destination(spec, level, entity, pos);
    }

    @Override
    public Portal.Transition getLocalTransition() {
        return Portal.Transition.CONFUSION;
    }

    public DimensionPortalSpec spec() {
        return spec;
    }
}
