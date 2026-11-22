package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 牌位物品仅在点击上表面且上一格为空时落地，并复制来源保留的文字字段。 */
public final class MemorialTabletItem extends ContentItem {
    public MemorialTabletItem(Item.Properties properties, List<Component> tooltip) { super(properties, tooltip); }
    @Override public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().above();
        if (context.getClickedFace() != Direction.UP || context.getPlayer() == null || !level.isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        if (!level.isClientSide()) {
            String itemId = BuiltInRegistries.ITEM.getKey(context.getItemInHand().getItem()).getPath();
            BlockState state = InscribedDecorContent.block(itemId.substring(0, itemId.length() - 4)).defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getPlayer().getDirection().getOpposite());
            level.setBlock(pos, state, 3);
            CompoundTag saved = context.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (!saved.getString("plaquetxt").isEmpty()) {
                InscribedDecorBlockEntity block = (InscribedDecorBlockEntity) level.getBlockEntity(pos);
                CompoundTag data = block.getPersistentData();
                data.putString("plaquetxt", saved.getString("plaquetxt"));
                data.putInt("zishu", saved.getInt("zishu"));
                data.putString("fangxiang", InscribedDecor.direction(context.getPlayer().getDirection().getOpposite()));
                for (int index = 0; index < saved.getInt("zishu"); index++) {
                    data.putString("zidi" + index, saved.getString("zidi" + index));
                    data.putDouble("ziweizhi" + index, saved.getDouble("ziweizhi" + index));
                }
                block.syncText();
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
