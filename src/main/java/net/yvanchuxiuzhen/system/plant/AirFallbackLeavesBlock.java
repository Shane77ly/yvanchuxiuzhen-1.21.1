package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 玉树叶使用来源叶片生命周期，并在空战利品时回退空气。 */
public final class AirFallbackLeavesBlock extends FruitTreeLeavesBlock {
    public AirFallbackLeavesBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(Blocks.AIR)) : drops;
    }
}
