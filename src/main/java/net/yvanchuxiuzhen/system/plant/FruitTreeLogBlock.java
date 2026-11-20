package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 树干保留内容表的放置与旋转状态，空战利品按来源回落到对应木材。 */
public final class FruitTreeLogBlock extends ContentBlock {
    public FruitTreeLogBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        BlockRow.Drop fallback = traits().row().behavior().fallbackDrop().orElseThrow();
        if (fallback.item().equals("self")) return List.of(new ItemStack(this, fallback.count()));
        ResourceLocation id = fallback.item().contains(":") ? ResourceLocation.parse(fallback.item())
                : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, fallback.item());
        ItemLike item = BuiltInRegistries.ITEM.get(id);
        return List.of(new ItemStack(item, fallback.count()));
    }
}
