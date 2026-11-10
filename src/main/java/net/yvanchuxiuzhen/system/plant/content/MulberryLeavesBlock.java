package net.yvanchuxiuzhen.system.plant.content;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentLeaves;

/** 桑叶玩家破坏后按顺序独立抽取普通蚕与金蚕，静态掉落另由战利品表决定。 */
public final class MulberryLeavesBlock extends ContentLeaves {
    public MulberryLeavesBlock(BlockTraits traits) {
        super(traits);
    }

    /** 来源叶片只沿原木与叶片传递距离，不接入新增的非叶片距离属性。 */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, updateDistance(state, level, pos), 3);
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

    /** 来源叶片没有含水状态或水桶交互。目标父类的含水入口由本类关闭。 */
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

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (level instanceof ServerLevel server) {
            if (Math.random() < 0.05) spawn(server, pos, "silkwormentity");
            if (Math.random() < 0.005) spawn(server, pos, "silkwormentitygold");
        }
        return removed;
    }

    private static void spawn(ServerLevel level, BlockPos pos, String path) {
        Entity worm = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path)).create(level);
        worm.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        worm.setYBodyRot(0);
        worm.setYHeadRot(0);
        if (worm instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(worm.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        }
        level.addFreshEntity(worm);
    }
}
