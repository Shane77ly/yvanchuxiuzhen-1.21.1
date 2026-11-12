package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongBehavior;

/** 蛊罐方块保留来源外形、周期放虫入口、交互与库存掉落。 */
public final class TaoguanBlock extends ContentBlock implements EntityBlock {
    private final int grade;
    private final VoxelShape shape;

    public TaoguanBlock(BlockTraits traits, int grade) {
        super(traits);
        this.grade = grade;
        shape = switch (grade) {
            case 1 -> Block.box(4, 0, 4, 12, 8, 12);
            case 2 -> Shapes.or(Block.box(2, 0, 2, 14, 5, 14), Block.box(4, 5, 4, 12, 8, 12));
            default -> Block.box(2, 0, 2, 14, 15, 14);
        };
    }

    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return shape; }
    @Override protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TaoguanBlockEntity(pos, state); }

    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        level.scheduleTick(pos, this, 20);
    }

    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        GuchongBehavior.releaseFromJar(level, pos);
        level.scheduleTick(pos, this, 20);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        GuchongBehavior.interactJar(level, pos, player, InteractionHand.MAIN_HAND);
        return InteractionResult.SUCCESS;
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        GuchongBehavior.interactJar(level, pos, player, hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) { return List.of(); }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TaoguanBlockEntity jar) {
                Containers.dropContents(level, pos, jar);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, replacement, moved);
        }
    }

    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TaoguanBlockEntity jar ? AbstractContainerMenu.getRedstoneSignalFromContainer(jar) : 0;
    }
}
