package net.yvanchuxiuzhen.collection.tiandao;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;

/** 保存来源多结构分组配置与零槽容器身份，寻木按层衰减并逐片摆放来源模板。 */
public final class MultiStructureBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private static final ResourceLocation WOOD = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ancienttimes_wood_01");
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];

    public MultiStructureBlockEntity(BlockPos pos, BlockState state) {
        super(MultiStructureContent.TYPE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
        }
    }

    public IItemHandler handler(Direction direction) {
        return isRemoved() ? null : direction == null ? unsided : sided[direction.ordinal()];
    }

    void generate(ServerLevel level) {
        if (!getPersistentData().getString("jiegouzu").equals("寻木")) {
            return;
        }
        int thickness = Mth.nextInt(RandomSource.create(), 10, 15);
        int length = Mth.nextInt(RandomSource.create(), 5, 12);
        for (int layer = 0; layer < length; layer++) {
            for (int piece = 0; piece < thickness - layer; piece++) {
                BlockPos origin = worldPosition.offset(Mth.nextInt(RandomSource.create(), -4, 4),
                        layer * 5 + Mth.nextInt(RandomSource.create(), -4, 4), Mth.nextInt(RandomSource.create(), -4, 4));
                WorldStructures.place(level, WOOD, origin);
            }
        }
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.tiandaoduojiegou"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) {
            items = NonNullList.withSize(0, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items, registries);
        }
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
