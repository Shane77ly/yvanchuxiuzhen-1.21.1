package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 花椒与八角树干保留来源无轴向状态的普通木块，以及非空战利品优先的自身回落。 */
public final class FruitTreeLogBlock extends ContentBlock {
    public FruitTreeLogBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }
}
