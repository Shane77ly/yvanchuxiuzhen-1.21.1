package net.yvanchuxiuzhen.system.equipment.content;

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

/** 饰品台保存四个单件槽与类别 shipintainame；各面自动化均可放取全部槽，界面独立限制结果槽放入。 */
public final class JewelryTableBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JewelryTableBlockEntity(BlockPos pos, BlockState state) {
        super(EquipmentBlockEntities.JEWELRY_TABLE.get(), pos, state);
        for (Direction direction : Direction.values()) {
            sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
        }
    }

    public String category() {
        return getPersistentData().getString("shipintainame");
    }

    public void setCategory(String category) {
        getPersistentData().putString("shipintainame", category);
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public void cycleCategory(boolean forward) {
        String next = switch (category()) {
            case "", "头簪" -> forward ? "项坠" : "腰佩";
            case "项坠" -> forward ? "手镯" : "头簪";
            case "手镯" -> forward ? "腰佩" : "项坠";
            case "腰佩" -> forward ? "头簪" : "手镯";
            default -> null;
        };
        if (next != null) setCategory(next);
    }

    /** 每次成功合成都同步状态与库存，同类别重复合成也能把动画请求送到客户端。 */
    public void playAnimation(int animation) {
        BlockState state = getBlockState();
        BlockState animated = state.setValue(JewelryTableBlock.ANIMATION, animation);
        level.setBlock(worldPosition, animated, Block.UPDATE_ALL);
        level.sendBlockUpdated(worldPosition, state, animated, Block.UPDATE_ALL);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(4, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
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
        return 4;
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
        return Component.literal("shipintai");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Shipintai");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new JewelryTableMenu(id, inventory, this);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0, 1, 2, 3};
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

    private PlayState idlePredicate(AnimationState<JewelryTableBlockEntity> state) {
        if (getBlockState().getValue(JewelryTableBlock.ANIMATION) == 0) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("0"));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<JewelryTableBlockEntity> state) {
        int animation = getBlockState().getValue(JewelryTableBlock.ANIMATION);
        AnimationController<JewelryTableBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(JewelryTableBlock.ANIMATION, 0), Block.UPDATE_ALL);
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
