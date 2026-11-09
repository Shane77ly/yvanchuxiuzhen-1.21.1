package net.yvanchuxiuzhen.system.fulu.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.fulu.AshBurnerRuntime;
import net.yvanchuxiuzhen.platform.ModId;

/** 烧灰菜单只收原木；自动化的任意物品入口由方块实体独立承担。 */
public final class AshBurnerMenu extends AbstractContainerMenu {
    private final AshBurnerBlockEntity burner;
    private final BlockPos pos;
    private final Level level;

    public AshBurnerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public AshBurnerMenu(int id, Inventory inventory, BlockPos pos) {
        super(FuluMenus.ASH_BURNER.get(), id);
        this.pos = pos.immutable();
        level = inventory.player.level();
        burner = level.getBlockEntity(pos) instanceof AshBurnerBlockEntity found ? found : null;
        IItemHandler items = burner == null ? new ItemStackHandler(9) : burner.handler(null);
        for (int index = 0; index < 9; index++) {
            addSlot(new SlotItemHandler(items, index, 61 + index % 3 * 18, 17 + index / 3 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "logsitem")));
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new AshBurnerMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.huafushaohui")), pos);
    }

    public void help(ServerPlayer player) {
        if (burner != null && level.getBlockEntity(pos) == burner) AshBurnerRuntime.help(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (burner == null && player instanceof ServerPlayer serverPlayer) {
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = getSlot(slot).getItem();
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
                else player.getInventory().placeItemBackInInventory(stack);
                getSlot(slot).set(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 9, false)) {
            if (index < 36) moveItemStackTo(stack, 36, slots.size(), true);
            else moveItemStackTo(stack, 9, 36, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
