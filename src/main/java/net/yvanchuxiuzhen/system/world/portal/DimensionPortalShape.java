package net.yvanchuxiuzhen.system.world.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 以天道维度方块为框架的 2×3 至 21×21 门形检查与门户内框生成。 */
public final class DimensionPortalShape {
    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 21;

    private final LevelAccessor level;
    private final DimensionPortalSpec spec;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<DimensionPortalShape> findEmptyPortalShape(LevelAccessor level, BlockPos pos,
            Direction.Axis axis, DimensionPortalSpec spec) {
        return findPortalShape(level, pos, shape -> shape.isValid() && shape.numPortalBlocks == 0, axis, spec);
    }

    public static Optional<DimensionPortalShape> findPortalShape(LevelAccessor level, BlockPos pos,
            Predicate<DimensionPortalShape> predicate, Direction.Axis axis, DimensionPortalSpec spec) {
        Optional<DimensionPortalShape> optional = Optional.of(new DimensionPortalShape(level, pos, axis, spec)).filter(predicate);
        if (optional.isPresent()) {
            return optional;
        }
        Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        return Optional.of(new DimensionPortalShape(level, pos, other, spec)).filter(predicate);
    }

    public DimensionPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis, DimensionPortalSpec spec) {
        this.level = level;
        this.spec = spec;
        this.axis = axis;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = calculateBottomLeft(pos);
        if (this.bottomLeft == null) {
            this.bottomLeft = pos;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = calculateWidth();
            if (this.width > 0) {
                this.height = calculateHeight();
            }
        }
    }

    private BlockPos calculateBottomLeft(BlockPos pos) {
        for (int minY = Math.max(level.getMinBuildHeight(), pos.getY() - MAX_HEIGHT);
                pos.getY() > minY && isEmpty(level.getBlockState(pos.below()), spec); pos = pos.below()) {
            // 来源向下寻找框架底边，不提前限制横向范围。
        }
        Direction direction = rightDir.getOpposite();
        int distance = getDistanceUntilEdgeAboveFrame(pos, direction) - 1;
        return distance < 0 ? null : pos.relative(direction, distance);
    }

    private int calculateWidth() {
        int distance = getDistanceUntilEdgeAboveFrame(bottomLeft, rightDir);
        return distance >= MIN_WIDTH && distance <= MAX_WIDTH ? distance : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 0; distance <= MAX_WIDTH; distance++) {
            cursor.set(pos).move(direction, distance);
            BlockState state = level.getBlockState(cursor);
            if (!isEmpty(state, spec)) {
                return isFrame(state) ? distance : 0;
            }
            BlockState below = level.getBlockState(cursor.move(Direction.DOWN));
            if (!isFrame(below)) {
                return 0;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int distance = getDistanceUntilTop(cursor);
        return distance >= MIN_HEIGHT && distance <= MAX_HEIGHT && hasTopFrame(cursor, distance) ? distance : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos cursor, int distanceToTop) {
        for (int i = 0; i < width; i++) {
            cursor.set(bottomLeft).move(Direction.UP, distanceToTop).move(rightDir, i);
            if (!isFrame(level.getBlockState(cursor))) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos cursor) {
        for (int distance = 0; distance < MAX_HEIGHT; distance++) {
            cursor.set(bottomLeft).move(Direction.UP, distance).move(rightDir, -1);
            if (!isFrame(level.getBlockState(cursor))) {
                return distance;
            }
            cursor.set(bottomLeft).move(Direction.UP, distance).move(rightDir, width);
            if (!isFrame(level.getBlockState(cursor))) {
                return distance;
            }
            for (int i = 0; i < width; i++) {
                cursor.set(bottomLeft).move(Direction.UP, distance).move(rightDir, i);
                BlockState state = level.getBlockState(cursor);
                if (!isEmpty(state, spec)) {
                    return distance;
                }
                if (state.is(DimensionPortalRuntime.portalBlock(spec))) {
                    numPortalBlocks++;
                }
            }
        }
        return MAX_HEIGHT;
    }

    private boolean isFrame(BlockState state) {
        Block frame = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaodimensionblock"));
        return state.is(frame);
    }

    private static boolean isEmpty(BlockState state, DimensionPortalSpec spec) {
        return state.isAir() || state.is(DimensionPortalRuntime.portalBlock(spec));
    }

    public boolean isValid() {
        return bottomLeft != null && width >= MIN_WIDTH && width <= MAX_WIDTH && height >= MIN_HEIGHT && height <= MAX_HEIGHT;
    }

    public void createPortalBlocks() {
        BlockState state = DimensionPortalRuntime.portalBlock(spec).defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        BlockPos.betweenClosed(bottomLeft, bottomLeft.relative(Direction.UP, height - 1).relative(rightDir, width - 1))
                .forEach(pos -> {
                    level.setBlock(pos, state, 18);
                    if (level instanceof ServerLevel server) {
                        server.getPoiManager().add(pos, DimensionPortalTypes.holder(spec));
                    }
                });
    }

    public boolean isComplete() {
        return isValid() && numPortalBlocks == width * height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle rectangle, Direction.Axis axis, Vec3 pos,
            EntityDimensions dimensions) {
        double width = rectangle.axis1Size - dimensions.width();
        double height = rectangle.axis2Size - dimensions.height();
        BlockPos min = rectangle.minCorner;
        double x = width > 0.0 ? Mth.clamp(Mth.inverseLerp(pos.get(axis) - (min.get(axis) + dimensions.width() / 2.0), 0.0, width), 0.0, 1.0) : 0.5;
        double y = height > 0.0 ? Mth.clamp(Mth.inverseLerp(pos.y - min.getY(), 0.0, height), 0.0, 1.0) : 0.0;
        Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double z = pos.get(other) - (min.get(other) + 0.5);
        return new Vec3(x, y, z);
    }

    public static DimensionTransition createTransition(ServerLevel level, BlockUtil.FoundRectangle rectangle,
            Direction.Axis sourceAxis, Vec3 offset, Entity entity, Vec3 speed,
            float yRot, float xRot, DimensionTransition.PostDimensionTransition postTransition) {
        BlockPos min = rectangle.minCorner;
        BlockState state = level.getBlockState(min);
        Direction.Axis destinationAxis = state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double width = rectangle.axis1Size;
        double height = rectangle.axis2Size;
        EntityDimensions dimensions = entity.getDimensions(entity.getPose());
        int rotation = sourceAxis == destinationAxis ? 0 : 90;
        Vec3 destinationSpeed = sourceAxis == destinationAxis ? speed : new Vec3(speed.z, speed.y, -speed.x);
        double x = dimensions.width() / 2.0 + (width - dimensions.width()) * offset.x();
        double y = (height - dimensions.height()) * offset.y();
        double z = 0.5 + offset.z();
        boolean alongX = destinationAxis == Direction.Axis.X;
        Vec3 destination = new Vec3(min.getX() + (alongX ? x : z), min.getY() + y, min.getZ() + (alongX ? z : x));
        return new DimensionTransition(level, destination, destinationSpeed, yRot + rotation, xRot, postTransition);
    }
}
