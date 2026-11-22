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

/** 展示家具持有单件槽和放置时的文字朝向，展示锁状态由公共附件持有。 */
public final class DisplayFurnitureBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final DisplayFurnitureVariant variant;
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];

    public DisplayFurnitureBlockEntity(DisplayFurnitureVariant variant, BlockPos pos, BlockState state) {
        super(DisplayFurnitureContent.type(variant), pos, state);
        this.variant = variant;
        items = NonNullList.withSize(variant.slots(), ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public DisplayFurnitureVariant variant() { return variant; }
    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }
    public String displayDirection() { return getPersistentData().getString("fangxiang"); }
    public void placed(Direction direction) {
        getPersistentData().putString("fangxiang", switch (direction) {
            case NORTH -> "北";
            case SOUTH -> "南";
            case WEST -> "西";
            case EAST -> "东";
            default -> throw new IllegalArgumentException("展示家具只使用水平方向");
        });
        setChanged();
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return variant.slots(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen." + variant.id()); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DisplayFurnitureMenu(variant.cabinet(), id, inventory, worldPosition);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public int[] getSlotsForFace(Direction side) { return IntStream.range(0, getContainerSize()).toArray(); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return variant.cabinet() || slot != 0; }
}
