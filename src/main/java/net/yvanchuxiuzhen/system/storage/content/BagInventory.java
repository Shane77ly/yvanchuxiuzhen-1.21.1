package net.yvanchuxiuzhen.system.storage.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ComponentItemHandler;

/** 乾坤袋真实库存随物品容器组件保存；能力入口沿用来源仅排除同档袋，菜单另有储物标签限制。 */
public final class BagInventory extends ComponentItemHandler {
    private final BagKind kind;

    public BagInventory(ItemStack bag, BagKind kind) {
        super(bag, DataComponents.CONTAINER, kind.size());
        this.kind = kind;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.is(kind.item());
    }
}
