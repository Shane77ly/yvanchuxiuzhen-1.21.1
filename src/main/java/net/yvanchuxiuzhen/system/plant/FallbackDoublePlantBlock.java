package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentDoublePlant;

/** 双格植物按来源决定上半部掉落，并在战利品为空时使用内容行的回退。 */
class FallbackDoublePlantBlock extends ContentDoublePlant {
    FallbackDoublePlantBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (traits().row().behavior().dropLowerOnly().orElse(false)
                && state.getValue(HALF) != DoubleBlockHalf.LOWER) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty() || traits().row().behavior().fallbackDrop().isEmpty()) return drops;
        BlockRow.Drop fallback = traits().row().behavior().fallbackDrop().orElseThrow();
        if (fallback.item().equals("self")) return List.of(new ItemStack(this, fallback.count()));
        ResourceLocation id = fallback.item().contains(":") ? ResourceLocation.parse(fallback.item())
                : ResourceLocation.fromNamespaceAndPath(ModId.VALUE, fallback.item());
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(id), fallback.count()));
    }
}
