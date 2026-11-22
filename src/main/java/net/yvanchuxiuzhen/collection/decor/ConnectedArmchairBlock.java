package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 扶手椅方块：相邻两垂直方向均有同类椅子时自动变为转角形态（_02）。 */
public final class ConnectedArmchairBlock extends ContentBlock {
    public ConnectedArmchairBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        String prefix = id.substring(0, id.lastIndexOf('_'));
        Block chair01 = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + "_01"));
        Block chair02 = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + "_02"));
        updateCorner(level, pos, chair01, chair02);
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = pos.relative(dir);
            if (level.getBlockState(nPos).is(chair01)) {
                updateCorner(level, nPos, chair01, chair02);
            }
        }
    }

    private static boolean isChair(Level level, BlockPos pos, Block c1, Block c2) {
        BlockState s = level.getBlockState(pos);
        return s.is(c1) || s.is(c2);
    }

    private static void updateCorner(Level level, BlockPos pos, Block c1, Block c2) {
        boolean e = isChair(level, pos.east(), c1, c2);
        boolean w = isChair(level, pos.west(), c1, c2);
        boolean s = isChair(level, pos.south(), c1, c2);
        boolean n = isChair(level, pos.north(), c1, c2);
        Direction facing = null;
        if (e && s) facing = Direction.SOUTH;
        else if (w && s) facing = Direction.WEST;
        else if (w && n) facing = Direction.NORTH;
        else if (e && n) facing = Direction.EAST;
        if (facing != null) {
            level.setBlock(pos, c2.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        }
    }
}
