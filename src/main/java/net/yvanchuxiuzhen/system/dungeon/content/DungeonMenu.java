package net.yvanchuxiuzhen.system.dungeon.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.dungeon.DungeonSettings;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;

/** 普通入口和控制器仅展示来源库存；管理入口开放门票槽，占位符没有库存槽。 */
public final class DungeonMenu extends AbstractContainerMenu {
    public static final int OPEN = 0;
    public static final int MANAGE = 1;
    public static final int CONTROLLER = 2;
    public static final int MARKER = 3;
    private final BlockPos position;
    private final DungeonBlockEntity block;
    private final CompoundTag initial;
    private final int kind;
    private final int customSlots;

    public DungeonMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), buffer.readVarInt(), buffer.readNbt());
    }

    public DungeonMenu(int id, Inventory inventory, BlockPos pos, int kind) {
        this(id, inventory, pos, kind, new CompoundTag());
    }

    private DungeonMenu(int id, Inventory inventory, BlockPos pos, int kind, CompoundTag initial) {
        super(DungeonMenus.type(kind), id);
        this.position = pos;
        this.kind = kind;
        this.initial = initial;
        block = inventory.player.level().getBlockEntity(pos) instanceof DungeonBlockEntity found ? found : null;
        customSlots = kind == CONTROLLER ? 45 : kind == MARKER ? 0 : 1;
        SimpleContainer fallback = new SimpleContainer(customSlots) {
            @Override public void setItem(int slot, ItemStack stack) {
                getItems().set(slot, stack);
                stack.limitSize(getMaxStackSize());
                setChanged();
            }
        };
        for (int slot = 0; slot < customSlots; slot++) {
            int x = kind == CONTROLLER ? 7 + slot % 9 * 18 : kind == MANAGE ? 223 : 38;
            int y = kind == CONTROLLER ? 42 + slot / 9 * 18 : kind == MANAGE ? 181 : 77;
            addSlot(new Slot(block == null ? fallback : block, slot, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return kind == MANAGE; }
                @Override public boolean mayPickup(Player player) { return kind == MANAGE; }
            });
        }
        if (kind != MARKER) {
            int y = kind == CONTROLLER ? 154 : kind == MANAGE ? 160 : 140;
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, y + row * 18));
            }
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, y + 58));
        }
    }

    public int kind() { return kind; }
    public CompoundTag data() { return block == null || block.getPersistentData().isEmpty() ? initial : block.getPersistentData(); }

    public void action(ServerPlayer player, String value, boolean attack) {
        if (!player.level().hasChunkAt(position) || player.level().getBlockEntity(position) != block || block == null) return;
        switch (kind) {
            case OPEN -> Dungeons.enter(player, position);
            case MANAGE -> DungeonSettings.entrance(block, value);
            case CONTROLLER -> DungeonSettings.controller(block, value);
            case MARKER -> DungeonSettings.marker(block, value, attack);
            default -> { }
        }
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < customSlots) {
            if (!moveItemStackTo(stack, customSlots, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (kind == MANAGE && moveItemStackTo(stack, 0, customSlots, false)) {
            slot.setChanged();
        } else {
            int hotbar = customSlots + 27;
            if (index < hotbar) moveItemStackTo(stack, hotbar, slots.size(), true);
            else moveItemStackTo(stack, customSlots, hotbar, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
