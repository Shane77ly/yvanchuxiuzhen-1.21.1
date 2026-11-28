package net.yvanchuxiuzhen.system.town.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.town.data.TownBuildPlan;
import net.yvanchuxiuzhen.system.town.data.TownData;

/** 规划卷轴按点击面放置核心；来源向下偏移两格且不消耗卷轴。 */
public final class TownPlanningItem extends ContentItem {
    public TownPlanningItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        Direction face = context.getClickedFace();
        BlockPos pos = face == Direction.DOWN ? context.getClickedPos().below(2) : context.getClickedPos().relative(face);
        Direction facing = face.getAxis().isHorizontal() ? face : context.getPlayer().getDirection();
        if (!context.getLevel().isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        context.getLevel().setBlock(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chengzhen_buildblock"))
                .defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing), 3);
        TownBuildPlan build = context.getItemInHand().getOrDefault(TownData.BUILD_PLAN.get(), TownBuildPlan.EMPTY);
        int orientation = switch (facing) {
            case NORTH -> 2;
            case WEST -> 3;
            case EAST -> 4;
            default -> 1;
        };
        build = build.withDirection(switch (facing) {
            case NORTH -> "坐南朝北";
            case WEST -> "坐东朝西";
            case EAST -> "坐西朝东";
            default -> "坐北朝南";
        });
        TownBlockEntity town = (TownBlockEntity) context.getLevel().getBlockEntity(pos);
        BlockPos shift = build.shift(orientation);
        BlockPos length = build.length(orientation);
        town.setShifting(shift.getX(), shift.getY(), shift.getZ());
        town.setLengthOf(length.getX(), length.getY(), length.getZ());
        town.setTeam(Relations.entityTeam(context.getPlayer()));
        town.setBuildPlan(build);
        context.getItemInHand().set(TownData.BUILD_PLAN.get(), build);
        town.sync();
        return InteractionResult.SUCCESS;
    }
}
