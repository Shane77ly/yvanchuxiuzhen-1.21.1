package net.yvanchuxiuzhen.collection.decor;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 蒲团放置生成独立载具，右键乘坐，玩家破坏或爆炸后移除中心载具。 */
public final class CushionBlock extends WaterloggedContentBlock {
    public CushionBlock(BlockTraits traits) { super(traits); }

    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!(level instanceof ServerLevel server)) return;
        DecorCushion cushion = new DecorCushion(DecorSetup.cushionType(), level);
        cushion.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, level.random.nextFloat() * 360, 0);
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        float rotation = direction == Direction.NORTH ? 180 : direction == Direction.EAST ? -90 : direction == Direction.WEST ? 90 : 0;
        cushion.setYRot(rotation);
        cushion.setXRot(0);
        cushion.setYBodyRot(rotation);
        cushion.setYHeadRot(rotation);
        cushion.yRotO = rotation;
        cushion.xRotO = 0;
        cushion.yBodyRotO = rotation;
        cushion.yHeadRotO = rotation;
        cushion.finalizeSpawn(server, server.getCurrentDifficultyAt(cushion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(cushion);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ride(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ride(level, pos, player);
        return InteractionResult.SUCCESS;
    }
    private static void ride(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        Vec3 center = center(pos);
        DecorCushion nearest = level.getEntitiesOfClass(DecorCushion.class, AABB.ofSize(center, 1, 1, 1)).stream()
                .min(Comparator.comparingDouble(cushion -> cushion.distanceToSqr(center))).orElse(null);
        player.startRiding(nearest);
    }

    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        boolean result = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        removeCushions(level, pos);
        return result;
    }
    @Override public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        removeCushions(level, pos);
    }
    private static void removeCushions(Level level, BlockPos pos) {
        if (!level.isClientSide()) level.getEntitiesOfClass(DecorCushion.class, AABB.ofSize(center(pos), .5, .5, .5)).forEach(DecorCushion::discard);
    }
    private static Vec3 center(BlockPos pos) { return new Vec3(pos.getX() + .5, pos.getY(), pos.getZ() + .5); }
}
