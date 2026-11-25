package net.yvanchuxiuzhen.system.world.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.world.StructureExploration;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 裂隙保存结构、怪物组、玩家显示名与四十次计划刻计数；来源当前不生成怪物。 */
public final class RiftBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] handlers = new IItemHandler[6];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private String structureName = "";
    private String mobList = "";
    private String playerName = "";
    private boolean releasing;
    private double releaseTicks;

    public RiftBlockEntity(BlockPos pos, BlockState state) {
        super(WorldBlockEntities.RIFT.get(), pos, state);
        for (Direction direction : Direction.values()) handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) { return isRemoved() ? null : direction == null ? unsided : handlers[direction.ordinal()]; }
    public String structureName() { return structureName; }
    public String mobList() { return mobList; }
    public String playerName() { return playerName; }
    public boolean releasing() { return releasing; }
    public double releaseTicks() { return releaseTicks; }

    public void beginRelease() {
        this.releasing = true;
        changed();
    }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public void structure(String name, String mobs) {
        this.structureName = name == null ? "" : name;
        this.mobList = mobs == null ? "" : mobs;
        changed();
    }

    public void playerName(String name) {
        this.playerName = name == null ? "" : name;
        changed();
    }

    public void tickRelease(ServerLevel level) {
        level.sendParticles(Particles.type("flyskill_06"), worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5, 5, 0, 0, 0, 1);
        if (releasing) {
            releaseTicks += 1;
            if (releaseTicks >= 40) {
                releasing = false;
                releaseTicks = 0;
            }
            changed();
        } else {
            StructureExploration.release(level, new Vec3(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()),
                    playerName, structureName);
            level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public int getContainerSize() { return 0; }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen.liexi"); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        structureName = tag.getString("jiegouname");
        mobList = tag.getString("moblist");
        playerName = tag.getString("playername");
        releasing = tag.getBoolean("kaishi");
        releaseTicks = tag.getDouble("MobcomeTick");
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        tag.putString("jiegouname", structureName);
        tag.putString("moblist", mobList);
        tag.putString("playername", playerName);
        tag.putBoolean("kaishi", releasing);
        tag.putDouble("MobcomeTick", releaseTicks);
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> getBlockState().getValue(RiftBlock.ANIMATION) == 0
                ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP));
        controllers.add(new AnimationController<>(this, "procedurecontroller", 0, this::procedure));
    }

    private PlayState procedure(AnimationState<RiftBlockEntity> state) {
        AnimationController<RiftBlockEntity> controller = state.getController();
        int animation = getBlockState().getValue(RiftBlock.ANIMATION);
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(RiftBlock.ANIMATION, 0), 3);
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
