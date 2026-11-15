package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 爆竹保留墙面、地面、顶面的来源朝向和碰撞盒；附着状态由本方块独占。 */
public final class AttachedFirecrackerBlock extends BlockFirework {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    public AttachedFirecrackerBlock(BlockTraits traits) {
        super(traits, BlockFireworkKind.FIRECRACKER);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, FACE); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        return face.getAxis() == Direction.Axis.Y
                ? defaultBlockState().setValue(FACE, face == Direction.DOWN ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, context.getHorizontalDirection())
                : defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, face);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        AttachFace face = state.getValue(FACE);
        if (face != AttachFace.WALL) {
            int y = face == AttachFace.CEILING ? 14 : 0;
            return facing.getAxis() == Direction.Axis.Z ? box(3, y, 0, 13, y + 2, 16) : box(0, y, 3, 16, y + 2, 13);
        }
        return switch (facing) {
            case NORTH -> box(3, 0, 14, 13, 16, 16);
            case EAST -> box(0, 0, 3, 2, 16, 13);
            case WEST -> box(14, 0, 3, 16, 16, 13);
            default -> box(3, 0, 0, 13, 16, 2);
        };
    }
}
