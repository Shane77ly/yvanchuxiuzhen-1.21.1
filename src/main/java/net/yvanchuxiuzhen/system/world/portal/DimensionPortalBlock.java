package net.yvanchuxiuzhen.system.world.portal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
