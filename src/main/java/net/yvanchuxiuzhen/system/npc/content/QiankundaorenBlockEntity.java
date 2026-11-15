package net.yvanchuxiuzhen.system.npc.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 乾坤道人方块实体保存按玩家去重的状态及54次烟雾周期。 */
public final class QiankundaorenBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QiankundaorenBlockEntity(BlockPos pos, BlockState state) {
        super(QiankunBlockEntities.DAO.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }
    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }

    public void interact(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || level == null || level.isClientSide) return;
        String key = serverPlayer.getUUID() + "zhuangtai";
        CompoundTag data = getPersistentData();
        if (data.getString(key).isEmpty()) {
            level.setBlock(worldPosition, getBlockState().setValue(QiankunBlock.ANIMATION, 1), Block.UPDATE_ALL);
            data.putBoolean("yanwubaofa", true);
            data.putString(key, "准备");
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void tickSmoke(ServerLevel level) {
        CompoundTag data = getPersistentData();
        if (!data.getBoolean("yanwubaofa")) return;
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.5;
        double z = worldPosition.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, 0, 0, 0, 1);
        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 10, 0, 0, 0, 1);
        double tick = data.getDouble("yanwutick") + 1;
        data.putDouble("yanwutick", tick);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        if (tick >= 54) {
            data.putBoolean("yanwubaofa", false);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            data.putDouble("yanwutick", 0);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.qiankundaoren"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction direction) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::idle));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }
    private PlayState idle(AnimationState<QiankundaorenBlockEntity> state) {
        return getBlockState().getValue(QiankunBlock.ANIMATION) == 0 ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP;
    }
    private PlayState procedure(AnimationState<QiankundaorenBlockEntity> state) {
        int animation = getBlockState().getValue(QiankunBlock.ANIMATION);
        AnimationController<QiankundaorenBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(QiankunBlock.ANIMATION, 0), Block.UPDATE_ALL);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
