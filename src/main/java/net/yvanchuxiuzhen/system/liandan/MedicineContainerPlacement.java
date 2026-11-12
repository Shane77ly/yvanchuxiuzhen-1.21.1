package net.yvanchuxiuzhen.system.liandan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.MedicineContainerBlockEntity;

/** 药瓶放置与拾回只搬移物品实例，服用和容器身份转换由药效入口处理。 */
public final class MedicineContainerPlacement {
    private MedicineContainerPlacement() {
    }

    /** 潜行点击上表面时放入一件原实例，来源同类双手持物优先从副手扣除。 */
    public static void place(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().above();
        if (level.isClientSide() || player == null || context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos)) return;
        ItemStack stack = context.getItemInHand();
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() + "block";
        BlockState state = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)).defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, player.getDirection().getOpposite());
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof MedicineContainerBlockEntity bottle) {
            bottle.setItem(0, stack.copyWithCount(1));
        }
        InteractionHand hand = player.getOffhandItem().is(stack.getItem()) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack held = player.getItemInHand(hand);
        held.shrink(1);
        player.setItemInHand(hand, held);
        player.getInventory().setChanged();
    }

    public static void pickUp(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;
        if (level.getBlockEntity(pos) instanceof MedicineContainerBlockEntity bottle) {
            ItemHandlerHelper.giveItemToPlayer(player, bottle.getItem(0).copyWithCount(1));
            bottle.setItem(0, ItemStack.EMPTY);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }
}
