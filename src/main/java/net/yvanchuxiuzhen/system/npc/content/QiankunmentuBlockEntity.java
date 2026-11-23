package net.yvanchuxiuzhen.system.npc.content;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.system.npc.api.QiankunQuestRequestEvent;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 门徒方块实体保存来源单槽库存与 renwuset 数值。 */
public final class QiankunmentuBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QiankunmentuBlockEntity(BlockPos pos, BlockState state) {
        super(QiankunBlockEntities.DISCIPLE.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }

    public void interact(Player player) {
        if (level == null || level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (player.isCreative() && player.isShiftKeyDown()) {
            QiankunMenus.open(serverPlayer, worldPosition);
            return;
        }
        if (!player.isShiftKeyDown()) {
            int questIndex = (int) renwuset();
            if (questIndex == 0) {
                serverPlayer.displayClientMessage(Component.literal("......"), false);
            } else if (questIndex >= 1) {
                NeoForge.EVENT_BUS.post(new QiankunQuestRequestEvent(serverPlayer, questIndex));
            }
        }
    }

    public double renwuset() { return getPersistentData().getDouble("renwuset"); }
    public void incrementRenwuset() {
        double value = renwuset() + 1;
        getPersistentData().putDouble("renwuset", value);
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        if (value >= 10) {
            getPersistentData().putDouble("renwuset", 0);
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return 1; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.qiankunmentu"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[] {0}; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idle));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }
    private PlayState idle(AnimationState<QiankunmentuBlockEntity> state) {
        return getBlockState().getValue(QiankunmentuBlock.ANIMATION) == 0 ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP;
    }
    private PlayState procedure(AnimationState<QiankunmentuBlockEntity> state) {
        int animation = getBlockState().getValue(QiankunmentuBlock.ANIMATION);
        AnimationController<QiankunmentuBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(QiankunmentuBlock.ANIMATION, 0), Block.UPDATE_ALL);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
