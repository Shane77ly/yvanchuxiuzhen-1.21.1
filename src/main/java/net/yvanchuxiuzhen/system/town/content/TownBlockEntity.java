package net.yvanchuxiuzhen.system.town.content;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** 城镇容器与规划、所有权状态随方块保存；全部槽允许来源侧面物品操作。 */
public final class TownBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private final boolean planning;
    private NonNullList<ItemStack> items;
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private boolean constructionCompleted;
    private int shiftingX, shiftingY, shiftingZ;
    private int lengthOfX, lengthOfY, lengthOfZ;
    private String team = "";
    private CompoundTag buildData = new CompoundTag();
    private CompoundTag townBuild = new CompoundTag();
    private String masterUuid = "";
    private String townName = "";
    private String townUuid = "";
    private int townLevelNumber;
    private String townLevelName = "";
    private String townJiaotou = "";
    private String townCanmou = "";
    private int residentPopulation, houseNumber, housePopulation, floatingPopulation, hotelNumber, hotelPopulation,
            deathCount, troopCount, residentCultivators, floatingCultivators;

    public TownBlockEntity(BlockPos pos, BlockState state, boolean planning) {
        super(planning ? TownBlockEntities.PLANNING.get() : TownBlockEntities.CENTER.get(), pos, state);
        this.planning = planning;
        items = NonNullList.withSize(planning ? 18 : 1, ItemStack.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public boolean completed() { return constructionCompleted; }
    public void setCompleted(boolean value) { constructionCompleted = value; }
    public int shiftingX() { return shiftingX; }
    public int shiftingY() { return shiftingY; }
    public int shiftingZ() { return shiftingZ; }
    public void setShifting(int x, int y, int z) { shiftingX = x; shiftingY = y; shiftingZ = z; }
    public void setShiftingAxis(String axis, int value) {
        switch (axis) { case "X" -> shiftingX = value; case "Y" -> shiftingY = value; case "Z" -> shiftingZ = value; }
    }
    public int lengthOfX() { return lengthOfX; }
    public int lengthOfY() { return lengthOfY; }
    public int lengthOfZ() { return lengthOfZ; }
    public void setLengthOf(int x, int y, int z) { lengthOfX = x; lengthOfY = y; lengthOfZ = z; }
    public void setLengthOfAxis(String axis, int value) {
        switch (axis) { case "X" -> lengthOfX = value; case "Y" -> lengthOfY = value; case "Z" -> lengthOfZ = value; }
    }
    public String team() { return team; }
    public void setTeam(String value) { team = value == null ? "" : value; }
    public CompoundTag buildData() { return buildData.copy(); }
    public void setBuildData(CompoundTag value) { buildData = value == null ? new CompoundTag() : value.copy(); }
    public CompoundTag townBuild() { return townBuild.copy(); }
    public void setTownBuild(CompoundTag value) { townBuild = value == null ? new CompoundTag() : value.copy(); }
    public String masterUuid() { return masterUuid; }
    public void setMasterUuid(String value) { masterUuid = value == null ? "" : value; }
    public String townName() { return townName; }
    public void setTownName(String value) { townName = value == null ? "" : value; }
    public String townUuid() { return townUuid; }
    public void setTownUuid(String value) { townUuid = value == null ? "" : value; }
    public int townLevelNumber() { return townLevelNumber; }
    public void setTownLevelNumber(int value) { townLevelNumber = value; }
    public String townLevelName() { return townLevelName; }
    public void setTownLevelName(String value) { townLevelName = value == null ? "" : value; }
    public String townJiaotou() { return townJiaotou; }
    public String townCanmou() { return townCanmou; }
    public int residentPopulation() { return residentPopulation; }
    public int houseNumber() { return houseNumber; }
    public int housePopulation() { return housePopulation; }
    public int floatingPopulation() { return floatingPopulation; }
    public int hotelNumber() { return hotelNumber; }
    public int hotelPopulation() { return hotelPopulation; }
    public int deathCount() { return deathCount; }
    public int troopCount() { return troopCount; }
    public int residentCultivators() { return residentCultivators; }
    public int floatingCultivators() { return floatingCultivators; }

    public void copyPlanFrom(TownBlockEntity source) {
        shiftingX = source.shiftingX;
        shiftingY = source.shiftingY;
        shiftingZ = source.shiftingZ;
        lengthOfX = source.lengthOfX;
        lengthOfY = source.lengthOfY;
        lengthOfZ = source.lengthOfZ;
        team = source.team;
        buildData = source.buildData.copy();
    }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(planning ? 18 : 1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        constructionCompleted = tag.getBoolean("construction_completed");
        shiftingX = tag.getInt("shiftingX");
        shiftingY = tag.getInt("shiftingY");
        shiftingZ = tag.getInt("shiftingZ");
        lengthOfX = tag.getInt("lengthofX");
        lengthOfY = tag.getInt("lengthofY");
        lengthOfZ = tag.getInt("lengthofZ");
        team = tag.getString("team");
        buildData = tag.getCompound("build_data").copy();
        townBuild = tag.getCompound("town_build").copy();
        masterUuid = tag.getString("belongToMasterUUID");
        townName = tag.getString("town_name");
        townUuid = tag.getString("town_uuid");
        townLevelNumber = tag.getInt("town_levelnumber");
        townLevelName = tag.getString("town_levelname");
        townJiaotou = tag.getString("town_jiaotou");
        townCanmou = tag.getString("town_canmou");
        residentPopulation = tag.getInt("town_resident_population");
        houseNumber = tag.getInt("town_house_number");
        housePopulation = tag.getInt("town_house_population");
        floatingPopulation = tag.getInt("town_floating_population");
        hotelNumber = tag.getInt("town_hotel_number");
        hotelPopulation = tag.getInt("town_hotel_population");
        deathCount = tag.getInt("town_death");
        troopCount = tag.getInt("number_of_troops");
        residentCultivators = tag.getInt("resident_xiuzhe");
        floatingCultivators = tag.getInt("resident_floating_xiuzhe");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (constructionCompleted) tag.putBoolean("construction_completed", true);
        if (shiftingX != 0) tag.putInt("shiftingX", shiftingX);
        if (shiftingY != 0) tag.putInt("shiftingY", shiftingY);
        if (shiftingZ != 0) tag.putInt("shiftingZ", shiftingZ);
        if (lengthOfX != 0) tag.putInt("lengthofX", lengthOfX);
        if (lengthOfY != 0) tag.putInt("lengthofY", lengthOfY);
        if (lengthOfZ != 0) tag.putInt("lengthofZ", lengthOfZ);
        if (!team.isEmpty()) tag.putString("team", team);
        if (!buildData.isEmpty()) tag.put("build_data", buildData);
        if (!townBuild.isEmpty()) tag.put("town_build", townBuild);
        if (!masterUuid.isEmpty()) tag.putString("belongToMasterUUID", masterUuid);
        if (!townName.isEmpty()) tag.putString("town_name", townName);
        if (!townUuid.isEmpty()) tag.putString("town_uuid", townUuid);
        if (townLevelNumber != 0) tag.putInt("town_levelnumber", townLevelNumber);
        if (!townLevelName.isEmpty()) tag.putString("town_levelname", townLevelName);
        if (!townJiaotou.isEmpty()) tag.putString("town_jiaotou", townJiaotou);
        if (!townCanmou.isEmpty()) tag.putString("town_canmou", townCanmou);
        if (residentPopulation != 0) tag.putInt("town_resident_population", residentPopulation);
        if (houseNumber != 0) tag.putInt("town_house_number", houseNumber);
        if (housePopulation != 0) tag.putInt("town_house_population", housePopulation);
        if (floatingPopulation != 0) tag.putInt("town_floating_population", floatingPopulation);
        if (hotelNumber != 0) tag.putInt("town_hotel_number", hotelNumber);
        if (hotelPopulation != 0) tag.putInt("town_hotel_population", hotelPopulation);
        if (deathCount != 0) tag.putInt("town_death", deathCount);
        if (troopCount != 0) tag.putInt("number_of_troops", troopCount);
        if (residentCultivators != 0) tag.putInt("resident_xiuzhe", residentCultivators);
        if (floatingCultivators != 0) tag.putInt("resident_floating_xiuzhe", floatingCultivators);
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
        return items.size();
    }

    @Override
    public int getMaxStackSize() {
        return planning ? 64 : 1;
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
        return Component.translatable("block.yvanchuxiuzhen." + (planning ? "chengzhen_buildblock" : "chengzhen_antai"));
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return planning ? new TownBuildMenu(id, inventory, worldPosition) : new TownMenu(id, inventory, worldPosition, 2);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }
}
