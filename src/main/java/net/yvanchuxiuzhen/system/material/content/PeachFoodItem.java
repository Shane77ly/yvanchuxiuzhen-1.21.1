package net.yvanchuxiuzhen.system.material.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 桃子保留药材身份与食用属性，食用后按来源栈数量规则交付桃核。 */
public final class PeachFoodItem extends ContentItem {
    public PeachFoodItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack kernel = new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "peachkernel")));
        super.finishUsingItem(stack, level, entity);
        if (stack.isEmpty()) return kernel;
        if (!level.isClientSide && entity instanceof Player player && !player.getAbilities().instabuild) {
            if (!player.getInventory().add(kernel)) player.drop(kernel, false);
        }
        return stack;
    }
}
