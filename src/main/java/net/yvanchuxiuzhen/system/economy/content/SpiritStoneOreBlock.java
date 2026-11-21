package net.yvanchuxiuzhen.system.economy.content;

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

/** 灵石矿保留战利品表的非空结果；空表按矿石内容行产出对应档位的未开封原矿。 */
public final class SpiritStoneOreBlock extends ContentBlock {
    private final BlockRow.Drop rawOre;

    public SpiritStoneOreBlock(BlockTraits traits) {
        super(traits);
        rawOre = traits.row().behavior().fallbackDrop().orElseThrow();
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) return drops;
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, rawOre.item())), rawOre.count()));
    }
}
