package net.yvanchuxiuzhen.system.dungeon.content;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
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

/** 入口两槽、控制器四十五槽、占位符零槽与画卷单槽分别持久化；空侧沿用来源父类的无方向库存。 */
public final class DungeonBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private final String kind;
    private NonNullList<ItemStack> items;
    private final IItemHandler[] sided = new IItemHandler[6];
    private final IItemHandler unsided = new InvWrapper(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private CompoundTag state = new CompoundTag();

    public DungeonBlockEntity(BlockPos pos, BlockState state) {
        super(DungeonBlockEntities.typeFor(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()), pos, state);
        kind = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        items = NonNullList.withSize(kind.equals("fubenblock") ? 45 : kind.equals("tiandaofuben") ? 2 : kind.equals("fabao_009") ? 1 : 0, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public String kind() { return kind; }
    public CompoundTag state() { return state; }
    public IItemHandler handler(Direction direction) { return isRemoved() ? null : direction == null ? unsided : sided[direction.ordinal()]; }

    public void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return items.size(); }
    @Override public int getMaxStackSize() { return kind.equals("fabao_009") ? 1 : 64; }
    @Override public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize());
        setChanged();
    }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen." + kind); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        if (kind.equals("fubenblock")) return new DungeonMenu(id, inventory, worldPosition, DungeonMenu.CONTROLLER);
        if (kind.equals("fubenentity")) return new DungeonMenu(id, inventory, worldPosition, DungeonMenu.MARKER);
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        tryLoadLootTable(tag);
        ContainerHelper.loadAllItems(tag, items, registries);
        state = tag.getCompound("dungeon_state").copy();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!state.isEmpty()) tag.put("dungeon_state", state);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int[] getSlotsForFace(Direction direction) { return IntStream.range(0, getContainerSize()).toArray(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return !kind.equals("fubenblock") && !kind.equals("fabao_009"); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }

    public int animation() { return getBlockState().getValue(((DungeonPortalBlock) getBlockState().getBlock()).animationProperty()); }
    public void animate(int animation) {
        level.setBlock(worldPosition, getBlockState().setValue(((DungeonPortalBlock) getBlockState().getBlock()).animationProperty(), animation), 3);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> animation() == 0
                ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }

    private PlayState procedure(AnimationState<DungeonBlockEntity> state) {
        AnimationController<DungeonBlockEntity> controller = state.getController();
        if (animation() != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation()), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                animate(0);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
