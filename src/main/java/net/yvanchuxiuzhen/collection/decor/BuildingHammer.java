package net.yvanchuxiuzhen.collection.decor;

import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.platform.ModId;

/** 建筑锤升降来源瓦片的两组四段形态，并移动硬度不高于10的其他方块状态。 */
final class BuildingHammer {
    private static final TagKey<Block> ROOF_TILES = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "wapian"));

    private BuildingHammer() { }

    static void leftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (event.getLevel().isClientSide() || event.getHand() != player.getUsedItemHand()
                || !(player.getMainHandItem().getItem() instanceof BuildingHammerItem)) return;
        shift(event.getLevel(), event.getPos(), false);
    }

    static void raise(Level level, BlockPos pos) {
        shift(level, pos, true);
    }

    private static void shift(Level level, BlockPos pos, boolean upward) {
        BlockState original = level.getBlockState(pos);
        if (original.is(ROOF_TILES)) {
            shiftTile(level, pos, original, upward);
        } else if (original.getDestroySpeed(level, pos) <= 10) {
            BlockPos destination = upward ? pos.above() : pos.below();
            if (upward && !level.isEmptyBlock(destination)) return;
            // 来源仅搬移方块状态；下降覆盖目标，方块实体由原生移除与创建生命周期处理。
            BlockState moved = upward ? copyProperties(original, level.getBlockState(destination)) : original;
            level.setBlock(destination, moved, Block.UPDATE_ALL);
            orient(level, destination, level.getBlockState(pos));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static void shiftTile(Level level, BlockPos pos, BlockState original, boolean upward) {
        String id = BuiltInRegistries.BLOCK.getKey(original.getBlock()).toString();
        for (int form = 1; form <= 8; form++) {
            String digit = Integer.toString(form);
            if (!id.contains(digit)) continue;
            boolean moving = upward ? form == 4 || form == 8 : form == 1 || form == 5;
            BlockPos destination = pos;
            if (moving) destination = upward ? pos.above() : pos.below();
            if (moving && !level.isEmptyBlock(destination)) return;
            int next = upward ? form + 1 : form - 1;
            if (moving) next = upward ? form - 3 : form + 3;
            ResourceLocation replacement = ResourceLocation.parse(id.replace(digit, Integer.toString(next)).toLowerCase(Locale.ENGLISH));
            BlockState state = BuiltInRegistries.BLOCK.get(replacement).defaultBlockState();
            if (upward) state = copyProperties(state, level.getBlockState(destination));
            level.setBlock(destination, state, Block.UPDATE_ALL);
            orient(level, destination, original);
            if (moving) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
    }

    private static BlockState copyProperties(BlockState state, BlockState previous) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : previous.getValues().entrySet()) {
            Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
            if (property != null) state = copyAcceptedValue(state, property, entry.getValue());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState copyAcceptedValue(BlockState state, Property<T> property, Comparable<?> value) {
        for (T candidate : property.getPossibleValues()) {
            if (candidate.equals(value)) return state.setValue(property, candidate);
        }
        return state;
    }

    private static void orient(Level level, BlockPos pos, BlockState original) {
        Direction direction = direction(original);
        BlockState state = level.getBlockState(pos);
        Property<?> property = state.getBlock().getStateDefinition().getProperty("facing");
        if (property instanceof DirectionProperty facing && facing.getPossibleValues().contains(direction)) {
            level.setBlock(pos, state.setValue(facing, direction), Block.UPDATE_ALL);
        } else {
            property = state.getBlock().getStateDefinition().getProperty("axis");
            if (property instanceof EnumProperty<?> axis && axis.getPossibleValues().contains(direction.getAxis())) {
                level.setBlock(pos, copyAcceptedValue(state, axis, direction.getAxis()), Block.UPDATE_ALL);
            }
        }
    }

    private static Direction direction(BlockState state) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty("facing");
        if (property instanceof DirectionProperty facing) return state.getValue(facing);
        property = state.getBlock().getStateDefinition().getProperty("axis");
        if (property instanceof EnumProperty<?> && state.getValue(property) instanceof Direction.Axis axis) {
            return Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        }
        return Direction.NORTH;
    }
}
