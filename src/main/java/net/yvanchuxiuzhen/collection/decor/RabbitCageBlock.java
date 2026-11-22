package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 闭笼的形状和含水状态取自来源内容表，交互由服务端处理。 */
public class RabbitCageBlock extends WaterloggedContentBlock {
    private final boolean open;

    public RabbitCageBlock(BlockTraits traits) { this(traits, false); }
    protected RabbitCageBlock(BlockTraits traits, boolean open) { super(traits); this.open = open; }

    private void interact(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server) RabbitCages.interact(server, pos, player, open);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        interact(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(RabbitCages.emptyCage()) : drops;
    }
}
