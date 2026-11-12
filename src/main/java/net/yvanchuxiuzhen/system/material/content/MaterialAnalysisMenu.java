package net.yvanchuxiuzhen.system.material.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.material.MaterialLibrary;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialInventoryRequestEvent;
import net.yvanchuxiuzhen.system.material.api.MaterialInventoryRequestEvent.Action;
import net.yvanchuxiuzhen.system.material.data.MaterialAnalysis;
import net.yvanchuxiuzhen.system.material.data.MaterialData;
import net.yvanchuxiuzhen.system.material.network.MaterialAnalysisViewPayload;

/** 千材录持有一格临时输入，按已缓存材料辨识并依次从材料仓、普通库存和输入槽扣料。 */
public final class MaterialAnalysisMenu extends AbstractContainerMenu {
    private static final TagKey<Item> MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "cailiao"));
    private final IItemHandler input;
    private final boolean bound;
    private final BlockPos openedAt;
    private String selectedId = "";
    private String selectedName = "";

    public MaterialAnalysisMenu(int id, Inventory inventory, BlockPos openedAt) {
        super(MaterialMenus.ANALYSIS.get(), id);
        this.openedAt = openedAt;
        Player owner = inventory.player;
        IItemHandler blockItems = owner.level().isClientSide ? null
                : owner.level().getCapability(Capabilities.ItemHandler.BLOCK, openedAt, null);
        bound = blockItems != null;
        input = bound ? blockItems : new ItemStackHandler(1);
        addSlot(new SlotItemHandler(input, 0, 30, -13) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(MATERIALS);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public String selectedId() {
        return selectedId;
    }

    public String selectedName() {
        return selectedName;
    }

    public void setView(String itemId, String name) {
        selectedId = itemId;
        selectedName = name;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().hasChunkAt(openedAt)) return false;
        switch (button) {
            case 0 -> refresh(serverPlayer);
            case 1 -> identify(serverPlayer);
            case 2 -> MaterialAnalysisHelp.attributes(serverPlayer);
            case 3 -> MaterialAnalysisHelp.levels(serverPlayer);
            default -> { return false; }
        }
        return true;
    }

    private void refresh(ServerPlayer player) {
        ItemStack stack = input.getStackInSlot(0);
        if (!stack.is(MATERIALS)) return;
        selectedId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        selectedName = stack.getDisplayName().getString();
        MaterialDefinition definition = MaterialLibrary.definition(selectedId);
        if (definition != null) {
            player.setData(MaterialData.ANALYSIS, MaterialLibrary.analysis(player).withMaximum(selectedId, definition.entryCount()));
            player.syncData(MaterialData.ANALYSIS);
        }
        PacketDistributor.sendToPlayer(player, new MaterialAnalysisViewPayload(containerId, selectedId, selectedName));
    }

    private void identify(ServerPlayer player) {
        ItemStack stack = input.getStackInSlot(0);
        if (!stack.is(MATERIALS)) return;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (selectedName.isEmpty() || !selectedId.equals(itemId)) {
            refresh(player);
            return;
        }
        MaterialAnalysis analysis = MaterialLibrary.analysis(player);
        int progress = analysis.progress(selectedId);
        if (progress >= analysis.maximum(selectedId)) {
            player.displayClientMessage(Component.literal("此材料的所有效果已全部解析完成。"), false);
            return;
        }
        Item item = stack.getItem();
        int inventoryCount = inventoryCount(player, item);
        int warehouseCount = NeoForge.EVENT_BUS.post(new MaterialInventoryRequestEvent(player, selectedId, Action.COUNT, 0)).result();
        int needed = progress * 2;
        if (inventoryCount + stack.getCount() + warehouseCount < needed) {
            player.displayClientMessage(Component.literal("§4您的背包里没有足够的" + selectedName + "，无法解析此材料。"), false);
            refresh(player);
            return;
        }
        if (warehouseCount > 0 && needed > 0) {
            needed -= NeoForge.EVENT_BUS.post(new MaterialInventoryRequestEvent(player, selectedId, Action.CONSUME, needed)).result();
        }
        if (needed > 0) {
            player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(item), Math.min(inventoryCount, needed), player.inventoryMenu.getCraftSlots());
            if (inventoryCount < needed) getSlot(0).remove(needed - inventoryCount);
        }
        MaterialDefinition definition = MaterialLibrary.definition(selectedId);
        analysis = analysis.reveal(selectedId, definition.entries().get(progress)).withMaximum(selectedId, definition.entryCount());
        player.setData(MaterialData.ANALYSIS, analysis);
        player.syncData(MaterialData.ANALYSIS);
        refresh(player);
    }

    private static int inventoryCount(ServerPlayer player, Item item) {
        IItemHandler inventory = player.getCapability(Capabilities.ItemHandler.ENTITY);
        int count = 0;
        if (inventory != null) {
            for (int index = 0; index < inventory.getSlots(); index++) {
                ItemStack stack = inventory.getStackInSlot(index);
                if (stack.is(item)) count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < 28) {
                if (!moveItemStackTo(stack, 28, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer && !bound) {
            ItemStack stack = input.extractItem(0, input.getStackInSlot(0).getCount(), false);
            clearContainer(player, new SimpleContainer(stack));
        }
    }
}
