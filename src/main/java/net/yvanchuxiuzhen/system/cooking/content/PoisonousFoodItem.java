package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 059 号食材完成食用后，独立按三成概率施加一分钟中毒。 */
public final class PoisonousFoodItem extends ContentItem {
    public PoisonousFoodItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && Math.random() < 0.3) entity.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 0));
        return result;
    }
}
