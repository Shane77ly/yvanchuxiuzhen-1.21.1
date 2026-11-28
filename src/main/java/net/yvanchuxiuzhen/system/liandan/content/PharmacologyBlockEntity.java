package net.yvanchuxiuzhen.system.liandan.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.yvanchuxiuzhen.system.liandan.LiandanHerbs.HerbCatalogSnapshot;

/** 药理台保存十一格可见物品与一格隐藏预览，六面仅允许抽取两个说明槽。 */
public final class PharmacologyBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(12, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private HerbCatalogSnapshot herbs = HerbCatalogSnapshot.EMPTY;
    private final String[] effectLines = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", ""};

    public PharmacologyBlockEntity(BlockPos pos, BlockState state) {
        super(LiandanBlockEntities.PHARMACOLOGY.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public HerbCatalogSnapshot herbs() { return herbs; }
    public void setHerbs(HerbCatalogSnapshot value) { herbs = value == null ? HerbCatalogSnapshot.EMPTY : value; }
    public String effectLine(int index) { return index >= 0 && index < effectLines.length ? effectLines[index] : ""; }
    public void setEffectLine(int index, String value) {
        if (index >= 0 && index < effectLines.length) effectLines[index] = value == null ? "" : value;
    }

    /** 实体放置时广播来源药性快照；按钮刷新显示由两端分别计算。 */
    public void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(12, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        herbs = tag.contains("zong")
                ? HerbCatalogSnapshot.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("zong"))
                        .result().orElse(HerbCatalogSnapshot.EMPTY)
                : HerbCatalogSnapshot.EMPTY;
        for (int index = 0; index < effectLines.length; index++) {
            effectLines[index] = tag.getString("effectxianshi" + index);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!herbs.isEmpty()) {
            HerbCatalogSnapshot.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), herbs)
                    .result().ifPresent(nbt -> tag.put("zong", nbt));
        }
        for (int index = 0; index < effectLines.length; index++) {
            if (!effectLines[index].isEmpty()) tag.putString("effectxianshi" + index, effectLines[index]);
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
        return 12;
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
        return Component.literal("yaolitaiblock");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yvanchuxiuzhen.yaolitaiblock");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new PharmacologyMenu(id, inventory, worldPosition);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 11;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot != 11;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 9 || slot == 10;
    }
}
