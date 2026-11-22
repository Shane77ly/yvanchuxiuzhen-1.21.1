package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 合成后保留自身不消耗的物品（ling_17）。 */
public final class ReusableCraftingItem extends ContentItem {
    public ReusableCraftingItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(this);
    }
}
