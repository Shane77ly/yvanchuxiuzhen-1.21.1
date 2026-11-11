package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 普通树苗的随机刻、计划刻、骨粉与来源保底掉落入口。 */
public final class TreeSaplingBlock extends ContentFlower implements BonemealableBlock {
    private final TreeSapling sapling;

    public TreeSaplingBlock(BlockTraits traits, TreeSapling sapling) {
        super(traits);
        this.sapling = sapling;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TreeSaplingGrowth.grow(sapling, level, pos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        TreeSaplingGrowth.grow(sapling, level, pos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        TreeSaplingGrowth.grow(sapling, level, pos);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (sapling != TreeSapling.PEACH) return super.useWithoutItem(state, level, pos, player, hit);
        super.useWithoutItem(state, level, pos, player, hit);
        convertPeach(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (sapling != TreeSapling.PEACH) return super.useItemOn(stack, state, level, pos, player, hand, hit);
        super.useItemOn(stack, state, level, pos, player, hand, hit);
        convertPeach(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    private static void convertPeach(Level level, BlockPos pos, Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem());
        if (!itemId.equals(id("jingxve")) && !itemId.equals(id("xvejing")) && !itemId.equals(id("cailiao_200"))) return;

        level.setBlock(pos, BuiltInRegistries.BLOCK.get(id("peachsaplingxve")).defaultBlockState(), Block.UPDATE_ALL);
        mainHand.shrink(1);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
