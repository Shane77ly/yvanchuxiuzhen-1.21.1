package net.yvanchuxiuzhen.system.dungeon.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
import net.yvanchuxiuzhen.system.dungeon.data.ControllerMobSpawn;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonMenuSnapshot;
import net.yvanchuxiuzhen.system.dungeon.data.MarkerInfo;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 入口两槽、控制器四十五槽、占位符零槽与画卷单槽分别持久化为强类型根字段，不嵌套套壳标签。 */
public final class DungeonBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, GeoBlockEntity {
    private final String kind;
    private NonNullList<ItemStack> items;
    private final IItemHandler[] sided = new IItemHandler[6];
    private final IItemHandler unsided = new InvWrapper(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 入口字段（tiandaofuben）与通用名称/定义引用
    private String dungeonName = "";
    private String templateId = "";
    private String description = "";
    private String text01 = "";
    private String text02 = "";
    private String text03 = "";
    private String text04 = "";
    private String rewardSummary = "";
    private String definitionId = "";
    private Optional<DungeonDefinition> customDefinition = Optional.empty();
    private double minLevel;
    private double maxLevel;
    private double maxPeople;
    private double preview;
    private double cooldownMax;
    private boolean onCooldown;
    private double cooldownTicks;
    private double entranceControllerX;
    private double entranceControllerY;
    private double entranceControllerZ;
    private double entrancePlayerX;
    private double entrancePlayerY;
    private double entrancePlayerZ;

    // 控制器字段（fubenblock）
    private boolean openRequested;
    private boolean active;
    private boolean closing;
    private String phase = "";
    private int controllerTick;
    private int waveIndex;
    private int waveMax;
    private int endTime;
    private int spawnCursor;
    private int waveEntityCount;
    private final int[] waveCounts = new int[5];
    private int sideLength;
    private int centerOffsetX;
    private int centerOffsetY;
    private int centerOffsetZ;
    private String returnDimension = "";
    private double returnX;
    private double returnY;
    private double returnZ;
    private int markerCount;
    private List<MarkerInfo> markers = new ArrayList<>();
    private List<ControllerMobSpawn> mobSpawns = new ArrayList<>();
    private final int[] rewardWeights = new int[18];

    // 占位符字段（fubenentity）
    private String markerId = "";
    private int markerX;
    private int markerY;
    private int markerZ;
    private int markerFacing;
    private boolean markerAttack;

    // 私人画卷方块字段（fabao_009）
    private boolean portalActive;
    private double portalTicks;

    public DungeonBlockEntity(BlockPos pos, BlockState state) {
        super(DungeonBlockEntities.typeFor(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath()), pos, state);
        kind = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        items = NonNullList.withSize(kind.equals("fubenblock") ? 45 : kind.equals("tiandaofuben") ? 2 : kind.equals("fabao_009") ? 1 : 0, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public String kind() { return kind; }
    public IItemHandler handler(Direction direction) { return isRemoved() ? null : direction == null ? unsided : sided[direction.ordinal()]; }

    public void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public String dungeonName() { return dungeonName; }
    public void setDungeonName(String value) { dungeonName = value; }
    public String templateId() { return templateId; }
    public void setTemplateId(String value) { templateId = value; }
    public String description() { return description; }
    public void setDescription(String value) { description = value; }
    public String text01() { return text01; }
    public String text02() { return text02; }
    public String text03() { return text03; }
    public String text04() { return text04; }
    public void setTextLine(int index, String value) {
        switch (index) {
            case 1 -> text01 = value;
            case 2 -> text02 = value;
            case 3 -> text03 = value;
            case 4 -> text04 = value;
            default -> { }
        }
    }
    public String rewardSummary() { return rewardSummary; }
    public void setRewardSummary(String value) { rewardSummary = value; }
    public String definitionId() { return definitionId; }
    public void setDefinitionId(String value) { definitionId = value; customDefinition = Optional.empty(); }
    public Optional<DungeonDefinition> customDefinition() { return customDefinition; }
    public void setCustomDefinition(DungeonDefinition definition) {
        customDefinition = Optional.ofNullable(definition);
        if (definition != null) definitionId = definition.id();
    }
    public double minLevel() { return minLevel; }
    public void setMinLevel(double value) { minLevel = value; }
    public double maxLevel() { return maxLevel; }
    public void setMaxLevel(double value) { maxLevel = value; }
    public double maxPeople() { return maxPeople; }
    public void setMaxPeople(double value) { maxPeople = value; }
    public double preview() { return preview; }
    public void setPreview(double value) { preview = value; }
    public double cooldownMax() { return cooldownMax; }
    public void setCooldownMax(double value) { cooldownMax = value; }
    public boolean onCooldown() { return onCooldown; }
    public void setOnCooldown(boolean value) { onCooldown = value; }
    public double cooldownTicks() { return cooldownTicks; }
    public void setCooldownTicks(double value) { cooldownTicks = value; }
    public double entranceControllerX() { return entranceControllerX; }
    public double entranceControllerY() { return entranceControllerY; }
    public double entranceControllerZ() { return entranceControllerZ; }
    public void setEntranceControllerPos(double x, double y, double z) {
        entranceControllerX = x; entranceControllerY = y; entranceControllerZ = z;
    }
    public double entrancePlayerX() { return entrancePlayerX; }
    public double entrancePlayerY() { return entrancePlayerY; }
    public double entrancePlayerZ() { return entrancePlayerZ; }
    public void setEntrancePlayerPos(double x, double y, double z) {
        entrancePlayerX = x; entrancePlayerY = y; entrancePlayerZ = z;
    }

    public boolean openRequested() { return openRequested; }
    public void setOpenRequested(boolean value) { openRequested = value; }
    public boolean active() { return active; }
    public void setActive(boolean value) { active = value; }
    public boolean closing() { return closing; }
    public void setClosing(boolean value) { closing = value; }
    public String phase() { return phase; }
    public void setPhase(String value) { phase = value; }
    public int controllerTick() { return controllerTick; }
    public void setControllerTick(int value) { controllerTick = value; }
    public int waveIndex() { return waveIndex; }
    public void setWaveIndex(int value) { waveIndex = value; }
    public int waveMax() { return waveMax; }
    public void setWaveMax(int value) { waveMax = value; }
    public int endTime() { return endTime; }
    public void setEndTime(int value) { endTime = value; }
    public int spawnCursor() { return spawnCursor; }
    public void setSpawnCursor(int value) { spawnCursor = value; }
    public int waveEntityCount() { return waveEntityCount; }
    public void setWaveEntityCount(int value) { waveEntityCount = value; }
    public int waveCount(int waveOneBased) { return waveOneBased >= 1 && waveOneBased <= 5 ? waveCounts[waveOneBased - 1] : 0; }
    public void setWaveCount(int waveOneBased, int count) { if (waveOneBased >= 1 && waveOneBased <= 5) waveCounts[waveOneBased - 1] = count; }
    public void resetWaveCounts() { Arrays.fill(waveCounts, 0); }
    public int sideLength() { return sideLength; }
    public void setSideLength(int value) { sideLength = value; }
    public int centerOffsetX() { return centerOffsetX; }
    public int centerOffsetY() { return centerOffsetY; }
    public int centerOffsetZ() { return centerOffsetZ; }
    public void setCenterOffset(int x, int y, int z) { centerOffsetX = x; centerOffsetY = y; centerOffsetZ = z; }
    public String returnDimension() { return returnDimension; }
    public void setReturnDimension(String value) { returnDimension = value; }
    public double returnX() { return returnX; }
    public double returnY() { return returnY; }
    public double returnZ() { return returnZ; }
    public void setReturnPos(double x, double y, double z) { returnX = x; returnY = y; returnZ = z; }
    public int markerCount() { return markerCount; }
    public void incrementMarkerCount() { markerCount++; }
    public List<MarkerInfo> markers() { return markers; }
    public void addMarker(MarkerInfo marker) { markers.add(marker); }
    public Optional<MarkerInfo> findMarker(String id) {
        return markers.stream().filter(m -> m.id().equals(id)).findFirst();
    }
    public List<ControllerMobSpawn> mobSpawns() { return mobSpawns; }
    public void resetMobSpawns() { mobSpawns.clear(); }
    public void addMobSpawn(ControllerMobSpawn spawn) { mobSpawns.add(spawn); }
    public int rewardWeight(int slot) { return slot >= 0 && slot < 18 ? rewardWeights[slot] : 0; }
    public void setRewardWeight(int slot, int weight) { if (slot >= 0 && slot < 18) rewardWeights[slot] = weight; }
    public void resetRewardWeights() { Arrays.fill(rewardWeights, 0); }

    public String markerId() { return markerId; }
    public void setMarkerId(String value) { markerId = value; }
    public int markerX() { return markerX; }
    public int markerY() { return markerY; }
    public int markerZ() { return markerZ; }
    public void setMarkerOffset(int x, int y, int z) { markerX = x; markerY = y; markerZ = z; }
    public int markerFacing() { return markerFacing; }
    public void setMarkerFacing(int value) { markerFacing = value; }
    public boolean markerAttack() { return markerAttack; }
    public void setMarkerAttack(boolean value) { markerAttack = value; }
    public MarkerInfo toMarkerInfo() { return new MarkerInfo(markerId, markerX, markerY, markerZ, markerFacing, markerAttack); }

    public boolean portalActive() { return portalActive; }
    public void setPortalActive(boolean value) { portalActive = value; }
    public double portalTicks() { return portalTicks; }
    public void setPortalTicks(double value) { portalTicks = value; }

    public DungeonMenuSnapshot menuSnapshot() {
        return new DungeonMenuSnapshot(
                preview, dungeonName, templateId, description,
                text01, text02, text03, text04,
                rewardSummary, minLevel, maxLevel, maxPeople,
                sideLength, markerId
        );
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

        dungeonName = tag.getString("fubenname");
        templateId = tag.getString("fubenid");
        description = tag.getString("fubenjieshao");
        text01 = tag.getString("text01");
        text02 = tag.getString("text02");
        text03 = tag.getString("text03");
        text04 = tag.getString("text04");
        rewardSummary = tag.getString("fubenjiangli");
        definitionId = tag.getString("definition_id");
        customDefinition = tag.contains("custom_definition")
                ? DungeonDefinition.CODEC.parse(NbtOps.INSTANCE, tag.get("custom_definition")).result()
                : Optional.empty();
        minLevel = tag.getDouble("fubenminlevel");
        maxLevel = tag.getDouble("fubenmaxlevel");
        maxPeople = tag.getDouble("fubenpeoplemax");
        preview = tag.getDouble("fubenyvlan");
        cooldownMax = tag.getDouble("fubenjieshujishiMax");
        onCooldown = tag.getBoolean("fubenbegin");
        cooldownTicks = tag.getDouble("fubenjieshujishi");
        entranceControllerX = tag.getDouble("fubenzuobiaoX");
        entranceControllerY = tag.getDouble("fubenzuobiaoY");
        entranceControllerZ = tag.getDouble("fubenzuobiaoZ");
        entrancePlayerX = tag.getDouble("fubenplayerX");
        entrancePlayerY = tag.getDouble("fubenplayerY");
        entrancePlayerZ = tag.getDouble("fubenplayerZ");

        openRequested = tag.getBoolean("fubenopen");
        active = tag.getBoolean("fubenstate");
        closing = tag.getBoolean("fubenclose");
        phase = tag.getString("fubenjindu");
        controllerTick = tag.getInt("fubentick");
        waveIndex = tag.getInt("fubenbocinum");
        waveMax = tag.getInt("fubenbociMax");
        endTime = tag.getInt("fubenjieshutime");
        spawnCursor = tag.getInt("fubenentityspawnnum");
        waveEntityCount = tag.getInt("fubenentitynum");
        for (int i = 0; i < 5; i++) waveCounts[i] = tag.getInt("fybenboci" + (i + 1) + "num");
        sideLength = tag.getInt("fubenbianchang");
        centerOffsetX = tag.getInt("fubenzhongxinX");
        centerOffsetY = tag.getInt("fubenzhongxinY");
        centerOffsetZ = tag.getInt("fubenzhongxinZ");
        returnDimension = tag.getString("chuansongid");
        returnX = tag.getDouble("chuansongX");
        returnY = tag.getDouble("chuansongY");
        returnZ = tag.getDouble("chuansongZ");
        markerCount = tag.getInt("zhanweifunum");
        markers = tag.contains("markers")
                ? new ArrayList<>(MarkerInfo.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("markers")).result().orElse(List.of()))
                : new ArrayList<>();
        mobSpawns = tag.contains("mob_spawns")
                ? new ArrayList<>(ControllerMobSpawn.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("mob_spawns")).result().orElse(List.of()))
                : new ArrayList<>();
        for (int i = 0; i < 18; i++) rewardWeights[i] = tag.getInt("gift" + i + "qvanzhong");

        markerId = tag.getString("zhanweifuid");
        markerX = tag.getInt("entityshengchengX");
        markerY = tag.getInt("entityshengchengY");
        markerZ = tag.getInt("entityshengchengZ");
        markerFacing = tag.getInt("entityshengchengchaoxiang");
        markerAttack = tag.getBoolean("entityshengchenggongji");

        portalActive = tag.getBoolean("chuanyve");
        portalTicks = tag.getDouble("blocktick");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);

        if (!dungeonName.isEmpty()) tag.putString("fubenname", dungeonName);
        if (!templateId.isEmpty()) tag.putString("fubenid", templateId);
        if (!description.isEmpty()) tag.putString("fubenjieshao", description);
        if (!text01.isEmpty()) tag.putString("text01", text01);
        if (!text02.isEmpty()) tag.putString("text02", text02);
        if (!text03.isEmpty()) tag.putString("text03", text03);
        if (!text04.isEmpty()) tag.putString("text04", text04);
        if (!rewardSummary.isEmpty()) tag.putString("fubenjiangli", rewardSummary);
        if (!definitionId.isEmpty()) tag.putString("definition_id", definitionId);
        customDefinition.flatMap(def -> DungeonDefinition.CODEC.encodeStart(NbtOps.INSTANCE, def).result())
                .ifPresent(encoded -> tag.put("custom_definition", encoded));
        if (minLevel != 0) tag.putDouble("fubenminlevel", minLevel);
        if (maxLevel != 0) tag.putDouble("fubenmaxlevel", maxLevel);
        if (maxPeople != 0) tag.putDouble("fubenpeoplemax", maxPeople);
        if (preview != 0) tag.putDouble("fubenyvlan", preview);
        if (cooldownMax != 0) tag.putDouble("fubenjieshujishiMax", cooldownMax);
        if (onCooldown) tag.putBoolean("fubenbegin", true);
        if (cooldownTicks != 0) tag.putDouble("fubenjieshujishi", cooldownTicks);
        if (entranceControllerX != 0) tag.putDouble("fubenzuobiaoX", entranceControllerX);
        if (entranceControllerY != 0) tag.putDouble("fubenzuobiaoY", entranceControllerY);
        if (entranceControllerZ != 0) tag.putDouble("fubenzuobiaoZ", entranceControllerZ);
        if (entrancePlayerX != 0) tag.putDouble("fubenplayerX", entrancePlayerX);
        if (entrancePlayerY != 0) tag.putDouble("fubenplayerY", entrancePlayerY);
        if (entrancePlayerZ != 0) tag.putDouble("fubenplayerZ", entrancePlayerZ);

        if (openRequested) tag.putBoolean("fubenopen", true);
        if (active) tag.putBoolean("fubenstate", true);
        if (closing) tag.putBoolean("fubenclose", true);
        if (!phase.isEmpty()) tag.putString("fubenjindu", phase);
        if (controllerTick != 0) tag.putInt("fubentick", controllerTick);
        if (waveIndex != 0) tag.putInt("fubenbocinum", waveIndex);
        if (waveMax != 0) tag.putInt("fubenbociMax", waveMax);
        if (endTime != 0) tag.putInt("fubenjieshutime", endTime);
        if (spawnCursor != 0) tag.putInt("fubenentityspawnnum", spawnCursor);
        if (waveEntityCount != 0) tag.putInt("fubenentitynum", waveEntityCount);
        for (int i = 0; i < 5; i++) if (waveCounts[i] != 0) tag.putInt("fybenboci" + (i + 1) + "num", waveCounts[i]);
        if (sideLength != 0) tag.putInt("fubenbianchang", sideLength);
        if (centerOffsetX != 0) tag.putInt("fubenzhongxinX", centerOffsetX);
        if (centerOffsetY != 0) tag.putInt("fubenzhongxinY", centerOffsetY);
        if (centerOffsetZ != 0) tag.putInt("fubenzhongxinZ", centerOffsetZ);
        if (!returnDimension.isEmpty()) tag.putString("chuansongid", returnDimension);
        if (returnX != 0) tag.putDouble("chuansongX", returnX);
        if (returnY != 0) tag.putDouble("chuansongY", returnY);
        if (returnZ != 0) tag.putDouble("chuansongZ", returnZ);
        if (markerCount != 0) tag.putInt("zhanweifunum", markerCount);
        if (!markers.isEmpty()) MarkerInfo.CODEC.listOf().encodeStart(NbtOps.INSTANCE, markers).result().ifPresent(encoded -> tag.put("markers", encoded));
        if (!mobSpawns.isEmpty()) ControllerMobSpawn.CODEC.listOf().encodeStart(NbtOps.INSTANCE, mobSpawns).result().ifPresent(encoded -> tag.put("mob_spawns", encoded));
        for (int i = 0; i < 18; i++) if (rewardWeights[i] != 0) tag.putInt("gift" + i + "qvanzhong", rewardWeights[i]);

        if (!markerId.isEmpty()) tag.putString("zhanweifuid", markerId);
        if (markerX != 0) tag.putInt("entityshengchengX", markerX);
        if (markerY != 0) tag.putInt("entityshengchengY", markerY);
        if (markerZ != 0) tag.putInt("entityshengchengZ", markerZ);
        if (markerFacing != 0) tag.putInt("entityshengchengchaoxiang", markerFacing);
        if (markerAttack) tag.putBoolean("entityshengchenggongji", true);

        if (portalActive) tag.putBoolean("chuanyve", true);
        if (portalTicks != 0) tag.putDouble("blocktick", portalTicks);
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
