package net.yvanchuxiuzhen.collection.decor;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 榉木沙发（beechfurniture_1601..1604）：放置自动连接形态，右键乘坐载具，破坏移除。 */
public final class SofaBlock extends ContentBlock {
    private static final TagKey<Block> SOFA_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "beechfurnitureshiliu"));

    public SofaBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(block("beechfurniture_1601"))) : drops;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel server)) return;
        boolean updated = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = pos.relative(dir);
            if (server.getBlockState(nPos).is(SOFA_TAG)) {
                updateSofa(server, nPos);
                updated = true;
            }
        }
        if (updated) {
            updateSofa(server, pos);
        } else {
            spawnSeat(server, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }
    }

    private static void updateSofa(ServerLevel level, BlockPos pos) {
        removeSeats(level, pos);
        boolean e = level.getBlockState(pos.east()).is(SOFA_TAG);
        boolean w = level.getBlockState(pos.west()).is(SOFA_TAG);
        boolean s = level.getBlockState(pos.south()).is(SOFA_TAG);
        boolean n = level.getBlockState(pos.north()).is(SOFA_TAG);
        BlockState current = level.getBlockState(pos);
        Direction facing = current.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? current.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        if (e && w) {
            Direction nextDir = facingOf(level.getBlockState(pos.east()), facing);
            if (!current.is(block("beechfurniture_1604"))) {
                level.setBlock(pos, block("beechfurniture_1604").defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, nextDir), 3);
            }
            return;
        }
        if (s && n) {
            Direction nextDir = facingOf(level.getBlockState(pos.south()), facing);
            if (!current.is(block("beechfurniture_1604"))) {
                level.setBlock(pos, block("beechfurniture_1604").defaultBlockState()
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, nextDir), 3);
            }
            return;
        }
        spawnSeat(level, pos, facing);
    }

    private static Direction facingOf(BlockState state, Direction fallback) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : fallback;
    }

    private static void spawnSeat(ServerLevel level, BlockPos pos, Direction direction) {
        DecorSeat seat = new DecorSeat(DecorSetup.seatType(), level);
        seat.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360F, 0F);
        float rotation = switch (direction) {
            case NORTH -> 180F;
            case EAST -> -90F;
            case WEST -> 90F;
            default -> 0F;
        };
        seat.setYRot(rotation);
        seat.setXRot(0F);
        seat.setYBodyRot(rotation);
        seat.setYHeadRot(rotation);
        seat.yRotO = rotation;
        seat.xRotO = 0F;
        seat.yBodyRotO = rotation;
        seat.yHeadRotO = rotation;
        seat.finalizeSpawn(level, level.getCurrentDifficultyAt(seat.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(seat);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ride(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ride(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void ride(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        DecorSeat nearest = level.getEntitiesOfClass(DecorSeat.class, AABB.ofSize(center, 1, 1, 1)).stream()
                .min(Comparator.comparingDouble(seat -> seat.distanceToSqr(center))).orElse(null);
        if (nearest != null) player.startRiding(nearest);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        boolean result = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        removeSeats(level, pos);
        return result;
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        removeSeats(level, pos);
    }

    private static void removeSeats(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            level.getEntitiesOfClass(DecorSeat.class, AABB.ofSize(center, 0.5, 0.5, 0.5)).forEach(DecorSeat::discard);
        }
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }
}
