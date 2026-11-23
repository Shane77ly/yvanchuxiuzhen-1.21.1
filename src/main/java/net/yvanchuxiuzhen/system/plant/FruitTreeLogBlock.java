package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 树干保留内容表的放置、旋转状态与斧头去皮行为，空战利品按来源回落到对应木材。 */
public final class FruitTreeLogBlock extends ContentBlock {
    private static final Map<String, String> STRIPPED_BY_LOG = Map.ofEntries(
            Map.entry("beechlog", "strippedbeechlog"),
            Map.entry("beechlogtop", "strippedbeechlog"),
            Map.entry("chestnutlog", "strippedchestnutlog"),
            Map.entry("chestnutlogtop", "strippedchestnutlog"),
            Map.entry("camphorlog", "strippedcamphorlog"),
            Map.entry("camphorlogtop", "strippedcamphorlog"),
            Map.entry("cypresslog", "strippedcypresslog"),
            Map.entry("cypresslogtop", "strippedcypresslog"),
            Map.entry("ginkgolog", "strippedginkgolog"),
            Map.entry("ginkgologtop", "strippedginkgolog"),
            Map.entry("nanmulog", "strippednanmulog"),
            Map.entry("nanmulogtop", "strippednanmulog"),
            Map.entry("sylvestrislog", "strippedsylvestrislog"),
            Map.entry("sylvestrislogtop", "strippedsylvestrislog"),
            Map.entry("toothedlog", "strippedtoothedlog"),
            Map.entry("toothedlogtop", "strippedtoothedlog")
    );

    public FruitTreeLogBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    @Nullable
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility == ItemAbilities.AXE_STRIP && context.getItemInHand().canPerformAction(itemAbility)) {
            String target = STRIPPED_BY_LOG.get(BuiltInRegistries.BLOCK.getKey(this).getPath());
            if (target != null) {
                Block stripped = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, target));
                BlockState result = stripped.defaultBlockState();
                if (result.hasProperty(BlockStateProperties.AXIS) && state.hasProperty(BlockStateProperties.AXIS)) {
                    result = result.setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS));
                }
                return result;
            }
        }
        return super.getToolModifiedState(state, context, itemAbility, simulate);
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
