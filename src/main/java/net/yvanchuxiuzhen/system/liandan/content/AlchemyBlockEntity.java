package net.yvanchuxiuzhen.system.liandan.content;

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

/** 丹炉保存十二槽库存与自身炉次数据；槽十一承载炼制中的临时成品，仍向自动化开放。 */
public final class AlchemyBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(12, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private CompoundTag herbs = new CompoundTag();
    private String alchemyName = "";
    private double alchemyLevel;
    private double alchemyFire;
    private double alchemyDannum;
    private double alchemistLevel;
    private double lianPower;
    private boolean fireToggle;
    private double firePower;
    private double firePowerTick;
    private boolean brewing;
    private double stepTick;
    private double brewTick;
    private double brewTickMax;
    private boolean explodeRisk;
    private double explodeSurvivalChance;
    private boolean explodedFlag;
    private double brewProgress;
    private int pendingExperience;

    public AlchemyBlockEntity(BlockPos pos, BlockState state) {
        super(LiandanBlockEntities.type(((AlchemyBlock) state.getBlock()).tier()), pos, state);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public int tier() {
        return ((AlchemyBlock) getBlockState().getBlock()).tier();
    }

    public CompoundTag herbs() { return herbs; }
    public void setHerbs(CompoundTag value) { herbs = value == null ? new CompoundTag() : value.copy(); }
    public String alchemyName() { return alchemyName; }
    public void setAlchemyName(String value) { alchemyName = value == null ? "" : value; }
    public double alchemyLevel() { return alchemyLevel; }
    public int alchemyLevelInt() { return (int) alchemyLevel; }
    public void setAlchemyLevel(double value) { alchemyLevel = value; }
    public double alchemyFire() { return alchemyFire; }
    public int alchemyFireInt() { return (int) alchemyFire; }
    public void setAlchemyFire(double value) { alchemyFire = value; }
    public int alchemyDannumInt() { return (int) alchemyDannum; }
    public void setAlchemyDannum(double value) { alchemyDannum = value; }
    public int alchemistLevelInt() { return (int) alchemistLevel; }
    public void setAlchemistLevel(double value) { alchemistLevel = value; }
    public double lianPower() { return lianPower; }
    public int lianPowerInt() { return (int) lianPower; }
    public void setLianPower(double value) { lianPower = value; }
    public boolean fireToggle() { return fireToggle; }
    public void setFireToggle(boolean value) { fireToggle = value; }
    public double firePower() { return firePower; }
    public void setFirePower(double value) { firePower = value; }
    public double firePowerTick() { return firePowerTick; }
    public void setFirePowerTick(double value) { firePowerTick = value; }
    public boolean brewing() { return brewing; }
    public void setBrewing(boolean value) { brewing = value; }
    public double stepTick() { return stepTick; }
    public void setStepTick(double value) { stepTick = value; }
    public double brewTick() { return brewTick; }
    public void setBrewTick(double value) { brewTick = value; }
    public double brewTickMax() { return brewTickMax; }
    public void setBrewTickMax(double value) { brewTickMax = value; }
    public boolean explodeRisk() { return explodeRisk; }
    public void setExplodeRisk(boolean value) { explodeRisk = value; }
    public double explodeSurvivalChance() { return explodeSurvivalChance; }
    public void setExplodeSurvivalChance(double value) { explodeSurvivalChance = value; }
    public boolean explodedFlag() { return explodedFlag; }
    public void setExplodedFlag(boolean value) { explodedFlag = value; }
    public int brewProgressInt() { return (int) brewProgress; }
    public void updateBrewProgress() { brewProgress = brewTickMax > 0 ? brewTick / brewTickMax * 100 : 0; }
    public int pendingExperience() { return pendingExperience; }
    public void setPendingExperience(int value) { pendingExperience = value; }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(12, ItemStack.EMPTY);
        if (!tryLoadLootTable(tag)) ContainerHelper.loadAllItems(tag, items, registries);
        herbs = tag.getCompound("zong").copy();
        alchemyName = tag.getString("alchemyName");
        alchemyLevel = tag.getDouble("alchemyLevel");
        alchemyFire = tag.getDouble("alchemyFire");
        alchemyDannum = tag.getDouble("alchemyDannum");
        alchemistLevel = tag.getDouble("Alchemy_LEVEL");
        lianPower = tag.getDouble("lianpower");
        fireToggle = tag.getBoolean("gengxin");
        firePower = tag.getDouble("firepower");
        firePowerTick = tag.getDouble("firepowertick");
        brewing = tag.getBoolean("liandanBegin");
        stepTick = tag.getDouble("tick");
        brewTick = tag.getDouble("lianqiTick");
        brewTickMax = tag.getDouble("lianqiTickMax");
        explodeRisk = tag.getBoolean("zhalu");
        explodeSurvivalChance = tag.getDouble("zhalugailv");
        explodedFlag = tag.getBoolean("zhenzhale");
        brewProgress = tag.getDouble("lianzhijindu");
        pendingExperience = tag.getInt("jingyanhuoqv");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!herbs.isEmpty()) tag.put("zong", herbs);
        if (!alchemyName.isEmpty()) tag.putString("alchemyName", alchemyName);
        if (alchemyLevel != 0) tag.putDouble("alchemyLevel", alchemyLevel);
        if (alchemyFire != 0) tag.putDouble("alchemyFire", alchemyFire);
        if (alchemyDannum != 0) tag.putDouble("alchemyDannum", alchemyDannum);
        if (alchemistLevel != 0) tag.putDouble("Alchemy_LEVEL", alchemistLevel);
        if (lianPower != 0) tag.putDouble("lianpower", lianPower);
        if (fireToggle) tag.putBoolean("gengxin", true);
        if (firePower != 0) tag.putDouble("firepower", firePower);
        if (firePowerTick != 0) tag.putDouble("firepowertick", firePowerTick);
        if (brewing) tag.putBoolean("liandanBegin", true);
        if (stepTick != 0) tag.putDouble("tick", stepTick);
        if (brewTick != 0) tag.putDouble("lianqiTick", brewTick);
        if (brewTickMax != 0) tag.putDouble("lianqiTickMax", brewTickMax);
        if (explodeRisk) tag.putBoolean("zhalu", true);
        if (explodeSurvivalChance != 0) tag.putDouble("zhalugailv", explodeSurvivalChance);
        if (explodedFlag) tag.putBoolean("zhenzhale", true);
        if (brewProgress != 0) tag.putDouble("lianzhijindu", brewProgress);
        if (pendingExperience != 0) tag.putInt("jingyanhuoqv", pendingExperience);
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
        return 12;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
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
        return Component.literal("alchemy_0" + tier());
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Alchemy 0 " + tier());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return IntStream.range(0, 12).toArray();
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

    private PlayState idlePredicate(AnimationState<AlchemyBlockEntity> state) {
        return getBlockState().getValue(AlchemyBlock.ANIMATION) == 0
                ? state.setAndContinue(RawAnimation.begin().thenLoop("0")) : PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<AlchemyBlockEntity> state) {
        int animation = getBlockState().getValue(AlchemyBlock.ANIMATION);
        AnimationController<AlchemyBlockEntity> controller = state.getController();
        if (animation != 0 && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(Integer.toString(animation), Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                level.setBlock(worldPosition, getBlockState().setValue(AlchemyBlock.ANIMATION, 0), Block.UPDATE_ALL);
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
