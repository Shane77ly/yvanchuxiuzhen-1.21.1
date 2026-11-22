package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 墙门家具：同向相邻自动合为双门，右键按点击面切换开关形态并联动邻门。 */
public final class WallDoorBlock extends ContentBlock {
    public WallDoorBlock(BlockTraits traits) {
        super(traits);
    }

    private static String prefix(String path) {
        if (path.equals("beechwalldoor")) return "beechwalldoor";
        int idx = path.lastIndexOf('_');
        return idx > 0 ? path.substring(0, idx) : path;
    }

    private static String closedSingleId(String pfx) {
        return pfx.equals("beechwalldoor") ? "beechwalldoor" : pfx + "_01";
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        String pfx = prefix(id);
        if (!id.equals(closedSingleId(pfx))) return;
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (level.getBlockState(pos.relative(facing.getClockWise())).is(this)) {
            level.setBlock(pos, block(pfx + "_04").defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        toggle(state, level, pos, hit.getDirection());
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        toggle(state, level, pos, hit.getDirection());
        return InteractionResult.SUCCESS;
    }

    private void toggle(BlockState state, Level level, BlockPos pos, Direction hitFace) {
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        String pfx = prefix(id);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        String nextId = nextVariant(id, pfx, facing, hitFace);
        if (nextId == null) return;
        level.setBlock(pos, block(nextId).defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        propagate(level, pos.relative(facing.getClockWise()), pfx, hitFace);
        propagate(level, pos.relative(facing.getCounterClockWise()), pfx, hitFace);
    }

    private static String nextVariant(String id, String pfx, Direction facing, Direction hitFace) {
        boolean opposite = hitFace == facing.getOpposite();
        if (id.equals(closedSingleId(pfx))) return pfx + (opposite ? "_03" : "_02");
        if (id.equals(pfx + "_02") || id.equals(pfx + "_03")) return closedSingleId(pfx);
        if (id.equals(pfx + "_04")) return pfx + (opposite ? "_06" : "_05");
        if (id.equals(pfx + "_05") || id.equals(pfx + "_06")) return pfx + "_04";
        return null;
    }

    private static void propagate(Level level, BlockPos pos, String pfx, Direction hitFace) {
        BlockState target = level.getBlockState(pos);
        String targetId = BuiltInRegistries.BLOCK.getKey(target.getBlock()).getPath();
        if (!prefix(targetId).equals(pfx) || !target.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return;
        Direction facing = target.getValue(BlockStateProperties.HORIZONTAL_FACING);
        String nextId = nextVariant(targetId, pfx, facing, hitFace);
        if (nextId != null) {
            level.setBlock(pos, block(nextId).defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        }
    }
}
