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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 家具持有实际库存和文字朝向，六面与无方向能力共享同一份持久状态。 */
public final class UtilityFurnitureBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final UtilityFurnitureVariant variant;
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];

    public UtilityFurnitureBlockEntity(UtilityFurnitureVariant variant, BlockPos pos, BlockState state) {
        super(UtilityFurnitureContent.type(variant), pos, state);
        this.variant = variant;
        items = NonNullList.withSize(variant.slots(), ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public UtilityFurnitureVariant variant() { return variant; }
    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }
    public String displayDirection() { return getPersistentData().getString("fangxiang"); }
    @Override public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    public void placed(Direction direction) {
        getPersistentData().putString("fangxiang", switch (direction) {
            case NORTH -> "北";
            case SOUTH -> "南";
            case WEST -> "西";
            default -> "东";
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
    @Override public int getMaxStackSize() { return variant.stackLimit(); }
    /** 来源直接赋槽仅按家具容量截断，普通插入仍由菜单和能力限制物品自身堆叠。 */
    @Override public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize());
        setChanged();
    }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen." + variant.id()); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return variant == UtilityFurnitureVariant.TEAPOT ? ChestMenu.threeRows(id, inventory)
                : new DisplayFurnitureMenu(variant == UtilityFurnitureVariant.RACK, id, inventory, worldPosition);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public int[] getSlotsForFace(Direction side) { return IntStream.range(0, getContainerSize()).toArray(); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return variant == UtilityFurnitureVariant.TEAPOT || slot < 0 || slot >= getContainerSize();
    }
}
