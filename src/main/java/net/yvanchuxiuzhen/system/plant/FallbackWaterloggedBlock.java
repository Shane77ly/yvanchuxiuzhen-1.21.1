package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 含水植物保留水状态，战利品为空时使用内容行记录的来源掉落。 */
final class FallbackWaterloggedBlock extends WaterloggedContentBlock {
    FallbackWaterloggedBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        BlockRow.Drop fallback = traits().row().behavior().fallbackDrop().orElseThrow();
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(fallback.item())), fallback.count()));
    }
}
