package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 文字装饰的普通右键消费染料，牌位潜行收回保留文字。 */
public final class InscribedDecorBlock extends ContentBlock implements EntityBlock {
    private final InscribedDecorVariant variant;
    public InscribedDecorBlock(BlockTraits traits, InscribedDecorVariant variant) { super(traits); this.variant = variant; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new InscribedDecorBlockEntity(variant, pos, state); }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        interact(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(level, pos, player);
        return InteractionResult.SUCCESS;
    }
    private static void interact(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) InscribedDecor.interact((InscribedDecorBlockEntity) level.getBlockEntity(pos), player);
    }
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) { return (InscribedDecorBlockEntity) level.getBlockEntity(pos); }
    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int value) {
        super.triggerEvent(state, level, pos, id, value);
        BlockEntity block = level.getBlockEntity(pos);
        return block != null && block.triggerEvent(id, value);
    }
    private ItemStack item() { return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, variant.itemId()))); }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> original = super.getDrops(state, params);
        return original.isEmpty() ? List.of(item()) : original;
    }
    @Override public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) { return item(); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return variant.comparator(); }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return 0; }
}
