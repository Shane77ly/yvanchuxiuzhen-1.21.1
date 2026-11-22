package net.yvanchuxiuzhen.system.economy.content;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** 来源经济方块的实际库存和附加数据，服务端是唯一写入者，更新包提供界面只读显示。 */
public final class EconomyBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items;
    private CompoundTag state = new CompoundTag();
    private boolean auctionRejected;
    private final IItemHandler handler = new InvWrapper(this);

    public EconomyBlockEntity(BlockPos pos, BlockState blockState) {
        super(EconomyBlockEntities.type(blockState), pos, blockState);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    }

    public boolean auction() { return ((EconomyBlock) getBlockState().getBlock()).auction(); }
    public CompoundTag state() { return state; }
    public boolean auctionRejected() { return auctionRejected; }
    public void rejectAuction() { auctionRejected = true; }
    public void rearmAuction() { auctionRejected = false; }
    public IItemHandler handler(Direction direction) { return handler; }
    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        changed();
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        state = tag.getCompound("economy").copy();
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        tag.put("economy", state.copy());
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return auction() ? 21 : 7; }
    @Override public int getMaxStackSize() { return auction() ? 1 : 64; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> contents) { items = contents; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen." + (auction() ? "paimaiblock" : "shopblock_01")); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new CommerceMenu(id, inventory, worldPosition, auction() ? CommerceMenu.Kind.AUCTION_SETTINGS : CommerceMenu.Kind.PLAYER_SHOP); }
    @Override public int[] getSlotsForFace(Direction direction) { return IntStream.range(0, getContainerSize()).toArray(); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
}
