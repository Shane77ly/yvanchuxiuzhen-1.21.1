package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
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
            PlaqueInscription inscription = context.getItemInHand().getOrDefault(DecorData.PLAQUE_INSCRIPTION.get(), PlaqueInscription.EMPTY);
            if (!inscription.text().isEmpty()) {
                InscribedDecorBlockEntity block = (InscribedDecorBlockEntity) level.getBlockEntity(pos);
                List<String> chars = new java.util.ArrayList<>(inscription.glyphs().size());
                List<Double> offsets = new java.util.ArrayList<>(inscription.glyphs().size());
                for (PlaqueInscription.Glyph glyph : inscription.glyphs()) {
                    chars.add(glyph.glyph());
                    offsets.add(glyph.offset());
                }
                block.setGlyphs(inscription.text(), InscribedDecor.direction(context.getPlayer().getDirection().getOpposite()), chars, offsets);
                block.syncText();
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
