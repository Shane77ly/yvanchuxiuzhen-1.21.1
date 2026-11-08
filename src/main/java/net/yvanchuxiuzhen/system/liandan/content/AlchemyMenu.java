package net.yvanchuxiuzhen.system.liandan.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.LiandanBrewing;

/** 炼丹菜单显示容器、八材料、成品与丹方共十一槽；槽十一仅存于丹炉库存。 */
public final class AlchemyMenu extends AbstractContainerMenu {
    public static final TagKey<Item> BOTTLES = ItemTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "pingguan"));
    private static final TagKey<Block> FURNACES = BlockTags.create(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "danludinglu"));
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public AlchemyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(12), buffer.readBlockPos());
    }

    public AlchemyMenu(int id, Inventory inventory, AlchemyBlockEntity furnace) {
        this(id, inventory, furnace, furnace.getBlockPos());
    }

    private AlchemyMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(LiandanMenus.ALCHEMY.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        for (int index = 0; index < 9; index++) {
            final int input = index;
            addSlot(new Slot(container, index, 170 + index * 18, 229) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return input != 0 || stack.is(BOTTLES);
                }
            });
        }
        addSlot(new Slot(container, 9, 242, 160) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                claim(player);
            }

            @Override
            public void onQuickCraft(ItemStack stack, ItemStack original) {
                super.onQuickCraft(stack, original);
                claim(inventory.player);
            }
        });
        addSlot(new Slot(container, 10, 48, 127));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 256 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 170 + column * 18, 314));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public CompoundTag data() {
        return level.getBlockEntity(pos) instanceof AlchemyBlockEntity furnace ? furnace.getPersistentData() : new CompoundTag();
    }

    public String furnaceName() {
        if (!(level.getBlockEntity(pos) instanceof AlchemyBlockEntity)) return "";
        String name = data().getString("alchemyName");
        return name.isEmpty() ? new ItemStack(level.getBlockState(pos).getBlock()).getDisplayName().getString() : name;
    }

    public String itemId(int slot) {
        return BuiltInRegistries.ITEM.getKey(container.getItem(slot).getItem()).toString();
    }

    public int progress() {
        return level.getBlockState(pos).is(FURNACES) ? data().getInt("lianzhijindu") : 0;
    }

    private void claim(Player player) {
        if (player instanceof ServerPlayer serverPlayer && container instanceof AlchemyBlockEntity furnace
                && level.getBlockEntity(pos) == furnace) {
            LiandanBrewing.claimExperience(furnace, serverPlayer);
        }
    }

    /** 服务端仅接受当前菜单仍绑定的真实丹炉，来源允许打开后离开容器距离。 */
    public void press(ServerPlayer player, int button) {
        if (!(container instanceof AlchemyBlockEntity furnace) || level.getBlockEntity(pos) != furnace) return;
        switch (button) {
            case 0 -> LiandanBrewing.loadRecipe(furnace, player);
            case 1 -> LiandanBrewing.saveRecipe(furnace, player);
            case 2 -> LiandanBrewing.begin(furnace);
            default -> LiandanBrewing.help(player, button);
        }
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

    /** 来源合并已有堆叠同样检查 mayPlace，成品槽与错误容器均不能被 Shift 合并。 */
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
