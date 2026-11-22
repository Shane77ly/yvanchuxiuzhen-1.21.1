package net.yvanchuxiuzhen.system.npc.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 门徒方块：来源只在创意潜行右键时打开任务设置页。 */
public final class QiankunmentuBlock extends ContentBlock implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 1);
    public QiankunmentuBlock(BlockTraits traits) { super(traits); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder); builder.add(ANIMATION);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new QiankunmentuBlockEntity(pos, state); }
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof QiankunmentuBlockEntity entity ? entity : null;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof QiankunmentuBlockEntity entity) entity.interact(player);
        return InteractionResult.SUCCESS;
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof QiankunmentuBlockEntity entity) entity.interact(player);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) { return List.of(); }
    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }
}
