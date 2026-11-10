package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentDoublePlant;

/** 成熟谷物仅下半格回落六颗对应种子，不接受骨粉或生长推进。 */
public final class CerealMatureBlock extends ContentDoublePlant {
    private final CerealCrop crop;

    public CerealMatureBlock(BlockTraits traits, CerealCrop crop) {
        super(traits);
        this.crop = crop;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(crop.seed(), 6)) : drops;
    }
}
