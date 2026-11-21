package net.yvanchuxiuzhen.system.material.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 保留矿石战利品表中的时运等结果；仅为空时才按内容行返回粗矿。 */
public final class RawMetalOreBlock extends ContentBlock {
    private final BlockRow.Drop raw;

    public RawMetalOreBlock(BlockTraits traits) {
        super(traits);
        raw = traits.row().behavior().fallbackDrop().orElseThrow();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, raw.item())), raw.count()));
    }
}
