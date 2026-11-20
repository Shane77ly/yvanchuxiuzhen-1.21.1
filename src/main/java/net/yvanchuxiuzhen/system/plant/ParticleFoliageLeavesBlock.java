package net.yvanchuxiuzhen.system.plant;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentLeaves;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 按来源叶距生命周期与周期广播银杏、白杨叶粒子。 */
public final class ParticleFoliageLeavesBlock extends ContentLeaves {
    private static final int UPDATE_INTERVAL = 20;
    private final String particleId;

    public ParticleFoliageLeavesBlock(BlockTraits traits, String particleId) {
        super(traits);
        this.particleId = particleId;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, UPDATE_INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, updateDistance(state, level, pos), 3);
        if (level.isEmptyBlock(pos.below()) && Math.random() < 0.2) {
            level.sendParticles(Particles.type(particleId), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    1, 2, 0, 2, 0);
        }
        level.scheduleTick(pos, this, UPDATE_INTERVAL);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        int distance = distanceAt(neighbor) + 1;
        if (distance != 1 || state.getValue(DISTANCE) != distance) level.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateDistance(defaultBlockState().setValue(PERSISTENT, true), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
        return false;
    }

    @Override
    public ItemStack pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    private static BlockState updateDistance(BlockState state, LevelAccessor level, BlockPos pos) {
        int distance = 7;
        for (Direction direction : Direction.values()) {
            distance = Math.min(distance, distanceAt(level.getBlockState(pos.relative(direction))) + 1);
            if (distance == 1) break;
        }
        return state.setValue(DISTANCE, distance);
    }

    private static int distanceAt(BlockState state) {
        if (state.is(BlockTags.LOGS)) return 0;
        return state.getBlock() instanceof LeavesBlock ? state.getValue(DISTANCE) : 7;
    }
}
