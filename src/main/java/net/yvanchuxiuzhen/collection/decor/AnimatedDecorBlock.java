package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 装饰梯子迁移到上一格；签筒保留独立动画范围与双端右键入口。 */
public class AnimatedDecorBlock extends WaterloggedContentBlock implements EntityBlock {
    public static final IntegerProperty LADDER_ANIMATION = IntegerProperty.create("animation", 0, 1);
    public static final IntegerProperty FORTUNE_ANIMATION = IntegerProperty.create("animation", 0, 2);

    public AnimatedDecorBlock(BlockTraits traits) { super(traits); }
    public AnimatedDecorVariant variant() { return AnimatedDecorVariant.LADDER; }
    public IntegerProperty animationProperty() { return LADDER_ANIMATION; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(animationProperty());
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AnimatedDecorBlockEntity(variant(), pos, state); }
    @Override protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AnimatedDecorBlockEntity block ? block : null;
    }
    @Override protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity block = level.getBlockEntity(pos);
        return block != null && block.triggerEvent(id, parameter);
    }
    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (variant() == AnimatedDecorVariant.LADDER) {
            BlockPos above = pos.above();
            BlockState relocated = state;
            for (Map.Entry<Property<?>, Comparable<?>> entry : level.getBlockState(above).getValues().entrySet()) {
                Property<?> property = state.getBlock().getStateDefinition().getProperty(entry.getKey().getName());
                if (property != null) relocated = copyCompatible(relocated, property, entry.getValue());
            }
            level.setBlock(above, relocated, Block.UPDATE_ALL);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyCompatible(BlockState state, Property<T> property, Comparable<?> value) {
        return property.getPossibleValues().contains(value) ? state.setValue(property, (T) value) : state;
    }

    public static final class Fortune extends AnimatedDecorBlock {
        public Fortune(BlockTraits traits) { super(traits); }
        @Override public AnimatedDecorVariant variant() { return AnimatedDecorVariant.FORTUNE; }
        @Override public IntegerProperty animationProperty() { return FORTUNE_ANIMATION; }
        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            FortuneDraws.shake(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit) {
            FortuneDraws.shake(level, pos, player);
            return ItemInteractionResult.SUCCESS;
        }
    }
}
