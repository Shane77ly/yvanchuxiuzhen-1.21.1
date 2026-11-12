package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.WaterloggedContentBlock;

/** 枫脂按水平点击面定向；垂直点击面固定朝北，并保留含水与来源掉落规则。 */
public final class MapleResinBlock extends WaterloggedContentBlock {
    public MapleResinBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction facing = clickedFace.getAxis() == Direction.Axis.Y ? Direction.NORTH : clickedFace;
        BlockState state = defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        if (traits().waterlogged()) {
            boolean water = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
            state = state.setValue(BlockStateProperties.WATERLOGGED, water);
        }
        return state;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (!drops.isEmpty()) {
            return drops;
        }
        return List.of(new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "mapleresinitem"))));
    }
}
