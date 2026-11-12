package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 八层烟灰形状与掉落数量来自内容表，失去上方支撑时掉落后销毁。 */
public final class SootBlock extends ContentBlock {
    public SootBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        int layers = Integer.parseInt(BuiltInRegistries.BLOCK.getKey(this).getPath().substring("yanhui_".length()));
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yanhuiitem")), layers));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)) {
            Block.dropResources(state, level, BlockPos.containing(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), null);
            level.destroyBlock(pos, false);
        }
    }
}
