package net.yvanchuxiuzhen.system.quest.content;

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

/** 任务伪装方块持有一个展示槽及自身的连续任务、商店配置。 */
public final class DisguisedQuestBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private String displayDirection = "";
    private boolean showParticles;
    private boolean refreshDue;
    private int refreshTick;
    private final String[] renwulist = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final String[] tasklist = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final String[] shopinformation = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final String[] productlist = new String[]{"", "", "", "", "", "", "", "", "", ""};

    public DisguisedQuestBlockEntity(BlockPos pos, BlockState state) {
        super(QuestBlockEntities.DISGUISED.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public String displayDirection() { return displayDirection; }
    public boolean showParticles() { return showParticles; }
    public boolean refreshDue() { return refreshDue; }
    public int refreshTick() { return refreshTick; }
    public String renwulist(int row) { return row >= 1 && row <= 10 ? renwulist[row - 1] : ""; }
    public String tasklist(int row) { return row >= 1 && row <= 10 ? tasklist[row - 1] : ""; }
    public String shopInformation(int row) { return row >= 1 && row <= 10 ? shopinformation[row - 1] : ""; }
    public String productList(int row) { return row >= 1 && row <= 10 ? productlist[row - 1] : ""; }

    public void setDisplayDirection(String value) { displayDirection = value == null ? "" : value; setChanged(); }
    public void setShowParticles(boolean value) { showParticles = value; setChanged(); }
    public void setRefreshDue(boolean value) { refreshDue = value; setChanged(); }
    public void setRefreshTick(int value) { refreshTick = value; setChanged(); }
    public void setRenwulist(int row, String value) { if (row >= 1 && row <= 10) { renwulist[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setTasklist(int row, String value) { if (row >= 1 && row <= 10) { tasklist[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setShopInformation(int row, String value) { if (row >= 1 && row <= 10) { shopinformation[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setProductList(int row, String value) { if (row >= 1 && row <= 10) { productlist[row - 1] = value == null ? "" : value; setChanged(); } }

    public void setField(String key, String value) {
        if ("fangxiang".equals(key)) { setDisplayDirection(value); return; }
        for (int row = 1; row <= 10; row++) {
            if (("renwulist" + row).equals(key)) { setRenwulist(row, value); return; }
            if (("tasklist" + row).equals(key)) { setTasklist(row, value); return; }
            if (("shopinformation" + row).equals(key)) { setShopInformation(row, value); return; }
            if (("productlist" + row).equals(key)) { setProductList(row, value); return; }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        displayDirection = tag.getString("fangxiang");
        showParticles = tag.getBoolean("lizixianshi");
        refreshDue = tag.getBoolean("shuaxin");
        refreshTick = tag.getInt("shuaxintick");
        for (int row = 1; row <= 10; row++) {
            renwulist[row - 1] = tag.getString("renwulist" + row);
            tasklist[row - 1] = tag.getString("tasklist" + row);
            shopinformation[row - 1] = tag.getString("shopinformation" + row);
            productlist[row - 1] = tag.getString("productlist" + row);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!displayDirection.isEmpty()) tag.putString("fangxiang", displayDirection);
        if (showParticles) tag.putBoolean("lizixianshi", true);
        if (refreshDue) tag.putBoolean("shuaxin", true);
        if (refreshTick != 0) tag.putInt("shuaxintick", refreshTick);
        for (int row = 1; row <= 10; row++) {
            if (!renwulist[row - 1].isEmpty()) tag.putString("renwulist" + row, renwulist[row - 1]);
            if (!tasklist[row - 1].isEmpty()) tag.putString("tasklist" + row, tasklist[row - 1]);
            if (!shopinformation[row - 1].isEmpty()) tag.putString("shopinformation" + row, shopinformation[row - 1]);
            if (!productlist[row - 1].isEmpty()) tag.putString("productlist" + row, productlist[row - 1]);
        }
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
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.getFirst().isEmpty();
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
        return Component.translatable("block.yvanchuxiuzhen.renwuweizhuangfangkuai");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot != 1;
    }
}
