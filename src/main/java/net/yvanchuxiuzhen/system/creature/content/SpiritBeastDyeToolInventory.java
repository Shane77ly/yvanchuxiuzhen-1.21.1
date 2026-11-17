package net.yvanchuxiuzhen.system.creature.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.yvanchuxiuzhen.system.creature.item.SpiritBeastDyeToolItem;

/** 染色工具的三槽物品容器组件。 */
public final class SpiritBeastDyeToolInventory extends ComponentItemHandler {
    public SpiritBeastDyeToolInventory(ItemStack tool) {
        super(tool, DataComponents.CONTAINER, 3);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof SpiritBeastDyeToolItem);
    }
}
