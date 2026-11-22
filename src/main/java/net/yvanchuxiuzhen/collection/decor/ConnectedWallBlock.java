package net.yvanchuxiuzhen.collection.decor;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 隔断墙方块：放置后按四向同木种标签自动连接为直墙、转角、丁字或十字形。 */
public final class ConnectedWallBlock extends ContentBlock {
    private static final Map<String, String> TAG_BY_PREFIX = Map.of(
            "sylvestriswall", "sylwall",
            "chestnutwall", "chewall",
            "beechwall", "beewall",
            "cypresswall", "cypwall",
            "toothedwall", "toowall",
            "camphorwall", "camwall",
            "nanmuwall", "nanwall",
            "ginkgowall", "ginwall");

    public ConnectedWallBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        String prefix = id.substring(0, id.lastIndexOf('_'));
        String tagName = TAG_BY_PREFIX.get(prefix);
        if (tagName == null) return;
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, tagName));
        updateSelfAndNeighbors(level, pos, prefix, tag);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos fromPos, boolean moved) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, moved);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        if (id.equals("nanmuwall_04") && !level.getBlockState(pos.above()).isAir()) {
            Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "nanmuwall_03"));
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            level.setBlock(pos, target.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        }
    }

    private static void updateSelfAndNeighbors(Level level, BlockPos pos, String prefix, TagKey<Block> tag) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = pos.relative(dir);
            if (level.getBlockState(nPos).is(tag)) updateOne(level, nPos, prefix, tag);
        }
        if (level.getBlockState(pos).is(tag)) updateOne(level, pos, prefix, tag);
    }

    private static void updateOne(Level level, BlockPos pos, String prefix, TagKey<Block> tag) {
        boolean n = level.getBlockState(pos.north()).is(tag);
        boolean s = level.getBlockState(pos.south()).is(tag);
        boolean e = level.getBlockState(pos.east()).is(tag);
        boolean w = level.getBlockState(pos.west()).is(tag);
        int count = (n ? 1 : 0) + (s ? 1 : 0) + (e ? 1 : 0) + (w ? 1 : 0);
        String suffix;
        Direction facing = Direction.NORTH;
        if (count == 0) {
            suffix = "_01";
        } else if (count == 4) {
            suffix = "_06";
        } else if (count == 3) {
            suffix = "_05";
            facing = !n ? Direction.SOUTH : !s ? Direction.NORTH : !w ? Direction.EAST : Direction.WEST;
        } else if (count == 2) {
            if (n && s) {
                suffix = "_02";
                facing = Direction.NORTH;
            } else if (e && w) {
                suffix = "_02";
                facing = Direction.EAST;
            } else {
                suffix = "_04";
                facing = s && e ? Direction.SOUTH : s && w ? Direction.WEST : n && w ? Direction.NORTH : Direction.EAST;
            }
        } else {
            suffix = "_03";
            facing = n ? Direction.SOUTH : s ? Direction.NORTH : e ? Direction.WEST : Direction.EAST;
        }
        Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, prefix + suffix));
        BlockState next = target.defaultBlockState();
        if (next.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            next = next.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        if (!level.getBlockState(pos).equals(next)) {
            level.setBlock(pos, next, 3);
        }
    }
}
