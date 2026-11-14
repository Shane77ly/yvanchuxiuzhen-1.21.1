package net.yvanchuxiuzhen.system.guchong.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** 蛊笛六格展示菜单；页码和操作提示保存在当前主手，关闭时不回收展示副本。 */
public final class GuchongReleaseMenu extends AbstractContainerMenu {
    public static final int DISPLAY_SLOTS = 6;
    private final BlockPos position;

    public GuchongReleaseMenu(int id, Inventory inventory, BlockPos position) {
        super(GuchongMenus.RELEASE.get(), id);
        this.position = position;
        IItemHandler display = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, position, null);
        if (display == null) display = new ItemStackHandler(DISPLAY_SLOTS);
        for (int index = 0; index < DISPLAY_SLOTS; index++) {
            addSlot(new SlotItemHandler(display, index, 25 + index / 3 * 72, -22 + index % 3 * 27) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 85 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 143));
    }

    public BlockPos position() {
        return position;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof GuchongFluteItem;
    }

    /** 低于1的实际页码只在取展示范围时按第一页处理，不改写主手保存的页码。 */
    public void populate(Player player) {
        ItemStack flute = player.getMainHandItem();
        CompoundTag state = GuchongJarData.read(flute);
        for (int index = 0; index < DISPLAY_SLOTS; index++) {
            state.putString("xianshiweizhi" + index, "");
            getSlot(index).set(ItemStack.EMPTY);
        }
        double page = state.getDouble("pagenum");
        double start = ((page >= 1 ? page : 1) - 1) * DISPLAY_SLOTS;
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
        if (inventory != null) {
            int jarIndex = 0;
            int displayIndex = 0;
            for (int index = 0; index < inventory.getSlots(); index++) {
                ItemStack jar = inventory.getStackInSlot(index).copy();
                if (!jar.is(GuchongFluteRuntime.JARS)) continue;
                if (jarIndex >= start && jarIndex < start + DISPLAY_SLOTS) {
                    getSlot(displayIndex).set(jar.copyWithCount(1));
                    state.putString("xianshiweizhi" + displayIndex, status(GuchongJarData.read(jar).getBoolean("shifangguchong")));
                    displayIndex++;
                } else if (jarIndex >= start + DISPLAY_SLOTS) {
                    break;
                }
                jarIndex++;
            }
        }
        GuchongJarData.write(flute, state);
        broadcastChanges();
    }

    public void updateDisplayedJar(Player player, int selected, CompoundTag data, boolean releasing) {
        ItemStack display = getSlot(selected).getItem();
        if (!display.isEmpty()) GuchongJarData.write(display, data.copy());
        ItemStack flute = player.getMainHandItem();
        CompoundTag state = GuchongJarData.read(flute);
        state.putString("xianshiweizhi" + selected, status(releasing));
        GuchongJarData.write(flute, state);
    }

    private static String status(boolean releasing) {
        return releasing ? "§6收回蛊虫" : "§a释放蛊虫";
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer owner) || !stillValid(player) || button < 0 || button > 7) return false;
        if (button < 2) {
            turnPage(player, button);
            populate(player);
        } else {
            ItemStack flute = player.getMainHandItem();
            CompoundTag state = GuchongJarData.read(flute);
            state.putDouble("anniunum", button - 2);
            GuchongJarData.write(flute, state);
            GuchongFluteRuntime.activate(owner.serverLevel(), owner, this, button - 2);
            broadcastChanges();
        }
        return true;
    }

    private void turnPage(Player player, int button) {
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY, null);
        int count = 0;
        if (inventory != null) {
            for (int index = 0; index < inventory.getSlots(); index++) {
                if (inventory.getStackInSlot(index).is(GuchongFluteRuntime.JARS)) count++;
            }
        }
        if (count < 1) return;
        double pages = Math.ceil(count / (double) DISPLAY_SLOTS);
        ItemStack flute = player.getMainHandItem();
        CompoundTag state = GuchongJarData.read(flute);
        double page = state.getDouble("pagenum");
        state.putDouble("pagenum", button == 0 ? (page < pages ? page + 1 : 1) : (page > pages ? page - 1 : pages));
        GuchongJarData.write(flute, state);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < DISPLAY_SLOTS) return ItemStack.EMPTY;
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < DISPLAY_SLOTS + 27) {
            if (!moveItemStackTo(stack, DISPLAY_SLOTS + 27, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, DISPLAY_SLOTS, DISPLAY_SLOTS + 27, false)) {
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        return ItemStack.EMPTY;
    }
}
