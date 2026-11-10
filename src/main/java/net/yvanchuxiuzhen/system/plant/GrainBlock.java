package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 可采摘食材植株的花类属性与来源默认掉落；非空战利品表优先。 */
abstract class GrainBlock extends ContentFlower {
    protected final GrainCrop crop;
    private final boolean mature;

    GrainBlock(BlockTraits traits, GrainCrop crop, boolean mature) {
        super(traits);
        this.crop = crop;
        this.mature = mature;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(crop.drop(mature)) : drops;
    }
}
