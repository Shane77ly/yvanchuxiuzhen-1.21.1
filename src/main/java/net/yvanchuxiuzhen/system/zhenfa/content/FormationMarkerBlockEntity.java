package net.yvanchuxiuzhen.system.zhenfa.content;

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
import net.yvanchuxiuzhen.platform.ModId;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 母旗、子旗和罗盘保存各自库存与来源团队/绑定状态，并共同播放 Geo 动画。 */
public final class FormationMarkerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String kind;

    public FormationMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(ZhenfaBlockEntities.markerType(state), pos, state);
        kind = FormationBlockIds.path(state);
        items = NonNullList.withSize(kind.equals("luopan") ? 9 : 1, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public boolean compass() { return kind.equals("luopan"); }
    public String team() { return getPersistentData().getString("team"); }
    public boolean childBound() { return getPersistentData().getBoolean("qimubiaoshezhi"); }
    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }
    public void team(String team) {
        getPersistentData().putString("team", team);
        changed();
    }
    public void childBound(boolean bound) {
        getPersistentData().putBoolean("qimubiaoshezhi", bound);
        changed();
    }
    public void animation(int animation) {
        level.setBlock(worldPosition, getBlockState().setValue(FormationMarkerBlock.ANIMATION, animation), 3);
    }
    public void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return compass() ? 9 : 1; }
    @Override public int getMaxStackSize() { return compass() ? 64 : 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block." + ModId.VALUE + "." + kind); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return compass() ? IntStream.range(0, 9).toArray() : new int[] {0}; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> getBlockState().getValue(FormationMarkerBlock.ANIMATION) == 0
                ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }
    private PlayState procedure(AnimationState<FormationMarkerBlockEntity> state) {
        AnimationController<FormationMarkerBlockEntity> controller = state.getController();
        int animation = getBlockState().getValue(FormationMarkerBlock.ANIMATION);
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(FormationMarkerBlock.ANIMATION, 0), 3);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
