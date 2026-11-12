package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.cooking.MealEffects;

/** 普通餐食完成原生食用后，以原栈的剩余身份结算餐食标记。 */
public final class MealFoodItem extends ContentItem {
    public MealFoodItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        MealEffects.afterEating(level, entity, stack);
        return result;
    }
}
