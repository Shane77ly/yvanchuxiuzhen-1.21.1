package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.neoforged.neoforge.common.util.TriState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockRow;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 五种来源土壤的种植支持；内容属性、掉落与注册身份仍由原内容行提供。 */
public final class SourcePlantSoilBlock extends ContentBlock {
    /**
     * 1.18.2的原版BushBlock族身份，以及仙人掌、甘蔗。
     * 小垂滴叶和竹的来源存活路径不查询土壤，目标新增的植物也保留原生约束。
     */
    private static final Set<Block> VANILLA_PLANTS = Set.of(
            Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
            Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.SHORT_GRASS, Blocks.FERN,
            Blocks.DEAD_BUSH, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
            Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.WITHER_ROSE, Blocks.LILY_OF_THE_VALLEY,
            Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WHEAT, Blocks.CACTUS, Blocks.SUGAR_CANE,
            Blocks.ATTACHED_PUMPKIN_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM,
            Blocks.LILY_PAD, Blocks.NETHER_WART, Blocks.CARROTS, Blocks.POTATOES,
            Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
            Blocks.BEETROOTS, Blocks.SEA_PICKLE, Blocks.SWEET_BERRY_BUSH,
            Blocks.WARPED_FUNGUS, Blocks.WARPED_ROOTS, Blocks.NETHER_SPROUTS,
            Blocks.CRIMSON_FUNGUS, Blocks.CRIMSON_ROOTS, Blocks.AZALEA, Blocks.FLOWERING_AZALEA);

    public SourcePlantSoilBlock(BlockTraits traits) {
        super(traits);
    }

    /** 显式ground植物在自身canSurvive中执行白名单，不由土壤回调代替。 */
    public static boolean supports(BlockState plant) {
        Block block = plant.getBlock();
        return VANILLA_PLANTS.contains(block) || block instanceof BushBlock
                && BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(ModId.VALUE);
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition,
                                   Direction facing, BlockState plant) {
        return supports(plant) ? TriState.TRUE : TriState.DEFAULT;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) {
            return drops;
        }
        BlockRow.Drop fallback = traits().row().behavior().fallbackDrop().orElseThrow();
        ItemLike item = fallback.item().equals("self") ? this
                : BuiltInRegistries.ITEM.get(ResourceLocation.parse(fallback.item()));
        return List.of(new ItemStack(item, fallback.count()));
    }
}
