package net.yvanchuxiuzhen.system.fulu.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.fulu.api.ReincarnationRequestedEvent;

/** 浮生丹先执行普通食用，再交接轮回；不模拟死亡。 */
public final class ReincarnationPillItem extends ContentItem {
    public ReincarnationPillItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (level instanceof ServerLevel serverLevel) NeoForge.EVENT_BUS.post(new ReincarnationRequestedEvent(entity, serverLevel));
        return result;
    }
}
