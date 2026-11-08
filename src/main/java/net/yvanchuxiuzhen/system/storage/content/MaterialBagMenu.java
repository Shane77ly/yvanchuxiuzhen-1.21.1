package net.yvanchuxiuzhen.system.storage.content;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Group;
import net.yvanchuxiuzhen.system.storage.api.MaterialWarehouse.Preferences;
import net.yvanchuxiuzhen.system.storage.data.StorageData;

/** 输入槽持有真实物品，45个样本仅供选择；所有仓数量由服务端公开入口更新。 */
public final class MaterialBagMenu extends AbstractContainerMenu {
    public static final List<String> FILTER_ATTRIBUTES = List.of("heanum", "powernum", "jingnum", "attnum", "attdefnum",
            "mananum", "manadefnum", "soulnum", "souldefnum");
    private final Player owner;
    private final IItemHandler items;
    private final boolean bound;
    private final SimpleContainerData state = new SimpleContainerData(5);
    private final List<Long> deposits = new ArrayList<>();
    private int ticks;
    private long refreshAt = 2;

    public MaterialBagMenu(int id, Inventory inventory, BlockPos pos) {
        super(BagMenus.MATERIAL.get(), id);
        owner = inventory.player;
        IItemHandler blockItems = owner.level().isClientSide ? null
                : owner.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        bound = blockItems != null;
        items = bound ? blockItems : new ItemStackHandler(46);
        Preferences preferences = MaterialStorage.snapshot(owner).preferences();
        state.set(1, preferences.filters());
        state.set(2, preferences.automatic() ? 1 : 0);
        state.set(3, preferences.spending() ? 1 : 0);
        addDataSlots(state);
        addSlot(new SlotItemHandler(items, 0, 137, 321) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MaterialStorage.accepts(stack);
            }
        });
        for (int index = 1; index <= 45; index++) {
            addSlot(new SlotItemHandler(items, index, 119 + (index - 1) % 5 * 22, 115 + (index - 1) / 5 * 22) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 324 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 382));
    }

    public int selected() {
        return state.get(0);
    }

    public boolean option(int index) {
        return index < 9 ? (state.get(1) & (1 << index)) != 0 : state.get(index - 7) != 0;
    }

    public int detailRevision() {
        return state.get(4);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.containerMenu != this) return false;
        MaterialWarehouse warehouse = MaterialStorage.snapshot(player);
        Preferences preferences = warehouse.preferences();
        if (button == 0 || button == 1 || button == 53) {
            int page = preferences.page();
            int sort = preferences.sort();
            if (button == 0 && page > 1) page--;
            if (button == 1 && page < warehouse.pageMax()) page++;
            if (button == 53) sort = (sort + 1) % 3;
            MaterialStorage.preferences(serverPlayer, new Preferences(page, sort, preferences.filters(), preferences.automatic(), preferences.spending()));
            refresh();
            if (button != 53 && selected() >= 1) state.set(4, state.get(4) + 1);
        } else if (button == 2) {
            deposit(serverPlayer);
        } else if (button >= 3 && button <= 7) {
            ItemStack sample = selected() == 0 ? ItemStack.EMPTY : slots.get(selected()).getItem();
            String id = BuiltInRegistries.ITEM.getKey(sample.getItem()).toString();
            ItemStack withdrawn = MaterialStorage.withdraw(serverPlayer, id, sample.getCount(), 7 - button);
            if (!withdrawn.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, withdrawn);
            refreshAt = ticks + 1;
        } else if (button >= 8 && button <= 52) {
            state.set(0, button - 7);
            state.set(4, state.get(4) + 1);
        } else if (button >= 100 && button <= 121) {
            int option = (button - 100) / 2;
            boolean selected = (button & 1) != 0;
            if (option < 9) state.set(1, selected ? state.get(1) | (1 << option) : state.get(1) & ~(1 << option));
            else state.set(option - 7, selected ? 1 : 0);
        } else {
            return false;
        }
        broadcastChanges();
        return true;
    }

    /** 自动放入沿用每刻排入两刻后执行；回调仅在本菜单仍打开期间执行。 */
    public void tick(ServerPlayer player) {
        ticks++;
        if (option(9) && !slots.get(0).getItem().isEmpty()) deposits.add((long) ticks + 2);
        while (!deposits.isEmpty() && deposits.getFirst() <= ticks) {
            deposits.removeFirst();
            deposit(player);
        }
        int filterTicks = player.getData(StorageData.FILTER_TICKS) + 1;
        if (filterTicks >= 20) {
            Preferences previous = MaterialStorage.snapshot(player).preferences();
            if (previous.filters() != state.get(1) || previous.automatic() != option(9)) {
                MaterialStorage.preferences(player, new Preferences(previous.page(), previous.sort(), state.get(1), option(9), previous.spending()));
                refresh();
            }
            filterTicks = 0;
        }
        player.setData(StorageData.FILTER_TICKS, filterTicks);
        if (ticks == refreshAt) {
            refresh();
            if (ticks > 2 && selected() >= 1) state.set(4, state.get(4) + 1);
        }
    }

    private void deposit(ServerPlayer player) {
        ItemStack input = slots.get(0).getItem();
        MaterialStorage.deposit(player, input);
        slots.get(0).set(ItemStack.EMPTY);
        refresh();
    }

    private void refresh() {
        IItemHandlerModifiable modifiable = (IItemHandlerModifiable) items;
        MaterialWarehouse warehouse = MaterialStorage.snapshot(owner);
        List<Display> displays = new ArrayList<>();
        for (Group group : warehouse.groups()) {
            List<String> attributes = Materials.revealedAttributes(owner, group.itemId());
            boolean matches = true;
            for (int index = 0; index < FILTER_ATTRIBUTES.size(); index++) {
                if ((warehouse.preferences().filters() & (1 << index)) != 0 && !attributes.contains(FILTER_ATTRIBUTES.get(index))) matches = false;
            }
            if (!matches) continue;
            int remaining = group.levelCount();
            for (int level = 1; level <= 41; level++) {
                if (group.activeLevels().contains(level)) {
                    displays.add(new Display(group.itemId(), level));
                    remaining--;
                }
                if (remaining <= 0) break;
            }
        }
        if (warehouse.preferences().sort() == 1) displays.sort(Comparator.comparingInt(Display::level).reversed());
        if (warehouse.preferences().sort() == 2) displays.sort(Comparator.comparingInt(Display::level));
        int start = (warehouse.preferences().page() - 1) * 45;
        for (int slot = 1; slot <= 45; slot++) {
            int index = start + slot - 1;
            ItemStack sample = ItemStack.EMPTY;
            if (index < displays.size()) {
                Display display = displays.get(index);
                sample = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(display.itemId())), display.level());
                Materials.setInstance(sample, display.level(), 5);
            }
            modifiable.setStackInSlot(slot, sample);
        }
        broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            Preferences previous = MaterialStorage.snapshot(player).preferences();
            MaterialStorage.preferences(serverPlayer, new Preferences(previous.page(), previous.sort(), state.get(1), option(9), option(10)));
            if (!bound) {
                ItemStack input = items.extractItem(0, items.getStackInSlot(0).getCount(), false);
                clearContainer(player, new SimpleContainer(input));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index >= 1 && index <= 45) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack previous = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 46, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < 73) {
                if (!moveItemStackTo(stack, 73, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 46, 73, false)) return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == previous.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return previous;
    }

    private record Display(String itemId, int level) {
    }
}
