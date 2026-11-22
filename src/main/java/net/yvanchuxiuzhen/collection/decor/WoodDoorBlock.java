package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

/** 门扇家具：右键在左右门扇之间切换并旋转朝向 90 度，播放木门开关声。 */
public final class WoodDoorBlock extends ContentBlock {
    public WoodDoorBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        toggle(state, level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        toggle(state, level, pos);
        return InteractionResult.SUCCESS;
    }

    private void toggle(BlockState state, Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.WOODEN_DOOR_OPEN, SoundSource.NEUTRAL, 1F, 1F);
        if (level.isClientSide()) return;
        String id = BuiltInRegistries.BLOCK.getKey(this).getPath();
        boolean isLeft = id.endsWith("doorleft");
        if (!isLeft && !id.endsWith("doorright")) return;
        String counterpartId = isLeft
                ? id.substring(0, id.length() - "doorleft".length()) + "doorright"
                : id.substring(0, id.length() - "doorright".length()) + "doorleft";
        Block counterpart = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, counterpartId));
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction nextFacing = isLeft ? facing.getClockWise() : facing.getCounterClockWise();
        level.setBlock(pos, counterpart.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, nextFacing), 3);
    }
}
