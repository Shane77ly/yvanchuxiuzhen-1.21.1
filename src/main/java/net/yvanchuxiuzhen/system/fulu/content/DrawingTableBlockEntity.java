package net.yvanchuxiuzhen.system.fulu.content;

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

/** 画符台三槽库存和共享选择；侧面只抽成品，所有插入途径均禁止向成品槽放入。 */
public final class DrawingTableBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private double runeLevel;
    private int head;
    private int headIndex;
    private int pillar;
    private int pillarIndex;
    private int core;
    private int coreIndex;
    private int needInk;
    private String fuluName = "";

    public DrawingTableBlockEntity(BlockPos pos, BlockState state) {
        super(FuluBlockEntities.DRAWING_TABLE.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public double runeLevel() { return runeLevel; }
    public void setRuneLevel(double runeLevel) { this.runeLevel = runeLevel; }
    public int head() { return head; }
    public void setHead(int head) { this.head = head; }
    public int headIndex() { return headIndex; }
    public void setHeadIndex(int headIndex) { this.headIndex = headIndex; }
    public int pillar() { return pillar; }
    public void setPillar(int pillar) { this.pillar = pillar; }
    public int pillarIndex() { return pillarIndex; }
    public void setPillarIndex(int pillarIndex) { this.pillarIndex = pillarIndex; }
    public int core() { return core; }
    public void setCore(int core) { this.core = core; }
    public int coreIndex() { return coreIndex; }
    public void setCoreIndex(int coreIndex) { this.coreIndex = coreIndex; }
    public int needInk() { return needInk; }
    public void setNeedInk(int needInk) { this.needInk = needInk; }
    public String fuluName() { return fuluName; }
    public void setFuluName(String fuluName) { this.fuluName = fuluName == null ? "" : fuluName; }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(3, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        runeLevel = tag.getDouble("rune_level");
        head = tag.getInt("head");
        headIndex = tag.getInt("head_index");
        pillar = tag.getInt("pillar");
        pillarIndex = tag.getInt("pillar_index");
        core = tag.getInt("core");
        coreIndex = tag.getInt("core_index");
        needInk = tag.getInt("need_ink");
        fuluName = tag.getString("fulu_name");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        tag.putDouble("rune_level", runeLevel);
        tag.putInt("head", head);
        tag.putInt("head_index", headIndex);
        tag.putInt("pillar", pillar);
        tag.putInt("pillar_index", pillarIndex);
        tag.putInt("core", core);
        tag.putInt("core_index", coreIndex);
        tag.putInt("need_ink", needInk);
        tag.putString("fulu_name", fuluName);
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
        return 3;
    }

    /** 来源容器写栈只限容器容量；制作可将同类同品阶结果累计到六十四件。 */
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
        return Component.translatable("block.yvanchuxiuzhen.huafutai");
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new DrawingTableMenu(id, inventory, worldPosition);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 2;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0, 1, 2};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 2;
    }
}
