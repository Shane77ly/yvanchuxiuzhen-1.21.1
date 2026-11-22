package net.yvanchuxiuzhen.system.town.content;

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

/** 城镇容器与规划、所有权状态随方块保存；全部槽允许来源侧面物品操作。 */
public final class TownBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final boolean planning;
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];

    public TownBlockEntity(BlockPos pos, BlockState state, boolean planning) {
        super(planning ? TownBlockEntities.PLANNING.get() : TownBlockEntities.CENTER.get(), pos, state);
        this.planning = planning;
        items = NonNullList.withSize(planning ? 18 : 1, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(planning ? 18 : 1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
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
        return items.size();
    }

    @Override
    public int getMaxStackSize() {
        return planning ? 64 : 1;
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
        return Component.translatable("block.yvanchuxiuzhen." + (planning ? "chengzhen_buildblock" : "chengzhen_antai"));
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return planning ? new TownBuildMenu(id, inventory, worldPosition) : new TownMenu(id, inventory, worldPosition, 2);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}
