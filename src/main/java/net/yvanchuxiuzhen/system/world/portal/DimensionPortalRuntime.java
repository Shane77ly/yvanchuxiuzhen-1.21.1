package net.yvanchuxiuzhen.system.world.portal;

import java.util.Comparator;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 六组门户共用的目标维度选择、POI查找、螺旋建门与实体传送实现。 */
public final class DimensionPortalRuntime {
    private DimensionPortalRuntime() {
    }

    public static Block portalBlock(DimensionPortalSpec spec) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, spec.portalId()));
    }

    public static Optional<BlockUtil.FoundRectangle> findPortalAround(ServerLevel level, BlockPos pos,
            boolean near, WorldBorder border, DimensionPortalSpec spec) {
        PoiManager poiManager = level.getPoiManager();
        int radius = near ? 16 : 128;
        poiManager.ensureLoadedAndValid(level, pos, radius);
        return poiManager.getInSquare(holder -> holder.is(spec.poiKey()), pos, radius, PoiManager.Occupancy.ANY)
                .filter(record -> border.isWithinBounds(record.getPos()))
                .sorted(Comparator.comparingDouble((PoiRecord record) -> record.getPos().distSqr(pos))
                        .thenComparingInt(record -> record.getPos().getY()))
                .filter(record -> level.getBlockState(record.getPos()).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                .findFirst()
                .map(record -> {
                    BlockPos portal = record.getPos();
                    level.getChunkSource().addRegionTicket(spec.ticketType(), new ChunkPos(portal), 3, portal);
                    BlockState state = level.getBlockState(portal);
                    return BlockUtil.getLargestRectangleAround(portal, state.getValue(BlockStateProperties.HORIZONTAL_AXIS), 21,
                            Direction.Axis.Y, 21, candidate -> level.getBlockState(candidate) == state);
                });
    }

    public static Optional<BlockUtil.FoundRectangle> createPortal(ServerLevel level, BlockPos pos,
            Direction.Axis axis, DimensionPortalSpec spec) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        double bestDistance = -1.0;
        BlockPos best = null;
        double fallbackDistance = -1.0;
        BlockPos fallback = null;
        WorldBorder border = level.getWorldBorder();
        int maxY = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (BlockPos.MutableBlockPos candidate : BlockPos.spiralAround(pos, 16, Direction.EAST, Direction.SOUTH)) {
            int top = Math.min(maxY, level.getHeight(Heightmap.Types.MOTION_BLOCKING, candidate.getX(), candidate.getZ()));
            if (!border.isWithinBounds(candidate) || !border.isWithinBounds(candidate.move(direction, 1))) {
                continue;
            }
            candidate.move(direction.getOpposite(), 1);
            for (int y = top; y >= level.getMinBuildHeight(); y--) {
                candidate.setY(y);
                if (!level.isEmptyBlock(candidate)) {
                    continue;
                }
                int topY = y;
                while (y > level.getMinBuildHeight() && level.isEmptyBlock(candidate.move(Direction.DOWN))) {
                    y--;
                }
                if (y + 4 > maxY) {
                    continue;
                }
                int drop = topY - y;
                if (drop > 0 && drop < 3) {
                    continue;
                }
                candidate.setY(y);
                if (!canHostFrame(level, candidate, cursor, direction, spec, 0)) {
                    continue;
                }
                double distance = pos.distSqr(candidate);
                if (canHostFrame(level, candidate, cursor, direction, spec, -1)
                        && canHostFrame(level, candidate, cursor, direction, spec, 1)
                        && (bestDistance < 0.0 || bestDistance > distance)) {
                    bestDistance = distance;
                    best = candidate.immutable();
                }
                if (bestDistance < 0.0 && (fallbackDistance < 0.0 || fallbackDistance > distance)) {
                    fallbackDistance = distance;
                    fallback = candidate.immutable();
                }
            }
        }
        if (bestDistance < 0.0 && fallbackDistance >= 0.0) {
            best = fallback;
            bestDistance = fallbackDistance;
        }
        if (bestDistance < 0.0) {
            int minY = Math.max(level.getMinBuildHeight() + 1, 70);
            int maxPortalY = maxY - 9;
            if (maxPortalY < minY) {
                return Optional.empty();
            }
            best = new BlockPos(pos.getX(), Mth.clamp(pos.getY(), minY, maxPortalY), pos.getZ());
            if (!border.isWithinBounds(best)) {
                return Optional.empty();
            }
            Direction perpendicular = direction.getClockWise();
            for (int offset = -1; offset < 2; offset++) {
                for (int width = 0; width < 2; width++) {
                    for (int height = -1; height < 3; height++) {
                        BlockState state = height < 0 ? frameState() : Blocks.AIR.defaultBlockState();
                        cursor.setWithOffset(best, width * direction.getStepX() + offset * perpendicular.getStepX(), height,
                                width * direction.getStepZ() + offset * perpendicular.getStepZ());
                        level.setBlockAndUpdate(cursor, state);
                    }
                }
            }
        }
        for (int width = -1; width < 3; width++) {
            for (int height = -1; height < 4; height++) {
                if (width == -1 || width == 2 || height == -1 || height == 3) {
                    cursor.setWithOffset(best, width * direction.getStepX(), height, width * direction.getStepZ());
                    level.setBlock(cursor, frameState(), 3);
                }
            }
        }
        BlockState portal = portalBlock(spec).defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int width = 0; width < 2; width++) {
            for (int height = 0; height < 3; height++) {
                cursor.setWithOffset(best, width * direction.getStepX(), height, width * direction.getStepZ());
                level.setBlock(cursor, portal, 18);
                level.getPoiManager().add(cursor, DimensionPortalTypes.holder(spec));
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(best.immutable(), 2, 3));
    }

    public static DimensionTransition destination(DimensionPortalSpec spec, ServerLevel source,
            Entity entity, BlockPos entryPos) {
        ResourceKey<Level> destinationKey = source.dimension().equals(spec.dimension()) ? Level.OVERWORLD : spec.dimension();
        ServerLevel destination = source.getServer().getLevel(destinationKey);
        if (destination == null) {
            return null;
        }
        WorldBorder border = destination.getWorldBorder();
        double scale = DimensionType.getTeleportationScale(source.dimensionType(), destination.dimensionType());
        BlockPos exit = BlockPos.containing(Mth.clamp(entity.getX() * scale, border.getMinX() + 16.0, border.getMaxX() - 16.0),
                entity.getY(), Mth.clamp(entity.getZ() * scale, border.getMinZ() + 16.0, border.getMaxZ() - 16.0));
        Optional<BlockUtil.FoundRectangle> portal = findPortalAround(destination, exit, false, border, spec);
        Direction.Axis axis = source.getBlockState(entryPos).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
        if (portal.isEmpty()) {
            portal = createPortal(destination, exit, axis, spec);
        }
        if (portal.isEmpty()) {
            return null;
        }
        BlockUtil.FoundRectangle rectangle = portal.get();
        BlockState sourceState = source.getBlockState(entryPos);
        Vec3 relative = sourceState.hasProperty(NetherPortalBlock.AXIS)
                ? entity.getRelativePortalPosition(axis, BlockUtil.getLargestRectangleAround(entryPos, axis, 21, Direction.Axis.Y, 21,
                        candidate -> source.getBlockState(candidate) == sourceState))
                : new Vec3(0.5, 0.0, 0.0);
        DimensionTransition.PostDimensionTransition post = DimensionTransition.PLAY_PORTAL_SOUND
                .then(DimensionTransition.PLACE_PORTAL_TICKET);
        return DimensionPortalShape.createTransition(destination, rectangle, axis, relative, entity,
                entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), post);
    }

    private static boolean canHostFrame(ServerLevel level, BlockPos original, BlockPos.MutableBlockPos cursor,
            Direction direction, DimensionPortalSpec spec, int offset) {
        Direction perpendicular = direction.getClockWise();
        for (int width = -1; width < 3; width++) {
            for (int height = -1; height < 4; height++) {
                cursor.setWithOffset(original, direction.getStepX() * width + perpendicular.getStepX() * offset, height,
                        direction.getStepZ() * width + perpendicular.getStepZ() * offset);
                if (height < 0 && !level.getBlockState(cursor).isSolid()) {
                    return false;
                }
                if (height >= 0 && !level.isEmptyBlock(cursor)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockState frameState() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tiandaodimensionblock"))
                .defaultBlockState();
    }
}
