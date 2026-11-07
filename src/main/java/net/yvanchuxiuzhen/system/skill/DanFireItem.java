package net.yvanchuxiuzhen.system.skill;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.yihuo.api.Yihuo;

/** 丹火的物品身份与提示归 skill，火块放置和燃料归 yihuo。 */
public final class DanFireItem extends ContentItem {
    public DanFireItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return Yihuo.useDanFire(context);
    }
}
