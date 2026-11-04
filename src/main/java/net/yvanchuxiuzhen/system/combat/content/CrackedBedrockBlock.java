package net.yvanchuxiuzhen.system.combat.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/**
 * 破碎的基岩前两个阶段（来源 {@code Bedrock01Block}、{@code Bedrock02Block}）：被玩家破坏后在原位变成下一阶段
 * （{@code bedrock_01} → {@code bedrock_02} → {@code bedrock_03}），第三阶段破坏后掉出基岩。
 */
public final class CrackedBedrockBlock extends ContentBlock {
    private final String next;

    public CrackedBedrockBlock(BlockTraits traits, String next) {
        super(traits);
        this.next = next;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, next));
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
        return removed;
    }
}
