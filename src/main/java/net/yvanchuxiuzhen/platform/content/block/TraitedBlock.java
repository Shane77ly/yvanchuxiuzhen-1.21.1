package net.yvanchuxiuzhen.platform.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/**
 * 内容表方块共有的 NeoForge 扩展钩子：可燃性、蔓延速度、挖掘判定与附魔台加成取自内容表行，
 * 行中没有对应字段时沿用 NeoForge 的默认实现。
 */
public interface TraitedBlock extends IBlockExtension {
    BlockTraits traits();

    @Override
    default int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int value = traits().flammability();
        return value >= 0 ? value : IBlockExtension.super.getFlammability(state, level, pos, direction);
    }

    @Override
    default int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        int value = traits().fireSpread();
        return value >= 0 ? value : IBlockExtension.super.getFireSpreadSpeed(state, level, pos, direction);
    }

    @Override
    default boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        Boolean rule = traits().canHarvest(player);
        return rule != null ? rule : IBlockExtension.super.canHarvestBlock(state, level, pos, player);
    }

    @Override
    default float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return traits().enchantPower(IBlockExtension.super.getEnchantPowerBonus(state, level, pos));
    }
}
