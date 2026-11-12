package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 霓裳草种子在来源规定的泥土或草类材质上播种。 */
public final class NichangGrassSeedItem extends ContentItem {
    public NichangGrassSeedItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    public static NichangGrassSeedItem create(Properties properties, List<Component> tooltip) {
        return new NichangGrassSeedItem(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && PlantSoils.dirtOrGrass(context.getLevel().getBlockState(context.getClickedPos()))) {
            BlockPos pos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(pos)) {
                context.getLevel().setBlock(pos, NichangGrassCrop.INSTANCE.block(1).defaultBlockState(),
                        Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
