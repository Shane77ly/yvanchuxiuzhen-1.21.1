package net.yvanchuxiuzhen.collection.decor;

import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 文字、逐字位置及拼段坐标存于本方块实体；来源库存为零槽。 */
public final class InscribedDecorBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final InscribedDecorVariant variant;
    private NonNullList<ItemStack> items = NonNullList.withSize(0, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[6];
    private String plaqueText = "";
    private String displayDirection = "";
    private int rgbColour;
    private boolean hasRgbColour;
    private boolean glowing;
    private double glyphCount;
    private final List<String> glyphChars = new ArrayList<>();
    private final List<Double> glyphOffsets = new ArrayList<>();
    private boolean addHalf;
    private double blockNum;
    private double plaqueTextNum;
    private double plaqueAngle;
    private double pitchAngle;
    private double textX;
    private double textZ;
    private final List<Double> segmentOffsetX = new ArrayList<>();
    private final List<Double> segmentOffsetZ = new ArrayList<>();

    public InscribedDecorBlockEntity(InscribedDecorVariant variant, BlockPos pos, BlockState state) {
        super(InscribedDecorContent.type(variant), pos, state);
        this.variant = variant;
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }
    public InscribedDecorVariant variant() { return variant; }
    public IItemHandler handler(Direction direction) { return direction == null ? unsided : sided[direction.ordinal()]; }
    public String plaqueText() { return plaqueText; }
    public void setPlaqueText(String value) { plaqueText = value == null ? "" : value; }
    public String displayDirection() { return displayDirection; }
    public void setDisplayDirection(String value) { displayDirection = value == null ? "" : value; }
    public int rgbColour() { return rgbColour; }
    public boolean hasRgbColour() { return hasRgbColour; }
    public void setRgbColour(int value) { rgbColour = value; hasRgbColour = true; }
    public boolean glowing() { return glowing; }
    public void setGlowing(boolean value) { glowing = value; }
    public int glyphCountInt() { return (int) glyphCount; }
    public void setGlyphs(String text, String direction, List<String> chars, List<Double> offsets) {
        this.plaqueText = text == null ? "" : text;
        this.displayDirection = direction == null ? "" : direction;
        this.glyphCount = chars.size();
        this.glyphChars.clear();
        this.glyphChars.addAll(chars);
        this.glyphOffsets.clear();
        this.glyphOffsets.addAll(offsets);
    }
    public String glyphChar(int index) { return index >= 0 && index < glyphChars.size() ? glyphChars.get(index) : ""; }
    public double glyphOffset(int index) { return index >= 0 && index < glyphOffsets.size() ? glyphOffsets.get(index) : 0; }
    public boolean addHalf() { return addHalf; }
    public void setAddHalf(boolean value) { addHalf = value; }
    public int blockNumInt() { return (int) blockNum; }
    public double blockNum() { return blockNum; }
    public void setBlockNum(double value) { blockNum = value; }
    public void setPlaqueTextNum(double value) { plaqueTextNum = value; }
    public int plaqueAngleInt() { return (int) plaqueAngle; }
    public void setPlaqueAngle(double value) { plaqueAngle = value; }
    public double pitchAngle() { return pitchAngle; }
    public int pitchAngleInt() { return (int) pitchAngle; }
    public void setPitchAngle(double value) { pitchAngle = value; }
    public double textX() { return textX; }
    public void setTextX(double value) { textX = value; }
    public double textZ() { return textZ; }
    public void setTextZ(double value) { textZ = value; }
    public double segmentOffsetX(int index) { return index >= 0 && index < segmentOffsetX.size() ? segmentOffsetX.get(index) : 0; }
    public double segmentOffsetZ(int index) { return index >= 0 && index < segmentOffsetZ.size() ? segmentOffsetZ.get(index) : 0; }
    public void setSegmentOffsets(List<Double> xs, List<Double> zs) {
        segmentOffsetX.clear();
        segmentOffsetX.addAll(xs);
        segmentOffsetZ.clear();
        segmentOffsetZ.addAll(zs);
    }

    public void syncText() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(0, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        plaqueText = tag.getString("plaquetxt");
        displayDirection = tag.getString("fangxiang");
        hasRgbColour = tag.contains("rgbcolour");
        rgbColour = tag.getInt("rgbcolour");
        glowing = tag.getBoolean("faguang");
        glyphCount = tag.getDouble("zishu");
        glyphChars.clear();
        glyphOffsets.clear();
        for (int index = 0; index < (int) glyphCount; index++) {
            glyphChars.add(tag.getString("zidi" + index));
            glyphOffsets.add(tag.getDouble("ziweizhi" + index));
        }
        addHalf = tag.getBoolean("plaqueaddfive");
        blockNum = tag.getDouble("blocknum");
        plaqueTextNum = tag.getDouble("plaquetxtnum");
        plaqueAngle = tag.getDouble("plaquejiaodu");
        pitchAngle = tag.getDouble("qingjiao");
        textX = tag.getDouble("wenziX");
        textZ = tag.getDouble("wenziZ");
        segmentOffsetX.clear();
        segmentOffsetZ.clear();
        for (int index = 0; index < (int) blockNum; index++) {
            segmentOffsetX.add(tag.getDouble("block" + index + "Xpianyi"));
            segmentOffsetZ.add(tag.getDouble("block" + index + "Zpianyi"));
        }
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!plaqueText.isEmpty()) tag.putString("plaquetxt", plaqueText);
        if (!displayDirection.isEmpty()) tag.putString("fangxiang", displayDirection);
        if (hasRgbColour) tag.putInt("rgbcolour", rgbColour);
        if (glowing) tag.putBoolean("faguang", true);
        if (glyphCount != 0) tag.putDouble("zishu", glyphCount);
        for (int index = 0; index < glyphChars.size(); index++) {
            tag.putString("zidi" + index, glyphChars.get(index));
            tag.putDouble("ziweizhi" + index, index < glyphOffsets.size() ? glyphOffsets.get(index) : 0);
        }
        if (addHalf) tag.putBoolean("plaqueaddfive", true);
        if (blockNum != 0) tag.putDouble("blocknum", blockNum);
        if (plaqueTextNum != 0) tag.putDouble("plaquetxtnum", plaqueTextNum);
        if (plaqueAngle != 0) tag.putDouble("plaquejiaodu", plaqueAngle);
        if (pitchAngle != 0) tag.putDouble("qingjiao", pitchAngle);
        if (textX != 0) tag.putDouble("wenziX", textX);
        if (textZ != 0) tag.putDouble("wenziZ", textZ);
        for (int index = 0; index < segmentOffsetX.size(); index++) {
            tag.putDouble("block" + index + "Xpianyi", segmentOffsetX.get(index));
            tag.putDouble("block" + index + "Zpianyi", index < segmentOffsetZ.size() ? segmentOffsetZ.get(index) : 0);
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public int getContainerSize() { return items.size(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
    @Override protected Component getDefaultName() { return Component.translatable("block.yvanchuxiuzhen." + variant.id()); }
    @Override public Component getDisplayName() { return getDefaultName(); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return ChestMenu.threeRows(id, inventory); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return true; }
    @Override public int[] getSlotsForFace(Direction side) { return new int[0]; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return true; }
}
