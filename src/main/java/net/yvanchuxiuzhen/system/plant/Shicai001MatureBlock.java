package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentDoublePlant;

/** 成熟水稻仅下半格回落六颗种子，不接受骨粉和随机成长。 */
public final class Shicai001MatureBlock extends ContentDoublePlant {
    public Shicai001MatureBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "shicai_001seed")), 6)) : drops;
    }
}
