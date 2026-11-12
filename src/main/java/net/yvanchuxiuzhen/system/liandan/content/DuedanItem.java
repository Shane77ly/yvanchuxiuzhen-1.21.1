package net.yvanchuxiuzhen.system.liandan.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

/** 毒厄丹完成原版食用后，按来源顺序附加三种状态效果。 */
public final class DuedanItem extends ContentItem {
    public DuedanItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        StatusEffects.add(entity, "effect_003", 2400, 2);
        StatusEffects.add(entity, "effect_006", 2400, 2);
        StatusEffects.add(entity, "effect_005", 2400, 2);
        return result;
    }
}
