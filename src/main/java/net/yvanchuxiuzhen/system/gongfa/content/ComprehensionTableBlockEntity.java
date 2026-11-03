package net.yvanchuxiuzhen.system.gongfa.content;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;

/**
 * 参悟台的方块实体（来源 {@code CanwutaiBlockBlockEntity} 与方块实体数据）：一格物品槽（各面都可放取，只接受功法书；
 * 来源经漏斗可放入任何物品），
 * 模式 {@code canwufangshi}（“功法参悟”或“功法继承”）、界面三行说明 {@code wenben1}–{@code 3} 与可继承经验 {@code jingyanzong}。
 * 状态变化时同步给客户端，界面直接显示。
 */
public final class ComprehensionTableBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final String COMPREHEND = "功法参悟";
    public static final String INHERIT = "功法继承";
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private String mode = "";
    private final String[] texts = {"", "", ""};
    private double inheritable;

    public ComprehensionTableBlockEntity(BlockPos pos, BlockState state) {
        super(GongfaBlockEntities.COMPREHENSION_TABLE.get(), pos, state);
    }

    public String mode() {
        return mode;
    }

    public String text(int line) {
        return texts[line];
    }

    public double inheritable() {
        return inheritable;
    }

    /** 写入模式、三行说明与可继承经验后同步。 */
    public void show(String mode, String first, String second, String third, double inheritable) {
        this.mode = mode;
        texts[0] = first;
        texts[1] = second;
        texts[2] = third;
        this.inheritable = inheritable;
        changed();
    }

    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.set(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        mode = tag.getString("canwufangshi");
        for (int line = 0; line < 3; line++) {
            texts[line] = tag.getString("wenben" + (line + 1));
        }
        inheritable = tag.getDouble("jingyanzong");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putString("canwufangshi", mode);
        for (int line = 0; line < 3; line++) {
            tag.putString("wenben" + (line + 1), texts[line]);
        }
        tag.putDouble("jingyanzong", inheritable);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return Techniques.book(stack).isPresent();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}
