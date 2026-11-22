package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 单槽诱饵与强制开笼计数随方块保存；方向能力禁止自动放取，无方向能力供来源操作直接赋槽。 */
public final class RabbitCageBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];

    public RabbitCageBlockEntity(BlockPos pos, BlockState state) {
        super(RabbitCageBlockEntities.OPEN.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return isRemoved() ? null : direction == null ? unsided : sided[direction.ordinal()]; }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override protected void loadAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.loadAdditional(data, registries);
        if (!tryLoadLootTable(data)) items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(data, items, registries);
    }

    @Override protected void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        if (!trySaveLootTable(data)) ContainerHelper.saveAllItems(data, items, registries);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.zhuangshi_tulong_02"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot != 0; }
    @Override public int[] getSlotsForFace(Direction side) { return new int[]{0}; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot != 0; }
}
