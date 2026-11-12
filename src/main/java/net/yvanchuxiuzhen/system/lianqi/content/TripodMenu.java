package net.yvanchuxiuzhen.system.lianqi.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.TripodRuntime;

/** 炼器菜单显示九材料、成品与图纸槽；成品由领取按钮交付，隐藏槽十一只保存在鼎内。 */
public final class TripodMenu extends AbstractContainerMenu {
    private static final TagKey<Item> SPIRIT_STONES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshi"));
    private static final TagKey<Block> DEVICES = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danludinglu"));
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public TripodMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(12), buffer.readBlockPos());
    }

    public TripodMenu(int id, Inventory inventory, TripodBlockEntity tripod) {
        this(id, inventory, tripod, tripod.getBlockPos());
    }

    private TripodMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(LianqiMenus.TRIPOD.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        for (int index = 0; index < 9; index++) {
            addSlot(new Slot(container, index, 169 + index * 18, 203) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !stack.is(SPIRIT_STONES);
                }
            });
        }
        addSlot(new Slot(container, 9, 241, 160) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }
        });
        addSlot(new Slot(container, 10, 48, 127));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 256 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 314));
    }

    public BlockPos pos() {
        return pos;
    }

    public CompoundTag data() {
        return level.getBlockEntity(pos) instanceof TripodBlockEntity tripod ? tripod.getPersistentData() : new CompoundTag();
    }

    public String tripodName() {
        String name = data().getString("TripodName");
        return name.isEmpty() ? "普通鼎炉" : name;
    }

    public int progress() {
        return level.getBlockState(pos).is(DEVICES) ? data().getInt("lianzhijindu") : 0;
    }

    public String recipeLine(int slot) {
        CompoundTag recipe = container.getItem(10).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String key = "cailiao0" + slot;
        String name = recipe.getString(key + "name");
        if (name.isEmpty() || name.equals("无")) return "§4无";
        if (recipe.contains(key + "name_component")) {
            name = Component.Serializer.fromJson(recipe.getString(key + "name_component"), level.registryAccess()).getString();
        }
        ItemStack input = container.getItem(slot);
        boolean enough = BuiltInRegistries.ITEM.getKey(input.getItem()).toString().equals(recipe.getString(key))
                && input.getCount() >= recipe.getInt(key + "num");
        return (enough ? "§f" : "§4") + name + "（" + recipe.getInt(key + "num") + "）";
    }

    /** 按钮只操作当前菜单所绑定的真实鼎；来源允许开窗后离开装置距离。 */
    public void press(ServerPlayer player, int button) {
        if (!(container instanceof TripodBlockEntity tripod) || level.getBlockEntity(pos) != tripod) return;
        TripodRuntime.button(player, tripod, button);
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 11) {
            if (!moveItemStackTo(stack, 11, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 11, false)) {
            if (index < 38) moveItemStackTo(stack, 38, slots.size(), true);
            else moveItemStackTo(stack, 11, 38, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源堆叠合并同样检查 mayPlace，灵石和成品槽不能借 Shift 合并绕过放入限制。 */
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int step = reverse ? -1 : 1;
        if (stack.isStackable()) {
            for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
                Slot slot = slots.get(index);
                ItemStack existing = slot.getItem();
                if (!slot.mayPlace(existing) || existing.isEmpty() || !ItemStack.isSameItemSameComponents(stack, existing)) continue;
                int count = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount());
                if (count > 0) {
                    stack.shrink(count);
                    existing.grow(count);
                    slot.set(existing);
                    moved = true;
                }
            }
        }
        for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
            Slot slot = slots.get(index);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                slot.set(stack.split(Math.min(stack.getCount(), slot.getMaxStackSize())));
                moved = true;
                break;
            }
        }
        return moved;
    }
}
