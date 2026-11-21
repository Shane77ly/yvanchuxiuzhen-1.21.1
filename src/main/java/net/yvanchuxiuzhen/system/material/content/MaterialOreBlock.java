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
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 保留矿石战利品表中的时运等结果；仅为空时才按内容行返回来源指定的材料。 */
public final class MaterialOreBlock extends ContentBlock {
    public static ContentBlock createOre(BlockTraits traits) {
        return traits.waterlogged() ? ContentBlock.create(traits, Wet::new) : ContentBlock.create(traits, MaterialOreBlock::new);
    }

    private MaterialOreBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return withFallback(super.getDrops(state, params), traits());
    }

    private static List<ItemStack> withFallback(List<ItemStack> drops, BlockTraits traits) {
        if (!drops.isEmpty()) return drops;
        BlockRow.Drop fallback = traits.row().behavior().fallbackDrop().orElseThrow();
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, fallback.item())), fallback.count()));
    }

    private static final class Wet extends WaterloggedContentBlock {
        private Wet(BlockTraits traits) {
            super(traits);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return withFallback(super.getDrops(state, params), traits());
        }
    }
}
