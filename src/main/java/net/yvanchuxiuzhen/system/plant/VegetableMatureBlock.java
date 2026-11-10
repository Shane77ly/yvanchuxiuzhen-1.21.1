package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 011–015成熟阶段不接受骨粉；芹菜和大蒜仅在资源loot为空时提供来源固定掉落。 */
public final class VegetableMatureBlock extends ContentFlower {
    private final VegetableCrop crop;

    public VegetableMatureBlock(BlockTraits traits, VegetableCrop crop) {
        super(traits);
        this.crop = crop;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() && (crop == VegetableCrop.SHICAI_011 || crop == VegetableCrop.SHICAI_015)
                ? List.of(crop.fixedDrop(4)) : drops;
    }
}
