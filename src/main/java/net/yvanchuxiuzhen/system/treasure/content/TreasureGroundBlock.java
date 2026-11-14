package net.yvanchuxiuzhen.system.treasure.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.treasure.TreasureInteractions;

/** 地面藏宝通过原生方块使用入口消费，保留潜行与物品优先使用的调用顺序。 */
public final class TreasureGroundBlock extends ContentBlock {
    public TreasureGroundBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        TreasureInteractions.use(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        TreasureInteractions.use(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }
}
