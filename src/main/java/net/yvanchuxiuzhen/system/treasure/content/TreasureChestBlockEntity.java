package net.yvanchuxiuzhen.system.treasure.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 宝箱单件库存、开箱参数及动画状态随方块保存；无方向及六侧共享唯一物品槽。 */
public final class TreasureChestBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private boolean opened;
    private double tickCount;
    private String state = "";

    public TreasureChestBlockEntity(BlockPos pos, BlockState state) {
        super(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().equals("treasure_chest_11")
                ? TreasureBlockEntities.CHEST_11.get() : TreasureBlockEntities.CHEST_03.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
        }
    }

    public boolean opened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public double tickCount() {
        return tickCount;
    }

    public void setTickCount(double tickCount) {
        this.tickCount = tickCount;
    }

    public String openState() {
        return state;
    }

    public void setOpenState(String state) {
        this.state = state == null ? "" : state;
    }

    public boolean locked() {
        return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath().equals("treasure_chest_11");
    }

    public int animation() {
        return getBlockState().getValue(((TreasureChestBlock) getBlockState().getBlock()).animationProperty());
    }

    public void animate(int animation) {
        level.setBlock(worldPosition, getBlockState().setValue(((TreasureChestBlock) getBlockState().getBlock()).animationProperty(), animation), Block.UPDATE_ALL);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public void sync() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
        tryLoadLootTable(tag);
        ContainerHelper.loadAllItems(tag, items, registries);
        opened = tag.getBoolean("dakai");
        tickCount = tag.getDouble("tick");
        state = tag.getString("zhuangtai");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (opened) tag.putBoolean("dakai", true);
        if (tickCount != 0) tag.putDouble("tick", tickCount);
        if (!state.isEmpty()) tag.putString("zhuangtai", state);
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
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.yvanchuxiuzhen." + BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath());
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idlePredicate));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedurePredicate));
    }

    private PlayState idlePredicate(AnimationState<TreasureChestBlockEntity> state) {
        if (animation() == 0) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("0"));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<TreasureChestBlockEntity> state) {
        int animation = animation();
        AnimationController<TreasureChestBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                animate(0);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
