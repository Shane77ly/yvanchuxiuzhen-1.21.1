package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentShovel;
import net.yvanchuxiuzhen.platform.content.LegacyTier;
import net.yvanchuxiuzhen.platform.content.block.TraitedBlock;

/** 药锄先执行锹的原生操作，再按来源植物材质采集方块并判定虫卵掉落。 */
public final class Gongjv008Item extends ContentShovel {
    private static final Set<Block> PLANTS_AND_LEAVES = Set.of(
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
            Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING,
            Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID,
            Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.WITHER_ROSE,
            Blocks.LILY_OF_THE_VALLEY, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WHEAT,
            Blocks.SUGAR_CANE, Blocks.ATTACHED_PUMPKIN_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.PUMPKIN_STEM,
            Blocks.MELON_STEM, Blocks.LILY_PAD, Blocks.NETHER_WART, Blocks.COCOA, Blocks.CARROTS, Blocks.POTATOES,
            Blocks.CHORUS_PLANT, Blocks.CHORUS_FLOWER, Blocks.BEETROOTS, Blocks.SWEET_BERRY_BUSH,
            Blocks.WARPED_FUNGUS, Blocks.CRIMSON_FUNGUS, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT,
            Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT,
            Blocks.SPORE_BLOSSOM, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.MOSS_CARPET,
            Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF);
    private static final Set<Block> REPLACEABLE_PLANTS = Set.of(
            Blocks.SHORT_GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.VINE, Blocks.GLOW_LICHEN,
            Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.TALL_GRASS,
            Blocks.LARGE_FERN, Blocks.HANGING_ROOTS);
    private static final ResourceLocation INSECT_EGG = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chongluan");

    public Gongjv008Item(LegacyTier tier, Properties properties, List<Component> tooltip) {
        super(tier, properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        if (context.getLevel() instanceof ServerLevel level && context.getPlayer() != null) {
            harvest(level, context.getClickedPos(), context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    private static void harvest(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        HarvestKind kind = harvestKind(state);
        if (kind == HarvestKind.NONE) {
            return;
        }
        drop(level, pos, new ItemStack(state.getBlock()));
        Property<?> half = state.getBlock().getStateDefinition().getProperty("half");
        String halfName = half instanceof EnumProperty<?> ? state.getValues().get(half).toString() : "";
        level.destroyBlock(pos, false);
        if (kind == HarvestKind.REPLACEABLE) {
            if (halfName.equals("upper")) {
                level.destroyBlock(pos.below(), false);
            } else if (halfName.equals("lower")) {
                level.destroyBlock(pos.above(), false);
            }
        }
        double chance = 0.01;
        if (player.hasEffect(MobEffects.LUCK)) {
            chance += (player.getEffect(MobEffects.LUCK).getAmplifier() + 1) * 0.02;
        }
        if (player.hasEffect(MobEffects.UNLUCK)) {
            int amplifier = player.hasEffect(MobEffects.LUCK) ? player.getEffect(MobEffects.LUCK).getAmplifier() : 0;
            chance -= (amplifier + 1) * 0.02;
        }
        if (Math.random() < chance) {
            drop(level, pos, new ItemStack(BuiltInRegistries.ITEM.get(INSECT_EGG)));
        }
    }

    /** 原版成员对应1.18.2材质，本模组方块直接读取已迁移的来源材质。 */
    private static HarvestKind harvestKind(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof TraitedBlock traited) {
            return switch (traited.traits().row().identity().material()) {
                case "LEAVES", "PLANT" -> HarvestKind.SINGLE;
                case "REPLACEABLE_PLANT" -> HarvestKind.REPLACEABLE;
                default -> HarvestKind.NONE;
            };
        }
        if (REPLACEABLE_PLANTS.contains(block)) {
            return HarvestKind.REPLACEABLE;
        }
        return PLANTS_AND_LEAVES.contains(block) ? HarvestKind.SINGLE : HarvestKind.NONE;
    }

    private static void drop(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        drop.setPickUpDelay(10);
        level.addFreshEntity(drop);
    }

    private enum HarvestKind {
        NONE,
        SINGLE,
        REPLACEABLE
    }
}
