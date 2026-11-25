package net.yvanchuxiuzhen.system.quest.content;

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

/** 告示牌拥有六个展示槽；来源仅第六槽允许自动化取放，菜单不开放展示槽。 */
public final class BountyBoardBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final CompoundTag[] entries = new CompoundTag[]{
            new CompoundTag(), new CompoundTag(), new CompoundTag(),
            new CompoundTag(), new CompoundTag(), new CompoundTag()
    };
    private long refreshClearAt;

    public BountyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(BountyBlockEntities.BOARD.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public CompoundTag entry(int index) {
        if (index < 1 || index > 6) return new CompoundTag();
        return entries[index - 1].copy();
    }

    public void entry(int index, CompoundTag value) {
        if (index < 1 || index > 6) return;
        entries[index - 1] = value == null ? new CompoundTag() : value.copy();
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public long refreshClearAt() {
        return refreshClearAt;
    }

    public void setRefreshClearAt(long gameTime) {
        this.refreshClearAt = gameTime;
        setChanged();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        for (int index = 1; index <= 6; index++) {
            entries[index - 1] = tag.getCompound("xvanshangweizhi" + index).copy();
        }
        refreshClearAt = tag.getLong("refresh_clear_at");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        for (int index = 1; index <= 6; index++) {
            tag.put("xvanshangweizhi" + index, entries[index - 1]);
        }
        if (refreshClearAt > 0) tag.putLong("refresh_clear_at", refreshClearAt);
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
        return 6;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
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
        return Component.translatable("block.yvanchuxiuzhen.xvanshang");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new BountyBoardMenu(id, inventory, worldPosition);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 5;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, 6).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 5;
    }
}
