package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 挂灯笼（fuqi_04）：放置时若上方仍为 fuqi_04，则将其替换为 fuqi_0401。 */
public final class Fuqi04Block extends ContentBlock {
    public Fuqi04Block(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        BlockPos above = pos.above();
        if (level.getBlockState(above).is(this)) {
            Block upper = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fuqi_0401"));
            level.setBlock(above, upper.defaultBlockState(), 3);
        }
    }
}
