package net.yvanchuxiuzhen.system.cooking.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 餐具的单槽库存归烹饪独占写入，服务端取放并同步堆叠显示。 */
public final class TablewareBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];

    public TablewareBlockEntity(BlockPos pos, BlockState state) {
        super(TablewareBlockEntities.type(state), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public TablewareBlock.Kind kind() {
        return ((TablewareBlock) getBlockState().getBlock()).kind();
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    /** 自然方块使用始终读取主手；创造模式同样扣除叠放的餐具。 */
    public void use(Player player) {
        ItemStack held = player.getMainHandItem();
        if (player.isShiftKeyDown()) {
            take(player);
        } else if (held.is(getBlockState().getBlock().asItem())) {
            ItemStack stored = getItem(0);
            int count;
            if (!stored.is(getBlockState().getBlock().asItem())) {
                count = 1;
            } else if (stored.getCount() < getMaxStackSize()) {
                count = stored.getCount() + 1;
            } else {
                return;
            }
            setItem(0, new ItemStack(getBlockState().getBlock(), count));
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            if (held.getCount() - 1 >= 1) {
                held.setCount(held.getCount() - 1);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
            } else {
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            player.getInventory().setChanged();
        } else if (held.isEmpty()) {
            take(player);
        }
    }

    private void take(Player player) {
        ItemStack stored = getItem(0).copy();
        if (!stored.isEmpty()) {
            int count = stored.getCount();
            ItemHandlerHelper.giveItemToPlayer(player, stored.copyWithCount(1));
            setItem(0, count - 1 >= 1 ? stored.copyWithCount(count - 1) : ItemStack.EMPTY);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(getBlockState().getBlock()));
            level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** 强制赋槽只按来源容器容量截量，自动化插入仍遵循包装器的物品堆叠限制。 */
    @Override
    public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
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
    public int getMaxStackSize() { return kind().capacity(); }

    @Override
    public int getContainerSize() { return 1; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    protected NonNullList<ItemStack> getItems() { return items; }

    @Override
    protected void setItems(NonNullList<ItemStack> items) { this.items = items; }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.yvanchuxiuzhen." + kind().id());
    }

    @Override
    public Component getDisplayName() { return getDefaultName(); }

    /** 来源提供者打开独立空的二十七槽箱，不绑定本实体单槽库存。 */
    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) { return new int[] {0}; }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) { return true; }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
}
