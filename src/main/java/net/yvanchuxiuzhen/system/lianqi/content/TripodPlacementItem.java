package net.yvanchuxiuzhen.system.lianqi.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 重阳金鼎物品在所点顶面上方第二格直接放鼎，并按来源清空匹配的整只手槽。 */
public final class TripodPlacementItem extends ContentItem {
    public TripodPlacementItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && context.getClickedFace() == Direction.UP && !context.getLevel().isClientSide()) {
            ItemStack used = context.getItemInHand();
            InteractionHand hand = used.getItem() == player.getMainHandItem().getItem() ? InteractionHand.MAIN_HAND
                    : used.getItem() == player.getOffhandItem().getItem() ? InteractionHand.OFF_HAND : null;
            if (hand != null) {
                BlockPos pos = context.getClickedPos().above(2);
                Block tripod = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "tripod_08"));
                context.getLevel().setBlock(pos, tripod.defaultBlockState(), Block.UPDATE_ALL);
                context.getLevel().setBlock(pos, context.getLevel().getBlockState(pos)
                        .setValue(BlockStateProperties.HORIZONTAL_FACING, player.getDirection().getOpposite()), Block.UPDATE_ALL);
                player.setItemInHand(hand, ItemStack.EMPTY);
                player.getInventory().setChanged();
            }
        }
        return InteractionResult.SUCCESS;
    }
}
