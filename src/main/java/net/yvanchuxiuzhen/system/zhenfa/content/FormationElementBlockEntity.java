package net.yvanchuxiuzhen.system.zhenfa.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.platform.ModId;

/** 阵元拥有材料、伪装和阵基检测写入的坐标与角度；玩家操作只在服务端执行。 */
public final class FormationElementBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private BlockPos formationOrigin = BlockPos.ZERO;
    private float displayAngle;

    public FormationElementBlockEntity(BlockPos pos, BlockState state) {
        super(ZhenfaBlockEntities.ELEMENT.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    /** 材料查询返回副本，布局检测不获得本实体库存的写权限。 */
    public ItemStack material() {
        return getItem(0).copy();
    }

    public ItemStack disguise() {
        return getItem(1).copy();
    }

    public BlockPos formationOrigin() {
        return formationOrigin;
    }

    public float displayAngle() {
        return displayAngle;
    }

    /** 对应来源第四检测过程写入的三个整数坐标与单精度展示角度。 */
    public void assignFormation(BlockPos origin, float angle) {
        formationOrigin = origin.immutable();
        displayAngle = angle;
        setChanged();
        synchronize();
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    /** 来源读取当前主手：材料与空手控制槽零，其他方块物品只切换槽一伪装。 */
    public void use(Player player) {
        ItemStack held = player.getMainHandItem();
        boolean insert = false;
        boolean take = false;
        if (held.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "buzhencailiao")))) {
            insert = getItem(0).isEmpty();
            take = !insert;
        } else if (held.isEmpty()) {
            take = !getItem(0).isEmpty();
        } else if (held.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) {
            setItem(1, getItem(1).isEmpty() ? new ItemStack(held.getItem()) : ItemStack.EMPTY);
        }
        if (getItem(0).getCount() > 1) setItem(0, getItem(0).copyWithCount(1));
        if (insert) {
            setItem(0, held.copyWithCount(1));
            held.shrink(1);
            player.getInventory().setChanged();
        } else if (take) {
            ItemHandlerHelper.giveItemToPlayer(player, getItem(0).copyWithCount(1));
            setItem(0, ItemStack.EMPTY);
        }
        synchronize();
    }

    private void synchronize() {
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        CompoundTag state = tag.getCompound("FormationElement");
        formationOrigin = new BlockPos(state.getInt("OriginX"), state.getInt("OriginY"), state.getInt("OriginZ"));
        displayAngle = state.getFloat("DisplayAngle");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        CompoundTag state = new CompoundTag();
        state.putInt("OriginX", formationOrigin.getX());
        state.putInt("OriginY", formationOrigin.getY());
        state.putInt("OriginZ", formationOrigin.getZ());
        state.putFloat("DisplayAngle", displayAngle);
        tag.put("FormationElement", state);
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
        return 2;
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
        return Component.translatable("block.yvanchuxiuzhen.zhenyvan");
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    /** 来源提供者创建独立二十七槽箱，方块使用本身不打开此菜单。 */
    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {0, 1};
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
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
