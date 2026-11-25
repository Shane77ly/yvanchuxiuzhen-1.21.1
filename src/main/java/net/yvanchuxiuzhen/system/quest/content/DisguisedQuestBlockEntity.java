package net.yvanchuxiuzhen.system.quest.content;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.yvanchuxiuzhen.system.economy.api.EconomyCatalogs;
import net.yvanchuxiuzhen.system.economy.api.ShopCatalogDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplateDefinition;
import net.yvanchuxiuzhen.system.quest.api.QuestTemplates;

/** 任务伪装方块持有一个展示槽及十行强类型任务模板 ID 与商店配置。 */
public final class DisguisedQuestBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final IItemHandler unsided = new InvWrapper(this);
    private final IItemHandler[] sided = new IItemHandler[Direction.values().length];
    private String displayDirection = "";
    private boolean showParticles;
    private boolean refreshDue;
    private int refreshTick;
    private final String[] advancedQuests = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final String[] ordinaryQuests = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final String[] shopIds = new String[]{"", "", "", "", "", "", "", "", "", ""};
    private final ShopCatalogDefinition[] customShops = new ShopCatalogDefinition[10];

    public DisguisedQuestBlockEntity(BlockPos pos, BlockState state) {
        super(QuestBlockEntities.DISGUISED.get(), pos, state);
        Arrays.fill(customShops, ShopCatalogDefinition.EMPTY);
        for (Direction direction : Direction.values()) sided[direction.ordinal()] = new SidedInvWrapper(this, direction);
    }

    public IItemHandler handler(Direction direction) {
        return direction == null ? unsided : sided[direction.ordinal()];
    }

    public String displayDirection() { return displayDirection; }
    public boolean showParticles() { return showParticles; }
    public boolean refreshDue() { return refreshDue; }
    public int refreshTick() { return refreshTick; }
    public String advancedQuest(int row) { return row >= 1 && row <= 10 ? advancedQuests[row - 1] : ""; }
    public String ordinaryQuest(int row) { return row >= 1 && row <= 10 ? ordinaryQuests[row - 1] : ""; }
    public String shopId(int row) { return row >= 1 && row <= 10 ? shopIds[row - 1] : ""; }
    public ShopCatalogDefinition customShop(int row) { return row >= 1 && row <= 10 ? customShops[row - 1] : ShopCatalogDefinition.EMPTY; }

    public Optional<QuestTemplateDefinition> resolveQuest(int row) {
        String id = advancedQuest(row);
        if (id.isEmpty()) id = ordinaryQuest(row);
        return QuestTemplates.find(id);
    }

    public ShopCatalogDefinition resolveShop(int row) {
        ShopCatalogDefinition custom = customShop(row);
        if (!custom.info().isEmpty() || !custom.products().isEmpty()) return custom;
        return EconomyCatalogs.catalog(shopId(row));
    }

    public void setDisplayDirection(String value) { displayDirection = value == null ? "" : value; setChanged(); }
    public void setShowParticles(boolean value) { showParticles = value; setChanged(); }
    public void setRefreshDue(boolean value) { refreshDue = value; setChanged(); }
    public void setRefreshTick(int value) { refreshTick = value; setChanged(); }
    public void setAdvancedQuest(int row, String value) { if (row >= 1 && row <= 10) { advancedQuests[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setOrdinaryQuest(int row, String value) { if (row >= 1 && row <= 10) { ordinaryQuests[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setShopId(int row, String value) { if (row >= 1 && row <= 10) { shopIds[row - 1] = value == null ? "" : value; setChanged(); } }
    public void setCustomShop(int row, ShopCatalogDefinition shop) { if (row >= 1 && row <= 10) { customShops[row - 1] = shop == null ? ShopCatalogDefinition.EMPTY : shop; setChanged(); } }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tryLoadLootTable(tag)) items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        displayDirection = tag.getString("fangxiang");
        showParticles = tag.getBoolean("lizixianshi");
        refreshDue = tag.getBoolean("shuaxin");
        refreshTick = tag.getInt("shuaxintick");
        readSlots(tag.getList("advanced_quests", Tag.TAG_STRING), advancedQuests);
        readSlots(tag.getList("ordinary_quests", Tag.TAG_STRING), ordinaryQuests);
        readSlots(tag.getList("shop_ids", Tag.TAG_STRING), shopIds);
        Arrays.fill(customShops, ShopCatalogDefinition.EMPTY);
        CompoundTag customTag = tag.getCompound("custom_shops");
        for (int row = 1; row <= 10; row++) {
            Tag encoded = customTag.get(Integer.toString(row));
            if (encoded != null) {
                customShops[row - 1] = ShopCatalogDefinition.CODEC.parse(NbtOps.INSTANCE, encoded).result().orElse(ShopCatalogDefinition.EMPTY);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!trySaveLootTable(tag)) ContainerHelper.saveAllItems(tag, items, registries);
        if (!displayDirection.isEmpty()) tag.putString("fangxiang", displayDirection);
        if (showParticles) tag.putBoolean("lizixianshi", true);
        if (refreshDue) tag.putBoolean("shuaxin", true);
        if (refreshTick != 0) tag.putInt("shuaxintick", refreshTick);
        writeSlots(tag, "advanced_quests", advancedQuests);
        writeSlots(tag, "ordinary_quests", ordinaryQuests);
        writeSlots(tag, "shop_ids", shopIds);
        CompoundTag customTag = new CompoundTag();
        for (int row = 1; row <= 10; row++) {
            ShopCatalogDefinition shop = customShops[row - 1];
            if (!shop.info().isEmpty() || !shop.products().isEmpty()) {
                customTag.put(Integer.toString(row), ShopCatalogDefinition.CODEC.encodeStart(NbtOps.INSTANCE, shop).getOrThrow());
            }
        }
        if (!customTag.isEmpty()) tag.put("custom_shops", customTag);
    }

    private static void readSlots(ListTag list, String[] target) {
        Arrays.fill(target, "");
        for (int index = 0; index < Math.min(target.length, list.size()); index++) {
            target[index] = list.getString(index);
        }
    }

    private static void writeSlots(CompoundTag tag, String key, String[] source) {
        boolean any = false;
        for (String value : source) {
            if (!value.isEmpty()) {
                any = true;
                break;
            }
        }
        if (!any) return;
        ListTag list = new ListTag();
        for (String value : source) list.add(StringTag.valueOf(value));
        tag.put(key, list);
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
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.getFirst().isEmpty();
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
        return Component.translatable("block.yvanchuxiuzhen.renwuweizhuangfangkuai");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot != 1;
    }
}
