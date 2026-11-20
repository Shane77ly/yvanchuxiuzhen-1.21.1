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
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 野生花草保留内容表的土壤与效果规则，空战利品按来源回落。 */
public final class FallbackFlowerBlock extends ContentFlower {
    public FallbackFlowerBlock(BlockTraits traits) {
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
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(id), fallback.count()));
    }
}
