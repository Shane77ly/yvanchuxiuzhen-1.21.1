package net.yvanchuxiuzhen.collection.decor;

import java.util.stream.IntStream;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 家具库存由服务端方块实体保存；六面允许放入，取出仅由无方向能力和玩家菜单完成。 */
public final class FurnitureStorageBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final FurnitureStorageVariant variant;
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];

    public FurnitureStorageBlockEntity(FurnitureStorageVariant variant, BlockPos pos, BlockState state) {
        super(FurnitureStorageContent.blockEntityType(variant), pos, state);
        this.variant = variant;
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithFullMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        return variant.layout().slots();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    /** 来源容器的直接写入只按六十四件裁剪；菜单与能力另行遵守物品自身堆叠上限。 */
    @Override
    public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize());
        setChanged();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.yvanchuxiuzhen." + variant.id());
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new FurnitureStorageMenu(variant.layout(), id, inventory, worldPosition);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot < 0 || slot >= getContainerSize();
    }
}
