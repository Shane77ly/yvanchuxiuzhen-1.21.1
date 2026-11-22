package net.yvanchuxiuzhen.system.treasure.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;
import net.yvanchuxiuzhen.system.treasure.TreasureCaskets;

/** 宝匣的原生方块右键执行来源指令；移除时掉落尚存单槽内容。 */
public final class TreasureCasketBlock extends WaterloggedContentBlock implements EntityBlock {
    public TreasureCasketBlock(BlockTraits traits) {
        super(traits);
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TreasureCasketBlockEntity(pos, state); }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        TreasureCaskets.use(level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        TreasureCaskets.use(level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity casket = level.getBlockEntity(pos);
        return casket != null && casket.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TreasureCasketBlockEntity casket) {
            Containers.dropContents(level, pos, casket);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
