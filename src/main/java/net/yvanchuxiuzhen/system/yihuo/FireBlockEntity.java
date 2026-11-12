package net.yvanchuxiuzhen.system.yihuo;

import java.util.Set;
import java.util.stream.Collectors;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 火块保存燃料与施放者；友方表在放置后共享只读引用，重载后独立，容器为零槽。 */
public final class FireBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private Set<String> friendlyTeams = Set.of();
    private final IItemHandler[] handlers = new IItemHandler[Direction.values().length];

    public FireBlockEntity(BlockPos pos, BlockState state) {
        super(YihuoBlockEntities.type(state), pos, state);
        for (Direction direction : Direction.values()) handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    double power() {
        return getPersistentData().getDouble("power");
    }

    void power(double value) {
        getPersistentData().putDouble("power", value);
        changed();
    }

    public void initialize(Player owner, double power) {
        CompoundTag data = getPersistentData();
        data.putDouble("power", power);
        data.putString("ManaAttackUUID", owner.getStringUUID());
        data.putString("team", Relations.entityTeam(owner));
        friendlyTeams = Relations.friendlyTeams(owner);
        storeFriendlyTeams();
        changed();
    }

    Set<String> friendlyTeams() {
        return friendlyTeams;
    }

    private void storeFriendlyTeams() {
        CompoundTag friends = new CompoundTag();
        for (String team : friendlyTeams) friends.putBoolean(team, true);
        getPersistentData().put("friendlist", friends);
    }

    IItemHandler handler(Direction direction) {
        return handlers[direction.ordinal()];
    }

    private void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        CompoundTag friends = getPersistentData().getCompound("friendlist");
        friendlyTeams = friends.getAllKeys().stream().filter(friends::getBoolean).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        storeFriendlyTeams();
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithFullMetadata(registries);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal(((FireBlock) getBlockState().getBlock()).traits().row().id());
    }

    @Override
    public Component getDisplayName() {
        String number = ((FireBlock) getBlockState().getBlock()).traits().row().id().substring(8, 11);
        return Component.literal("Cailiao " + number.charAt(0) + " " + number.charAt(1) + " " + number.charAt(2) + "fireblock");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        items = stacks;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[0];
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
