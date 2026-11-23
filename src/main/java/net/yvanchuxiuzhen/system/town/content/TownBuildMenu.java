package net.yvanchuxiuzhen.system.town.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.town.TownRuntime;

/** 建设容器的十八格输入与服务端建造按钮。 */
public final class TownBuildMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    private final TownBlockEntity plan;

    public TownBuildMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public TownBuildMenu(int id, Inventory inventory, BlockPos pos) {
        super(TownMenus.BUILD.get(), id);
        this.pos = pos;
        plan = inventory.player.level().getBlockEntity(pos) instanceof TownBlockEntity found ? found : null;
        IItemHandler internal = plan == null ? new ItemStackHandler(18) : plan.handler(null);
        for (int slot = 0; slot < 18; slot++) {
            addSlot(new SlotItemHandler(internal, slot, 7 + slot % 9 * 18, 63 + slot / 9 * 18) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    if (inventory.player instanceof ServerPlayer && plan != null && !plan.isRemoved()) TownRuntime.countMaterials(plan);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 104 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 162));
        if (inventory.player instanceof ServerPlayer && plan != null) inventory.player.level().scheduleTick(pos, plan.getBlockState().getBlock(), 1);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new TownBuildMenu(id, inventory, pos),
                Component.literal("建筑物资")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || !(player instanceof ServerPlayer serverPlayer) || plan == null || plan.isRemoved()) return false;
        TownRuntime.build(serverPlayer, plan);
        return true;
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
        if (!moveItemStackTo(stack, index < 18 ? 18 : 0, index < 18 ? slots.size() : 18, index < 18)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return original;
    }
}
