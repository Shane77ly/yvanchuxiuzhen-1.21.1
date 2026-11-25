package net.yvanchuxiuzhen.system.craft.content;

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
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 陶轮十槽共享库存与制作选择；六面任意插取，客户端动画控制器结束后复位。 */
public final class PotteryWheelBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(10, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String[] texts = {"", "", ""};
    private int page;
    private int maxPage;
    private int selected;
    private final int[] needItems = new int[8];
    private final String[] itemNames = {"", "", "", "", "", "", "", ""};
    private final String[] outputIds = {"", "", "", "", "", "", "", ""};

    public PotteryWheelBlockEntity(BlockPos pos, BlockState state) {
        super(CraftBlockEntities.POTTERY_WHEEL.get(), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public void changed() {
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    /** 攻击与关闭清理目录槽及隐藏第十槽，保留结果槽。 */
    public void clearPreviews() {
        for (int slot = 1; slot < 10; slot++) setItem(slot, ItemStack.EMPTY);
        changed();
    }

    public void animate(int animation) {
        level.setBlock(worldPosition, getBlockState().setValue(PotteryWheelBlock.ANIMATION, animation), Block.UPDATE_ALL);
    }

    public String text(int line1Based) {
        return line1Based >= 1 && line1Based <= texts.length ? texts[line1Based - 1] : "";
    }

    public void setText(int line1Based, String value) {
        if (line1Based >= 1 && line1Based <= texts.length) texts[line1Based - 1] = value == null ? "" : value;
    }

    public int page() {
        return page;
    }

    public void setPage(int value) {
        page = value;
    }

    public int maxPage() {
        return maxPage;
    }

    public void setMaxPage(int value) {
        maxPage = value;
    }

    public int selected() {
        return selected;
    }

    public void setSelected(int value) {
        selected = value;
    }

    public int needItem(int index) {
        return index >= 0 && index < needItems.length ? needItems[index] : 0;
    }

    public void setNeedItem(int index, int value) {
        if (index >= 0 && index < needItems.length) needItems[index] = value;
    }

    public String itemName(int index) {
        return index >= 0 && index < itemNames.length ? itemNames[index] : "";
    }

    public void setItemName(int index, String value) {
        if (index >= 0 && index < itemNames.length) itemNames[index] = value == null ? "" : value;
    }

    public String outputId(int index) {
        return index >= 0 && index < outputIds.length ? outputIds[index] : "";
    }

    public void setOutputId(int index, String value) {
        if (index >= 0 && index < outputIds.length) outputIds[index] = value == null ? "" : value;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(10, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        for (int line = 1; line <= texts.length; line++) texts[line - 1] = tag.getString("wenben" + line);
        page = tag.getInt("yeshu");
        maxPage = tag.getInt("yeshumax");
        selected = tag.getInt("xvanzeweizhi");
        for (int i = 0; i < 8; i++) {
            needItems[i] = tag.getInt("needitem" + i);
            itemNames[i] = tag.getString("itemname" + i);
            outputIds[i] = tag.getString("chengpinid" + i);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        for (int line = 1; line <= texts.length; line++) {
            if (!texts[line - 1].isEmpty()) tag.putString("wenben" + line, texts[line - 1]);
        }
        if (page != 0) tag.putInt("yeshu", page);
        if (maxPage != 0) tag.putInt("yeshumax", maxPage);
        if (selected != 0) tag.putInt("xvanzeweizhi", selected);
        for (int i = 0; i < 8; i++) {
            if (needItems[i] != 0) tag.putInt("needitem" + i, needItems[i]);
            if (!itemNames[i].isEmpty()) tag.putString("itemname" + i, itemNames[i]);
            if (!outputIds[i].isEmpty()) tag.putString("chengpinid" + i, outputIds[i]);
        }
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
        return 10;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    /** 来源强制赋槽仅受容器64件上限约束，物品自身堆叠上限不参与截断。 */
    @Override
    public void setItem(int slot, ItemStack stack) {
        unpackLootTable(null);
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
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
        return Component.translatable("block.yvanchuxiuzhen.taolun");
    }

    @Override
    public Component getDisplayName() {
        return getDefaultName();
    }

    /** 来源保留独立箱子提供者，自然右键由陶轮运行入口打开工艺菜单。 */
    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return IntStream.range(0, 10).toArray();
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

    private PlayState idlePredicate(AnimationState<PotteryWheelBlockEntity> state) {
        if (getBlockState().getValue(PotteryWheelBlock.ANIMATION) == 0) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("0"));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<PotteryWheelBlockEntity> state) {
        int animation = getBlockState().getValue(PotteryWheelBlock.ANIMATION);
        AnimationController<PotteryWheelBlockEntity> controller = state.getController();
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
