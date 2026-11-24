package net.yvanchuxiuzhen.system.dungeon.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.dungeon.DungeonPlayers;
import net.yvanchuxiuzhen.system.dungeon.PaintingRuntime;
import net.yvanchuxiuzhen.system.dungeon.data.PaintingState;

/** 私人画卷按点击面直接放置，主手整栈移入来源单槽并登记主人和本端坐标。 */
public final class PaintingItem extends ContentItem {
    public PaintingItem(Properties properties, List<Component> tooltip) { super(properties, tooltip); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !context.getLevel().isClientSide && BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath().equals("fabao_00901")) {
            ItemStack painting = player.getMainHandItem().copy();
            BlockPos pos = context.getClickedFace() == Direction.UP ? context.getClickedPos().above(2) : context.getClickedPos().relative(context.getClickedFace());
            BlockState state = DungeonPlayers.block("fabao_009").defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, player.getDirection().getOpposite());
            context.getLevel().setBlock(pos, state, 3);
            PaintingState data = PaintingRuntime.state(painting)
                    .withPlacement(player.level().dimension().equals(DungeonPlayers.PAINTING), pos, player.getStringUUID());
            PaintingRuntime.save(painting, data);
            if (context.getLevel().getBlockEntity(pos) instanceof DungeonBlockEntity block) {
                block.setItem(0, painting.copyWithCount(1));
                block.changed();
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.getInventory().setChanged();
        }
        return InteractionResult.SUCCESS;
    }
}
