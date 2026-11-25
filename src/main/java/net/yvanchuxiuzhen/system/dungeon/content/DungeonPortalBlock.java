package net.yvanchuxiuzhen.system.dungeon.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.system.dungeon.DungeonEntrances;
import net.yvanchuxiuzhen.system.dungeon.PaintingRuntime;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;

/** 天道副本和画卷保留各自动画范围、含水与来源交互触发。 */
public class DungeonPortalBlock extends DungeonBlock implements SimpleWaterloggedBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 1);
    public static final IntegerProperty PAINTING_ANIMATION = IntegerProperty.create("animation", 0, 2);

    public DungeonPortalBlock(BlockTraits traits) { super(traits); }
    public IntegerProperty animationProperty() { return ANIMATION; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(animationProperty());
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DungeonBlockEntity block && block.kind().equals("tiandaofuben")) {
            DungeonEntrances.initialize(block);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DungeonBlockEntity block) {
            if (block.kind().equals("tiandaofuben")) DungeonEntrances.tick(block);
            else PaintingRuntime.tick(block);
        }
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (entity instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof DungeonBlockEntity block
                && block.kind().equals("tiandaofuben")) {
            CompoundTag transientState = player.getData(DungeonData.PLAYER_TRANSIENT);
            if (!transientState.getBoolean("openfubengui")) {
                transientState.putBoolean("openfubengui", true);
                Dungeons.open(player, pos, false);
            }
        }
    }

    @Override
    protected void use(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof DungeonBlockEntity block) {
            if (block.kind().equals("fabao_009")) PaintingRuntime.use(block, serverPlayer);
            else Dungeons.open(serverPlayer, pos, player.isCreative());
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (state.getBlock() != next.getBlock() && level.getBlockEntity(pos) instanceof DungeonBlockEntity block) {
            Containers.dropContents(level, pos, block);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    public static final class Painting extends DungeonPortalBlock {
        public Painting(BlockTraits traits) { super(traits); }
        @Override public IntegerProperty animationProperty() { return PAINTING_ANIMATION; }
    }
}
