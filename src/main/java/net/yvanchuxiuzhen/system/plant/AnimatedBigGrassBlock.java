package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.HitResult;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 高草摇摆期间的单格动画实体，三十刻后按来源写入顺序恢复双格植株。 */
public final class AnimatedBigGrassBlock extends ContentBlock implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 2);

    public AnimatedBigGrassBlock(BlockTraits traits) {
        super(traits);
    }

    public static Block block() {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "biggrassyincang"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ANIMATION);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnimatedBigGrassBlockEntity(pos, state);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AnimatedBigGrassBlockEntity grass ? grass : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() != asItem();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hit, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(BigGrassBlock.block());
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(Blocks.AIR)) : drops;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moving) {
        super.onPlace(state, level, pos, previous, moving);
        level.scheduleTick(pos, this, 30);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        Block grass = BigGrassBlock.block();
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos.above(), grass.defaultBlockState(), Block.UPDATE_ALL);
        BlockState above = level.getBlockState(pos.above());
        if (above.hasProperty(DoublePlantBlock.HALF)) {
            level.setBlock(pos.above(), above.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        }
        level.setBlock(pos, grass.defaultBlockState(), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, 30);
    }
}
