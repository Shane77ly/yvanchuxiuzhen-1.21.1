package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 食用后的骨头或果核：吃空返回一件，否则仅向非创造玩家库存交付。 */
public final class RemainderFoodItem extends ContentItem {
    private final ResourceLocation remainder;

    public RemainderFoodItem(Properties properties, List<Component> tooltip, ResourceLocation remainder) {
        super(properties, tooltip);
        this.remainder = remainder;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(remainder));
        super.finishUsingItem(stack, level, entity);
        if (stack.isEmpty()) return result;
        if (!level.isClientSide && entity instanceof Player player && !player.getAbilities().instabuild) {
            if (!player.getInventory().add(result)) player.drop(result, false);
        }
        return stack;
    }
}
