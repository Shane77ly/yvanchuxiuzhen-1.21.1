package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 成熟蔬菜通过破坏采收；仅芹菜与大蒜有空战利品回退。 */
public final class VegetableMatureBlock extends ContentFlower {
    private final VegetableCrop crop;

    public VegetableMatureBlock(BlockTraits traits, VegetableCrop crop) {
        super(traits);
        this.crop = crop;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() && (crop == VegetableCrop.SHICAI_011 || crop == VegetableCrop.SHICAI_015)
                ? List.of(crop.fixedDrop(4)) : drops;
    }
}
