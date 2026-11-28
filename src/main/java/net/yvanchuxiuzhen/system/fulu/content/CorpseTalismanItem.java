package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.fulu.api.CorpseReleaseEvent;
import net.yvanchuxiuzhen.system.fulu.api.SealedCorpse;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;

/** 封存符只在点击方块顶面时释放并消耗，实体生成交生物系统处理。 */
public final class CorpseTalismanItem extends ContentItem {
    public CorpseTalismanItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() == Direction.UP) {
            ItemStack stack = context.getItemInHand();
            if (context.getLevel() instanceof ServerLevel level) {
                NeoForge.EVENT_BUS.post(new CorpseReleaseEvent(level, context.getClickedPos(),
                        stack.getOrDefault(FuluData.SEALED_CORPSE.get(), SealedCorpse.EMPTY).corpse()));
            }
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
