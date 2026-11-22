package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.pathfinder.PathType;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentDoor;

/** 窗台门方块：AI 寻路类型固定为 DOOR_OPEN。 */
public final class SylvestrisSillWindowBlock extends ContentDoor {
    public SylvestrisSillWindowBlock(BlockTraits traits) {
        super(traits, BlockSetType.OAK);
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, Mob mob) {
        return PathType.DOOR_OPEN;
    }
}
