package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 叶片战利品为空时使用来源指定的物品。 */
final class FallbackLeavesBlock extends FruitTreeLeavesBlock {
    private final BlockRow.Drop fallback;

    FallbackLeavesBlock(BlockTraits traits) {
        super(traits);
        fallback = traits.row().behavior().fallbackDrop().orElseThrow();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        ResourceLocation id = fallback.item().contains(":") ? ResourceLocation.parse(fallback.item())
                : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, fallback.item());
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(id), fallback.count()));
    }
}
