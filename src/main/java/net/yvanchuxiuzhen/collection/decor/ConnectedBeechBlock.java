package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 榉木连接家具（beechfurniture_1701 / 2501 / 26）：放置时自动抬升或按标签连接形态。 */
public final class ConnectedBeechBlock extends ContentBlock {
    private static final TagKey<Block> TAG_17 = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "beechfurnitureshiqi"));
    private static final TagKey<Block> TAG_26 = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "beechfurniturepingfeng"));

    public ConnectedBeechBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        switch (id) {
            case "beechfurniture_1701" -> place17(level, pos);
            case "beechfurniture_2501" -> place25(level, pos, facing);
            case "beechfurniture_26" -> place26(level, pos, facing);
            default -> { }
        }
    }

    private static void place17(Level level, BlockPos pos) {
        boolean any = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = pos.relative(dir);
            if (level.getBlockState(nPos).is(TAG_17)) {
                update17(level, nPos);
                any = true;
            }
        }
        if (any) update17(level, pos);
    }

    private static void update17(Level level, BlockPos pos) {
        boolean n = level.getBlockState(pos.north()).is(TAG_17);
        boolean s = level.getBlockState(pos.south()).is(TAG_17);
        boolean e = level.getBlockState(pos.east()).is(TAG_17);
        boolean w = level.getBlockState(pos.west()).is(TAG_17);
        int count = (n ? 1 : 0) + (s ? 1 : 0) + (e ? 1 : 0) + (w ? 1 : 0);
        String targetId = count >= 3 ? "beechfurniture_1704"
                : count == 2 && !((n && s) || (e && w)) ? "beechfurniture_1703"
                : count >= 1 ? "beechfurniture_1702" : "beechfurniture_1701";
        BlockState cur = level.getBlockState(pos);
        Direction facing = cur.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? cur.getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.NORTH;
        level.setBlock(pos, block(targetId).defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
    }

    private static void place25(Level level, BlockPos pos, Direction facing) {
        BlockPos above = pos.above();
        Block b2501 = block("beechfurniture_2501");
        Block b2502 = block("beechfurniture_2502");
        Direction matchDir = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState nState = level.getBlockState(above.relative(dir));
            if (nState.is(b2501)) {
                matchDir = nState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                break;
            }
        }
        BlockState placed = (matchDir != null ? b2502 : b2501).defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, matchDir != null ? matchDir : facing);
        level.setBlock(above, placed, 3);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private static void place26(Level level, BlockPos pos, Direction facing) {
        BlockPos above = pos.above();
        level.setBlock(above, block("beechfurniture_26").defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        boolean any = false;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = above.relative(dir);
            if (level.getBlockState(nPos).is(TAG_26)) {
                update26(level, nPos, facing);
                any = true;
            }
        }
        if (any) update26(level, above, facing);
    }

    private static void update26(Level level, BlockPos pos, Direction fallback) {
        boolean e = level.getBlockState(pos.east()).is(TAG_26);
        boolean w = level.getBlockState(pos.west()).is(TAG_26);
        boolean s = level.getBlockState(pos.south()).is(TAG_26);
        boolean n = level.getBlockState(pos.north()).is(TAG_26);
        String targetId = (e && w) || (s && n) ? "beechfurniture_2603"
                : (e || w || s || n) ? "beechfurniture_2602" : "beechfurniture_26";
        BlockState cur = level.getBlockState(pos);
        Direction facing = cur.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? cur.getValue(BlockStateProperties.HORIZONTAL_FACING) : fallback;
        level.setBlock(pos, block(targetId).defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id));
    }
}
