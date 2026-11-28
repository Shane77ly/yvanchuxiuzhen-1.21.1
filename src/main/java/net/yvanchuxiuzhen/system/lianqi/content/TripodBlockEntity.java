package net.yvanchuxiuzhen.system.lianqi.content;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 炼器鼎保存十二槽库存，侧面仅允许提取成品槽九及隐藏构造槽十一。 */
public final class TripodBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(12, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private String tripodName = "";
    private int tripodLevel;
    private int tripodFire;
    private int tripodPurification;
    private int direction;
    private int tempAngle;
    private int refinerLevel;
    private double lianPower;
    private boolean refining;
    private String refiningLabel = "";
    private boolean fireToggle;
    private double firePower;
    private int firePowerTick;
    private int lianqiTick;
    private int lianqiTickMax;
    private int progress;
    private int explodeTick;
    private int explodeLevel;
    private int pendingExperience;
    private boolean failed;
    private ItemStack plannedItem = ItemStack.EMPTY;

    public TripodBlockEntity(BlockPos pos, BlockState state) {
        super(LianqiBlockEntities.type(((TripodBlock) state.getBlock()).tier()), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public int tier() {
        return ((TripodBlock) getBlockState().getBlock()).tier();
    }

    public String tripodName() { return tripodName; }
    public void setTripodName(String value) { tripodName = value == null ? "" : value; }
    public int tripodLevel() { return tripodLevel; }
    public void setTripodLevel(int value) { tripodLevel = value; }
    public int tripodFire() { return tripodFire; }
    public void setTripodFire(int value) { tripodFire = value; }
    public int tripodPurification() { return tripodPurification; }
    public void setTripodPurification(int value) { tripodPurification = value; }
    public int direction() { return direction; }
    public void setDirection(int value) { direction = value; }
    public int tempAngle() { return tempAngle; }
    public void setTempAngle(int value) { tempAngle = value; }
    public int refinerLevel() { return refinerLevel; }
    public void setRefinerLevel(int value) { refinerLevel = value; }
    public double lianPower() { return lianPower; }
    public int lianPowerInt() { return (int) lianPower; }
    public void setLianPower(double value) { lianPower = value; }
    public boolean refining() { return refining; }
    public void setRefining(boolean value) { refining = value; }
    public String refiningLabel() { return refiningLabel; }
    public void setRefiningLabel(String value) { refiningLabel = value == null ? "" : value; }
    public boolean fireToggle() { return fireToggle; }
    public void setFireToggle(boolean value) { fireToggle = value; }
    public double firePower() { return firePower; }
    public int firePowerInt() { return (int) firePower; }
    public void setFirePower(double value) { firePower = value; }
    public int firePowerTick() { return firePowerTick; }
    public void setFirePowerTick(int value) { firePowerTick = value; }
    public int lianqiTick() { return lianqiTick; }
    public void setLianqiTick(int value) { lianqiTick = value; }
    public int lianqiTickMax() { return lianqiTickMax; }
    public void setLianqiTickMax(int value) { lianqiTickMax = value; }
    public int progress() { return progress; }
    public void setProgress(int value) { progress = value; }
    public int explodeTick() { return explodeTick; }
    public void setExplodeTick(int value) { explodeTick = value; }
    public int explodeLevel() { return explodeLevel; }
    public void setExplodeLevel(int value) { explodeLevel = value; }
    public int pendingExperience() { return pendingExperience; }
    public void setPendingExperience(int value) { pendingExperience = value; }
    public boolean failed() { return failed; }
    public void setFailed(boolean value) { failed = value; }
    public ItemStack plannedItem() { return plannedItem; }
    public void setPlannedItem(ItemStack value) { plannedItem = value == null ? ItemStack.EMPTY : value.copy(); }

    public void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(12, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items, registries);
        tripodName = tag.getString("TripodName");
        tripodLevel = tag.getInt("TripodLevel");
        tripodFire = tag.getInt("TripodFire");
        tripodPurification = tag.getInt("TripodPurification");
        direction = tag.getInt("fangxiang");
        tempAngle = tag.getInt("jiaodulinshi");
        refinerLevel = tag.getInt("RefinerLEVEL");
        lianPower = tag.getDouble("lianpower");
        refining = tag.getBoolean("lianqiBegin");
        refiningLabel = tag.getString("lianqiwenzi");
        fireToggle = tag.getBoolean("gengxin");
        firePower = tag.getDouble("firepower");
        firePowerTick = tag.getInt("firepowertick");
        lianqiTick = tag.getInt("lianqiTick");
        lianqiTickMax = tag.getInt("lianqiTickMax");
        progress = tag.getInt("lianzhijindu");
        explodeTick = tag.getInt("zhadingtick");
        explodeLevel = tag.getInt("zhadinglevel");
        pendingExperience = tag.getInt("jingyanhuoqv");
        failed = tag.getBoolean("lianqishibai");
        plannedItem = tag.contains("item") ? ItemStack.parseOptional(registries, tag.getCompound("item")) : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!tripodName.isEmpty()) tag.putString("TripodName", tripodName);
        if (tripodLevel != 0) tag.putInt("TripodLevel", tripodLevel);
        if (tripodFire != 0) tag.putInt("TripodFire", tripodFire);
        if (tripodPurification != 0) tag.putInt("TripodPurification", tripodPurification);
        if (direction != 0) tag.putInt("fangxiang", direction);
        if (tempAngle != 0) tag.putInt("jiaodulinshi", tempAngle);
        if (refinerLevel != 0) tag.putInt("RefinerLEVEL", refinerLevel);
        if (lianPower != 0) tag.putDouble("lianpower", lianPower);
        if (refining) tag.putBoolean("lianqiBegin", true);
        if (!refiningLabel.isEmpty()) tag.putString("lianqiwenzi", refiningLabel);
        if (fireToggle) tag.putBoolean("gengxin", true);
        if (firePower != 0) tag.putDouble("firepower", firePower);
        if (firePowerTick != 0) tag.putInt("firepowertick", firePowerTick);
        if (lianqiTick != 0) tag.putInt("lianqiTick", lianqiTick);
        if (lianqiTickMax != 0) tag.putInt("lianqiTickMax", lianqiTickMax);
        if (progress != 0) tag.putInt("lianzhijindu", progress);
        if (explodeTick != 0) tag.putInt("zhadingtick", explodeTick);
        if (explodeLevel != 0) tag.putInt("zhadinglevel", explodeLevel);
        if (pendingExperience != 0) tag.putInt("jingyanhuoqv", pendingExperience);
        if (failed) tag.putBoolean("lianqishibai", true);
        if (!plannedItem.isEmpty()) tag.put("item", plannedItem.save(registries));
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
        return 12;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
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
        return Component.translatable("block.yvanchuxiuzhen.tripod_0" + tier());
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return tier() == 1 ? ChestMenu.threeRows(id, inventory) : new TripodMenu(id, inventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return IntStream.range(0, 12).toArray();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 9;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 9 || slot == 11;
    }
}
